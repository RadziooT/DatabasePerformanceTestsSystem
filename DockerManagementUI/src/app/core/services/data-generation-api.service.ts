import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  DataGenerationService,
  DataGenerationStartRequest,
  DataGenerationStatusResponse,
} from '../../api';

export type DataGenerationStatus = DataGenerationStatusResponse;
export type DataGenerationRequest = DataGenerationStartRequest;

@Injectable({
  providedIn: 'root',
})
export class DataGenerationApiService {
  private readonly dataGenerationService = inject(DataGenerationService);

  startDataGeneration(payload: DataGenerationRequest): Observable<DataGenerationStatus> {
    return this.dataGenerationService.startDataGeneration({ dataGenerationStartRequest: payload });
  }

  getDataGenerationStatus(): Observable<DataGenerationStatus> {
    return this.dataGenerationService.getDataGenerationStatus();
  }

  createIdleStatus(): DataGenerationStatus {
    return {
      state: 'IDLE',
      progressPercent: 0,
      currentStep: '',
    };
  }
}
