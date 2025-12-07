-- Create enum types
CREATE TYPE order_status AS ENUM (
    'PAYMENT_RECEIVED',
    'SUPPLIER_ORDER_PLACED',
    'SUPPLIER_CONFIRMED',
    'SHIPPED',
    'DELIVERED',
    'CANCELLED',
    'REFUNDED',
    'REQUIRES_MANUAL_REVIEW'
);

CREATE TYPE payment_status AS ENUM (
    'PENDING',
    'COMPLETED',
    'FAILED',
    'REFUNDED',
    'DISPUTED'
);

CREATE TYPE shipment_status AS ENUM (
    'PENDING',
    'PROCESSING',
    'SHIPPED',
    'DELIVERED',
    'DELAYED',
    'RETURNED'
);

-- Customers table
CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    stripe_customer_id VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE
);

-- Suppliers table
CREATE TABLE suppliers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    website VARCHAR(255),
    api_endpoint VARCHAR(500),
    api_key VARCHAR(500),
    performance_score DECIMAL(5,2) DEFAULT 100.00,
    is_active BOOLEAN DEFAULT TRUE,
    is_backup BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Products table
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    sku VARCHAR(100) UNIQUE NOT NULL,
    supplier_id BIGINT REFERENCES suppliers(id),
    backup_supplier_id BIGINT REFERENCES suppliers(id),
    supplier_price DECIMAL(10,2) NOT NULL,
    selling_price DECIMAL(10,2) NOT NULL,
    current_stock INTEGER DEFAULT 0,
    min_stock_level INTEGER DEFAULT 10,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Orders table
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(50) UNIQUE NOT NULL,
    customer_id BIGINT REFERENCES customers(id) NOT NULL,
    status order_status NOT NULL DEFAULT 'PAYMENT_RECEIVED',
    total_amount DECIMAL(10,2) NOT NULL,
    tax_amount DECIMAL(10,2) DEFAULT 0,
    shipping_amount DECIMAL(10,2) DEFAULT 0,
    net_amount DECIMAL(10,2) NOT NULL,
    requires_review BOOLEAN DEFAULT FALSE,
    review_reason TEXT,
    shipping_address JSONB NOT NULL,
    billing_address JSONB NOT NULL,
    estimated_delivery_start DATE,
    estimated_delivery_end DATE,
    actual_delivery_date DATE,
    cut_off_time TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Order items table
CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT REFERENCES orders(id) NOT NULL,
    product_id BIGINT REFERENCES products(id) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    supplier_id BIGINT REFERENCES suppliers(id),
    supplier_confirmation_id VARCHAR(255),
    tracking_number VARCHAR(100),
    shipment_status shipment_status DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Payments table
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT REFERENCES orders(id) NOT NULL,
    stripe_payment_intent_id VARCHAR(255) UNIQUE,
    stripe_charge_id VARCHAR(255),
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    status payment_status NOT NULL DEFAULT 'PENDING',
    fee_amount DECIMAL(10,2) DEFAULT 0,
    net_amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(50),
    payment_gateway VARCHAR(50) DEFAULT 'STRIPE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Audit trail table
CREATE TABLE audit_trails (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    user_id BIGINT,
    user_action VARCHAR(100) NOT NULL,
    original_price DECIMAL(10,2),
    selling_price DECIMAL(10,2),
    profit_margin DECIMAL(5,2),
    price_change_history JSONB,
    before_state JSONB,
    after_state JSONB,
    audit_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45)
);

-- Supplier receipts table
CREATE TABLE supplier_receipts (
    id BIGSERIAL PRIMARY KEY,
    supplier_id BIGINT REFERENCES suppliers(id) NOT NULL,
    order_item_id BIGINT REFERENCES order_items(id),
    receipt_number VARCHAR(100) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    s3_url VARCHAR(500) NOT NULL,
    receipt_date DATE NOT NULL,
    ocr_data JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Order communications table
CREATE TABLE order_communications (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT REFERENCES orders(id) NOT NULL,
    communication_type VARCHAR(50) NOT NULL,
    subject VARCHAR(255),
    content TEXT NOT NULL,
    sender_email VARCHAR(255),
    recipient_email VARCHAR(255),
    sentiment_score DECIMAL(5,2),
    attachments JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Price history table
CREATE TABLE price_history (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT REFERENCES products(id) NOT NULL,
    old_price DECIMAL(10,2) NOT NULL,
    new_price DECIMAL(10,2) NOT NULL,
    change_percentage DECIMAL(5,2) NOT NULL,
    change_reason VARCHAR(100) NOT NULL,
    effective_from TIMESTAMP NOT NULL,
    changed_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Profit analysis table
CREATE TABLE profit_analysis (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT REFERENCES orders(id) NOT NULL,
    selling_price DECIMAL(10,2) NOT NULL,
    supplier_price DECIMAL(10,2) NOT NULL,
    stripe_fee DECIMAL(10,2) NOT NULL,
    aws_cost DECIMAL(10,2) DEFAULT 0,
    transaction_cost DECIMAL(10,2) DEFAULT 0,
    refund_reserve DECIMAL(10,2) DEFAULT 0,
    shipping_insurance DECIMAL(10,2) DEFAULT 0,
    net_profit DECIMAL(10,2) NOT NULL,
    profit_margin DECIMAL(5,2) NOT NULL,
    calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Reconciliation audit table
CREATE TABLE reconciliation_audit (
    id BIGSERIAL PRIMARY KEY,
    stripe_charge_id VARCHAR(255) NOT NULL,
    supplier_receipt_id BIGINT REFERENCES supplier_receipts(id),
    customer_amount DECIMAL(10,2) NOT NULL,
    supplier_amount DECIMAL(10,2),
    discrepancy_amount DECIMAL(10,2),
    discrepancy_reason TEXT,
    reconciled_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Supplier performance table
CREATE TABLE supplier_performance (
    id BIGSERIAL PRIMARY KEY,
    supplier_id BIGINT REFERENCES suppliers(id) NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    total_orders INTEGER DEFAULT 0,
    on_time_delivery_rate DECIMAL(5,2) DEFAULT 0,
    order_accuracy_rate DECIMAL(5,2) DEFAULT 0,
    communication_score DECIMAL(5,2) DEFAULT 0,
    price_competitiveness DECIMAL(5,2) DEFAULT 0,
    overall_score DECIMAL(5,2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Returns table
CREATE TABLE returns (
    id BIGSERIAL PRIMARY KEY,
    order_item_id BIGINT REFERENCES order_items(id) NOT NULL,
    return_reason TEXT NOT NULL,
    return_status VARCHAR(50) DEFAULT 'REQUESTED',
    refund_amount DECIMAL(10,2),
    refund_status VARCHAR(50),
    tracking_number VARCHAR(100),
    received_at TIMESTAMP,
    processed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for performance
CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_supplier_id ON order_items(supplier_id);
CREATE INDEX idx_payments_order_id ON payments(order_id);
CREATE INDEX idx_audit_trails_entity ON audit_trails(entity_type, entity_id);
CREATE INDEX idx_supplier_receipts_supplier_id ON supplier_receipts(supplier_id);
CREATE INDEX idx_price_history_product_id ON price_history(product_id);
CREATE INDEX idx_profit_analysis_order_id ON profit_analysis(order_id);
