import { computed, inject, Injectable, signal } from '@angular/core';
import { EnvironmentOptionsResponse } from '../api';
import { EnvironmentInitializationApiService } from '../core/services/environment-initialization-api.service';
import { NotificationService } from '../core/services/notification.service';
import { DatabaseType, DatabaseVolumeType } from '../api';

@Injectable({
  providedIn: 'root',
})
export class AppState {
  private readonly initApi = inject(EnvironmentInitializationApiService);
  private readonly notificationService = inject(NotificationService);

  private readonly _loading = signal<boolean>(false);
  private readonly _options = signal<EnvironmentOptionsResponse | null>(null);
  private readonly _error = signal<string | null>(null);
  private readonly _selectedVolumeSize = signal<DatabaseVolumeType | null>(null);
  private readonly _selectedDatabaseType = signal<DatabaseType | null>(null);

  public readonly loading = this._loading.asReadonly();
  public readonly options = this._options.asReadonly();
  public readonly error = this._error.asReadonly();
  public readonly selectedVolumeSize = this._selectedVolumeSize.asReadonly();
  public readonly selectedDatabaseType = this._selectedDatabaseType.asReadonly();

  public readonly databaseTypes = computed(() => this._options()?.databaseTypes || []);
  public readonly volumeTypes = computed(() => this._options()?.volumeTypes || []);
  public readonly performanceTestTypes = computed(
    () => this._options()?.performanceTestTypes || [],
  );

  public loadInitialConfiguration(): void {
    if (this._options() !== null) return;

    this._loading.set(true);
    this._error.set(null);
    this.initApi.getAvailableEnvironmentOptions().subscribe({
      next: (options) => {
        this._options.set(options);
        this._loading.set(false);
      },
      error: () => {
        this._loading.set(false);
        this._error.set('Failed to load configuration options');
        this.notificationService.error('Failed to load configuration options', 'Error');
      },
    });
  }

  public setSelectedVolumeSize(volumeSize: DatabaseVolumeType): void {
    this._selectedVolumeSize.set(volumeSize);
  }

  public setSelectedDatabaseType(databaseType: DatabaseType): void {
    this._selectedDatabaseType.set(databaseType);
  }
}
