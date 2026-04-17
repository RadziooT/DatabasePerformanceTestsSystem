import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { EnvironmentRunRequest, EnvironmentService } from '../../api';

@Injectable({
  providedIn: 'root',
})
export class EnvironmentApiService {
  private readonly environmentService = inject(EnvironmentService);

  startEnvironment(environmentRunRequest: EnvironmentRunRequest): Observable<void> {
    return this.environmentService.startEnvironment({ environmentRunRequest });
  }
}
