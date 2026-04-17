import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  FormBuilder,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { Router } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCardModule } from '@angular/material/card';

import { DatabaseType, DatabaseVolumeType, EnvironmentRunRequest } from '../../../../api';
import { NotificationService } from '../../../../core/services/notification.service';
import { ConfirmationModalComponent } from '../../../../shared/components/confirmation-modal/confirmation-modal.component';
import { LoadingSpinnerComponent } from '../../../../shared/components/spinner/loading-spinner.component';
import { SuccessModalComponent } from '../../../../shared/components/success-modal/success-modal.component';
import { AppState } from '../../../../store/app.state';
import { EnvironmentApiService } from '../../../../core/services/environment-api.service';

interface EnvironmentStartForm {
  databaseContainerId: FormControl<DatabaseType | ''>;
  volumeId: FormControl<DatabaseVolumeType | ''>;
  shutdownContainers: FormControl<boolean>;
  removeContainers: FormControl<boolean>;
  deleteVolumes: FormControl<boolean>;
}

@Component({
  selector: 'app-environment-start',
  imports: [
    ReactiveFormsModule,
    LoadingSpinnerComponent,
    ConfirmationModalComponent,
    SuccessModalComponent,
    MatFormFieldModule,
    MatSelectModule,
    MatCheckboxModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatCardModule,
  ],
  templateUrl: './environment-start.component.html',
  styleUrl: './environment-start.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EnvironmentStartComponent {
  private readonly fb = inject(FormBuilder);
  private readonly appState = inject(AppState);
  private readonly environmentApi = inject(EnvironmentApiService);
  private readonly notificationService = inject(NotificationService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly databaseTypes = this.appState.databaseTypes;
  protected readonly volumeTypes = this.appState.volumeTypes;
  protected readonly isLoading = this.appState.loading;

  protected readonly showConfirmationModal = signal(false);
  protected readonly showSuccessModal = signal(false);
  protected readonly isSubmitting = signal(false);

  protected readonly configForm: FormGroup<EnvironmentStartForm> =
    this.fb.group<EnvironmentStartForm>({
      databaseContainerId: this.fb.nonNullable.control<DatabaseType | ''>('', Validators.required),
      volumeId: this.fb.nonNullable.control<DatabaseVolumeType | ''>('', Validators.required),
      shutdownContainers: this.fb.nonNullable.control(false),
      removeContainers: this.fb.nonNullable.control(false),
      deleteVolumes: this.fb.nonNullable.control(false),
    });

  constructor() {
    this.configForm.controls.removeContainers.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((removeContainers) => {
        if (removeContainers) {
          this.configForm.controls.shutdownContainers.setValue(true, { emitEvent: false });
          this.configForm.controls.shutdownContainers.disable({ emitEvent: false });
          return;
        }

        this.configForm.controls.shutdownContainers.enable({ emitEvent: false });
      });
  }

  protected get deleteVolumesChecked(): boolean {
    return this.configForm.controls.deleteVolumes.value;
  }

  protected onSubmit(): void {
    if (this.configForm.invalid) {
      this.configForm.markAllAsTouched();
      this.notificationService.error('Please fill all required fields', 'Validation Error');
      return;
    }

    if (this.configForm.controls.deleteVolumes.value) {
      this.showConfirmationModal.set(true);
      return;
    }

    this.submitForm();
  }

  protected onConfirmSubmit(): void {
    this.showConfirmationModal.set(false);
    this.submitForm();
  }

  protected onCancelSubmit(): void {
    this.showConfirmationModal.set(false);
  }

  protected onSuccessModalDismiss(): void {
    this.showSuccessModal.set(false);
  }

  protected onSuccessModalClose(): void {
    this.showSuccessModal.set(false);
    void this.router.navigate(['/docker-resources']);
  }

  private submitForm(): void {
    const formValue = this.configForm.getRawValue();
    const request: EnvironmentRunRequest = {
      databaseType: formValue.databaseContainerId as DatabaseType,
      databaseVolumeType: formValue.volumeId as DatabaseVolumeType,
      shutdownContainers: formValue.shutdownContainers || formValue.removeContainers,
      removeContainers: formValue.removeContainers,
      deleteVolumes: formValue.deleteVolumes,
    };

    this.isSubmitting.set(true);
    this.environmentApi.startEnvironment(request).subscribe({
      next: () => {
        this.appState.setSelectedDatabaseType(request.databaseType);
        this.appState.setSelectedVolumeSize(request.databaseVolumeType);
        this.isSubmitting.set(false);
        this.showSuccessModal.set(true);
      },
      error: () => {
        this.isSubmitting.set(false);
        this.notificationService.error('Failed to Start Environment', 'Error');
      },
    });
  }
}
