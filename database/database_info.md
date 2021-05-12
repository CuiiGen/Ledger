# 数据库说明文档

> 该文档为项目中数据库相关说明文档
> 
> 包含了数据库工具使用说明以及数据库关系表字段说明
> 
> 文档中所有SQL语句可直接在h2数据库中执行
> 
> 作者：iamroot
> 
> 2021年5月12日

## 版本说明

1. 该工程中所使用的数据库工具为h2-1.4.200，可通过[官网](http://www.h2database.com/html/download.html)或者[GitHub](https://github.com/h2database/h2database)进行下载。
1. 需要注意不同版本之间数据库工具互不兼容，一般不要随意使用其它版本工具打开数据库文件。

## 连接说明

1. 下载压缩包并解压，双击执行`./bin/h2w.bat`脚本，会在浏览器端启动H2控制台；
1. 在弹出的控制台中，选项`JDBC URL`一般为`jdbc:h2:file:[数据库文件存在路径]\[数据库文件名]`，`用户名`为`root`，`密码`为`sH6AkexU93exhBB`，其它项一般保持不变；
1. 点击`连接`即可连接本地数据库，若数据库文件不存在则会自动创建。

## 关系表说明

根据软件需求分析，该项目中共需要三张关系表，分别为
- `accounts`，存储账户相关信息；
- `labels`，存储账目记录的标签表；
- `ledger`，记录详细流水。

表中字段详细说明如后续所示。

### 账户表

|    字段名    |   数据类型    |   注释   | 非空 |   备注   |
| :----------: | :-----------: | :------: | :--: | :------: |
|    `name`    | `VARCHAR(32)` |  账户名  |  是  |   主键   |
| `createtime` |  `DATETIME`   | 创建时间 |  是  | 创建更新 |
|  `balance`   |   `DOUBLE`    |   余额   |      | 默认为0  |

建表命令为：

```sql
DROP TABLE `accounts` IF EXISTS;

CREATE TABLE `accounts` (
    `name` VARCHAR(32) NOT NULL COMMENT '账户名',
    `createtime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `balance` DOUBLE DEFAULT 0 COMMENT '余额',
    PRIMARY KEY (`name`)
);
```

### 标签表

|    字段名    |   数据类型    |   注释   | 非空 |   备注   |
| :----------: | :-----------: | :------: | :--: | :------: |
|   `label`    | `VARCHAR(32)` |   标签   |  是  |   主键   |
| `createtime` |  `DATETIME`   | 创建时间 |  是  | 创建更新 |

建表命令为：

```sql
DROP TABLE `labels` IF EXISTS;

CREATE TABLE `labels` (
    `label` VARCHAR(32) NOT NULL COMMENT '标签',
    `createtime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`label`)
);
-- 默认标签拒绝删除
INSERT INTO `labels` VALUES('存入', DEFAULT);
INSERT INTO `labels` VALUES('退款', DEFAULT);
```

### 账本

|    字段名    |     数据类型      |    注释    | 非空 |   备注   |
| :----------: | :---------------: | :--------: | :--: | :------: |
| `createtime` |    `DATETIME`     |  记账时间  |  是  |   主键   |
|    `name`    |   `VARCHAR(32)`   | 相关账户名 |  是  | 关联外键 |
|    `type`    | `ENUM('1', '-1')` | 收入或支出 |  是  |          |
|   `amount`   |     `DOUBLE`      |    金额    |  是  |          |
|   `label`    |   `VARCHAR(32)`   |    标签    |  是  | 关联外键 |
|   `remark`   |      `TEXT`       |    备注    |  否  |          |

注：所有外键限制删除，级联更新。建表命令为：

```sql
DROP TABLE `ledger` IF EXISTS;

CREATE TABLE `ledger` (
    `createtime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记账时间',
    `name` VARCHAR(32) NOT NULL COMMENT '账户名',
    `type` ENUM('1', '-1') NOT NULL COMMENT '收入或支出',
    `amount` DOUBLE NOT NULL COMMENT '金额',
    `label` VARCHAR(32) DEFAULT NULL COMMENT '标签',
    `remark` TEXT DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`createtime`),
    CONSTRAINT `ledger_ibfk_1` FOREIGN KEY (`name`) REFERENCES `accounts` (`name`) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `ledger_ibfk_2` FOREIGN KEY (`label`) REFERENCES `labels` (`label`) ON DELETE RESTRICT ON UPDATE CASCADE
);
```

## 查询说明

一般查询使用`SELECT`直接查询即可，标签表查询方面由于需要关联流水表，因此可以建立视图`view_labels`以方便查询。视图建立命令为：

```sql
CREATE
OR REPLACE VIEW `view_labels` AS
SELECT
    `labels`.*,
    sum(`ledger`.`amount`) AS `amount`
FROM
    `labels`
    LEFT JOIN `ledger` ON `labels`.`label` = `ledger`.`label`
GROUP BY
    `labels`.`label`
ORDER BY
    `labels`.`createtime` DESC;
```