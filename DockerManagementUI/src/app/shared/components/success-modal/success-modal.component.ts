import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  inject,
  input,
  OnDestroy,
  OnInit,
  output,
} from '@angular/core';

@Component({
  selector: 'app-success-modal',
  imports: [],
  templateUrl: './success-modal.component.html',
  styleUrl: './success-modal.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SuccessModalComponent implements OnInit, OnDestroy {
  readonly isOpen = input.required<boolean>();
  readonly title = input<string>('Success');
  readonly message = input.required<string>();
  readonly buttonText = input<string>('OK');

  readonly confirmed = output<void>();
  readonly dismissed = output<void>();
  readonly buttonClicked = output<void>();

  private readonly elementRef = inject(ElementRef);

  ngOnInit(): void {
    document.body.appendChild(this.elementRef.nativeElement);
  }

  ngOnDestroy(): void {
    this.elementRef.nativeElement.remove();
  }

  protected onButtonClick(): void {
    this.confirmed.emit();
  }

  protected onDismissClick(): void {
    this.dismissed.emit();
  }

  protected onBackdropClick(event: MouseEvent): void {
    if (event.target === event.currentTarget) {
      this.onDismissClick();
    }
  }
}
