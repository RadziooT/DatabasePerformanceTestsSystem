import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AppState } from './store/app.state';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  private readonly appState = inject(AppState);

  constructor() {
    this.appState.loadInitialConfiguration();
  }
}
