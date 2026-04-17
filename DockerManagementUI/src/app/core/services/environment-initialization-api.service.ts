import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { EnvironmentInitializationService, EnvironmentOptionsResponse } from '../../api';

@Injectable({
  providedIn: 'root',
})
export class EnvironmentInitializationApiService {
  private readonly environmentInitializationService = inject(EnvironmentInitializationService);

  getAvailableEnvironmentOptions(): Observable<EnvironmentOptionsResponse> {
    return this.environmentInitializationService.getAvailableEnvironmentOptions();
  }
}
