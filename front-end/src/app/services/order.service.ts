import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Order, OrderItem, OrderStatus } from '../models/order.model';

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private apiUrl = 'http://localhost:8080/api/orders';

  constructor(private http: HttpClient) { }

  getOrders(page: number = 0, size: number = 10, status?: OrderStatus): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (status) {
      params = params.set('status', status);
    }

    return this.http.get<any>(this.apiUrl, { params });
  }

  getOrderById(id: number): Observable<Order> {
    return this.http.get<Order>(`${this.apiUrl}/${id}`);
  }

  createOrder(order: Partial<Order>): Observable<Order> {
    return this.http.post<Order>(this.apiUrl, order);
  }

  updateOrderStatus(id: number, status: OrderStatus): Observable<Order> {
    return this.http.patch<Order>(`${this.apiUrl}/${id}/status`, { status });
  }

  updateOrderItemTracking(orderItemId: number, trackingNumber: string): Observable<OrderItem> {
    return this.http.patch<OrderItem>(`${this.apiUrl}/items/${orderItemId}/tracking`, { trackingNumber });
  }

  getSupplierBuyList(date: string): Observable<any> {
    const params = new HttpParams().set('date', date);
    return this.http.get(`${this.apiUrl}/admin/supplier-buy-list`, { params });
  }

  getOrdersRequiringReview(): Observable<Order[]> {
    return this.http.get<Order[]>(`${this.apiUrl}/admin/requires-review`);
  }

  approveOrderReview(orderId: number, approved: boolean, reason?: string): Observable<Order> {
    return this.http.post<Order>(`${this.apiUrl}/${orderId}/review`, { approved, reason });
  }

  getFulfillmentDashboard(): Observable<any> {
    return this.http.get(`${this.apiUrl}/admin/fulfillment-dashboard`);
  }
}
