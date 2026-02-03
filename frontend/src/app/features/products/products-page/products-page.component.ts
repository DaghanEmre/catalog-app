import { Component, ViewChild, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { Table, TableModule } from 'primeng/table';
import { ToolbarModule } from 'primeng/toolbar';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { DropdownModule } from 'primeng/dropdown';
import { TagModule } from 'primeng/tag';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { SkeletonModule } from 'primeng/skeleton';
import { MessageModule } from 'primeng/message';

import { ConfirmationService, MessageService } from 'primeng/api';

import { Subject, debounceTime, distinctUntilChanged } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { ProductsService } from '../products.service';
import { AuthService } from '../../../core/auth/auth.service';
import { Product, ProductRequest, ProductStatus } from '../../../shared/models/product.model';

type StatusOption = { label: string; value: ProductStatus | null };

@Component({
    selector: 'app-products-page',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        TableModule,
        ToolbarModule,
        ButtonModule,
        DialogModule,
        InputTextModule,
        InputNumberModule,
        DropdownModule,
        TagModule,
        ToastModule,
        ConfirmDialogModule,
        SkeletonModule,
        MessageModule
    ],
    template: `
    <p-toast></p-toast>
    <p-confirmDialog></p-confirmDialog>

    <div class="card">
      <!-- Toolbar -->
      <p-toolbar styleClass="mb-3">
        <div class="p-toolbar-group-start">
          <h2 class="title">Products Catalog</h2>
          <span class="subtitle" *ngIf="!isAdmin">
            <i class="pi pi-eye"></i> View Mode
          </span>
          <span class="subtitle admin" *ngIf="isAdmin">
            <i class="pi pi-shield"></i> Admin Mode
          </span>
        </div>

        <div class="p-toolbar-group-end">
          <p-button
            label="Clear Filters"
            icon="pi pi-filter-slash"
            severity="secondary"
            [text]="true"
            (onClick)="clearFilters()"
          ></p-button>

          <p-button
            *ngIf="isAdmin"
            label="New Product"
            icon="pi pi-plus"
            (onClick)="openCreate()"
          ></p-button>
        </div>
      </p-toolbar>

      <!-- Filter Row -->
      <div class="filters mb-3">
        <span class="p-input-icon-left flex-1">
          <i class="pi pi-search"></i>
          <input
            pInputText
            type="text"
            [(ngModel)]="globalSearchValue"
            (ngModelChange)="onGlobalSearchValueChange($event)"
            placeholder="Search by name or status..."
            class="w-full"
          />
        </span>

        <p-dropdown
          class="status-filter"
          [options]="statusFilterOptions"
          optionLabel="label"
          optionValue="value"
          [(ngModel)]="selectedStatus"
          placeholder="Filter by status"
          (onChange)="applyStatusFilter()"
          [showClear]="true"
        ></p-dropdown>
      </div>

      <!-- Products Table -->
      <p-table
        #dt
        [value]="products"
        [loading]="loading"
        [paginator]="true"
        [rows]="10"
        [rowsPerPageOptions]="[10, 20, 50]"
        [globalFilterFields]="['name', 'status']"
        sortMode="multiple"
        [rowHover]="true"
        styleClass="p-datatable-sm"
        responsiveLayout="scroll"
      >
        <ng-template pTemplate="header">
          <tr>
            <th pSortableColumn="id" style="width: 90px;">
              ID <p-sortIcon field="id"></p-sortIcon>
            </th>

            <th pSortableColumn="name">
              Name <p-sortIcon field="name"></p-sortIcon>
            </th>

            <th pSortableColumn="price" style="width: 170px;">
              Price <p-sortIcon field="price"></p-sortIcon>
            </th>

            <th pSortableColumn="stock" style="width: 130px;">
              Stock <p-sortIcon field="stock"></p-sortIcon>
            </th>

            <th pSortableColumn="status" style="width: 170px;">
              Status <p-sortIcon field="status"></p-sortIcon>
            </th>

            <th *ngIf="isAdmin" style="width: 140px;">Actions</th>
          </tr>
        </ng-template>

        <!-- Loading Skeleton -->
        <ng-template pTemplate="loadingbody">
          <tr *ngFor="let _ of skeletonRows">
            <td><p-skeleton width="3rem" height="1rem"></p-skeleton></td>
            <td><p-skeleton width="12rem" height="1rem"></p-skeleton></td>
            <td><p-skeleton width="6rem" height="1rem"></p-skeleton></td>
            <td><p-skeleton width="4rem" height="1rem"></p-skeleton></td>
            <td><p-skeleton width="7rem" height="1.3rem"></p-skeleton></td>
            <td *ngIf="isAdmin">
              <div class="flex gap-2">
                <p-skeleton width="2rem" height="2rem"></p-skeleton>
                <p-skeleton width="2rem" height="2rem"></p-skeleton>
              </div>
            </td>
          </tr>
        </ng-template>

        <!-- Table Body -->
        <ng-template pTemplate="body" let-p>
          <tr>
            <td>{{ p.id }}</td>
            <td><strong>{{ p.name }}</strong></td>
            <td>{{ p.price | currency:'TRY':'symbol':'1.2-2' }}</td>
            <td>{{ p.stock | number }}</td>
            <td>
              <p-tag
                [value]="p.status"
                [severity]="statusSeverity(p.status)"
              ></p-tag>
            </td>

            <td *ngIf="isAdmin">
              <p-button
                icon="pi pi-pencil"
                severity="secondary"
                [text]="true"
                [rounded]="true"
                (onClick)="openEdit(p)"
                pTooltip="Edit product"
                tooltipPosition="top"
              ></p-button>
              <p-button
                icon="pi pi-trash"
                severity="danger"
                [text]="true"
                [rounded]="true"
                (onClick)="confirmDelete(p)"
                pTooltip="Delete product"
                tooltipPosition="top"
              ></p-button>
            </td>
          </tr>
        </ng-template>

        <!-- Empty Message -->
        <ng-template pTemplate="emptymessage">
          <tr>
            <td [attr.colspan]="isAdmin ? 6 : 5" class="text-center">
              <div class="empty-message">
                <i class="pi pi-inbox"></i>
                <p>No products found</p>
                <small *ngIf="globalSearchValue || selectedStatus">
                  Try adjusting your filters
                </small>
              </div>
            </td>
          </tr>
        </ng-template>
      </p-table>
    </div>

    <!-- Create/Edit Dialog -->
    <p-dialog
      [(visible)]="dialogVisible"
      [header]="dialogTitle"
      [modal]="true"
      [style]="{ width: '550px' }"
      [closable]="!saving"
      [draggable]="false"
    >
      <form #f="ngForm" (ngSubmit)="save(f)" novalidate>
        <div class="form">
          <!-- Name Field -->
          <div class="field">
            <label for="name" class="required">Product Name</label>
            <input
              pInputText
              id="name"
              [(ngModel)]="form.name"
              name="name"
              required
              minlength="1"
              maxlength="255"
              [disabled]="saving"
              class="w-full"
              #nameModel="ngModel"
              placeholder="Enter product name"
            />

            <p-message
              *ngIf="(submitted || nameModel.touched) && nameModel.invalid"
              severity="error"
              [text]="nameError(nameModel.errors)"
              styleClass="w-full mt-2"
            ></p-message>
          </div>

          <!-- Price Field -->
          <div class="field">
            <label for="price" class="required">Price (TRY)</label>
            <p-inputNumber
              inputId="price"
              [(ngModel)]="form.price"
              name="price"
              required
              [min]="0.01"
              [step]="0.01"
              [disabled]="saving"
              mode="currency"
              currency="TRY"
              locale="tr-TR"
              [minFractionDigits]="2"
              [maxFractionDigits]="2"
              class="w-full"
              #priceModel="ngModel"
              placeholder="0.00"
            ></p-inputNumber>

            <p-message
              *ngIf="(submitted || priceModel.touched) && priceModel.invalid"
              severity="error"
              text="Price must be greater than 0"
              styleClass="w-full mt-2"
            ></p-message>
          </div>

          <!-- Stock Field -->
          <div class="field">
            <label for="stock" class="required">Stock Quantity</label>
            <p-inputNumber
              inputId="stock"
              [(ngModel)]="form.stock"
              name="stock"
              required
              [min]="0"
              [step]="1"
              [useGrouping]="true"
              [disabled]="saving"
              mode="decimal"
              [minFractionDigits]="0"
              [maxFractionDigits]="0"
              class="w-full"
              #stockModel="ngModel"
              placeholder="0"
            ></p-inputNumber>

            <p-message
              *ngIf="(submitted || stockModel.touched) && stockModel.invalid"
              severity="error"
              text="Stock cannot be negative"
              styleClass="w-full mt-2"
            ></p-message>
          </div>

          <!-- Status Field -->
          <div class="field">
            <label for="status" class="required">Status</label>
            <p-dropdown
              inputId="status"
              [options]="statusOptions"
              optionLabel="label"
              optionValue="value"
              [(ngModel)]="form.status"
              name="status"
              required
              [disabled]="saving"
              class="w-full"
              #statusModel="ngModel"
              placeholder="Select status"
            ></p-dropdown>

            <p-message
              *ngIf="(submitted || statusModel.touched) && statusModel.invalid"
              severity="error"
              text="Status is required"
              styleClass="w-full mt-2"
            ></p-message>
          </div>

          <!-- Form Error -->
          <p-message
            *ngIf="formError"
            severity="error"
            [text]="formError"
            styleClass="w-full"
          ></p-message>
        </div>

        <!-- Dialog Footer -->
        <ng-template pTemplate="footer">
          <p-button
            label="Cancel"
            icon="pi pi-times"
            severity="secondary"
            [text]="true"
            [disabled]="saving"
            (onClick)="closeDialog()"
            type="button"
          ></p-button>

          <p-button
            label="Save"
            icon="pi pi-check"
            [loading]="saving"
            type="submit"
          ></p-button>
        </ng-template>
      </form>
    </p-dialog>
  `,
    styles: [`
    .card {
      background: #fff;
      border-radius: 12px;
      padding: 1.5rem;
      box-shadow: 0 1px 3px rgba(0,0,0,0.1);
    }

    .title {
      margin: 0;
      font-size: 1.5rem;
      font-weight: 600;
      color: #1e293b;
    }

    .subtitle {
      margin-left: 1rem;
      font-size: 0.875rem;
      color: #64748b;
      display: inline-flex;
      align-items: center;
      gap: 0.25rem;
      padding: 0.25rem 0.75rem;
      background: #f1f5f9;
      border-radius: 9999px;
    }

    .subtitle.admin {
      background: #dbeafe;
      color: #1e40af;
    }

    .filters {
      display: flex;
      gap: 0.75rem;
      align-items: center;
    }

    .flex-1 {
      flex: 1;
    }

    .status-filter {
      width: 240px;
    }

    .form {
      display: grid;
      gap: 1.25rem;
      padding: 0.5rem 0;
    }

    .field label {
      display: block;
      margin-bottom: 0.5rem;
      font-weight: 600;
      color: #334155;
      font-size: 0.875rem;
    }

    .field label.required::after {
      content: ' *';
      color: #ef4444;
    }

    .w-full {
      width: 100%;
    }

    .mt-2 {
      margin-top: 0.5rem;
    }

    .mb-3 {
      margin-bottom: 1rem;
    }

    .flex {
      display: flex;
    }

    .gap-2 {
      gap: 0.5rem;
    }

    .text-center {
      text-align: center;
    }

    .empty-message {
      padding: 3rem 1rem;
      color: #94a3b8;
    }

    .empty-message i {
      font-size: 3rem;
      display: block;
      margin-bottom: 1rem;
      opacity: 0.5;
    }

    .empty-message p {
      font-size: 1.125rem;
      font-weight: 500;
      margin: 0.5rem 0;
      color: #64748b;
    }

    .empty-message small {
      color: #94a3b8;
    }
  `]
})
export class ProductsPageComponent {
    @ViewChild('dt') dt?: Table;

    products: Product[] = [];
    loading = false;

    dialogVisible = false;
    dialogTitle = 'New Product';
    saving = false;
    editingId: number | null = null;

    submitted = false;

    form: ProductRequest = { name: '', price: 0, stock: 0, status: 'ACTIVE' };
    formError = '';

    skeletonRows = Array.from({ length: 8 });

    statusOptions: { label: string; value: ProductStatus }[] = [
        { label: 'Active', value: 'ACTIVE' },
        { label: 'Discontinued', value: 'DISCONTINUED' }
    ];

    statusFilterOptions: StatusOption[] = [
        { label: 'All Statuses', value: null },
        { label: 'Active', value: 'ACTIVE' },
        { label: 'Discontinued', value: 'DISCONTINUED' }
    ];

    // ✅ Global search with two-way binding
    globalSearchValue = '';
    selectedStatus: ProductStatus | null = null;

    private readonly destroyRef = inject(DestroyRef);
    private readonly globalSearch$ = new Subject<string>();
    private lastGlobalTerm = '';

    get isAdmin(): boolean {
        return this.auth.isAdmin();
    }

    constructor(
        private api: ProductsService,
        private auth: AuthService,
        private toast: MessageService,
        private confirm: ConfirmationService
    ) {
        // Debounced global search (300ms)
        this.globalSearch$
            .pipe(
                debounceTime(300),
                distinctUntilChanged(),
                takeUntilDestroyed(this.destroyRef)
            )
            .subscribe((term) => {
                this.lastGlobalTerm = term;
                this.dt?.filterGlobal(term, 'contains');
            });
    }

    ngOnInit(): void {
        this.reload();
    }

    reload(): void {
        this.loading = true;
        this.api.list().subscribe({
            next: (data) => {
                this.products = data ?? [];
                this.loading = false;
                this.reapplyFilters();
            },
            error: (err) => {
                this.loading = false;
                this.toast.add({
                    severity: 'error',
                    summary: 'Error',
                    detail: err?.error?.detail || 'Failed to load products',
                    life: 5000
                });
            }
        });
    }

    // ✅ Handle search input changes
    onGlobalSearchValueChange(value: string): void {
        this.globalSearch$.next(value ?? '');
    }

    // ✅ Status dropdown filter
    applyStatusFilter(): void {
        if (!this.dt) return;

        if (this.selectedStatus == null) {
            this.dt.filter('', 'status', 'equals');
        } else {
            this.dt.filter(this.selectedStatus, 'status', 'equals');
        }
    }

    // ✅ Clear all filters (including input)
    clearFilters(): void {
        this.selectedStatus = null;
        this.globalSearchValue = '';
        this.lastGlobalTerm = '';

        this.dt?.clear();

        this.toast.add({
            severity: 'info',
            summary: 'Filters Cleared',
            detail: 'All filters have been reset',
            life: 3000
        });
    }

    // ✅ Reapply filters after reload
    private reapplyFilters(): void {
        if (!this.dt) return;

        const term = (this.globalSearchValue || '').trim();
        if (term) {
            this.lastGlobalTerm = term;
            this.dt.filterGlobal(term, 'contains');
        }

        if (this.selectedStatus) {
            this.dt.filter(this.selectedStatus, 'status', 'equals');
        }
    }

    statusSeverity(status: ProductStatus): 'success' | 'danger' | 'secondary' {
        return status === 'ACTIVE' ? 'success' : 'danger';
    }

    openCreate(): void {
        this.dialogTitle = 'New Product';
        this.editingId = null;
        this.form = { name: '', price: 0, stock: 0, status: 'ACTIVE' };
        this.formError = '';
        this.submitted = false;
        this.dialogVisible = true;
    }

    openEdit(p: Product): void {
        this.dialogTitle = `Edit Product #${p.id}`;
        this.editingId = p.id;
        this.form = { name: p.name, price: p.price, stock: p.stock, status: p.status };
        this.formError = '';
        this.submitted = false;
        this.dialogVisible = true;
    }

    closeDialog(): void {
        if (this.saving) return;
        this.dialogVisible = false;
    }

    nameError(errors: any): string {
        if (!errors) return 'Name is required';
        if (errors['required']) return 'Name is required';
        if (errors['minlength']) return 'Name is required';
        return 'Invalid name';
    }

    save(formRef: any): void {
        this.submitted = true;
        this.formError = '';

        if (!formRef?.valid) {
            this.formError = 'Please fix validation errors.';
            return;
        }

        const payload: ProductRequest = {
            name: (this.form.name || '').trim(),
            price: Number(this.form.price),
            stock: Number(this.form.stock),
            status: this.form.status
        };

        this.saving = true;

        const done = (toastDetail: string) => {
            this.saving = false;
            this.dialogVisible = false;
            this.toast.add({ severity: 'success', summary: 'Success', detail: toastDetail });
            this.reload();
        };

        if (this.editingId == null) {
            this.api.create(payload).subscribe({
                next: () => done('Product created'),
                error: (err) => {
                    this.saving = false;
                    this.toast.add({ severity: 'error', summary: 'Error', detail: err?.error?.detail || 'Create failed' });
                }
            });
        } else {
            this.api.update(this.editingId, payload).subscribe({
                next: () => done('Product updated'),
                error: (err) => {
                    this.saving = false;
                    this.toast.add({ severity: 'error', summary: 'Error', detail: err?.error?.detail || 'Update failed' });
                }
            });
        }
    }

    confirmDelete(p: Product): void {
        this.confirm.confirm({
            header: 'Confirm',
            message: `Delete product "${p.name}"?`,
            icon: 'pi pi-exclamation-triangle',
            accept: () => this.delete(p)
        });
    }

    private delete(p: Product): void {
        this.api.delete(p.id).subscribe({
            next: () => {
                this.toast.add({ severity: 'success', summary: 'Deleted', detail: 'Product deleted' });
                this.reload();
            },
            error: (err) => {
                this.toast.add({ severity: 'error', summary: 'Error', detail: err?.error?.detail || 'Delete failed' });
            }
        });
    }
}
