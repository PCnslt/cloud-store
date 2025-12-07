# Dropshipping Frontend

Angular frontend for the dropshipping automation platform.

## Features
- Dashboard with key metrics
- Order management with status tracking
- Product catalog management
- Supplier management
- Customer management
- Reporting and analytics
- Responsive design with Angular Material

## Technology Stack
- Angular 17
- TypeScript
- Angular Material
- RxJS
- Chart.js for data visualization
- Moment.js for date handling

## Setup Instructions

1. **Prerequisites**
   - Node.js 18+ 
   - npm 9+ or yarn

2. **Install Dependencies**
   ```bash
   npm install
   ```

3. **Development Server**
   ```bash
   npm start
   ```
   Navigate to `http://localhost:4200/`

4. **Build for Production**
   ```bash
   npm run build
   ```
   The build artifacts will be stored in the `dist/` directory.

5. **Running Tests**
   ```bash
   npm test
   ```

## Project Structure
```
src/
├── app/
│   ├── components/     # Reusable UI components
│   ├── pages/         # Page components
│   ├── services/      # API services
│   ├── models/        # TypeScript interfaces
│   ├── guards/        # Route guards
│   ├── interceptors/  # HTTP interceptors
│   └── shared/        # Shared modules and utilities
├── assets/            # Static assets
└── environments/      # Environment configurations
```

## Key Pages
- **Dashboard**: Overview of key metrics and recent activity
- **Orders**: Order management with status tracking
- **Products**: Product catalog management
- **Suppliers**: Supplier management and performance
- **Customers**: Customer management
- **Reports**: Analytics and reporting
- **Login**: Authentication page

## API Integration
The frontend communicates with the backend REST API at `http://localhost:8080/api`. Update the API URL in services if needed.

## Development Notes
- Uses Angular Material for consistent UI components
- Implements lazy loading for better performance
- Includes HTTP interceptors for authentication
- Responsive design for mobile and desktop
- Uses reactive forms with validation

## Environment Configuration
Create `environment.ts` and `environment.prod.ts` files in `src/environments/` for different configurations.

Example `environment.ts`:
```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
