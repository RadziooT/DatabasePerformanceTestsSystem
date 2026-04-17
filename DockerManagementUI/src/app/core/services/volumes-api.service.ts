import { inject, Injectable } from '@angular/core';
import { Observable, catchError, tap, throwError } from 'rxjs';
import { VolumesDto, VolumesService, VolumeType } from '../../api';
import { NotificationService } from './notification.service';

@Injectable({
  providedIn: 'root',
})
export class VolumesApiService {
  private readonly volumesService = inject(VolumesService);
  private readonly notificationService = inject(NotificationService);

  getVolumes(): Observable<VolumesDto[]> {
    return this.volumesService.getVolumes().pipe(
      catchError((error) => {
        this.notificationService.error('Failed to load volumes', 'Error');
        return throwError(() => error);
      }),
    );
  }

  deleteVolume(volumeType: VolumeType, volumeName?: string): Observable<void> {
    return this.volumesService.deleteVolume({ volumeType }).pipe(
      tap(() =>
        this.notificationService.success(
          `Volume ${volumeName || volumeType} deleted successfully`,
          'Success',
        ),
      ),
      catchError((error) => {
        this.notificationService.error(
          `Failed to delete volume ${volumeName || volumeType}`,
          'Error',
        );
        return throwError(() => error);
      }),
    );
  }

  deleteAllVolumes(): Observable<void> {
    return this.volumesService.deleteAllVolumes().pipe(
      tap(() => this.notificationService.success('All volumes have been removed', 'Success')),
      catchError((error) => {
        this.notificationService.error('Failed to remove all volumes', 'Error');
        return throwError(() => error);
      }),
    );
  }
}
