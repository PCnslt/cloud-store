export interface Order {
  id: number;
  orderNumber: string;
  customerId: number;
  status: OrderStatus;
  totalAmount: number;
  taxAmount: number;
  shippingAmount: number;
  netAmount: number;
  requiresReview: boolean;
  reviewReason?: string;
  shippingAddress: any;
  billingAddress: any;
  estimatedDeliveryStart?: string;
  estimatedDeliveryEnd?: string;
  actualDeliveryDate?: string;
  cutOffTime?: string;
  createdAt: string;
  updatedAt: string;
  customer?: Customer;
  orderItems?: OrderItem[];
}

export interface OrderItem {
  id: number;
  orderId: number;
  productId: number;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
  supplierId?: number;
  supplierConfirmationId?: string;
  trackingNumber?: string;
  shipmentStatus: ShipmentStatus;
  createdAt: string;
  updatedAt: string;
  product?: Product;
  supplier?: Supplier;
}

export enum OrderStatus {
  PAYMENT_RECEIVED = 'PAYMENT_RECEIVED',
  SUPPLIER_ORDER_PLACED = 'SUPPLIER_ORDER_PLACED',
  SUPPLIER_CONFIRMED = 'SUPPLIER_CONFIRMED',
  SHIPPED = 'SHIPPED',
  DELIVERED = 'DELIVERED',
  CANCELLED = 'CANCELLED',
  REFUNDED = 'REFUNDED',
  REQUIRES_MANUAL_REVIEW = 'REQUIRES_MANUAL_REVIEW'
}

export enum ShipmentStatus {
  PENDING = 'PENDING',
  PROCESSING = 'PROCESSING',
  SHIPPED = 'SHIPPED',
  DELIVERED = 'DELIVERED',
  DELAYED = 'DELAYED',
  RETURNED = 'RETURNED'
}

export interface Customer {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  phone?: string;
  stripeCustomerId?: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface Product {
  id: number;
  name: string;
  description?: string;
  sku: string;
  supplierId: number;
  backupSupplierId?: number;
  supplierPrice: number;
  sellingPrice: number;
  currentStock: number;
  minStockLevel: number;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
  supplier?: Supplier;
  backupSupplier?: Supplier;
}

export interface Supplier {
  id: number;
  name: string;
  email: string;
  phone?: string;
  website?: string;
  apiEndpoint?: string;
  apiKey?: string;
  performanceScore: number;
  isActive: boolean;
  isBackup: boolean;
  createdAt: string;
  updatedAt: string;
}
