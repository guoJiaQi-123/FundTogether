ALTER TABLE `funding_ledger` ADD COLUMN `balance_after` decimal(10,2) DEFAULT NULL COMMENT '交易后余额' AFTER `amount`;
