-- ============================================
-- 数据库重构：创建新表结构
-- ============================================

-- 1. 客户维度表
CREATE TABLE IF NOT EXISTS dim_customer (
    id BIGSERIAL PRIMARY KEY,
    customer_code VARCHAR(50) UNIQUE NOT NULL,
    customer_name VARCHAR(100),
    customer_short_name VARCHAR(50),
    customer_category_code VARCHAR(20),
    customer_category_desc VARCHAR(50),
    sales_region_code VARCHAR(50),
    sales_region_desc VARCHAR(50),
    payment_terms VARCHAR(50),
    payment_terms_desc VARCHAR(100),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. 产品维度表
CREATE TABLE IF NOT EXISTS dim_product (
    id BIGSERIAL PRIMARY KEY,
    product_code VARCHAR(50) UNIQUE NOT NULL,
    product_name VARCHAR(100),
    product_full_name VARCHAR(200),
    spec_model VARCHAR(100),
    product_line_code VARCHAR(50),
    product_line_desc VARCHAR(50),
    product_group_code VARCHAR(20),
    product_group_desc VARCHAR(50),
    unit VARCHAR(20),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. 销售员维度表
CREATE TABLE IF NOT EXISTS dim_salesperson (
    id BIGSERIAL PRIMARY KEY,
    salesperson_code VARCHAR(50) UNIQUE NOT NULL,
    salesperson_name VARCHAR(50),
    sales_org_code VARCHAR(50),
    sales_org_desc VARCHAR(50),
    sales_region_code VARCHAR(50),
    sales_region_desc VARCHAR(50),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. 订单主表（订单级别）
CREATE TABLE IF NOT EXISTS sales_order_master (
    id BIGSERIAL PRIMARY KEY,
    order_no VARCHAR(50) UNIQUE NOT NULL,
    order_type_code VARCHAR(20),
    order_type_desc VARCHAR(50),
    order_status INTEGER DEFAULT 0,
    order_create_date DATE,
    customer_code VARCHAR(50),
    customer_po_no VARCHAR(50),
    customer_po_date DATE,
    contract_no VARCHAR(50),
    currency VARCHAR(10),
    exchange_rate NUMERIC(38,6),
    reference_currency VARCHAR(10),
    reference_exchange_rate NUMERIC(38,6),
    payment_terms VARCHAR(50),
    payment_terms_desc VARCHAR(100),
    sales_org_code VARCHAR(50),
    sales_org_desc VARCHAR(50),
    sales_region_code VARCHAR(50),
    sales_region_desc VARCHAR(50),
    distribution_channel_code VARCHAR(20),
    distribution_channel_desc VARCHAR(50),
    market_segment_code VARCHAR(50),
    market_segment_desc VARCHAR(100),
    salesperson_code VARCHAR(50),
    salesperson_name VARCHAR(50),
    sales_assistant VARCHAR(50),
    sales_assistant_name VARCHAR(50),
    fae VARCHAR(50),
    fae_name VARCHAR(50),
    estimated_delivery_date DATE,
    supply_chain_delivery_date DATE,
    header_remark VARCHAR(500),
    reject_reason_code VARCHAR(10),
    reject_reason_desc VARCHAR(100),
    creator VARCHAR(50),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5. 订单明细表（行级别）
CREATE TABLE IF NOT EXISTS sales_order_line (
    id BIGSERIAL PRIMARY KEY,
    order_no VARCHAR(50) NOT NULL,
    order_line_no VARCHAR(20) NOT NULL,
    product_code VARCHAR(50),
    product_name VARCHAR(100),
    product_full_name VARCHAR(200),
    spec_model VARCHAR(100),
    unit VARCHAR(20),
    order_quantity NUMERIC(38,2),
    unit_price NUMERIC(38,4),
    unit_price_no_tax NUMERIC(38,4),
    order_amount_no_tax NUMERIC(38,2),
    order_amount_tax NUMERIC(38,2),
    order_tax NUMERIC(38,2),
    shipped_quantity NUMERIC(38,2),
    shipped_amount_no_tax NUMERIC(38,2),
    shipped_amount_tax NUMERIC(38,2),
    unshipped_quantity NUMERIC(38,2),
    unshipped_amount_no_tax NUMERIC(38,2),
    unshipped_amount_tax NUMERIC(38,2),
    invoiced_quantity NUMERIC(38,2),
    invoiced_amount_no_tax NUMERIC(38,2),
    invoiced_amount_tax NUMERIC(38,2),
    uninvoiced_quantity NUMERIC(38,2),
    uninvoiced_amount_no_tax NUMERIC(38,2),
    uninvoiced_amount_tax NUMERIC(38,2),
    original_quantity NUMERIC(38,2),
    original_order_no VARCHAR(50),
    reference_doc VARCHAR(50),
    reference_item VARCHAR(50),
    over_delivery_quantity NUMERIC(38,2),
    over_delivery_amount NUMERIC(38,2),
    is_over_delivery VARCHAR(10),
    line_remark VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(order_no, order_line_no)
);

-- 6. 月度汇总表
CREATE TABLE IF NOT EXISTS sales_monthly_summary (
    id BIGSERIAL PRIMARY KEY,
    year INTEGER NOT NULL,
    month INTEGER NOT NULL,
    sales_amount NUMERIC(38,2) DEFAULT 0,
    order_count INTEGER DEFAULT 0,
    order_line_count INTEGER DEFAULT 0,
    shipped_amount NUMERIC(38,2) DEFAULT 0,
    invoiced_amount NUMERIC(38,2) DEFAULT 0,
    customer_count INTEGER DEFAULT 0,
    product_count INTEGER DEFAULT 0,
    avg_order_amount NUMERIC(38,2) DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(year, month)
);

-- 7. 客户月度汇总表
CREATE TABLE IF NOT EXISTS customer_monthly_summary (
    id BIGSERIAL PRIMARY KEY,
    year INTEGER NOT NULL,
    month INTEGER NOT NULL,
    customer_code VARCHAR(50) NOT NULL,
    customer_name VARCHAR(100),
    sales_amount NUMERIC(38,2) DEFAULT 0,
    order_count INTEGER DEFAULT 0,
    product_count INTEGER DEFAULT 0,
    region VARCHAR(50),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(year, month, customer_code)
);

-- 8. 产品月度汇总表
CREATE TABLE IF NOT EXISTS product_monthly_summary (
    id BIGSERIAL PRIMARY KEY,
    year INTEGER NOT NULL,
    month INTEGER NOT NULL,
    product_code VARCHAR(50) NOT NULL,
    product_name VARCHAR(100),
    product_line VARCHAR(50),
    sales_quantity NUMERIC(38,2) DEFAULT 0,
    sales_amount NUMERIC(38,2) DEFAULT 0,
    order_count INTEGER DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(year, month, product_code)
);

-- 9. 销售员月度汇总表
CREATE TABLE IF NOT EXISTS salesperson_monthly_summary (
    id BIGSERIAL PRIMARY KEY,
    year INTEGER NOT NULL,
    month INTEGER NOT NULL,
    salesperson_code VARCHAR(50) NOT NULL,
    salesperson_name VARCHAR(50),
    sales_amount NUMERIC(38,2) DEFAULT 0,
    shipped_amount NUMERIC(38,2) DEFAULT 0,
    order_count INTEGER DEFAULT 0,
    target_amount NUMERIC(38,2) DEFAULT 0,
    completion_rate NUMERIC(5,2) DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(year, month, salesperson_code)
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_order_line_order_no ON sales_order_line(order_no);
CREATE INDEX IF NOT EXISTS idx_order_line_product ON sales_order_line(product_code);
CREATE INDEX IF NOT EXISTS idx_order_master_date ON sales_order_master(order_create_date);
CREATE INDEX IF NOT EXISTS idx_order_master_status ON sales_order_master(order_status);
CREATE INDEX IF NOT EXISTS idx_order_master_customer ON sales_order_master(customer_code);
CREATE INDEX IF NOT EXISTS idx_order_master_salesperson ON sales_order_master(salesperson_code);
CREATE INDEX IF NOT EXISTS idx_monthly_summary_ym ON sales_monthly_summary(year, month);
CREATE INDEX IF NOT EXISTS idx_customer_monthly ON customer_monthly_summary(year, month, customer_code);
CREATE INDEX IF NOT EXISTS idx_product_monthly ON product_monthly_summary(year, month, product_code);
CREATE INDEX IF NOT EXISTS idx_salesperson_monthly ON salesperson_monthly_summary(year, month, salesperson_code);
