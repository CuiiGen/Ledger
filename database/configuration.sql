-- root
-- sH6AkexU93exhBB
-- 创建账户
DROP TABLE `accounts` IF EXISTS;

CREATE TABLE `accounts` (
    `name` VARCHAR(32) NOT NULL COMMENT '账户名',
    `createtime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `balance` DOUBLE DEFAULT 0 COMMENT '余额',
    PRIMARY KEY (`name`)
);

-- 标签表
DROP TABLE `labels` IF EXISTS;

CREATE TABLE `labels` (
    `label` VARCHAR(32) NOT NULL COMMENT '标签',
    `createtime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`label`)
);

INSERT INTO
    `labels`
VALUES
    ('存入', DEFAULT);

INSERT INTO
    `labels`
VALUES
    ('退款', DEFAULT);

-- 账本
DROP TABLE `ledger` IF EXISTS;

CREATE TABLE `ledger` (
    `createtime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记账时间',
    `name` VARCHAR(32) NOT NULL COMMENT '账户名',
    `type` ENUM('1', '-1') NOT NULL COMMENT '收入或支出',
    `amount` DOUBLE DEFAULT 0 NOT NULL COMMENT '金额',
    `label` VARCHAR(32) DEFAULT NULL COMMENT '标签',
    `remark` TEXT DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`createtime`),
    CONSTRAINT `ledger_ibfk_1` FOREIGN KEY (`name`) REFERENCES `accounts` (`name`) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `ledger_ibfk_2` FOREIGN KEY (`label`) REFERENCES `labels` (`label`) ON DELETE RESTRICT ON UPDATE CASCADE
);

-- 查询标签
CREATE
OR REPLACE VIEW `view_labels` AS
SELECT
    `labels`.*,
    sum(`ledger`.`amount`) AS `amount`,
    count(`ledger`.`amount`) AS `count`
FROM
    `labels`
    LEFT JOIN `ledger` ON `labels`.`label` = `ledger`.`label`
GROUP BY
    `labels`.`label`
ORDER BY
    `labels`.`createtime` DESC;

-- 自定义函数 H2数据库中不支持
CREATE DEFINER = CURRENT_USER FUNCTION `record`(
    `name` VARCHAR(32),
    `type` TINYINT,
    `amount` DOUBLE,
    `label` VARCHAR(32),
    `remark` VARCHAR(255)
) RETURNS TINYINT(4) BEGIN declare i int;

declare t VARCHAR(2);

IF `type` > 0 THEN
SET
    i = 1;

SET
    t = '1';

ELSEIF `type` < 0 THEN
SET
    i = -1;

SET
    t = '-1';

ELSE RETURN 0;

END IF;

INSERT INTO
    ledger
VALUES
    (DEFAULT, `name`, t, amount, label, remark);

UPDATE
    accounts
SET
    balance = balance + i * amount
WHERE
    accounts.`name` = `name`;

RETURN 1;

END;