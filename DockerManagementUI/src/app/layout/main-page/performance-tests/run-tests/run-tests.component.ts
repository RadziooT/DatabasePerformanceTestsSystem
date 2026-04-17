import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { PerformanceTestSimulationType } from '../../../../api';

@Component({
  selector: 'app-run-tests',
  imports: [
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatSelectModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './run-tests.component.html',
  styleUrl: './run-tests.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RunTestsComponent {
  simulationTypes = input.required<PerformanceTestSimulationType[]>();
  selectedSimulationType = input.required<PerformanceTestSimulationType>();
  runLoading = input.required<boolean>();
  canStart = input.required<boolean>();
  environmentNotStarted = input.required<boolean>();
  simulationOptionsUnavailable = input.required<boolean>();
  runError = input<string | null>(null);

  simulationTypeSelected = output<PerformanceTestSimulationType>();
  runTestsClicked = output<void>();

  protected formatSimulationType(simulationType: PerformanceTestSimulationType): string {
    return simulationType
      .toLowerCase()
      .split('_')
      .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
      .join('');
  }
}
