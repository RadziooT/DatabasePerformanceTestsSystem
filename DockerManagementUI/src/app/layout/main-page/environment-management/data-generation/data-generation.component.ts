import { HttpErrorResponse } from '@angular/common/http';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  computed,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { interval, Subscription } from 'rxjs';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatProgressBarModule } from '@angular/material/progress-bar';

import { DatabaseType, DatabaseVolumeType, DataGenerationState } from '../../../../api';
import {
  DataGenerationApiService,
  DataGenerationRequest,
  DataGenerationStatus,
} from '../../../../core/services/data-generation-api.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { AppState } from '../../../../store/app.state';

interface DataGenerationForm {
  databaseType: FormControl<DatabaseType | ''>;
  volumeType: FormControl<DatabaseVolumeType | ''>;
}

const POLLING_INTERVAL_MS = 2500;
const MAX_CONSECUTIVE_POLLING_ERRORS = 3;

@Component({
  selector: 'app-data-generation',
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatProgressBarModule,
  ],
  templateUrl: './data-generation.component.html',
  styleUrl: './data-generation.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DataGenerationComponent {
  private readonly fb = inject(FormBuilder);
  private readonly appState = inject(AppState);
  private readonly notificationService = inject(NotificationService);
  private readonly dataGenerationApiService = inject(DataGenerationApiService);
  private readonly destroyRef = inject(DestroyRef);

  private statusPollingSubscription: Subscription | null = null;
  private consecutivePollingErrors = 0;
  private lastSelectionKey: string | null = null;
  private terminalStateNotified: DataGenerationState | null = null;

  protected readonly isSubmitting = signal(false);
  protected readonly isStatusLoading = signal(false);
  protected readonly hasStatusCard = signal(false);
  protected readonly status = signal<DataGenerationStatus>(
    this.dataGenerationApiService.createIdleStatus(),
  );

  protected readonly statusState = computed(() => this.status().state ?? DataGenerationState.IDLE);
  protected readonly statusMessage = computed(() => null);
  protected readonly progress = computed(() => this.status().progressPercent ?? 0);
  protected readonly currentStep = computed(() => this.status().currentStep || '');
  protected readonly isRunning = computed(() => this.statusState() === DataGenerationState.RUNNING);
  protected readonly isFinalized = computed(
    () =>
      this.statusState() === DataGenerationState.SUCCEEDED ||
      this.statusState() === DataGenerationState.FAILED,
  );
  protected readonly isFailed = computed(() => this.statusState() === DataGenerationState.FAILED);
  protected readonly progressLabel = computed(() => `${this.progress()}%`);
  protected readonly canStart = computed(() => !this.isSubmitting() && !this.isRunning());

  protected readonly databaseTypes: DatabaseType[] = this.appState.databaseTypes();
  protected readonly volumeTypes: DatabaseVolumeType[] = this.appState.volumeTypes();

  protected readonly generationForm = this.fb.group<DataGenerationForm>({
    databaseType: this.fb.nonNullable.control<DatabaseType | ''>('', Validators.required),
    volumeType: this.fb.nonNullable.control<DatabaseVolumeType | ''>('', Validators.required),
  });

  constructor() {
    this.destroyRef.onDestroy(() => this.stopPolling());

    this.generationForm.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => {
      const selectionKey = this.getSelectionKey();
      if (this.lastSelectionKey && selectionKey !== this.lastSelectionKey) {
        this.resetRunState();
      }
    });

    this.refreshStatus();
  }

  protected generateData(): void {
    if (this.generationForm.invalid || !this.canStart()) {
      this.generationForm.markAllAsTouched();
      if (!this.canStart()) {
        return;
      }

      this.notificationService.error('Please select database and volume type', 'Validation Error');
      return;
    }

    this.resetRunState();
    this.isSubmitting.set(true);

    const formValue = this.generationForm.getRawValue();
    const request: DataGenerationRequest = {
      databaseType: formValue.databaseType as DatabaseType,
      volumeType: formValue.volumeType as DatabaseVolumeType,
    };
    this.lastSelectionKey = this.getSelectionKey();

    this.dataGenerationApiService.startDataGeneration(request).subscribe({
      next: (status) => {
        this.isSubmitting.set(false);
        this.hasStatusCard.set(true);
        this.notificationService.success('Data generation started', 'Success');
        this.applyStatus(status);
      },
      error: (error: HttpErrorResponse) => {
        this.isSubmitting.set(false);
        this.handleStartError(error);
      },
    });
  }

  protected refreshStatus(): void {
    this.hasStatusCard.set(true);
    this.fetchStatus(true);
  }

  private startPolling(): void {
    if (this.statusPollingSubscription) {
      return;
    }

    this.consecutivePollingErrors = 0;
    this.statusPollingSubscription = interval(POLLING_INTERVAL_MS).subscribe(() =>
      this.fetchStatus(false),
    );
  }

  private stopPolling(): void {
    this.statusPollingSubscription?.unsubscribe();
    this.statusPollingSubscription = null;
  }

  private fetchStatus(showLoader: boolean): void {
    if (this.isStatusLoading()) {
      return;
    }

    this.isStatusLoading.set(showLoader);
    this.dataGenerationApiService.getDataGenerationStatus().subscribe({
      next: (status) => {
        this.consecutivePollingErrors = 0;
        this.isStatusLoading.set(false);
        this.applyStatus(status);
      },
      error: () => {
        this.isStatusLoading.set(false);
        this.handlePollingError();
      },
    });
  }

  private applyStatus(status: DataGenerationStatus): void {
    const normalizedStatus = this.normalizeStatus(status);

    this.status.set(normalizedStatus);
    this.hasStatusCard.set(true);

    if (normalizedStatus.state === DataGenerationState.RUNNING) {
      this.startPolling();
      return;
    }

    this.stopPolling();

    if (
      normalizedStatus.state === DataGenerationState.SUCCEEDED &&
      this.terminalStateNotified !== normalizedStatus.state
    ) {
      this.notificationService.success('Data generation completed', 'Success');
      this.terminalStateNotified = normalizedStatus.state;
      return;
    }

    if (
      normalizedStatus.state === DataGenerationState.FAILED &&
      this.terminalStateNotified !== normalizedStatus.state
    ) {
      this.notificationService.error(
        'Data generation failed. Please review backend logs.',
        'Data generation failed',
      );
      this.terminalStateNotified = normalizedStatus.state;
      return;
    }

    if (normalizedStatus.state === DataGenerationState.IDLE) {
      this.terminalStateNotified = null;
    }
  }

  private normalizeStatus(status: DataGenerationStatus): DataGenerationStatus {
    return {
      ...status,
      state: status.state ?? DataGenerationState.IDLE,
      progressPercent: Math.max(0, Math.min(100, status.progressPercent ?? 0)),
      currentStep: status.currentStep || '',
    };
  }

  private handleStartError(error: HttpErrorResponse): void {
    if (error.status === 409) {
      this.notificationService.warning(
        'A data generation job is already running. Loading current status.',
        'Job Running',
      );
      this.fetchStatus(true);
      this.startPolling();
      return;
    }

    if (error.status === 400) {
      this.notificationService.error(
        this.extractErrorMessage(error) ||
          'Cannot start data generation. Check selected values and environment prerequisites.',
        'Validation Error',
      );
      return;
    }

    this.notificationService.error('Failed to start data generation', 'Error');
  }

  private handlePollingError(): void {
    this.consecutivePollingErrors += 1;

    if (this.consecutivePollingErrors === 1) {
      this.notificationService.warning(
        'Temporary issue while loading data generation status. Retrying...',
        'Warning',
      );
      return;
    }

    if (this.consecutivePollingErrors >= MAX_CONSECUTIVE_POLLING_ERRORS) {
      this.stopPolling();
      this.notificationService.error(
        'Unable to refresh data generation status. Use Refresh status to retry.',
        'Status Unavailable',
      );
    }
  }

  private extractErrorMessage(error: HttpErrorResponse): string | null {
    const backendPayload = error.error as { message?: unknown } | string | null;

    if (
      backendPayload &&
      typeof backendPayload === 'object' &&
      typeof backendPayload.message === 'string'
    ) {
      return backendPayload.message;
    }

    if (typeof backendPayload === 'string' && backendPayload.trim().length > 0) {
      return backendPayload;
    }

    return null;
  }

  private resetRunState(): void {
    this.stopPolling();
    this.consecutivePollingErrors = 0;
    this.terminalStateNotified = null;
    this.hasStatusCard.set(false);
    this.status.set(this.dataGenerationApiService.createIdleStatus());
  }

  private getSelectionKey(): string {
    const formValue = this.generationForm.getRawValue();
    return `${formValue.databaseType}|${formValue.volumeType}`;
  }
}
