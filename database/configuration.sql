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

-- 报销
DROP TABLE `reimbursements` IF EXISTS;

CREATE TABLE `reimbursements` (
    `no` INT NOT NULL AUTO_INCREMENT COMMENT '报销单ID',
    `name` VARCHAR(32) NOT NULL COMMENT '备注',
    `complete` BOOLEAN DEFAULT FALSE NOT NULL COMMENT '已经完成',
    PRIMARY KEY (`no`)
);

-- 账本
DROP TABLE `ledger` IF EXISTS;

CREATE TABLE `ledger` (
    `isValid` VARCHAR(1) NOT NULL DEFAULT 'o' COMMENT '是否计数',
    `createtime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记账时间',
    `name` VARCHAR(32) NOT NULL COMMENT '账户名',
    `type` ENUM('1', '-1') NOT NULL COMMENT '收入或支出',
    `amount` DOUBLE DEFAULT 0 NOT NULL COMMENT '金额',
    `label` VARCHAR(32) DEFAULT NULL COMMENT '标签',
    `remark` TEXT DEFAULT NULL COMMENT '备注',
    `reimbursement` INT DEFAULT NULL COMMENT '报销',
    PRIMARY KEY (`createtime`),
    CONSTRAINT `ledger_ibfk_1` FOREIGN KEY (`name`) REFERENCES `accounts` (`name`) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `ledger_ibfk_2` FOREIGN KEY (`label`) REFERENCES `labels` (`label`) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `ledger_ibfk_3` FOREIGN KEY (`reimbursement`) REFERENCES `reimbursements` (`no`) ON DELETE RESTRICT ON UPDATE CASCADE
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

-- 查询报销
CREATE
OR REPLACE VIEW `view_reimbursement` AS
SELECT
    `reimbursements`.*,
    sum(`ledger`.`amount` * CAST(CAST(`ledger`.`type` AS VARCHAR) AS INT)) AS `interest`
FROM
    `reimbursements`
    LEFT JOIN `ledger` ON `reimbursements`.`no` = `ledger`.`reimbursement`
GROUP BY
    `reimbursements`.`no`
ORDER BY
    `reimbursements`.`no` DESC;

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

-- 每月流水查询
SELECT
    FORMATDATETIME(`CREATETIME`, 'yyyy-MM') AS x,
    SUM(`amount`) AS y
FROM
    `ledger`
WHERE
    `type` = '-1'
    AND `isvalid` = 'o'
GROUP BY
    x
ORDER BY
    x;

-- 数据库数值校验
SELECT
    `accounts`.*,
    `check`.`amount`
FROM
    `accounts`
    LEFT JOIN (
        SELECT
            `name`,
            SUM(`amount` * CAST(`type` AS CHAR)) AS `amount`
        FROM
            `ledger`
        GROUP BY
            `name`
    ) AS `check` ON `check`.`name` = `accounts`.`name`
WHERE
    ABS(`check`.`amount` - `accounts`.`balance`) > 1E-7
    AND `check`.`amount` IS NOT NULL;

-- 事务相关指令
SET
    AUTOCOMMIT OFF;

SET
    AUTOCOMMIT ON;

ROLLBACK;

COMMENT;

-- 饼图相关
-- 各类别计算
SELECT
    `label`,
    SUM(`amount`) AS `total`
FROM
    `ledger`
WHERE
    `isvalid` = 'o'
    AND `type` = '-1'
GROUP BY
    `label`
ORDER BY
    `total` DESC;