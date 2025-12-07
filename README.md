# Dropshipping Automation Platform - Implementation Prompt

## Project Context
Building a dropshipping automation platform with Angular frontend and Spring Boot backend. Implement the complete order fulfillment and audit system in Spring Boot with AWS integration.

## Technology Stack
- **Backend**: Spring Boot 3.2+, Java 17
- **Database**: PostgreSQL (RDS)
- **Storage**: AWS S3 for documents/receipts
- **Queue**: AWS SQS for async processing
- **Email**: AWS SES for notifications
- **Scheduling**: Spring Scheduler + AWS Lambda
- **Security**: Spring Security, JWT

## Project Structure
```
project/
├── frontend/    # Angular application (completely standalone)
└── backend/     # Spring Boot application (completely standalone)
```

## Core Business Requirements Implementation

### 1. Order Status Tracking Workflow
Implement these statuses with timestamps:
- `PAYMENT_RECEIVED` (Stripe webhook confirms)
- `SUPPLIER_ORDER_PLACED` (Order sent to supplier)
- `SUPPLIER_CONFIRMED` (Supplier acknowledges)
- `SHIPPED` (Tracking number received)
- `DELIVERED` (Carrier confirms)
- `CANCELLED` / `REFUNDED`

### 2. Daily Supplier Buy List System
Create endpoint: `GET /api/admin/supplier-buy-list?date=YYYY-MM-DD`
- Group orders by supplier with summarized totals
- Include formatted customer shipping addresses
- Generate PDF/CSV export with purchase instructions
- Track purchased vs pending items

### 3. Payment-Supplier Reconciliation
Create `TransactionReconciliationService`:
- Match Stripe charge IDs with supplier purchase receipts
- Flag amount discrepancies
- Daily reconciliation report at 11 PM EST
- Store results in `reconciliation_audit` table

### 4. Complete Audit Trail System
Design `AuditTrailEntity` with:
- `originalPrice`, `sellingPrice`, `profitMargin`
- `priceChangeHistory` (JSON)
- `auditTimestamp`, `userAction`
- `beforeState` and `afterState` (JSON diffs)

### 5. Supplier Receipt Archive
Create `SupplierReceiptService`:
- Store confirmation emails in S3: `supplier-receipts/YYYY/MM/DD/supplier-name/`
- Index receipts with S3 URL reference
- OCR processing for PDF data extraction
- 7-year retention policy

### 6. Duplicate Order Prevention
Implement `DuplicateOrderCheckFilter`:
- Redis cache: `customerId + productId + supplierId`
- 24-hour duplicate detection window
- Admin dashboard alerts for duplicates
- Manual override with reason logging

### 7. Inventory Verification Workflow
Before purchase:
1. Check supplier API for real-time stock
2. Fallback to cached inventory (hourly updates)
3. Display "Only X left" for low stock
4. Auto-update product status when OOS
5. Implement circuit breaker for API failures

### 8. High-Value Order Review
- Config threshold: `order.review.threshold=500`
- Status: `REQUIRES_MANUAL_REVIEW`
- Slack/email alerts to admin
- 4-hour SLA for review
- Audit log of review decisions

### 9. Out-of-Stock Exception Handling
When OOS between order/fulfillment:
1. Immediate customer notification
2. Options: refund, substitute, or waitlist
3. Auto-apply 10% discount coupon
4. Negative supplier reliability score
5. Temporary pause imports from supplier

### 10. Address Validation System
Integrate with:
- USPS Address Validation API (US)
- SmartyStreets (international)
- Flag invalid addresses pre-order
- Customer notification to correct
- Store validation results

### 11. Customer Communication Archive
Create `OrderCommunicationEntity` storing:
- All emails sent/received
- Customer service transcripts
- Phone call summaries
- File attachments
- Sentiment analysis score (AWS Comprehend)

### 12. Daily Cut-Off Process
- Configurable: `order.cutoff.time=14:00 EST`
- Before cutoff: "Ships today"
- After cutoff: "Ships next business day"
- Clear checkout display
- Automated email reminders

### 13. Automatic Tracking Number Capture
Implement `TrackingNumberExtractorService`:
1. Monitor supplier emails via AWS SES
2. Regex extract tracking numbers
3. Validate with carrier APIs
4. Auto-update status to SHIPPED
5. Send customer notification

### 14. Profit Calculation Engine
Create `ProfitCalculatorService` with formula:
```
Net Profit = (Selling Price - Supplier Price) 
             - Stripe Fees (2.9% + $0.30)
             - AWS Costs (pro-rated)
             - Transaction Costs
             - Refund Reserve (2%)
             - Shipping Insurance
```
Store in `profit_analysis` table.

### 15. Three-Way Match System
Reconcile:
1. Customer Order (our system)
2. Supplier Purchase (confirmation)
3. Delivery Confirmation (carrier)
   Flag mismatches for manual review. Auto-close after 7 days.

### 16. Backup Supplier System
- Maintain `primary_supplier_id` and `backup_supplier_id`
- Auto-switch when primary: OOS or price >15% higher
- Track backup supplier performance separately
- Admin dashboard for supplier assignments

### 17. Price Change History
Create `PriceHistoryEntity` tracking:
- Old price, new price, change percentage
- Change reason (supplier, margin, etc.)
- Effective date/time
- User who authorized (or "system")
- Impact on existing carts

### 18. Automatic Status Updates
Subscribe to carrier webhooks for:
- Out for delivery
- Delivery exception
- Delivery confirmed
- Failed attempts
  Auto-update status and notify customer.

### 19. Suspicious Order Flagging
Rules engine flags for verification:
- Multiple orders to same address (different cards)
- Expedited shipping on low-value items
- International to high-fraud countries
- Billing/shipping address mismatch
- Orders >$1000 from new customers

### 20. Daily Financial Closing
Scheduled midnight job:
1. Reconcile Stripe transactions
2. Match with bank deposits (Plaid API)
3. Generate daily P&L statement
4. Update accounting system (QuickBooks API)
5. Archive snapshot to S3

### 21. Supplier Performance Scoring
Score (0-100) based on:
- On-time delivery: 40%
- Order accuracy: 30%
- Communication responsiveness: 20%
- Price competitiveness: 10%
  Auto-pause suppliers with score <70

### 22. Delayed Shipment Notification
When tracking shows delay:
1. Auto-detect (carrier ETA vs actual)
2. Send apology email with new ETA
3. Offer $5 credit or free shipping next order
4. Update customer dashboard expectation
5. Escalate to supplier if pattern

### 23. Centralized Dashboard
Admin endpoint: `GET /api/admin/fulfillment-dashboard`
Showing:
- Orders awaiting supplier purchase (by supplier)
- High-value orders needing review
- Delayed shipments requiring action
- Today's reconciliation status
- Supplier performance alerts

### 24. Automatic Refund Processing
When supplier cancels order:
1. Auto-refund customer via Stripe
2. Send cancellation apology email
3. Issue 15% discount coupon
4. Log supplier refund reason
5. Update inventory and product status

### 25. Partial Shipment Handling
Support:
- Split shipments from multiple suppliers
- Backordered items with separate tracking
- Partial refunds for unavailable items
- Consolidated customer notifications
- Proper inventory deduction per shipment

### 26. Original Content Storage
Before modifying products:
1. Store original title/description/images in S3
2. Version control for changes
3. Diff comparison for audits
4. Maintain supplier attribution
5. Legal compliance archive

### 27. Regular Data Backup
Implement:
- Daily incremental backups to S3
- Weekly full database dumps
- Monthly archival to AWS Glacier
- Backup verification (test restore)
- Retention: 30 days/1 year/7 years

### 28. Processor Audit Logging
Every order action logs:
- User ID (or "system")
- Action performed
- Timestamp
- IP address (manual actions)
- Before/after state (JSON diff)
  Access via: `GET /api/audit/order/{orderId}`

### 29. Tax and Sales Reporting
Auto-generate:
- Monthly sales tax by jurisdiction
- Quarterly 1099-K reports
- Annual profit/loss statement
- Inventory valuation report
- Export to CSV/Excel one-click

### 30. High-Risk Order Verification
For flagged orders:
- Additional identity verification required
- Manual approval workflow
- 24-hour processing delay for review
- Possible phone verification
- Document verification decision

### 31. Post-Delivery Survey System
7 days after delivery:
1. Send NPS survey (1-10)
2. Request product review
3. Ask about delivery experience
4. Incentive: 5% off next order
5. Aggregate feedback for supplier scoring

### 32. Supplier Price Change Handling
When supplier price changes between order/fulfillment:
- Price decrease: keep margin, notify customer
- Price increase <5%: absorb cost, note for review
- Price increase >5%: contact customer for approval
- Update supplier agreement if pattern

### 33. Shipping Guarantee Tracking
Track supplier promises:
- "2-day shipping" guarantees
- Free shipping thresholds
- International delivery windows
- Auto-file claims when missed
- Track claim status and reimbursements

### 34. Standardized Email Templates
Templates for:
- Order confirmation
- Shipping notification
- Delivery confirmation
- Delay notification
- Backorder update
- Refund processed
- Review request
  All with variables and brand styling.

### 35. Returns Management System
Endpoints:
- `POST /api/returns/initiate` - Start return
- `GET /api/returns/{id}/label` - Generate label
- `POST /api/returns/{id}/receive` - Mark returned
- `POST /api/returns/{id}/refund` - Process refund
- `GET /api/returns/analytics` - Return rates

### 36. Weekly Fulfillment Optimization
Every Monday 9 AM:
1. Analyze last week's metrics
2. Identify bottlenecks
3. Suggest process improvements
4. Auto-adjust cut-off times if needed
5. Email report to operations team

### 37. Supplier Communication Archive
Store all communications:
- API request/response logs
- Email correspondence
- Chat transcripts
- Phone call summaries
- Supplier portal messages
  Searchable by supplier/order/date.

### 38. Delivery Date Promise Engine
Calculate and display:
- Supplier processing time (by supplier)
- Shipping time (by carrier/service)
- Buffer days (supplier reliability)
- Business day adjustments
- Holiday calendar exclusions
  Show: "Estimated delivery: Jan 15-18"

### 39. Quality Check Process
For orders >$200:
1. Verify item matches order
2. Check for visible damage
3. Take photo for records
4. Update quality check status
5. Flag issues to supplier with evidence

### 40. Disaster Recovery Plan
Implement:
- Hot standby in different AWS region
- Database replication with failover
- Order processing pause/resume
- Emergency contact escalation
- Post-mortem analysis framework
- Quarterly DR drills

## Payment Processing System (41-52)

### 41. Stripe Integration Layer
- Implement `StripeWebhookController`
- Create `PaymentProcessingService` with: `processPayment()`, `createRefund()`, `handleDispute()`, `updatePaymentMethod()`
- Store Stripe payment intent IDs
- Idempotency keys for duplicate prevention

### 42. Payment Gateway Abstraction
Design `PaymentGateway` interface supporting:
1. Stripe (primary)
2. PayPal (backup)
3. Future gateways
   Factory pattern for selection by customer/country.

### 43. Payment Failure Recovery
Automated retry logic:
- 1st failure: Immediate retry
- 2nd failure: Wait 1 hour, retry
- 3rd failure: Notify customer, offer alternative
- Card update flow for expired cards
- Store failure reasons for analytics

### 44. Refund Management System
Complete refund workflow:
- Full/partial refund processing
- Reason categorization
- Timing: Instant vs 5-10 business days
- Status tracking
- Accounting impact
- Customer notification with ETA

### 45. Chargeback/Dispute Handling
When chargeback received:
1. Auto-collect evidence
2. Submit to Stripe within deadline
3. Track dispute status
4. If lost: refund, update fraud scoring
5. If won: restore funds, update status
   Dashboard for active disputes.

### 46. Payment Method Vault
Secure storage:
- Stripe Customer ID linkage
- Default payment method tracking
- Card expiration monitoring (30-day warnings)
- Payment method analytics
- PCI-compliant storage (no raw card data)

### 47. Daily Payment Reconciliation
Scheduled job:
1. Fetch Stripe settlement report
2. Match with order payments
3. Flag discrepancies
4. Generate reconciliation report
5. Update accounting system
6. Alert for >1% variances

### 48. Tax Calculation Engine
Integrate with TaxJar/Avalara or implement:
- Real-time tax at checkout
- Sales tax by jurisdiction
- Tax-exempt handling
- Digital goods tax rules
- International VAT/GST
- Store business tax certificates

### 49. Multi-Currency Support
For international orders:
- Display prices in local currency
- Real-time FX rate updates
- Payment in customer's currency
- Show conversion fees
- Settlement in USD
- Track FX gain/loss

### 50. Fraud Detection System
Integrate Stripe Radar/MaxMind or implement:
- Velocity checks
- IP geolocation mismatch
- High-risk country detection
- BIN validation
- Device fingerprinting
- 3D Secure for high-risk
- Manual review queue

### 51. Payment Analytics Dashboard
Monitor:
- Payment success/failure rates
- Average transaction value
- Most used payment methods
- Chargeback ratio
- Fraud detection effectiveness
- Processing costs
- Refund rate by product

### 52. PCI Compliance Measures
- Never log full card numbers
- Mask in UI (**** **** **** 1234)
- Secure transmission (TLS 1.2+)
- Regular security audits
- Penetration testing
- Secure data deletion
- Access controls

## Deliverables
1. Complete Spring Boot application with endpoints
2. PostgreSQL schema migration scripts
3. AWS CloudFormation templates
4. API documentation (OpenAPI 3.0)
5. Postman collection
6. Docker configuration
7. Load testing scripts
8. Monitoring dashboards (CloudWatch)

## Key Metrics
- Order fulfillment rate (%)
- Average fulfillment time (hours)
- Supplier reliability score
- Customer satisfaction (NPS)
- Profit margin accuracy
- System uptime

**Note**: Keep frontend/backend completely independent. Use environment variables, never hardcode secrets. Implement proper logging and unit tests. Start with database schema, then services in dependency order.
```