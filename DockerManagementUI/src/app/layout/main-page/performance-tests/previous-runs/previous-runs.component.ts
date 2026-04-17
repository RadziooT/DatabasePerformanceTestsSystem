import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { DatePipe } from '@angular/common';
import { SafeResourceUrl } from '@angular/platform-browser';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { PerformanceTestRunSummary } from '../../../../core/services/performance-tests-api.service';
import { ReportViewerComponent } from '../report-viewer/report-viewer.component';

@Component({
  selector: 'app-previous-runs',
  imports: [
    DatePipe,
    ReportViewerComponent,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatSelectModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './previous-runs.component.html',
  styleUrl: './previous-runs.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PreviousRunsComponent {
  runsLoading = input.required<boolean>();
  runs = input.required<PerformanceTestRunSummary[]>();
  selectedRunId = input.required<string | null>();
  runsError = input<string | null>(null);

  reportLoading = input.required<boolean>();
  reportError = input<string | null>(null);
  reportUrl = input.required<SafeResourceUrl | null>();

  runLoading = input<boolean>(false);

  runSelected = output<string>();
  refreshClicked = output<void>();
  reportLoaded = output<void>();
  reportLoadFailed = output<void>();
}
