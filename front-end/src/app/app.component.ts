import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  template: `
    <div class="app-container">
      <app-navbar *ngIf="isAuthenticated"></app-navbar>
      <div class="main-content" [class.with-navbar]="isAuthenticated">
        <app-sidebar *ngIf="isAuthenticated"></app-sidebar>
        <div class="content-area">
          <router-outlet></router-outlet>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .app-container {
      display: flex;
      flex-direction: column;
      height: 100vh;
    }
    .main-content {
      display: flex;
      flex: 1;
      overflow: hidden;
    }
    .main-content.with-navbar {
      height: calc(100vh - 64px);
    }
    .content-area {
      flex: 1;
      padding: 20px;
      overflow-y: auto;
      background-color: #f5f5f5;
    }
  `]
})
export class AppComponent {
  isAuthenticated = true; // This should come from auth service
  title = 'Dropshipping Automation Platform';
}
