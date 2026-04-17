import { Component, ChangeDetectionStrategy, output } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

interface NavItem {
  path: string;
  label: string;
  icon: string;
}

@Component({
  selector: 'app-sidebar',
  imports: [RouterLink, RouterLinkActive, MatButtonModule, MatIconModule],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SidebarComponent {
  readonly toggleSidebar = output<void>();

  protected readonly navItems: NavItem[] = [
    {
      path: '/info',
      label: 'Info',
      icon: 'info-circle'
    },
    {
      path: '/docker-resources',
      label: 'Docker Resources',
      icon: 'hdd'
    },
    {
      path: '/environment-management',
      label: 'Environment Management',
      icon: 'gear'
    },
    {
      path: '/tests',
      label: 'Performance Tests',
      icon: 'speedometer2'
    }
  ];
}
