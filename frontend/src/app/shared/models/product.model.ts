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
