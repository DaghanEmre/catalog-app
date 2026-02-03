import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { LoginPageComponent } from './features/auth/login-page/login-page.component';
import { ProductsPageComponent } from './features/products/products-page/products-page.component';

export const routes: Routes = [
    { path: '', pathMatch: 'full', redirectTo: 'products' },
    { path: 'login', component: LoginPageComponent },
    { path: 'products', component: ProductsPageComponent, canActivate: [authGuard] },
    { path: '**', redirectTo: 'products' }
];
