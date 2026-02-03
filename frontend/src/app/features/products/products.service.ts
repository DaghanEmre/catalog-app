import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Product, ProductRequest, ProductStatus, PagedResponse } from '../../shared/models/product.model';

@Injectable({ providedIn: 'root' })
export class ProductsService {
    private readonly base = '/api/products';

    constructor(private http: HttpClient) { }

    /**
     * List all products (client-side filtering).
     * Deprecated in favor of listPaged() for better scalability.
     * @returns Observable of all products
     */
    list(): Observable<Product[]> {
        return this.http.get<Product[]>(this.base);
    }

    /**
     * List products with server-side pagination, search, and filtering.
     * PR-1 Feature: Backend endpoint ready, frontend integration in PR-2.
     *
     * NOTE: Planned for PR-2 (Frontend Architecture Refactor)
     * This method will be used by ProductsPageComponent when we implement:
     * - PrimeNG virtual scroll lazy loading
     * - Products Facade (state management)
     * - Reactive component with OnPush detection
     *
     * @param opts Pagination options:
     *   - page: page number (0-indexed)
     *   - size: items per page (1-200)
     *   - q: search query (optional, matches product name)
     *   - status: filter by status ACTIVE|DISCONTINUED (optional)
     *   - sort: sort field and direction, e.g. "name,asc" (optional)
     * @returns Observable of paginated products with metadata
     */
    listPaged(opts: {
        page: number;
        size: number;
        q?: string;
        status?: ProductStatus | null;
        sort?: string;
    }): Observable<PagedResponse<Product>> {
        let params = new HttpParams()
            .set('page', String(opts.page))
            .set('size', String(opts.size));

        if (opts.q?.trim()) {
            params = params.set('q', opts.q.trim());
        }
        if (opts.status) {
            params = params.set('status', opts.status);
        }
        if (opts.sort?.trim()) {
            params = params.set('sort', opts.sort.trim());
        }

        return this.http.get<PagedResponse<Product>>(`${this.base}/paged`, { params });
    }

    create(req: ProductRequest): Observable<Product> {
        return this.http.post<Product>(this.base, req);
    }

    update(id: number, req: ProductRequest): Observable<Product> {
        return this.http.put<Product>(`${this.base}/${id}`, req);
    }

    delete(id: number): Observable<void> {
        return this.http.delete<void>(`${this.base}/${id}`);
    }
}
