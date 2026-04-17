import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  inject,
  signal,
} from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { AppState } from '../../../store/app.state';
import { catchError, EMPTY, finalize, take } from 'rxjs';
import { NotificationService } from '../../../core/services/notification.service';
import { MatTabsModule } from '@angular/material/tabs';

import { PerformanceTestSimulationType } from '../../../api';
import {
  PerformanceTestRunSummary,
  PerformanceTestsApiService,
} from '../../../core/services/performance-tests-api.service';
import { RunTestsComponent } from './run-tests/run-tests.component';
import { PreviousRunsComponent } from './previous-runs/previous-runs.component';

@Component({
  selector: 'app-performance-tests',
  imports: [MatTabsModule, RunTestsComponent, PreviousRunsComponent],
  templateUrl: './performance-tests.component.html',
  styleUrl: './performance-tests.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PerformanceTestsComponent {
  private readonly api = inject(PerformanceTestsApiService);
  private readonly notificationService = inject(NotificationService);
  private readonly sanitizer = inject(DomSanitizer);
  private readonly appState = inject(AppState);

  protected readonly runLoading = signal(false);
  protected readonly runsLoading = signal(false);
  protected readonly runs = signal<PerformanceTestRunSummary[]>([]);
  protected readonly selectedRunId = signal<string | null>(null);

  protected readonly runError = signal<string | null>(null);
  protected readonly runsError = signal<string | null>(null);
  protected readonly reportLoading = signal(false);
  protected readonly reportError = signal<string | null>(null);

  protected readonly simulationTypes = this.appState.performanceTestTypes;
  protected readonly selectedSimulationType = signal<PerformanceTestSimulationType>(
    PerformanceTestSimulationType.BASIC_SIMULATION,
  );

  protected readonly selectedRun = computed(
    () => this.runs().find((run) => run.runId === this.selectedRunId()) ?? null,
  );
  protected readonly simulationOptionsUnavailable = computed(
    () => this.simulationTypes().length === 0,
  );
  protected readonly environmentNotStarted = computed(
    () => !this.appState.selectedDatabaseType() || !this.appState.selectedVolumeSize(),
  );
  protected readonly canStart = computed(
    () => !this.runLoading() && this.simulationTypes().length > 0 && !this.environmentNotStarted(),
  );

  protected readonly reportUrl = computed<SafeResourceUrl | null>(() => {
    const selectedRunId = this.selectedRunId();
    if (!selectedRunId) {
      return null;
    }

    const runReportUrl = this.api.buildRunReportUrl(selectedRunId);
    return this.sanitizer.bypassSecurityTrustResourceUrl(runReportUrl);
  });

  constructor() {
    effect(() => {
      const simulationTypes = this.simulationTypes();
      if (simulationTypes.length === 0) {
        return;
      }

      const selected = this.selectedSimulationType();
      if (simulationTypes.includes(selected)) {
        return;
      }

      const fallback = simulationTypes.includes(PerformanceTestSimulationType.BASIC_SIMULATION)
        ? PerformanceTestSimulationType.BASIC_SIMULATION
        : simulationTypes[0];
      this.selectedSimulationType.set(fallback);
    });

    this.loadRuns();
  }

  protected runPerformanceTests(): void {
    if (!this.canStart()) {
      return;
    }

    const databaseType = this.appState.selectedDatabaseType();
    if (!databaseType) {
      this.notificationService.error(
        'Database type is not selected. Please start environment first.',
        'Error',
      );
      return;
    }

    const volumeSize = this.appState.selectedVolumeSize();
    if (!volumeSize) {
      this.notificationService.error(
        'Volume size is not selected. Please start environment first.',
        'Error',
      );
      return;
    }

    this.runError.set(null);
    this.runLoading.set(true);

    this.api
      .runPerformanceTests(databaseType, this.selectedSimulationType(), volumeSize)
      .pipe(
        take(1),
        finalize(() => this.runLoading.set(false)),
        catchError(() => {
          this.runError.set('Failed to trigger performance tests.');
          return EMPTY;
        }),
      )
      .subscribe(() => {
        this.notificationService.success('Performance tests triggered successfully.', 'Success');
        this.loadRuns();
      });
  }

  protected loadRuns(): void {
    this.runsError.set(null);
    this.runsLoading.set(true);

    this.api
      .listRuns()
      .pipe(
        take(1),
        finalize(() => this.runsLoading.set(false)),
        catchError(() => {
          this.runsError.set('Failed to load available performance test runs.');
          this.runs.set([]);
          this.selectedRunId.set(null);
          return EMPTY;
        }),
      )
      .subscribe((runs) => {
        const validRuns = runs.filter((run) => Boolean(run.runId));
        this.runs.set(validRuns);

        if (validRuns.length === 0) {
          this.selectedRunId.set(null);
          this.reportLoading.set(false);
          return;
        }

        const currentSelectedRunId = this.selectedRunId();
        const selectedRunStillExists = validRuns.some((run) => run.runId === currentSelectedRunId);

        if (!selectedRunStillExists) {
          this.selectedRunId.set(null);
          this.reportLoading.set(false);
        }
      });
  }

  protected onSimulationTypeSelected(value: string): void {
    this.selectedSimulationType.set(value as PerformanceTestSimulationType);
  }

  protected formatSimulationType(simulationType: PerformanceTestSimulationType): string {
    return simulationType
      .toLowerCase()
      .split('_')
      .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
      .join('');
  }

  protected onRunSelected(runId: string): void {
    if (runId === this.selectedRunId()) {
      return;
    }

    this.reportError.set(null);
    this.selectedRunId.set(runId ? runId : null);
    this.reportLoading.set(Boolean(runId));
  }

  protected onReportLoaded(): void {
    this.reportError.set(null);
    this.reportLoading.set(false);
  }

  protected onReportLoadFailed(): void {
    this.reportError.set('Failed to load the selected HTML report.');
    this.reportLoading.set(false);
  }
}
