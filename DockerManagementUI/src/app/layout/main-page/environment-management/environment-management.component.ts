import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatTabsModule } from '@angular/material/tabs';

@Component({
  selector: 'app-environment-management',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, MatTabsModule],
  templateUrl: './environment-management.component.html',
  styleUrl: './environment-management.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EnvironmentManagementComponent {}
