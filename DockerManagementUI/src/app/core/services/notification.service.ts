import { inject, Injectable } from '@angular/core';
import { ToastrService } from 'ngx-toastr';

@Injectable({
  providedIn: 'root',
})
export class NotificationService {
  private readonly toastService = inject(ToastrService);

  success(message: string, title?: string): void {
    this.toastService.success(message, title, {
      timeOut: 3000,
      progressBar: true,
      closeButton: true,
    });
  }

  error(message: string, title?: string): void {
    this.toastService.error(message, title, {
      timeOut: 5000,
      progressBar: true,
      closeButton: true,
    });
  }

  warning(message: string, title?: string): void {
    this.toastService.warning(message, title, {
      timeOut: 4000,
      progressBar: true,
      closeButton: true,
    });
  }

  info(message: string, title?: string): void {
    this.toastService.info(message, title, {
      timeOut: 3000,
      progressBar: true,
      closeButton: true,
    });
  }
}
