import { Routes } from '@angular/router';
import { MainLayoutComponent } from './layout/main-layout.component';

export const routes: Routes = [
  {
    path: '',
    component: MainLayoutComponent,
    children: [
      {
        path: '',
        redirectTo: 'info',
        pathMatch: 'full',
      },
      {
        path: 'info',
        loadComponent: () =>
          import('./layout/main-page/info/info.component').then((m) => m.InfoComponent),
      },
      {
        path: 'docker-resources',
        loadComponent: () =>
          import('./layout/main-page/docker-resources/docker-resources.component').then(
            (m) => m.DockerResourcesComponent,
          ),
      },
      {
        path: 'containers',
        redirectTo: 'docker-resources',
        pathMatch: 'full',
      },
      {
        path: 'volumes',
        redirectTo: 'docker-resources',
        pathMatch: 'full',
      },
      {
        path: 'configuration',
        redirectTo: 'environment-management',
        pathMatch: 'full',
      },
      {
        path: 'environment-management',
        loadComponent: () =>
          import('./layout/main-page/environment-management/environment-management.component').then(
            (m) => m.EnvironmentManagementComponent,
          ),
        children: [
          {
            path: '',
            redirectTo: 'environment-start',
            pathMatch: 'full',
          },
          {
            path: 'environment',
            redirectTo: 'environment-start',
            pathMatch: 'full',
          },
          {
            path: 'environment-start',
            loadComponent: () =>
              import('./layout/main-page/environment-management/environment-start/environment-start.component').then(
                (m) => m.EnvironmentStartComponent,
              ),
          },
          {
            path: 'data-generation',
            loadComponent: () =>
              import('./layout/main-page/environment-management/data-generation/data-generation.component').then(
                (m) => m.DataGenerationComponent,
              ),
          },
        ],
      },
      {
        path: 'tests',
        loadComponent: () =>
          import('./layout/main-page/performance-tests/performance-tests.component').then(
            (m) => m.PerformanceTestsComponent,
          ),
      },
    ],
  },
  {
    path: '**',
    redirectTo: 'info',
  },
];
