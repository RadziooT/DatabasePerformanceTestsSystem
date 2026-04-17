import { ChangeDetectionStrategy, Component } from '@angular/core';
import { ContainersComponent } from './containers/containers.component';
import { VolumesComponent } from './volumes/volumes.component';
import { MatTabsModule } from '@angular/material/tabs';

@Component({
  selector: 'app-docker-resources',
  imports: [ContainersComponent, VolumesComponent, MatTabsModule],
  templateUrl: './docker-resources.component.html',
  styleUrl: './docker-resources.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DockerResourcesComponent {}
