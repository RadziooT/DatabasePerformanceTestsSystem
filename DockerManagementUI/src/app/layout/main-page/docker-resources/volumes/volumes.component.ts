import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { VolumeStatus, VolumesDto } from '../../../../api';
import { VolumesApiService } from '../../../../core/services/volumes-api.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatProgressBarModule } from '@angular/material/progress-bar';

@Component({
  selector: 'app-volumes',
  imports: [
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatProgressBarModule,
  ],
  templateUrl: './volumes.component.html',
  styleUrl: './volumes.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class VolumesComponent implements OnInit {
  private readonly notificationService = inject(NotificationService);
  private readonly volumesApiService = inject(VolumesApiService);

  protected readonly isLoading = signal(false);
  protected readonly volumes = signal<VolumesDto[]>([]);
  protected readonly volumeOperations = signal<Record<string, boolean>>({});
  protected readonly displayedColumns = ['name', 'mountpoint', 'status', 'actions'];

  ngOnInit(): void {
    this.loadVolumes();
  }

  protected loadVolumes(): void {
    this.isLoading.set(true);
    this.volumesApiService.getVolumes().subscribe({
      next: (volumes) => {
        this.volumes.set(volumes);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      },
    });
  }

  protected refreshVolumes(): void {
    this.loadVolumes();
  }

  protected deleteVolume(volume: VolumesDto): void {
    if (!volume.name || !volume.volumeType) {
      this.notificationService.error('Volume name or type is missing', 'Error');
      return;
    }

    this.setVolumeOperation(volume.name, true);
    this.volumesApiService.deleteVolume(volume.volumeType, volume.name).subscribe({
      next: () => {
        this.setVolumeOperation(volume.name!, false);
        this.loadVolumes();
      },
      error: () => {
        this.setVolumeOperation(volume.name!, false);
      },
    });
  }

  protected removeAllVolumes(): void {
    this.isLoading.set(true);
    this.volumesApiService.deleteAllVolumes().subscribe({
      next: () => {
        this.isLoading.set(false);
        this.loadVolumes();
      },
      error: () => {
        this.isLoading.set(false);
      },
    });
  }

  protected isVolumeLoading(volumeName: string | undefined): boolean {
    if (!volumeName) {
      return false;
    }

    return this.volumeOperations()[volumeName] ?? false;
  }

  protected isVolumeCreated(volume: VolumesDto): boolean {
    return volume.status === VolumeStatus.CREATED;
  }

  private setVolumeOperation(volumeName: string, loading: boolean): void {
    this.volumeOperations.update((ops) => ({ ...ops, [volumeName]: loading }));
  }
}
