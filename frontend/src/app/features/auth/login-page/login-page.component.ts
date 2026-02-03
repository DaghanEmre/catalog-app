import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { ButtonModule } from 'primeng/button';
import { MessageModule } from 'primeng/message';

import { AuthService } from '../../../core/auth/auth.service';

@Component({
    selector: 'app-login-page',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        CardModule,
        InputTextModule,
        PasswordModule,
        ButtonModule,
        MessageModule
    ],
    template: `
    <div class="wrap">
      <p-card header="Login" class="card">
        <form (ngSubmit)="submit()" novalidate>
          <div class="field">
            <label>Username</label>
            <input pInputText [(ngModel)]="username" name="username" [disabled]="loading" class="w-full" />
          </div>

          <div class="field">
            <label>Password</label>
            <p-password
              [(ngModel)]="password"
              name="password"
              [disabled]="loading"
              [feedback]="false"
              [toggleMask]="true"
              styleClass="w-full"
            ></p-password>
          </div>

          <p-message *ngIf="error" severity="error" [text]="error" styleClass="w-full"></p-message>

          <p-button
            label="Sign In"
            type="submit"
            [loading]="loading"
            styleClass="w-full"
          ></p-button>

          <div class="hint">
            <small>
              <b>Demo:</b><br/>
              admin / admin123 (ROLE_ADMIN)<br/>
              user / user123 (ROLE_USER)
            </small>
          </div>
        </form>
      </p-card>
    </div>
  `,
    styles: [`
    .wrap {
      min-height: calc(100vh - 64px);
      display: grid;
      place-items: center;
      padding: 1rem;
    }
    .card { width: 100%; max-width: 420px; }
    .field { margin-bottom: 1rem; }
    label { display: block; margin-bottom: .35rem; font-weight: 600; }
    .hint { margin-top: 1.25rem; padding-top: .75rem; border-top: 1px solid #e5e7eb; color: #6b7280; }
    .w-full { width: 100%; }
  `]
})
export class LoginPageComponent {
    username = '';
    password = '';
    loading = false;
    error = '';

    constructor(private auth: AuthService, private router: Router) { }

    submit(): void {
        if (!this.username || !this.password) {
            this.error = 'Please enter username and password';
            return;
        }

        this.loading = true;
        this.error = '';

        this.auth.login({ username: this.username, password: this.password }).subscribe({
            next: () => this.router.navigate(['/products']),
            error: (err) => {
                this.loading = false;
                this.error = err?.error?.detail || 'Invalid credentials';
            }
        });
    }
}
