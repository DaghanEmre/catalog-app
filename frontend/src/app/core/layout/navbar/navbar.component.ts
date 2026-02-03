import { Component, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';

import { MenubarModule } from 'primeng/menubar';
import { ButtonModule } from 'primeng/button';

import { AuthService } from '../../auth/auth.service';

@Component({
    selector: 'app-navbar',
    standalone: true,
    imports: [CommonModule, RouterLink, MenubarModule, ButtonModule],
    template: `
    <p-menubar [model]="items()">
      <ng-template pTemplate="start">
        <a routerLink="/" class="brand">
          <i class="pi pi-box"></i>
          <span>Catalog App</span>
        </a>
      </ng-template>

      <ng-template pTemplate="end">
        <div class="right">
          <span class="user" *ngIf="user() as u">
            <i class="pi pi-user"></i>
            {{ u.username }} · {{ u.role }}
          </span>

          <p-button
            *ngIf="isAuthed()"
            label="Logout"
            icon="pi pi-sign-out"
            severity="secondary"
            [text]="true"
            (onClick)="logout()"
          ></p-button>

          <p-button
            *ngIf="!isAuthed()"
            label="Login"
            icon="pi pi-sign-in"
            [text]="true"
            (onClick)="goLogin()"
          ></p-button>
        </div>
      </ng-template>
    </p-menubar>
  `,
    styles: [`
    .brand {
      display: flex;
      align-items: center;
      gap: .5rem;
      text-decoration: none;
      color: inherit;
      font-weight: 600;
    }
    .right {
      display: flex;
      align-items: center;
      gap: .75rem;
    }
    .user {
      color: #495057;
      display: flex;
      align-items: center;
      gap: .4rem;
      font-size: .9rem;
    }
  `]
})
export class NavbarComponent {
    private userSig = signal(this.auth.getCurrentUser());
    user = computed(() => this.userSig());

    isAuthed = computed(() => this.auth.isAuthenticated());

    items = computed(() => {
        const authed = this.auth.isAuthenticated();
        return [
            { label: 'Products', icon: 'pi pi-list', routerLink: '/products', visible: authed }
        ];
    });

    constructor(private auth: AuthService, private router: Router) {
        this.auth.currentUser$.subscribe(u => this.userSig.set(u));
    }

    logout(): void {
        this.auth.logout();
        this.router.navigate(['/login']);
    }

    goLogin(): void {
        this.router.navigate(['/login']);
    }
}
