import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  DatabaseType,
  GatlingRunDto,
  PerformanceTestSimulationType,
  PerformanceTestsService,
  DatabaseVolumeType,
} from '../../api';
import { environment } from '../../../environments/environment';

export type PerformanceTestRunSummary = GatlingRunDto;

@Injectable({
  providedIn: 'root',
})
export class PerformanceTestsApiService {
  private readonly performanceTestsService = inject(PerformanceTestsService);
  private readonly baseUrl = environment.apiUrl;

  runPerformanceTests(
    databaseType: DatabaseType,
    simulationType: PerformanceTestSimulationType,
    volumeSize: DatabaseVolumeType,
  ): Observable<void> {
    return this.performanceTestsService.runPerformanceTests({
      performanceTestsRunRequest: { databaseType, simulationType, volumeSize },
    });
  }

  listRuns(): Observable<PerformanceTestRunSummary[]> {
    return this.performanceTestsService.getPerformanceTestRuns();
  }

  buildRunReportUrl(runId: string): string {
    return `${this.baseUrl}/api/performance-tests/runs/${encodeURIComponent(runId)}/report`;
  }
}
