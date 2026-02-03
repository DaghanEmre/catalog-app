import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Product, ProductRequest } from '../../shared/models/product.model';

@Injectable({ providedIn: 'root' })
export class ProductsService {
    private readonly base = '/api/products';

    constructor(private http: HttpClient) { }

    list(): Observable<Product[]> {
        return this.http.get<Product[]>(this.base);
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
