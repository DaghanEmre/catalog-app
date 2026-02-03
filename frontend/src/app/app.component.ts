import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';

import { NavbarComponent } from './core/layout/navbar/navbar.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, NavbarComponent],
  template: `
    <app-navbar></app-navbar>
    <main class="app-shell">
      <router-outlet></router-outlet>
    </main>
  `,
  styles: [`
    .app-shell {
      max-width: 1200px;
      margin: 0 auto;
      padding: 1rem;
    }
  `]
})
export class AppComponent { }
