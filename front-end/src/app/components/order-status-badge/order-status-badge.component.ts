import { Component, Input } from '@angular/core';
import { OrderStatus } from '../../models/order.model';

@Component({
  selector: 'app-order-status-badge',
  template: `
    <span class="status-badge" [ngClass]="getStatusClass()">
      {{ getStatusText() }}
    </span>
  `,
  styles: [`
    .status-badge {
      padding: 4px 8px;
      border-radius: 12px;
      font-size: 12px;
      font-weight: 500;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }
    .status-payment-received {
      background-color: #e3f2fd;
      color: #1976d2;
    }
    .status-supplier-order-placed {
      background-color: #fff3e0;
      color: #f57c00;
    }
    .status-supplier-confirmed {
      background-color: #e8f5e9;
      color: #388e3c;
    }
    .status-shipped {
      background-color: #e1f5fe;
      color: #0288d1;
    }
    .status-delivered {
      background-color: #f1f8e9;
      color: #689f38;
    }
    .status-cancelled {
      background-color: #ffebee;
      color: #d32f2f;
    }
    .status-refunded {
      background-color: #f3e5f5;
      color: #7b1fa2;
    }
    .status-requires-manual-review {
      background-color: #fff8e1;
      color: #ff8f00;
    }
  `]
})
export class OrderStatusBadgeComponent {
  @Input() status!: OrderStatus;

  getStatusClass(): string {
    const statusMap: { [key in OrderStatus]: string } = {
      [OrderStatus.PAYMENT_RECEIVED]: 'status-payment-received',
      [OrderStatus.SUPPLIER_ORDER_PLACED]: 'status-supplier-order-placed',
      [OrderStatus.SUPPLIER_CONFIRMED]: 'status-supplier-confirmed',
      [OrderStatus.SHIPPED]: 'status-shipped',
      [OrderStatus.DELIVERED]: 'status-delivered',
      [OrderStatus.CANCELLED]: 'status-cancelled',
      [OrderStatus.REFUNDED]: 'status-refunded',
      [OrderStatus.REQUIRES_MANUAL_REVIEW]: 'status-requires-manual-review'
    };
    return statusMap[this.status] || 'status-payment-received';
  }

  getStatusText(): string {
    const statusMap: { [key in OrderStatus]: string } = {
      [OrderStatus.PAYMENT_RECEIVED]: 'Payment Received',
      [OrderStatus.SUPPLIER_ORDER_PLACED]: 'Supplier Order Placed',
      [OrderStatus.SUPPLIER_CONFIRMED]: 'Supplier Confirmed',
      [OrderStatus.SHIPPED]: 'Shipped',
      [OrderStatus.DELIVERED]: 'Delivered',
      [OrderStatus.CANCELLED]: 'Cancelled',
      [OrderStatus.REFUNDED]: 'Refunded',
      [OrderStatus.REQUIRES_MANUAL_REVIEW]: 'Requires Review'
    };
    return statusMap[this.status] || this.status;
  }
}
