import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';
import { SafeResourceUrl } from '@angular/platform-browser';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-report-viewer',
  imports: [MatProgressSpinnerModule, MatIconModule],
  templateUrl: './report-viewer.component.html',
  styleUrl: './report-viewer.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ReportViewerComponent {
  readonly reportUrl = input<SafeResourceUrl | null>(null);
  readonly title = input('Performance test report');
  readonly loading = input(false);

  readonly loaded = output<void>();
  readonly loadFailed = output<void>();

  protected readonly hasReport = computed(() => this.reportUrl() !== null);

  protected onFrameLoad(): void {
    this.loaded.emit();
  }

  protected onFrameError(): void {
    this.loadFailed.emit();
  }
}
