USE fund_together;
ALTER TABLE support_order ADD COLUMN delivery_status TINYINT NOT NULL DEFAULT 0 COMMENT '发货状态：0-未发货，1-已发货';
ALTER TABLE support_order ADD COLUMN delivery_time DATETIME NULL COMMENT '发货时间';
ALTER TABLE support_order ADD COLUMN express_no VARCHAR(64) NULL COMMENT '物流单号';
