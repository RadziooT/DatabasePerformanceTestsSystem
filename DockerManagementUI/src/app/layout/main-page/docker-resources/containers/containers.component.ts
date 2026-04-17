import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { ContainerActions, ContainerDto } from '../../../../api';
import { ContainersApiService } from '../../../../core/services/containers-api.service';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatProgressBarModule } from '@angular/material/progress-bar';

@Component({
  selector: 'app-containers',
  imports: [
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatProgressBarModule,
  ],
  templateUrl: './containers.component.html',
  styleUrl: './containers.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ContainersComponent implements OnInit {
  private readonly containersApiService = inject(ContainersApiService);

  protected readonly isLoading = signal(false);
  protected readonly loadingContainers = signal<Set<string>>(new Set());
  protected readonly containers = signal<ContainerDto[]>([]);
  protected readonly displayedColumns = ['id', 'name', 'state', 'image', 'volumes', 'actions'];

  ngOnInit(): void {
    this.loadContainers();
  }

  protected loadContainers(): void {
    this.isLoading.set(true);
    this.containersApiService.getAllContainers().subscribe({
      next: (containers) => {
        this.containers.set(containers);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      },
    });
  }

  protected refreshContainers(): void {
    this.loadContainers();
  }

  protected isActionEnabled(container: ContainerDto, actionType: 'stop' | 'delete'): boolean {
    const actionMap: Record<'stop' | 'delete', ContainerActions> = {
      stop: ContainerActions.STOP,
      delete: ContainerActions.DELETE,
    };
    return container.availableActions?.includes(actionMap[actionType]) ?? false;
  }

  protected isContainerLoading(container: ContainerDto): boolean {
    return this.loadingContainers().has(container.containerType || '');
  }

  private setContainerLoading(containerType: string, isLoading: boolean): void {
    this.loadingContainers.update((set) => {
      const newSet = new Set(set);
      if (isLoading) {
        newSet.add(containerType);
      } else {
        newSet.delete(containerType);
      }
      return newSet;
    });
  }

  protected stopContainer(container: ContainerDto): void {
    const type = container.containerType;
    if (!type) return;

    this.setContainerLoading(type, true);
    this.containersApiService.stopContainer(type).subscribe({
      next: () => {
        this.setContainerLoading(type, false);
        this.loadContainers();
      },
      error: () => {
        this.setContainerLoading(type, false);
      },
    });
  }

  protected deleteContainer(container: ContainerDto): void {
    const type = container.containerType;
    if (!type) return;

    this.setContainerLoading(type, true);
    this.containersApiService.deleteContainer(type).subscribe({
      next: () => {
        this.setContainerLoading(type, false);
        this.loadContainers();
      },
      error: () => {
        this.setContainerLoading(type, false);
      },
    });
  }

  protected stopAllContainers(): void {
    this.isLoading.set(true);
    this.containersApiService.stopAllContainers().subscribe({
      next: () => {
        this.loadContainers();
      },
      error: () => {
        this.isLoading.set(false);
      },
    });
  }

  protected getVolumesDisplay(container: ContainerDto): string {
    return container.volumes?.length ? container.volumes.join(', ') : '-';
  }

  protected getStateBadgeClass(state: string | undefined): string {
    switch (state) {
      case 'RUNNING':
        return 'bg-success';
      case 'STOPPED':
      case 'EXITED':
      case 'DEAD':
        return 'bg-danger';
      case 'NOT_CREATED':
        return 'bg-secondary';
      default:
        return 'bg-warning text-dark';
    }
  }
}
