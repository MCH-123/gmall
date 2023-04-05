# gmall

#### 介绍
谷粒商城系统

**谷粒商城是一个 B2C 模式的电商平台，销售自营商品给客户。**

谷粒商城项目是一套电商项目，包括前台商城系统以及后台管理系统，基于 SpringCloud + SpringCloudAlibaba + MyBatis-Plus实现，采用 Docker 容器化部署。前台商城系统包括：用户登录、注册、商品搜索、商品详情、购物车、下订单流程、秒杀活动等模块。后台管理系统包括：系统管理、商品系统、优惠营销、库存系统、订单系统、用户系统、内容管理等七大模块。

#### 技术架构

##### 前端

| 技术      | 说明       | 官网                                                         |
| :-------- | :--------- | :----------------------------------------------------------- |
| Vue       | 前端框架   | [https://vuejs.org](https://gitee.com/link?target=https%3A%2F%2Fvuejs.org) |
| ElementUI | 前端UI框架 | [https://element.eleme.io](https://gitee.com/link?target=https%3A%2F%2Felement.eleme.io) |
| Thymeleaf | 模板引擎   | [https://www.thymeleaf.org](https://gitee.com/link?target=https%3A%2F%2Fwww.thymeleaf.org) |
| Node.js   | 服务端Js   | [https://nodejs.org/en](https://gitee.com/link?target=https%3A%2F%2Fnodejs.org%2Fen) |

+++

##### 后端

| 技术               | 说明                 | 官网                                                         |
| ------------------ | -------------------- | ------------------------------------------------------------ |
| SpringBoot         | 微服务开发工具包     | https://spring.io/projects/spring-boot                       |
| SpringCloud        | 微服务组件           | [https://spring.io/projects/spring-cloud](https://gitee.com/link?target=https%3A%2F%2Fspring.io%2Fprojects%2Fspring-cloud) |
| SpringCloudAlibaba | 阿里巴巴微服务组件   | [https://spring.io/projects/spring-cloud-alibaba](https://gitee.com/link?target=https%3A%2F%2Fspring.io%2Fprojects%2Fspring-cloud-alibaba) |
| MyBatis-Plus       | ORM框架              | [ https://mp.baomidou.com](https://gitee.com/link?target=https%3A%2F%2Fmp.baomidou.com) |
| renren-generator   | 人人开源项目逆向工程 | https://gitee.com/renrenio/renren-generator                  |
| Elasticsearch      | 搜索引擎             | [https://github.com/elastic/elasticsearch](https://gitee.com/link?target=https%3A%2F%2Fgithub.com%2Felastic%2Felasticsearch) |
| RabbitMQ           | 消息队列             | [https://www.rabbitmq.com](https://gitee.com/link?target=https%3A%2F%2Fwww.rabbitmq.com) |
| Redis              | 缓存中间件           | https://redis.io/                                            |
| Redisson           | 分布式锁             | [https://github.com/redisson/redisson](https://gitee.com/link?target=https%3A%2F%2Fgithub.com%2Fredisson%2Fredisson) |
| MySQL              | 数据库               | https://www.mysql.com/                                       |
| Docker             | 应用容器             | [https://www.docker.com](https://gitee.com/link?target=https%3A%2F%2Fwww.docker.com) |

#### 项目结构

gmall

|—— gmall-admin -- 后台管理模块

|—— gmall-common -- 公用模块

|—— gmall-generator -- 代码生成模块

|—— gmall-gateway -- 网关模块

|—— gmall-auth-- 授权中心模块

|—— gmall-gateway -- 网关模块

|—— gmall-index -- 首页模块

|—— gmall-search -- 搜索模块

|—— gmall-pms -- 商品管理模块

|—— gmall-item -- 商品详情模块

|—— gmall-wms -- 库存管理模块

|—— gmall-sms -- 营销信息模块

|—— gmall-ums -- 用户管理模块

|—— gmall-cart-- 购物车模块

|—— gmall-oms -- 订单管理模块

|—— gmall-order-- 订单交互模块

|—— gmall-scheduled -- 定时任务模块

|—— gmall-payment-- 支付模块



#### 项目演示

1. docker容器服务

   ![](imgs\docker.png)

2. 后台管理

   ![](imgs\后台系统.png)

   ![后台系统-1](imgs\后台系统-1.png)

#### 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request


#### 特技

1.  使用 Readme\_XXX.md 来支持不同的语言，例如 Readme\_en.md, Readme\_zh.md
2.  Gitee 官方博客 [blog.gitee.com](https://blog.gitee.com)
3.  你可以 [https://gitee.com/explore](https://gitee.com/explore) 这个地址来了解 Gitee 上的优秀开源项目
4.  [GVP](https://gitee.com/gvp) 全称是 Gitee 最有价值开源项目，是综合评定出的优秀开源项目
5.  Gitee 官方提供的使用手册 [https://gitee.com/help](https://gitee.com/help)
6.  Gitee 封面人物是一档用来展示 Gitee 会员风采的栏目 [https://gitee.com/gitee-stars/](https://gitee.com/gitee-stars/)
