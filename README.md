# Ledger账本

> 我的自定义账本，主要记录账户转账或一般性流水消费，支持自定义子账户，支持消费记录的查询、导出和备份等功能。
>
> iamroot
>
> 2021年9月8日

## 文件说明

```
Ledger
│  .gitignore                           // gitignore文件
│  README.md                            // 说明文档
│
├─backup                                // 输出数据库备份文件
│      backup_20210711110853.sql        // 时间戳命名数据库备份文件
│
├─database                              // 数据库相关文件
│      configuration.sql                // 建表SQL命令
│      database_info.md                 // 关系表说明文档
│      database_info.pdf                // 文档PDF
│      Ledger.mv.db                     // h2数据库
│
├─font                                  // 存放自定义字体
│      SourceHanSansCN-Regular.otf      // 思源黑体
│
├─icon                                  // 放置图标
│      Ledger.png                       // 程序主图标
│
├─installer                             // 输出可执行jar路径
├─libs                                  // 第三方库
│  │  h2-1.4.200.jar                    // h2数据库JDBC
│  │  jfreechart-1.5.3.jar              // 绘图
│  │  zip4j-2.9.0.jar                   // 压缩包操作
│  │
│  └─apache-log4j-2.13.0-bin            // log4j2相关库
│          log4j-api-2.13.0.jar
│          log4j-core-2.13.0.jar
│
├─logs                                  // 输出保存日志
└─src                                   // 源码
```

## 源代码说明

后续详细更新
