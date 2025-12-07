import { Component, OnInit } from '@angular/core';
import { OrderService } from '../../services/order.service';
import { Order, OrderStatus } from '../../models/order.model';

@Component({
  selector: 'app-dashboard',
  template: `
    <div class="dashboard-container">
      <h1>Dashboard</h1>
      
      <div class="stats-grid">
        <mat-card class="stat-card">
          <mat-card-header>
            <mat-card-title>Total Orders</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="stat-value">{{ stats.totalOrders || 0 }}</div>
          </mat-card-content>
        </mat-card>

        <mat-card class="stat-card">
          <mat-card-header>
            <mat-card-title>Pending Fulfillment</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="stat-value">{{ stats.pendingFulfillment || 0 }}</div>
          </mat-card-content>
        </mat-card>

        <mat-card class="stat-card">
          <mat-card-header>
            <mat-card-title>Require Review</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="stat-value">{{ stats.requiresReview || 0 }}</div>
          </mat-card-content>
        </mat-card>

        <mat-card class="stat-card">
          <mat-card-header>
            <mat-card-title>Today's Revenue</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="stat-value">${{ (stats.todayRevenue || 0).toFixed(2) }}</div>
          </mat-card-content>
        </mat-card>
      </div>

      <div class="dashboard-sections">
        <mat-card class="section-card">
          <mat-card-header>
            <mat-card-title>Recent Orders</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <table mat-table [dataSource]="recentOrders" class="mat-elevation-z0">
              <ng-container matColumnDef="orderNumber">
                <th mat-header-cell *matHeaderCellDef>Order #</th>
                <td mat-cell *matCellDef="let order">{{ order.orderNumber }}</td>
              </ng-container>

              <ng-container matColumnDef="customer">
                <th mat-header-cell *matHeaderCellDef>Customer</th>
                <td mat-cell *matCellDef="let order">
                  {{ order.customer?.firstName }} {{ order.customer?.lastName }}
                </td>
              </ng-container>

              <ng-container matColumnDef="status">
                <th mat-header-cell *matHeaderCellDef>Status</th>
                <td mat-cell *matCellDef="let order">
                  <app-order-status-badge [status]="order.status"></app-order-status-badge>
                </td>
              </ng-container>

              <ng-container matColumnDef="totalAmount">
                <th mat-header-cell *matHeaderCellDef>Amount</th>
                <td mat-cell *matCellDef="let order">${{ order.totalAmount.toFixed(2) }}</td>
              </ng-container>

              <ng-container matColumnDef="createdAt">
                <th mat-header-cell *matHeaderCellDef>Date</th>
                <td mat-cell *matCellDef="let order">
                  {{ order.createdAt | date:'short' }}
                </td>
              </ng-container>

              <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
              <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
            </table>

            <div *ngIf="recentOrders.length === 0" class="no-data">
              No recent orders
            </div>
          </mat-card-content>
        </mat-card>

        <mat-card class="section-card">
          <mat-card-header>
            <mat-card-title>Supplier Buy List (Today)</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div *ngIf="supplierBuyList.length === 0" class="no-data">
              No supplier purchases needed today
            </div>
            <div *ngFor="let supplier of supplierBuyList" class="supplier-item">
              <h4>{{ supplier.name }}</h4>
              <p>{{ supplier.orderCount }} orders • ${{ supplier.totalAmount.toFixed(2) }}</p>
            </div>
          </mat-card-content>
        </mat-card>
      </div>
    </div>
  `,
  styles: [`
    .dashboard-container {
      padding: 20px;
    }
    .stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
      gap: 20px;
      margin-bottom: 30px;
    }
    .stat-card {
      text-align: center;
    }
    .stat-value {
      font-size: 2.5rem;
      font-weight: bold;
      color: #3f51b5;
    }
    .dashboard-sections {
      display: grid;
      grid-template-columns: 2fr 1fr;
      gap: 20px;
    }
    .section-card {
      height: 400px;
      overflow: auto;
    }
    table {
      width: 100%;
    }
    .no-data {
      text-align: center;
      padding: 40px;
      color: #999;
    }
    .supplier-item {
      padding: 10px;
      border-bottom: 1px solid #eee;
    }
    .supplier-item:last-child {
      border-bottom: none;
    }
    @media (max-width: 768px) {
      .dashboard-sections {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class DashboardComponent implements OnInit {
  stats = {
    totalOrders: 0,
    pendingFulfillment: 0,
    requiresReview: 0,
    todayRevenue: 0
  };

  recentOrders: Order[] = [];
  supplierBuyList: any[] = [];
  displayedColumns = ['orderNumber', 'customer', 'status', 'totalAmount', 'createdAt'];

  constructor(private orderService: OrderService) {}

  ngOnInit(): void {
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    // Load recent orders
    this.orderService.getOrders(0, 5).subscribe(response => {
      this.recentOrders = response.content || [];
    });

    // Load supplier buy list for today
    const today = new Date().toISOString().split('T')[0];
    this.orderService.getSupplierBuyList(today).subscribe(data => {
      this.supplierBuyList = data || [];
    });

    // Load dashboard stats (mock data for now)
    this.stats = {
      totalOrders: 125,
      pendingFulfillment: 23,
      requiresReview: 5,
      todayRevenue: 3421.50
    };
  }
}
