import { inject, Injectable } from '@angular/core';
import { NotificationService } from './notification.service';

@Injectable({
  providedIn: 'root',
})
export class ErrorHandlerService {
  private readonly notificationService = inject(NotificationService);

  handleHttpError(error: {
    error?: { message?: string };
    message?: string;
    statusText?: string;
    status?: number;
  }): void {
    let errorMessage = 'An HTTP error occurred';

    if (error.error?.message) {
      errorMessage = error.error.message;
    } else if (error.message) {
      errorMessage = error.message;
    } else if (error.statusText) {
      errorMessage = `${error.status}: ${error.statusText}`;
    }

    console.error('HTTP Error:', error);
    this.notificationService.error(errorMessage, 'Error');
  }
}
