import { EnvironmentProviders, makeEnvironmentProviders } from '@angular/core';

import { BASE_PATH, Configuration } from '../../api';
import { environment } from '../../../environments/environment';

export function provideApiConfiguration(): EnvironmentProviders {
  return makeEnvironmentProviders([
    { provide: BASE_PATH, useValue: environment.apiUrl },
    {
      provide: Configuration,
      useFactory: () => new Configuration({ basePath: environment.apiUrl }),
    },
  ]);
}
