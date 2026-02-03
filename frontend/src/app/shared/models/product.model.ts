export type ProductStatus = 'ACTIVE' | 'DISCONTINUED';

export interface Product {
    id: number;
    name: string;
    price: number;
    stock: number;
    status: ProductStatus;
}

export interface ProductRequest {
    name: string;
    price: number;
    stock: number;
    status: ProductStatus | string;
}

/**
 * PR-1 Feature: Server-side pagination response wrapper.
 * Returned by GET /api/products/paged endpoint.
 */
export interface PagedResponse<T> {
    items: T[];
    totalElements: number;
    page: number;
    size: number;
}

