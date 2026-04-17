import { inject, Injectable } from '@angular/core';
import { Observable, catchError, tap, throwError } from 'rxjs';
import { ContainerDto, ContainersService, ContainerType } from '../../api';
import { NotificationService } from './notification.service';

@Injectable({
  providedIn: 'root',
})
export class ContainersApiService {
  private readonly containersService = inject(ContainersService);
  private readonly notificationService = inject(NotificationService);

  getAllContainers(): Observable<ContainerDto[]> {
    return this.containersService.getAllContainers().pipe(
      catchError((error) => {
        this.notificationService.error('Failed to load containers', 'Error');
        return throwError(() => error);
      }),
    );
  }

  stopContainer(containerType: ContainerType): Observable<void> {
    return this.containersService.stopContainer({ containerType }).pipe(
      tap(() => this.notificationService.success('Container stopped successfully', 'Success')),
      catchError((error) => {
        this.notificationService.error('Failed to stop container', 'Error');
        return throwError(() => error);
      }),
    );
  }

  deleteContainer(containerType: ContainerType): Observable<void> {
    return this.containersService.deleteContainer({ containerType }).pipe(
      tap(() => this.notificationService.success('Container deleted successfully', 'Success')),
      catchError((error) => {
        this.notificationService.error('Failed to delete container', 'Error');
        return throwError(() => error);
      }),
    );
  }

  stopAllContainers(): Observable<void> {
    return this.containersService.stopAllContainers().pipe(
      tap(() => this.notificationService.success('All containers have been stopped', 'Success')),
      catchError((error) => {
        this.notificationService.error('Failed to stop all containers', 'Error');
        return throwError(() => error);
      }),
    );
  }
}
