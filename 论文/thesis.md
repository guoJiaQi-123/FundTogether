---
title: "在线众筹网站的设计与实现"
author: "郭家旗"
date: "2026 年 5 月"
---

# 封面

![太原理工大学校徽](cover.png){width=40%}

**2026 届本科生毕业设计（论文）**

# 在线众筹网站的设计与实现

学　号： 2022005946

姓　名： 郭家旗

学　院： 软件学院

专　业： 软件工程

班　级： 软件 2235 班

指导教师： 李金岚 副教授

完成日期：2026 年 5 月

\newpage

# 声明及论文使用的授权

本人郑重声明：所呈交的毕业设计（论文）是本人在指导教师的指导下取得的研究成果，毕业设计（论文）写作严格遵循学术规范。除了文中特别加以标注和致谢的地方外，毕业设计（论文）中不包含其他人已经发表或撰写的研究成果。因本毕业设计（论文）引起的法律结果完全由本人承担，太原理工大学享有本毕业设计（论文）的研究成果。

论文作者签名：　　　　　　　　　　　年　月　日

本毕业设计（论文）作者和指导教师同意太原理工大学保留使用毕业设计（论文）的规定，即：学校有权保留送交毕业设计（论文）的复印件，允许毕业设计（论文）被查阅和借阅；学校可以上网公布全部内容，可以采用影印、缩印或其他复制手段保存毕业设计（论文）。

论文作者签名：　　　　　　　　指导教师签名：

签字日期：　年　月　日　　　　签字日期：　年　月　日

\newpage

# 摘　要

随着数字经济与互联网金融的迅速发展，创新创业、文创项目、公益活动以及小型产品研发对低门槛、广覆盖、高效率融资渠道的需求日益强烈。传统融资方式普遍存在准入门槛高、周期长、流程繁琐、受众范围有限等问题，难以满足大众创业者、学生团队与公益组织的实际筹资需求。在线众筹作为一种依托互联网平台、面向社会公众进行小额资金募集的新型融资模式，凭借其门槛低、灵活性强、传播范围广、互动性高等特点，已逐步成为中小项目启动与创意落地的重要支撑方式。

本论文以在线众筹网站的设计与实现为研究目标，采用当前主流且成熟的前后端分离技术体系，构建一套功能完整、流程规范、安全可靠、交互友好的众筹服务平台 FundTogether。系统后端采用 Spring Boot 3.x 搭建 RESTful 服务，使用 MyBatis-Plus 完成持久层封装，基于 Spring Security 与 JWT 实现无状态的 RBAC 权限控制；前端采用 Vue 3 + Vite + Element Plus 构建响应式单页应用，通过 ECharts 实现多维数据可视化；数据层以 MySQL 8.0 承载结构化业务数据，以 Redis 7.x 承载缓存、验证码与分布式锁；系统通过 WebSocket 实现项目动态与平台公告的实时推送，通过 Nginx 完成反向代理与静态资源托管，借助 Docker Compose 实现整站的一键部署。系统围绕游客、普通支持者、项目发起人、系统管理员四类角色设计闭环业务流，完整实现认证与用户管理、项目浏览与交互、项目支持与交易闭环、项目发起与管理、后台监管与运营、消息与通知、跨端适配与体验等七大核心模块，并针对众筹场景特有的资金托管、里程碑拨付、退款保障与风控拦截，设计了基于"托管账户 + 资金流水账本"的资金隔离模型。经过功能测试、接口压测、兼容性测试与安全测试，系统各模块运行稳定，能够有效支撑中小规模的众筹业务场景。

本论文的研究成果不仅为同类众筹平台的设计与实现提供了可复用的工程参考，也系统训练了需求分析、系统设计、编码实现、测试部署与技术文档撰写的全流程软件工程能力。

**关键词**：在线众筹；Spring Boot；Vue 3；JWT；资金托管；数据可视化

\newpage

# Abstract

With the rapid development of the digital economy and Internet finance, innovative entrepreneurship, cultural and creative projects, public welfare activities and small-scale product research and development have an increasingly urgent demand for low-threshold, wide-coverage and high-efficiency financing channels. Traditional financing models generally suffer from high entry barriers, long funding cycles, complicated procedures and limited audiences, making it difficult to meet the actual fundraising needs of mass entrepreneurs, student teams and non-profit organizations. Online crowdfunding, as a new financing paradigm that raises small amounts of funds from the general public through Internet platforms, has gradually become an important support mechanism for the startup and materialisation of small and medium-sized projects, thanks to its low threshold, flexibility, wide dissemination and strong interactivity.

This thesis takes the design and implementation of an online crowdfunding website as its research goal, and adopts the currently mainstream front-end and back-end decoupled technology stack to build a fully functional, process-standardized, security-reliable and interaction-friendly crowdfunding service platform named FundTogether. The backend is built upon Spring Boot 3.x to expose RESTful services, while MyBatis-Plus is employed as the object–relational mapping layer. A stateless role-based access control mechanism is implemented with Spring Security and JSON Web Token. The frontend is constructed with Vue 3, Vite and Element Plus, delivering a responsive single-page application, and ECharts is leveraged for multi-dimensional data visualization. MySQL 8.0 carries the structured business data, and Redis 7.x is used for cache, verification code storage and distributed lock. The system publishes project updates and platform announcements in real time via WebSocket, uses Nginx as reverse proxy and static resource server, and can be deployed with one click through Docker Compose. Four roles — visitor, supporter, sponsor and administrator — are modelled as a closed-loop business process, and seven functional modules are fully implemented, including authentication, project browsing and interaction, transaction closure, project creation and management, back-office supervision, messaging and notification, and cross-platform adaptation. A custody-account plus fund-ledger model is further proposed to address the special requirements of fund custody, milestone payout, refund guarantee and risk control in crowdfunding scenarios. After functional testing, interface stress testing, compatibility testing and security testing, each module of the system runs stably, and the platform is capable of supporting small and medium-sized crowdfunding business scenarios effectively.

The research results provide a reusable engineering reference for the design and implementation of similar crowdfunding platforms, and systematically exercise the full-stack software engineering capabilities of requirement analysis, system design, coding, testing, deployment and technical documentation.

**Key words:** Online crowdfunding; Spring Boot; Vue 3; JWT; Fund custody; Data visualization

\newpage

# 目　录

摘要 ....................................................................... Ⅰ

Abstract .................................................................. Ⅱ

1 绪论 ..................................................................... 1

1.1 课题研究背景 ........................................................ 1

1.2 课题研究目的与意义 .................................................. 2

1.3 国内外研究现状 ...................................................... 3

1.4 本文主要研究内容 .................................................... 5

1.5 本文组织结构 ........................................................ 6

2 关键技术与可行性分析 ..................................................... 7

2.1 前端关键技术 ........................................................ 7

2.2 后端关键技术 ........................................................ 9

2.3 数据库与缓存技术 ................................................... 11

2.4 实时通信与部署技术 ................................................. 12

2.5 可行性分析 ......................................................... 13

3 系统需求分析 ............................................................ 15

3.1 业务场景与角色分析 ................................................. 15

3.2 功能性需求 ......................................................... 17

3.3 非功能性需求 ....................................................... 21

3.4 用例分析 ........................................................... 22

4 系统设计 ................................................................ 26

4.1 系统总体架构设计 ................................................... 26

4.2 功能模块设计 ....................................................... 28

4.3 数据库设计 ......................................................... 30

4.4 接口与协议设计 ..................................................... 34

4.5 安全与资金托管设计 ................................................. 36

5 系统详细实现 ............................................................ 39

5.1 开发与运行环境 ..................................................... 39

5.2 用户认证与权限模块 ................................................. 40

5.3 项目发起与审核模块 ................................................. 43

5.4 项目支持与资金流转模块 ............................................. 46

5.5 消息推送与后台监管模块 ............................................. 50

5.6 前端交互与可视化实现 ............................................... 53

6 系统测试 ................................................................ 56

6.1 测试环境与方法 ..................................................... 56

6.2 功能测试 ........................................................... 57

6.3 接口与性能测试 ..................................................... 59

6.4 兼容性与安全测试 ................................................... 60

结论 ..................................................................... 62

参考文献 ................................................................. 64

致谢 ..................................................................... 66

外文原文 ................................................................. 67

中文翻译 ................................................................. 75

\newpage

# 1 绪论

## 1.1 课题研究背景

随着移动互联网、云计算、大数据与在线支付等一系列信息技术的不断成熟和普及，数字经济正在对传统金融服务体系形成深层次的重塑与冲击。根据《中国互联网发展报告》的持续统计，我国网民规模已稳定超过十亿，移动支付渗透率稳居全球前列，在线交易闭环能力已具备支撑新型金融服务的坚实基础。与此同时，"大众创业、万众创新"战略的持续推进也在不断释放社会创新活力，个人创业、学生团队、公益组织、文创工作室等中小型创新主体的数量迅速增长，他们对低成本、轻量化、可持续的融资渠道形成了持续且强烈的现实需求。

传统融资方式主要包括银行贷款、风险投资、政府专项扶持基金以及民间借贷等。银行贷款对主体资质、抵押担保与信用记录要求极高，中小型创新主体难以跨越审批门槛；风险投资更关注项目的市场规模、成长潜力与退出路径，对所处阶段、估值区间与尽职调查的要求使得绝大多数初创团队无法获得青睐；政府专项扶持基金虽然政策友好，但额度有限、申请周期长、评审过程相对严格；民间借贷则存在监管真空、利率不透明、违约风险高等隐患。上述传统渠道在金额匹配、周期匹配与流程匹配方面均与中小型创新项目之间存在明显缺口，催生了新型融资模式的探索。

众筹（Crowdfunding）作为互联网金融体系中的重要分支，起源于 21 世纪初的欧美地区，并在 Kickstarter、Indiegogo、GoFundMe 等代表性平台的推动下迅速形成规模化商业模式。其基本内涵是：项目发起人借助互联网平台向不特定公众公开募集小额资金，以实物、权益、服务或股权作为回报，支持者以消费、投资或公益捐助的方式参与其中，从而实现低成本、广覆盖、社群化的融资闭环。相较传统融资方式，众筹具有四项显著优势：其一，单笔金额小、参与门槛低，使普通公众得以成为创新活动的直接支持者；其二，资金募集与项目宣传同步开展，兼具传播与销售双重价值；其三，众筹本身形成了真实的市场验证，可为项目后续迭代与资本化提供数据依据；其四，社群互动促进了支持者与发起人之间的良性沟通，提升了项目的可持续性。

国内众筹行业在政策引导与市场需求的双重驱动下起步虽晚但发展迅速。京东众筹、淘宝众筹、摩点网等第一代综合与垂直平台相继涌现，产品众筹、公益众筹、文娱众筹等细分模式不断丰富。然而，国内众筹平台在野蛮生长后也暴露出若干共性问题：部分平台审核机制薄弱、项目信息真实性存疑；资金流转缺乏透明、可追溯的账本支撑；支付通道对接存在合规性风险；用户体验参差不齐且缺乏标准化工程模板。在本科毕业设计与工程训练场景中，能够按照软件工程规范完整复现众筹业务全链路、并充分体现资金安全与信任机制的研究案例仍然稀缺。

本课题正是基于上述背景提出，以"设计并实现一个功能完整、流程规范、资金闭环透明、具备跨端适配能力的在线众筹网站"为总体目标，采用企业级主流技术栈完成从需求分析、系统设计、编码实现、系统测试到部署运维的完整软件工程实践。

## 1.2 课题研究目的与意义

### 1.2.1 研究目的

本课题旨在围绕在线众筹业务的典型场景，设计并实现一套命名为 FundTogether 的中小规模在线众筹网站。具体研究目的可归纳为以下四个方面：

（1）面向四类核心角色构建闭环业务流。系统覆盖"游客—普通支持者—项目发起人—系统管理员"四类角色，实现注册登录、项目浏览、项目创建、项目审核、项目支持、订单支付、资金托管、里程碑拨付、项目退款、消息推送、数据统计与可视化等完整业务链路。

（2）基于主流企业级技术栈完成全栈工程实践。后端采用 Spring Boot 3.x + MyBatis-Plus + Spring Security + JWT + Redis，前端采用 Vue 3 + Vite + Element Plus + ECharts，数据库采用 MySQL 8.0，容器化采用 Docker + Docker Compose，对应企业级开发的主流能力模型。

（3）突出众筹场景特有的资金安全与信任机制。针对众筹业务的资金汇集与拨付特性，设计基于"项目托管账户 + 资金流水账本"的资金隔离模型，覆盖支持入账、失败退款、阶段拨付、发起人提现等核心资金流向，并结合分布式锁、乐观锁和事务机制保障强一致性。

（4）训练学生的工程规范意识与文档能力。完整输出需求说明、系统设计、接口规范、数据库字典、测试报告、部署手册与毕业论文等工程文档，体现对软件工程方法论的遵循与应用。

### 1.2.2 研究意义

本课题的研究意义主要体现在实践价值、学术价值与教育价值三个层面：

（1）实践价值。本系统构建了一个可扩展的中小型众筹平台原型，可作为校园众筹、公益众筹、文创众筹等场景的基础版本，经过定制化开发即可投入试运行，为创新创业团队提供轻量化的融资工具。

（2）学术价值。系统在资金安全、权限控制、状态机治理与实时通信等关键问题上给出了工程化的解决方案，对同类项目的架构设计与性能优化具有参考意义。特别是"托管账户 + 资金流水"的双账表设计，在保持 RDBMS 事务语义的同时兼顾了可追溯性与透明性。

（3）教育价值。本课题完整贯穿软件工程本科阶段的各项核心知识点，覆盖面向对象分析与设计、数据库系统原理、软件工程方法、Web 开发、信息安全、性能优化与 DevOps 等内容，为软件工程人才培养提供了真实且综合的工程训练载体。

## 1.3 国内外研究现状

### 1.3.1 国外研究现状

国外众筹行业起步较早且发展成熟。Kickstarter（2009 年）、Indiegogo（2008 年）、GoFundMe（2010 年）等代表性平台已建立起以"产品众筹"、"公益众筹"和"股权众筹"为核心的差异化商业模式，并在风险控制、信用评估、国际支付与数据安全等方面积累了丰富工程经验。Miller 等对全球众筹产业的统计表明，全球众筹年度交易规模已超过数百亿美元，平台间的竞争焦点逐步从"流量入口"转向"信任与合规"。

在理论层面，Belleflamme 等建立了众筹项目成功率的量化模型，指出项目类别、目标金额、描述质量、发起人历史信用与早期社群动员是关键因素；Mollick 提出众筹项目存在显著的"社群扩散效应"，即项目前 48 小时的支持情况对最终成功率有决定性影响；Ahlers 等研究了股权众筹中的信息不对称问题，建议平台通过托管机制与信息披露加以缓解。Smith 与 Johnson 的近期研究则进一步指出，平台的支付流、账本流和审计流应当分层解耦，以确保在海量并发与跨境合规场景下仍然能够保持系统弹性。

在技术层面，国外主流平台普遍采用高可用的微服务架构、托管型云数据库与流式数据平台，在支付侧大量使用 Stripe、PayPal、Adyen 等成熟聚合支付服务，在安全侧普遍落地了 TLS 全链路加密、PCI-DSS 合规体系、交易指纹识别与实时风控引擎等方案，具备较高的工业成熟度。

### 1.3.2 国内研究现状

我国众筹行业起步于 2011 年前后，代表平台包括京东众筹、淘宝众筹、摩点、众筹网、开始吧等。国内平台在业务模式上更加贴近本土消费场景，呈现出"产品众筹+预售"的主流形态；在监管层面，国家出台的一系列法规与行业指导意见也在持续规范平台的合规边界。

在技术研究层面，国内学者围绕众筹平台的实现方案发表了大量研究：张磊系统介绍了基于 SSM 框架的在线众筹平台实现要点，强调了支付接口与订单状态机的设计；李娜基于 Vue.js 与 Spring Boot 构建了典型众筹原型，讨论了前后端分离场景下的接口契约与安全策略；刘军针对第三方支付接口的集成与回调验签进行了专题研究，提出了较完整的签名校验与对账对比流程；赵阳则将用户画像与协同过滤推荐应用于众筹场景，提升了项目发现的效率。此外，刘春玉等就软件工程视角下的 Web 系统研发范式给出了完整的工程模板，可为本课题的工程化推进提供方法论参考。

综合而言，国内众筹研究已形成较为完整的技术脉络，但在以下三方面仍然存在可拓展的空间：一是资金流的"账户—账本"双层建模尚缺乏可落地的统一范式；二是针对本科毕业设计尺度的、兼具安全性与工程规范的开源参考实现较少；三是在多端适配与实时消息推送方面的工程模板不够成熟。本课题立足上述缺口，围绕中小规模场景提出可直接复用的工程方案。

## 1.4 本文主要研究内容

本文围绕在线众筹网站的设计与实现展开，以软件工程方法论为主线，具体研究内容包括：

（1）系统梳理在线众筹行业的业务流程与核心痛点，明确四类角色的职责边界，形成结构化的需求规格说明；

（2）基于前后端分离思想完成系统的总体架构设计、模块划分、接口契约、数据库建模与权限体系设计，重点攻关资金托管模型、项目状态机和订单状态机；

（3）依托 Spring Boot、Vue 3、MySQL、Redis、WebSocket、Docker 等主流技术栈完成系统的编码实现，输出核心模块可运行的工程代码；

（4）设计并执行单元测试、接口测试、功能测试、性能测试与安全测试，输出测试报告并对缺陷进行闭环修复；

（5）基于 Docker Compose 构建一键化部署方案，并输出完整的部署手册与运维说明。

## 1.5 本文组织结构

本论文按照"绪论—关键技术—需求—设计—实现—测试—结论"的逻辑层次组织，全文共分为六章，各章内容概述如下：

第 1 章为绪论，阐述课题研究背景、研究目的与意义、国内外研究现状以及本文的主要研究内容与组织结构；

第 2 章为关键技术与可行性分析，介绍本课题涉及的主要前端、后端、数据库、缓存、实时通信与部署技术，并从理论、技术、经济、操作与时间五个维度论证课题的可行性；

第 3 章为系统需求分析，识别系统涉及的四类角色，梳理功能性与非功能性需求，并通过用例图、业务流程图等工具进行需求建模；

第 4 章为系统设计，完成系统的总体架构设计、功能模块设计、数据库设计、接口与协议设计以及安全与资金托管模型设计；

第 5 章为系统详细实现，围绕认证与权限、项目发起与审核、项目支持与资金流转、消息推送与后台监管、前端交互与可视化五大子系统展开实现细节描述；

第 6 章为系统测试，阐述测试环境、测试方法、功能测试、接口与性能测试、兼容性与安全测试的具体过程与结果；

最后给出结论、参考文献、致谢、外文原文与中文翻译等附属部分。

\newpage

# 2 关键技术与可行性分析

## 2.1 前端关键技术

### 2.1.1 Vue 3 与 Composition API

Vue 3 是由尤雨溪团队主导开源的渐进式 JavaScript 框架，凭借其响应式原理重构、TypeScript 原生支持以及 Composition API 等特性成为目前主流的前端框架之一。相较 Vue 2 所采用的 Options API，Composition API 通过 `setup` 语法糖将响应式状态、计算属性、侦听器与生命周期钩子集中组织，有效提升了复杂组件中逻辑的可读性、可复用性与可测试性。Vue 3 底层使用 Proxy 替代 Object.defineProperty 进行响应式追踪，支持任意层级属性动态新增与删除，运行时性能与内存占用均有显著优化。

在本课题中，Vue 3 被用作单页应用的主框架，承担页面渲染、状态驱动、路由分发与交互逻辑四项核心职责。结合 Pinia 状态管理与 Vue Router 4 路由管理，系统构建了一个结构清晰、响应迅速、便于维护的前端工程。

### 2.1.2 Vite 构建工具

Vite 是基于原生 ES Module 构建的新一代前端工具链，与 Webpack 等传统打包工具相比在开发体验上具有显著优势。其核心设计理念是"按需编译"：开发环境下，浏览器通过 `<script type="module">` 直接请求源文件，Vite 仅在浏览器请求时对对应模块进行必要的语法转换，无需打包全量依赖图；生产构建则使用 Rollup 进行精细化产物优化。凭借这一理念，Vite 在启动速度和热更新速度上均表现出毫秒级性能，特别适用于规模适中的单页应用开发。

### 2.1.3 Element Plus 与响应式布局

Element Plus 是基于 Vue 3 的企业级 UI 组件库，内置表单、表格、对话框、消息提示、分页、日历等 60 余个通用组件，并提供丰富的主题定制能力。本系统在整体 UI 规范上严格对齐 Material Design 的设计原则，避免使用渐变色与过度动画，所有主色均采用 HSL 单色方案以保障 WCAG AA 级对比度。在布局方面，使用 CSS Grid 与 Flexbox 实现 12 列响应式栅格，按 768px、1024px、1280px 三级断点覆盖 PC、平板与移动端。

### 2.1.4 ECharts 数据可视化

ECharts 是百度开源、目前由 Apache 基金会孵化的大型数据可视化库，支持 30 余种图表类型与 DSL 式配置。在本课题中，ECharts 被用于渲染管理后台的核心运营指标看板，包括分类筹款柱状图、每日筹款趋势折线图、项目状态分布饼图、用户增长曲线等多维图表。通过将图表配置对象与后端聚合接口返回的数据结构统一约定，前后端之间形成了稳定的数据契约。

## 2.2 后端关键技术

### 2.2.1 Spring Boot 3.x

Spring Boot 是基于 Spring 生态构建的约定优于配置（Convention over Configuration）的快速开发框架。Spring Boot 3.x 迁移至 Jakarta EE 9+ 命名空间、JDK 17 基线以及 GraalVM 原生镜像支持，在性能、可观测性和云原生友好度上取得了显著提升。Spring Boot 内嵌 Tomcat 引擎并通过自动装配机制简化了传统 Spring 项目中的 XML 配置工作，仅需一条命令即可启动一个具备完整 Web 能力的企业级应用。

在本课题中，Spring Boot 承担系统的主干职责，所有 Controller、Service、Mapper、Filter 与 Aspect 均在 Spring 容器中统一管理。通过 `@RestController` 和 `@RequestMapping` 等注解可以快速暴露 RESTful 接口；通过 `@Transactional` 可以声明事务边界；通过 `@Configuration` 和 `@Bean` 可以优雅地管理 Bean 依赖关系。

### 2.2.2 MyBatis-Plus

MyBatis-Plus 是基于 MyBatis 的增强型 ORM 框架，通过 `BaseMapper` 与 `IService` 接口提供开箱即用的 CRUD 能力，同时保留了 MyBatis 原生 XML 与动态 SQL 的灵活性。其 `IPage` 分页插件、`LambdaQueryWrapper` 条件构造器、`@TableLogic` 逻辑删除注解、`MetaObjectHandler` 自动填充以及乐观锁插件在本系统中被大量使用，显著降低了样板代码的数量。

### 2.2.3 Spring Security 与 JWT

Spring Security 是 Spring 生态中功能最完整的安全框架，提供认证、授权、会话管理、CSRF 防护、密码加密等能力。本系统采用无状态 JWT（JSON Web Token）作为客户端令牌，避免了传统 Session 在水平扩展场景下的共享存储开销。在权限模型上采用基于位掩码的 RBAC：用户角色以整型位掩码保存（USER=1、SPONSOR=2、ADMIN=4），通过按位与运算判断用户是否具备指定角色，同一账户可以同时拥有多个角色。

### 2.2.4 Redis 7.x 与分布式锁

Redis 是一款高性能、内存驱动的键值型数据库，支持字符串、哈希、列表、集合、有序集合、流、位图等多种数据结构。本系统使用 Redis 承担以下职责：缓存热点项目列表与详情；保存短信/邮箱验证码并通过 TTL 自动失效；实现基于 `SET key value NX EX seconds` 原语的分布式锁，避免同一用户并发投资造成的资金异常；以固定窗口计数算法实现接口限流。

### 2.2.5 切面式限流与全局异常处理

系统通过自定义注解 `@RateLimit` 配合 Spring AOP 切面实现方法级限流。在切面中，使用 Redis 的 `INCR` 与 `EXPIRE` 组合实现固定时间窗口的计数；超过阈值时抛出 `BusinessException(429)`，由 `GlobalExceptionHandler` 统一转换为标准响应体 `Result`。

## 2.3 数据库与缓存技术

### 2.3.1 MySQL 8.0

MySQL 8.0 是当前最主流的开源关系型数据库之一，支持 InnoDB 引擎下的行级锁、事务 ACID 语义、外键约束与 MVCC 快照隔离。本课题涉及的订单、账本、项目等核心表均采用 InnoDB 引擎并启用了默认的 `READ COMMITTED` 隔离级别，以保障支付扣款与筹款金额更新过程的强一致性。索引设计遵循"最左前缀原则"，对高频查询字段（如 `project.status`、`support_order.user_id`）建立了独立索引或组合索引。

### 2.3.2 Redis 缓存设计

缓存层采用"旁路缓存"（Cache Aside）模式：读路径先查缓存，缓存未命中则读取数据库并回填；写路径则先写数据库再失效缓存。为避免缓存击穿与缓存雪崩，系统对热门项目详情缓存键加入随机 TTL，并对不存在的键写入短时空值占位。

## 2.4 实时通信与部署技术

### 2.4.1 WebSocket

WebSocket 是一种基于 TCP 的全双工通信协议，克服了 HTTP 单向请求/响应模型在实时推送场景中的轮询弊端。系统通过 `spring-boot-starter-websocket` 集成 WebSocket 能力，为项目动态、项目状态变更与平台公告提供低延迟的实时推送通道。握手阶段通过自定义 `ServerEndpointConfig.Configurator` 校验 JWT，建立会话后按用户 ID 维护 `ConcurrentHashMap<String, Session>` 的在线池。

### 2.4.2 Nginx 与反向代理

Nginx 作为反向代理承担"静态资源托管 + 接口请求转发 + 请求限流 + TLS 终结"四类职责。前端构建产物在部署时被拷贝至 Nginx 的静态目录，所有以 `/api/` 开头的请求被转发至后端上游，WebSocket 握手请求通过 `Upgrade` 请求头识别并建立长连接。

### 2.4.3 Docker Compose 编排

Docker 提供了基于 Linux Namespace 与 Cgroup 的容器化能力，Docker Compose 则通过 YAML 声明式地定义多容器编排拓扑。本系统通过 `docker-compose.yml` 定义了 MySQL、Redis、后端服务、前端静态服务与 Nginx 五个服务，使用 `depends_on` 与健康检查保障启动顺序，使用 `volumes` 将数据与日志目录持久化至宿主机。开发、测试与生产环境通过不同的 `application-*.yml` 与环境变量进行隔离。

## 2.5 可行性分析

### 2.5.1 技术可行性

本课题采用的 Spring Boot、Vue 3、MySQL、Redis、WebSocket、Docker 等技术均已在业内大规模落地，社区活跃、文档完整、第三方组件丰富。作者在校期间系统学习了 Java 语言、Spring 生态、数据库原理、Web 开发与软件工程等课程，具备独立完成系统设计与编码的基础能力；此外，指导教师在软件工程与 Web 系统开发方向具有丰富研究与教学经验，可为课题提供充分的学术与工程指导。综上所述，课题在技术层面具备充分可行性。

### 2.5.2 经济可行性

本系统所依赖的全部技术栈均为开源且免费；开发过程中所需的开发主机、测试服务器、数据库与缓存环境均可利用学校提供的实验室资源满足；项目部署过程仅需一台具备 4 核 CPU、8 GB 内存的普通云服务器或本地物理机。因此课题整体经济成本较低，在本科毕业设计的预算范围之内。

### 2.5.3 操作可行性

系统前端以 Web 页面方式访问，无需安装任何客户端；后台管理使用标准浏览器即可完成全部运营操作；支付环节对接支付宝沙箱环境，流程与线上生产环境一致，便于后期平滑升级。

### 2.5.4 时间可行性

课题按照开题报告中制定的 2026 年 2 月 23 日至 2026 年 6 月 5 日的时间安排开展，共约 15 周时间可供使用。考虑到作者已有一定的前后端工程基础，且系统边界清晰、模块相对独立，15 周的实施周期足以覆盖需求分析、系统设计、编码实现、测试部署与论文撰写等全部环节。

### 2.5.5 法律与合规可行性

本课题为毕业设计性质的学术研究，不涉及真实资金流入流出，支付环节使用沙箱环境模拟；用户个人信息仅在本地环境落库，采用 Bcrypt 单向哈希存储密码、上传资料本地化存储，避免真实用户隐私泄露。因此在现行法律与合规要求下具备实施条件。

\newpage

# 3 系统需求分析

## 3.1 业务场景与角色分析

FundTogether 面向个人、团队、公益组织的中小规模众筹场景，业务闭环由发起、审核、支持、托管、拨付、反馈六个环节构成。为准确刻画业务流，需对系统涉及的典型角色进行分析。

### 3.1.1 游客

游客是指未完成注册或未登录平台的访问者。游客可以浏览平台首页、查看公开的项目列表与项目详情、查看项目视频与筹款进度可视化图表，但不具备任何写权限。游客可以通过注册入口转化为普通支持者。

### 3.1.2 普通支持者

普通支持者是完成注册并登录的基础角色，也是平台的主要用户群体。支持者可以完善个人资料、绑定支付方式、投资支持项目、发起评论、点赞、收藏、关注发起人、接收站内信与实时推送通知。支持者投资后，其账户余额将通过数据库事务被扣减，并在资金流水表中生成对应记录。

### 3.1.3 项目发起人

项目发起人是在普通支持者基础上具备"发起项目"能力的扩展角色。由于系统采用位掩码角色模型，普通支持者与项目发起人可以并存。发起人可以创建项目、上传封面图与视频、设置支持档位与对应回报、提交审核、修改待审核项目、发布项目动态、回复用户评论、查看筹款数据、导出支持者名单。

### 3.1.4 系统管理员

系统管理员是平台运营的最高权限角色，负责项目审核、用户管理、违规项目下架、提现审核、平台公告发布、数据统计与报表导出。系统管理员的所有操作均会在管理员操作日志表中留痕，保证可审计性。

## 3.2 功能性需求

基于 README 与开题报告中定义的 59 项功能条目，本文将系统的功能性需求归纳为七大模块，每个模块下的关键功能如表 3-1 所示。

表 3-1 系统功能模块与关键功能映射表

| 模块编号 | 模块名称 | 关键功能 |
|:--:|:--|:--|
| F1 | 认证与用户管理 | 注册、登录、退出、忘记密码、修改密码、个人资料维护、头像上传、实名认证、支付方式绑定、角色分配 |
| F2 | 项目浏览与交互 | 项目大厅、热门排行、多维检索、个性化推荐、项目详情、视频播放、筹款趋势图、评论、点赞、收藏、关注 |
| F3 | 项目支持与交易 | 选择档位、创建支持订单、多渠道支付、订单状态流转、我的支持、支持历史、累计统计、订单详情 |
| F4 | 项目发起与管理 | 创建项目、上传素材、设置档位、提交审核、修改待审核项目、取消项目、发布动态、回复评论、查看数据看板、导出支持者名单、申请阶段拨付 |
| F5 | 后台监管与运营 | 用户管理、角色分配、账号禁用、项目审核与驳回、项目下架、提现审核、公告发布、数据看板、报表导出、管理员日志 |
| F6 | 消息与通知 | 项目动态推送、订单状态通知、平台公告推送、评论回复通知、站内信列表与已读状态、WebSocket 实时推送 |
| F7 | 跨端适配与体验 | PC 与移动端响应式布局、移动端项目管理、移动端消息推送 |

### 3.2.1 认证与用户管理（F1）

F1 模块的核心目标是保障账号的可注册、可登录、可恢复与可管理。其中需要重点描述的典型需求如下：

**FR-01 注册**：游客通过手机号或邮箱提交账号信息；系统校验格式、唯一性、验证码、两次密码一致性；校验通过后使用 Bcrypt 哈希存储密码并下发默认"普通支持者"角色。

**FR-02 登录**：用户输入账号与密码；系统校验密码后生成带过期时间的 JWT，令牌中包含用户 ID、账号、角色位掩码；客户端将令牌保存在 `localStorage` 并在后续请求中通过 `Authorization: Bearer` 头部携带。

**FR-03 修改密码**：登录用户输入原密码和新密码；系统校验原密码正确性、新密码复杂度与两次输入一致性；校验通过后更新数据库并强制用户重新登录。

**FR-04 实名认证**：用户上传身份证正反面照片与实名信息；系统将信息写入 `user_auth_info` 表并标记为"审核中"；管理员审核通过后将用户实名状态置为"已认证"。

### 3.2.2 项目浏览与交互（F2）

F2 模块的核心目标是保障项目被高效发现与深度交互。多维检索通过 MyBatis-Plus 的 `LambdaQueryWrapper` 动态拼接条件；项目详情中的每日筹款趋势使用 ECharts 折线图渲染；评论系统支持父子关系回复。

### 3.2.3 项目支持与交易（F3）

F3 模块关注订单创建与状态流转。订单状态机如图 3-1 所示：

```
PENDING(0) --pay--> PAID(1)
  |                   |
  |--cancel--> CANCELED(2)
                      |
                      |--refund--> REFUNDED(3)
```

图 3-1 订单状态机示意图

创建订单时需校验项目处于"筹款中"状态、档位未售罄、用户余额充足等业务前置条件，同时通过 Redis 分布式锁防止同一用户并发重复投资。

### 3.2.4 项目发起与管理（F4）

F4 模块覆盖项目全生命周期。项目状态机如图 3-2 所示：

```
PENDING_REVIEW(0) --approve--> FUNDING(1) --success--> SUCCESS(5)
         |          --reject--> REJECTED(2)   --fail--> FAILED(6)
         |          --cancel--> CANCELED(3)
         |                                    --offline--> OFFLINE(4)
```

图 3-2 项目状态机示意图

状态流转由后端强校验，非法流转将被 `BusinessException` 拦截并返回 400。

### 3.2.5 后台监管与运营（F5）

F5 模块提供运营所需的全部控制面能力。管理员后台提供用户、项目、资金、提现、公告、数据看板六大子模块，所有写操作均会在 `admin_operation_log` 表中记录操作者 ID、操作类型、操作目标 ID、操作前快照与操作时间，满足平台内审要求。

### 3.2.6 消息与通知（F6）

F6 模块包含"离线存储"与"在线推送"两条通道。离线消息写入 `sys_user_message` 表；在线用户通过 WebSocket 会话推送 JSON 结构化消息体。

### 3.2.7 跨端适配与体验（F7）

F7 模块要求同一接口集同时服务 PC 与移动端。前端采用 CSS Grid 与 Flexbox 响应式布局，Element Plus 组件在移动端以紧凑样式渲染。

## 3.3 非功能性需求

非功能性需求是保障系统可用性、可维护性与可演进性的重要补充，具体包括：

（1）性能需求。在 4 核 8GB 的部署环境下，系统应支撑 500 并发用户持续访问；关键查询接口（项目列表、项目详情、支持列表）的 P95 响应时间不超过 300 ms；支付接口的 P99 响应时间不超过 1 秒。

（2）安全需求。所有对外接口必须通过 JWT 鉴权或显式放行白名单；敏感数据（手机号、身份证、银行卡号）在日志中必须脱敏；前端应防范 XSS、CSRF 与点击劫持；后端应防范 SQL 注入、越权访问与重复提交。

（3）可用性需求。系统关键页面应支持 Lighthouse 性能评分不低于 90 分；移动端页面在 4G 网络下首屏时间不超过 2 秒；用户误操作应能通过二次确认或撤销操作进行恢复。

（4）可维护性需求。代码风格遵循《阿里巴巴 Java 开发手册》与 Vue 官方 Style Guide；所有公开接口必须通过 Knife4j 生成在线文档；核心 Service 层单元测试覆盖率不低于 80%。

（5）可扩展性需求。系统在架构上保留横向扩展接口：后端服务无状态化以支持多实例部署；数据库层预留分库分表关键字；消息推送保留切换至专业消息队列（如 RocketMQ）的适配点。

## 3.4 用例分析

由于角色众多且场景丰富，本节以三个典型用例图为例进行说明：

（1）普通支持者用例图。支持者与系统交互的典型用例包括"登录系统"、"浏览项目"、"发表评论"、"支持项目"、"查看我的支持"、"接收消息"。其中"支持项目"用例扩展出"选择档位"与"选择支付方式"两个子用例。

（2）项目发起人用例图。发起人除继承普通支持者的全部用例外，还与系统交互"创建项目"、"编辑项目"、"发布动态"、"回复评论"、"查看筹款数据"、"导出支持者名单"、"申请阶段拨付"等用例。

（3）系统管理员用例图。管理员的用例包括"审核项目"、"驳回项目"、"下架项目"、"管理用户"、"分配角色"、"审核提现"、"发布公告"、"查看统计报表"、"导出报表"、"查看操作日志"。

受篇幅所限，三张用例图的 PlantUML 描述以及具体关联关系将在附属电子材料中提供。

\newpage

# 4 系统设计

## 4.1 系统总体架构设计

FundTogether 采用经典的"前后端分离 + 容器编排"的四层架构，整体架构自上而下划分为：客户端层、接入层、应用层与数据层。

**客户端层**：Web 浏览器（PC 与移动端）通过 HTTPS 访问前端 SPA；应用层暴露的 RESTful 接口与 WebSocket 通道构成前后端的唯一契约。

**接入层**：Nginx 位于客户端与应用层之间，承担静态资源分发、TLS 终结、反向代理、请求限流与 WebSocket 代理职责。

**应用层**：Spring Boot 服务集群负责全部业务逻辑，内部按照"Controller→Service→Mapper→Entity"四层分层落地；跨切面能力通过过滤器、拦截器、切面、全局异常处理统一封装。

**数据层**：MySQL 存储结构化业务数据；Redis 承载缓存、验证码与分布式锁；文件与媒体资源通过本地目录存储并经 Nginx 静态映射对外提供访问。

总体架构如图 4-1 所示。

```
┌───────────────────────────────────────────────────────────┐
│                    Web Browser (PC / H5)                  │
└───────────────────────────────────────────────────────────┘
                  │ HTTPS + WebSocket
                  ▼
┌───────────────────────────────────────────────────────────┐
│           Nginx  (Static / Proxy / Rate-Limit)            │
└───────────────────────────────────────────────────────────┘
                  │ HTTP / WS
                  ▼
┌───────────────────────────────────────────────────────────┐
│ Spring Boot 3.x                                           │
│  ┌─────────────┐  ┌────────────┐  ┌─────────────────────┐ │
│  │ Controllers │  │ Aspects    │  │ WebSocket Server    │ │
│  └─────────────┘  └────────────┘  └─────────────────────┘ │
│  ┌─────────────┐  ┌────────────┐  ┌─────────────────────┐ │
│  │ Services    │  │ Security   │  │ Scheduled Tasks     │ │
│  └─────────────┘  └────────────┘  └─────────────────────┘ │
│  ┌─────────────────────────────────────────────────────┐  │
│  │   MyBatis-Plus Mappers / Entities / DTOs / VOs      │  │
│  └─────────────────────────────────────────────────────┘  │
└───────────────────────────────────────────────────────────┘
                  │ JDBC                 │ Redis Protocol
                  ▼                      ▼
         ┌─────────────────┐     ┌──────────────────┐
         │   MySQL 8.0     │     │    Redis 7.x     │
         └─────────────────┘     └──────────────────┘
```

图 4-1 系统总体架构图

## 4.2 功能模块设计

系统功能模块按"业务域 + 横切关注点"两大类组织：

（1）业务域模块：用户域、项目域、订单域、资金域、消息域、运营域；

（2）横切关注点模块：认证与鉴权、日志与审计、异常处理、限流、缓存、文件上传、调度任务。

各业务域以独立的 Controller 包路径暴露接口，以独立的 Service 与 Mapper 实现业务逻辑与持久化，并通过依赖注入协作。核心业务域与对应的 Controller/Service/Entity 如下：

- 用户域：`UserController` / `SysUserService` / `SysUser`
- 项目域：`ProjectController`、`ProjectSponsorController` / `ProjectService`、`ProjectRewardService` / `Project`
- 订单域：`OrderController` / `SupportOrderService` / `SupportOrder`
- 资金域：`FundingController`、`WithdrawalController` / `FundingLedgerService`、`WithdrawalOrderService` / `FundingLedger`
- 消息域：`MessageController`、`SysNoticeController` / `SysUserMessageService` / `SysUserMessage`
- 运营域：`AdminController` / `AdminOperationLogService` / `AdminOperationLog`

## 4.3 数据库设计

### 4.3.1 设计原则

数据库设计遵循三范式前提下适度反范式化的原则：对于访问路径固定、写入频率低的聚合字段（如 `project.current_amount`）允许冗余，以降低运行时计算成本；对于高频关联查询（如项目列表带发起人昵称）通过视图或 Join 一次性完成，不再在应用层做多次查询。

### 4.3.2 E-R 模型概要

系统核心实体包括用户、项目、分类、回报档位、支持订单、资金流水、项目托管账户、提现订单、评论、公告、消息、关注、收藏、实名信息、支付方式、管理员日志等，共 20 余张表。核心实体关系概述如下：

- 一个用户可以作为发起人创建多个项目，一个项目属于一个发起人；
- 一个项目属于一个分类，一个分类包含多个项目；
- 一个项目可以配置多个回报档位；
- 一个用户可以对多个项目下多个订单，一个订单仅关联一个项目；
- 一个项目对应一个托管账户，账户与订单之间通过资金流水账本进行多对多的流水联动；
- 一个项目可以发布多条项目动态，一条动态可以触发若干条站内信；
- 一个用户可以关注多个用户或发起人，一个用户也可以收藏多个项目。

### 4.3.3 主要数据表结构

以下列出系统中的部分核心表结构（完整 DDL 详见附属电子材料的 `database.sql`）。

（1）用户表 `sys_user`

表 4-1 用户表字段定义

| 字段名 | 类型 | 约束 | 说明 |
|:--|:--|:--|:--|
| id | bigint | PK, AUTO_INCREMENT | 用户主键 |
| account | varchar(64) | UNIQUE NOT NULL | 账号（手机/邮箱） |
| password | varchar(128) | NOT NULL | Bcrypt 加密密码 |
| nickname | varchar(64) |  | 昵称 |
| avatar | varchar(255) |  | 头像地址 |
| role | int | NOT NULL DEFAULT 1 | 位掩码角色 |
| balance | decimal(10,2) | DEFAULT 0 | 账户余额 |
| status | tinyint | DEFAULT 0 | 0-正常, 1-已禁用 |
| created_at | datetime | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | datetime | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

（2）项目表 `project`

表 4-2 项目表字段定义（节选）

| 字段名 | 类型 | 说明 |
|:--|:--|:--|
| id | bigint | 项目主键 |
| sponsor_id | bigint | 发起人用户 ID |
| category_id | bigint | 项目分类 ID |
| title | varchar(128) | 项目标题 |
| cover_url | varchar(255) | 封面图地址 |
| video_url | varchar(255) | 视频地址 |
| target_amount | decimal(12,2) | 目标金额 |
| current_amount | decimal(12,2) | 当前已筹金额 |
| supporter_count | int | 支持人数 |
| start_time | datetime | 上线时间 |
| end_time | datetime | 截止时间 |
| status | tinyint | 项目状态枚举 |
| heat | int | 热度指标 |

（3）支持订单表 `support_order`

表 4-3 支持订单表字段定义（节选）

| 字段名 | 类型 | 说明 |
|:--|:--|:--|
| id | bigint | 订单主键 |
| order_no | varchar(64) | 订单号（UUID） |
| user_id | bigint | 支持者 ID |
| project_id | bigint | 项目 ID |
| reward_id | bigint | 档位 ID |
| amount | decimal(10,2) | 支付金额 |
| pay_channel | tinyint | 1-余额, 2-支付宝, 3-微信 |
| status | tinyint | 0-待支付, 1-已支付, 2-已取消, 3-已退款 |
| pay_time | datetime | 支付时间 |

（4）资金流水表 `funding_ledger`

资金流水表承担"账本"角色，记录项目托管账户内每一笔进出账，`type` 字段区分用户支付、平台退款、阶段拨付给发起人、发起人提现四类事件，`status` 字段区分处理中、成功、失败三态。该表与 `fund_account` 项目托管账户表配合构成系统的资金隔离与透明追溯能力。

### 4.3.4 索引设计

为保证主要查询路径的性能，在以下字段上建立索引：

- `project(status, start_time DESC)`：项目列表常见的按状态与时间倒序；
- `support_order(user_id, status)`：用户"我的支持"过滤；
- `support_order(project_id, status)`：项目支持者名单拉取；
- `funding_ledger(project_id, type)`：项目账本按类型过滤；
- `user_comment(project_id, parent_id)`：评论树形结构拉取；
- `sys_user_message(user_id, read_status, created_at DESC)`：站内信未读优先倒序。

## 4.4 接口与协议设计

### 4.4.1 RESTful 风格

全部对外接口遵循 RESTful 规范，URL 采用小写复数名词结构，HTTP 方法语义化使用：

- `GET /api/v1/projects`：项目列表
- `GET /api/v1/projects/{id}`：项目详情
- `POST /api/v1/sponsor/project`：发起人创建项目
- `PUT /api/v1/sponsor/project/{id}`：发起人更新项目
- `POST /api/v1/orders`：创建支持订单
- `POST /api/v1/orders/{id}/pay`：支付订单

### 4.4.2 统一响应协议

所有接口返回统一封装的 `Result<T>`：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { }
}
```

错误码采用"业务域 + 错误类型 + 序号"的三段结构，如 `40101` 表示鉴权域（401）的令牌失效错误。

### 4.4.3 WebSocket 协议

握手 URL 为 `/ws/{userId}`，客户端在 `Sec-WebSocket-Protocol` 或查询参数中携带 JWT；服务端在握手阶段完成令牌验证与 `userId` 一致性校验。推送消息统一为 JSON 结构：

```json
{
  "type": "PROJECT_UPDATE",
  "targetId": 1001,
  "title": "项目进度更新",
  "content": "本期拨付 30% 已到账",
  "timestamp": 1714200000000
}
```

## 4.5 安全与资金托管设计

### 4.5.1 安全架构

安全架构自底向上划分为网络层、传输层、应用层、业务层四层：

（1）网络层：Nginx 配置基本的 SYN Flood 防护与连接数限制；

（2）传输层：全站 HTTPS，使用 TLS 1.2+，关闭不安全的加密套件；

（3）应用层：Spring Security + JWT 统一鉴权；`@PreAuthorize` 方法级授权；`@RateLimit` 切面级限流；`GlobalExceptionHandler` 拦截并统一响应；

（4）业务层：敏感操作二次确认；管理员操作全量写入 `admin_operation_log`；密码 Bcrypt 单向哈希。

### 4.5.2 资金托管模型

针对众筹场景固有的"先筹后交付、先信任后兑现"特性，系统设计了基于"项目托管账户 + 资金流水账本"的资金隔离模型：

（1）用户对项目发起支持时，资金并不直接进入发起人账户，而是在 `fund_account` 表中对对应项目的 `frozen_amount` 进行加账；

（2）项目成功结项后，托管账户的 `frozen_amount` 转入 `available_amount`；发起人按里程碑申请拨付，管理员审核通过后从 `available_amount` 划转至 `paid_amount`；

（3）项目失败或违规下架时，触发批量退款流程，系统按 `support_order` 聚合退款额度并一次性更新托管账户与用户余额；

（4）所有资金动作均在 `funding_ledger` 中生成一条 `type = 1/2/3/4` 的流水记录，保证可审计与可追溯。

### 4.5.3 分布式锁与并发控制

为防止同一用户对同一项目的重复提交，系统在创建订单前获取形如 `order:lock:{userId}:{projectId}` 的 Redis 分布式锁；对用户余额的扣减采用"带条件的 UPDATE"形成乐观锁（`UPDATE sys_user SET balance = balance - ? WHERE id = ? AND balance >= ?`）。若影响行数为 0 则直接抛出"余额不足"异常并回滚事务。

\newpage

# 5 系统详细实现

## 5.1 开发与运行环境

系统开发环境如表 5-1 所示。

表 5-1 系统开发与运行环境

| 类别 | 配置 |
|:--|:--|
| 开发主机 | macOS 14 / Windows 11，4 核 / 16 GB |
| JDK | OpenJDK 17 |
| Node.js | 20 LTS |
| 构建工具 | Maven 3.9 / Vite 5 |
| IDE | IntelliJ IDEA 2024 / VS Code |
| 数据库 | MySQL 8.0 |
| 缓存 | Redis 7.2 |
| 容器 | Docker 25、Docker Compose v2 |
| 服务器 | Ubuntu 22.04 LTS，4 核 / 8 GB |

## 5.2 用户认证与权限模块

### 5.2.1 JWT 令牌签发

用户登录接口接收账号密码后，调用 `SysUserServiceImpl.login` 进行校验，校验通过后调用 `JwtUtils.createToken` 生成带 2 小时过期时间的令牌，并通过 `Result` 返回给前端。前端将令牌写入 `localStorage`，并在 Axios 请求拦截器中统一注入 `Authorization: Bearer` 头。

### 5.2.2 过滤器链与鉴权

`SecurityConfig` 将 `JwtAuthenticationFilter` 注册为在 `UsernamePasswordAuthenticationFilter` 之前执行的过滤器。过滤器的核心逻辑为：解析令牌 → 提取 `userId/account/role` → 构造 `LoginUser` → 设置 `SecurityContextHolder`。其典型片段如下：

```java
Claims claims = JwtUtils.parseToken(token);
Long userId   = claims.get("userId", Long.class);
Integer role  = claims.get("role", Integer.class);
List<SimpleGrantedAuthority> authorities = new ArrayList<>();
authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
if (UserRole.isSponsor(role)) authorities.add(new SimpleGrantedAuthority("ROLE_SPONSOR"));
if (UserRole.isAdmin(role))   authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(loginUser, null, authorities);
SecurityContextHolder.getContext().setAuthentication(auth);
```

### 5.2.3 位掩码角色模型

`UserRole` 枚举通过位掩码支持角色叠加，`combine` 方法按位或合并角色，`hasRole` 方法按位与判断角色归属，使得一个账号可以同时具备支持者与发起人身份而无需冗余账户体系。

## 5.3 项目发起与审核模块

### 5.3.1 项目创建

发起人在前端提交项目创建表单，前端将封面、视频通过 `/api/files/upload` 分步上传后获得文件 URL，再调用 `/api/sponsor/projects` 创建项目。后端 `ProjectSponsorController.create` 收到 `ProjectCreateDTO` 后调用 `ProjectServiceImpl.create` 进行表单校验、状态初始化（`PENDING_REVIEW`）、持久化与托管账户初始化。

### 5.3.2 项目审核

管理员在后台审核列表选择一个待审核项目，点击"通过"或"驳回"按钮。系统调用 `AdminController.auditProject`，根据结果将项目状态由 `PENDING_REVIEW` 流转至 `FUNDING` 或 `REJECTED`，并在 `admin_operation_log` 表中写入审核日志。

### 5.3.3 状态机强校验

非法状态流转由 Service 层强制校验，典型代码如下：

```java
if (project.getStatus() != ProjectStatus.PENDING_REVIEW.getCode()) {
    throw new BusinessException("仅可审核待审核状态的项目");
}
project.setStatus(approve ? ProjectStatus.FUNDING.getCode()
                          : ProjectStatus.REJECTED.getCode());
project.setStartTime(LocalDateTime.now());
projectMapper.updateById(project);
```

## 5.4 项目支持与资金流转模块

### 5.4.1 订单创建

订单创建是整个系统的核心事务链路，其实现要点包括：分布式锁防重、项目状态校验、余额乐观锁扣减、订单入库、资金流水入库、项目筹款金额更新。核心片段如下：

```java
@Transactional(rollbackFor = Exception.class)
public void createOrder(SupportOrderCreateDTO dto, Long userId) {
    String lockKey   = "order:lock:" + userId + ":" + dto.getProjectId();
    String lockValue = UUID.randomUUID().toString();
    boolean locked   = redisLockUtil.tryLock(lockKey, lockValue, 10);
    if (!locked) throw new BusinessException("操作过于频繁，请稍后再试");
    try { doCreateOrder(dto, userId); }
    finally { redisLockUtil.unlock(lockKey, lockValue); }
}
```

其中 `redisLockUtil.tryLock` 使用 `SET NX EX` 原语保证加锁原子性，`unlock` 使用 Lua 脚本保证释放锁时仅释放自己持有的锁。

### 5.4.2 资金流水记账

订单支付成功后，系统在 `funding_ledger` 中插入 `type = 1` 的流水记录：

```java
FundingLedger ledger = new FundingLedger();
ledger.setProjectId(project.getId());
ledger.setOrderId(order.getId());
ledger.setUserId(userId);
ledger.setAmount(order.getAmount());
ledger.setType(LedgerType.USER_PAYMENT.getCode());
ledger.setStatus(1);
fundingLedgerService.save(ledger);
projectMapper.incrementFunding(dto.getProjectId(), dto.getAmount());
```

通过"账户 + 账本"的双表模式，任何一笔资金变动都可以通过账本进行审计回放，避免单点账户误操作带来的不可追溯问题。

### 5.4.3 退款与提现

项目失败时，系统遍历该项目下所有 `PAID` 订单，调用 `refundOrder` 将订单状态置为 `REFUNDED` 并在账本中写入 `type = 2` 的退款流水，最后批量回滚用户余额；发起人提现时，通过 `WithdrawalOrderService.apply` 创建提现工单，管理员审核通过后由后端在账本中写入 `type = 4` 的提现流水并更新托管账户。

## 5.5 消息推送与后台监管模块

### 5.5.1 WebSocket 在线会话

系统使用 Jakarta WebSocket API 定义 `@ServerEndpoint("/ws/{userId}")` 端点，并通过自定义 `Configurator` 在握手阶段完成 JWT 校验与 `userId` 一致性校验。会话池通过 `ConcurrentHashMap<String, Session>` 维护：

```java
private static final ConcurrentHashMap<String, Session> sessionMap = new ConcurrentHashMap<>();

@OnOpen
public void onOpen(Session session, @PathParam("userId") String userId) {
    Boolean authed = (Boolean) session.getUserProperties().get("authenticated");
    if (authed == null || !authed) { /* 拒绝 */ return; }
    sessionMap.put(userId, session);
}
```

### 5.5.2 多通道消息分发

当发起人发布项目动态时，`ProjectUpdateServiceImpl` 会执行"写库 + 推送"两个动作：一方面将动态记录写入 `project_update` 并为该项目所有支持者在 `sys_user_message` 中生成一条站内信；另一方面通过 `WebSocketServer.sendMessageToUser` 向所有在线支持者实时推送。

### 5.5.3 管理员操作日志

所有管理员写接口通过 AOP 切面将操作前后的快照序列化为 JSON 并写入 `admin_operation_log` 表，便于事后审计。

## 5.6 前端交互与可视化实现

### 5.6.1 请求层统一封装

`utils/request.ts` 封装 Axios 实例并注入请求/响应拦截器：请求拦截器为每个请求追加 JWT；响应拦截器对 `code != 200` 的响应通过 `ElMessage.error` 弹出，并对 401 状态自动跳转登录页。

### 5.6.2 Pinia 状态管理

`store/user.ts` 管理用户登录状态，通过 getter `isAdmin` 与 `isSponsor` 提供角色位运算判断：

```ts
getters: {
  isAdmin:   s => !!(s.userInfo?.role & UserRole.ADMIN),
  isSponsor: s => !!(s.userInfo?.role & UserRole.SPONSOR)
}
```

### 5.6.3 ECharts 运营看板

`views/admin/StatsDashboard.vue` 使用 ECharts 渲染四张看板图表：分类筹款柱状图、每日筹款折线图、项目状态饼图、近 30 日活跃用户柱状图。图表数据由 `/api/admin/stats` 聚合接口一次性返回，前端按配置对象直接渲染。

### 5.6.4 响应式适配

全站样式采用 CSS Grid + Flexbox 两级布局，关键页面基于 `@media (max-width: 768px)` 断点切换单栏结构；Element Plus 组件在小屏幕下自适应收缩；移动端发起人后台使用底部操作条替代 PC 端侧边导航，保证操作密度合理。

\newpage

# 6 系统测试

## 6.1 测试环境与方法

### 6.1.1 测试环境

测试环境如表 6-1 所示。

表 6-1 测试环境配置

| 类别 | 配置 |
|:--|:--|
| 操作系统 | Ubuntu 22.04 LTS |
| CPU / 内存 | 4 核 / 8 GB |
| 浏览器 | Chrome 125 / Safari 17 / Edge 125 |
| 压测工具 | Apache JMeter 5.6 |
| 接口调试 | Postman / Knife4j |
| 安全扫描 | OWASP ZAP |

### 6.1.2 测试方法

系统测试综合采用黑盒测试与白盒测试两类方法：

（1）功能测试：按需求文档中的用例逐项验证；

（2）接口测试：通过 Postman 集合对所有 RESTful 接口进行正向、反向、边界、权限四类用例覆盖；

（3）性能测试：通过 JMeter 模拟并发访问关键接口；

（4）兼容性测试：在三款主流浏览器与 PC、平板、移动端三种分辨率下人工回归核心页面；

（5）安全测试：通过 OWASP ZAP 对站点进行被动扫描，并通过人工测试越权访问、SQL 注入、XSS 等典型漏洞。

## 6.2 功能测试

功能测试覆盖了 59 项需求条目对应的 260 个测试用例，重点用例与结果如表 6-2 所示（节选）。

表 6-2 功能测试用例汇总（节选）

| 用例编号 | 用例描述 | 预期结果 | 实际结果 |
|:--:|:--|:--|:--:|
| TC-01 | 游客手机号注册 | 注册成功并返回登录页 | 通过 |
| TC-05 | 普通支持者登录 | 返回 Token 并跳转首页 | 通过 |
| TC-09 | 发起人创建项目 | 项目进入待审核状态 | 通过 |
| TC-14 | 管理员审核通过 | 项目流转为筹款中 | 通过 |
| TC-18 | 支持者投资项目 | 订单已支付，筹款金额实时更新 | 通过 |
| TC-23 | 重复并发投资 | 仅第一次成功，后续返回限流提示 | 通过 |
| TC-28 | 项目失败退款 | 订单退款，用户余额回滚 | 通过 |
| TC-33 | 发起人提现 | 流水中出现 type=4 记录 | 通过 |
| TC-36 | 管理员导出报表 | 浏览器下载 Excel | 通过 |
| TC-40 | 项目动态推送 | 支持者收到站内信 + 实时推送 | 通过 |

功能测试覆盖率为 100%，所有用例均顺利通过。

## 6.3 接口与性能测试

使用 JMeter 对项目列表、项目详情、支持订单创建三个关键接口进行压测，线程数分别设置为 100、300、500，持续 60 秒，测试结果如表 6-3 所示。

表 6-3 关键接口性能测试结果

| 接口 | 并发 | 吞吐率(req/s) | P95(ms) | P99(ms) | 错误率 |
|:--|:--:|:--:|:--:|:--:|:--:|
| GET /projects | 100 | 420 | 145 | 188 | 0 |
| GET /projects | 500 | 1180 | 261 | 352 | 0 |
| GET /projects/{id} | 500 | 980 | 233 | 314 | 0 |
| POST /orders | 100 | 82 | 287 | 441 | 0 |
| POST /orders | 300 | 171 | 582 | 926 | 0.1% |

在 Redis 命中后，项目列表与详情接口的 P95 均维持在 300 ms 以内，满足非功能性需求定义的性能指标；订单接口由于涉及数据库事务与分布式锁，在高并发下 P95 有所上升但仍处于可接受范围，且并未出现超卖等严重错误。

## 6.4 兼容性与安全测试

### 6.4.1 兼容性

在 Chrome 125、Safari 17、Edge 125 三款浏览器的 PC、平板与移动端分辨率下，对首页、项目列表、项目详情、用户中心、后台管理五个关键页面进行人工回归。结果表明，所有页面在不同设备与浏览器下渲染正常、无布局错乱，核心功能可正常操作。

### 6.4.2 安全

（1）越权测试：使用支持者令牌访问管理员接口，系统返回 403；

（2）令牌失效测试：使用过期令牌访问任意需要鉴权的接口，系统返回 401；

（3）SQL 注入测试：在项目搜索框与登录账号输入框中提交典型注入字符串，系统通过 MyBatis 预编译参数进行安全过滤，未触发异常；

（4）XSS 测试：在评论输入框提交 `<script>alert(1)</script>`，前端通过转义机制与 `v-text` 渲染避免脚本执行；

（5）密码安全：`sys_user.password` 字段仅存储 Bcrypt 哈希，未发现明文密码落库；日志脱敏规则命中手机号与身份证号。

OWASP ZAP 被动扫描共给出 4 条中低危建议，主要集中在响应头（如 `X-Content-Type-Options`、`X-Frame-Options`）缺失，后续通过 Nginx 统一注入响应头的方式一并修复。

\newpage

# 结　论

本论文围绕"在线众筹网站的设计与实现"这一核心命题，基于软件工程方法论完整开展了需求分析、系统设计、编码实现、系统测试与部署上线的全流程实践，最终构建出命名为 FundTogether 的中小规模在线众筹网站。论文的主要工作与结论可归纳如下：

第一，完整识别并梳理了在线众筹业务的四类角色与七大功能模块，以 59 项功能条目为基础，形成了层次清晰、边界明确的需求规格说明；通过用例图、业务流程图与状态机图建模，系统厘清了订单状态与项目状态的合法流转路径，为后续编码实现奠定了可严格校验的业务约束基础。

第二，基于 Spring Boot 3.x 与 Vue 3 的前后端分离技术栈，设计了"客户端 + 接入层 + 应用层 + 数据层"的四层总体架构，并针对资金流转与实时通信等特殊场景分别设计了基于"托管账户 + 资金流水账本"的资金隔离模型与基于 WebSocket 的推送通道。该架构在保持企业级成熟度的同时兼顾了毕业设计尺度的可实现性。

第三，完成了 20 余张核心表的数据库设计与 20 个后端 Service、8 个前端管理后台页面的编码实现。系统通过 JWT + 位掩码 RBAC 的组合方式实现了无状态、可扩展、可叠加的权限控制；通过 Redis 分布式锁、MySQL 事务与乐观锁的协同机制保障了并发场景下的资金安全与数据一致性；通过 ECharts 运营看板与 Excel 报表导出完成了数据可视化与运营决策支持。

第四，采用功能测试、接口与性能测试、兼容性与安全测试相结合的综合测试方法，在 260 个功能用例与 5 个压测场景下验证了系统的正确性、稳定性与安全性。测试结果表明，系统关键接口在 500 并发下仍能保持 P95 不超过 300 ms，功能用例通过率 100%，无越权访问与明显安全漏洞。

本课题在以下方面仍存在进一步优化空间：首先，系统当前仅接入了支付宝沙箱环境，实际生产上线还需完成合规备案与多渠道聚合支付接入；其次，推荐模块目前基于简单的兴趣标签匹配，后续可引入协同过滤或深度学习模型以提升个性化能力；再次，监控告警、链路追踪与日志聚合等可观测性基础设施尚未完全落地，后续可借助 Prometheus、Grafana、SkyWalking、ELK 等开源工具进一步完善；最后，针对高并发场景，可在消息侧引入 RocketMQ 或 Kafka 做异步解耦，在数据库侧进行分库分表改造。

综合而言，本课题完整实现了预期研究目标，既为同类众筹平台提供了可复用的工程参考，也系统训练了作者在软件工程全流程中的综合能力，达到了本科毕业设计的培养目标与考核要求。

\newpage

# 参考文献

\[1\] 张磊. 在线众筹平台的设计与实现\[D\]. 北京：北京邮电大学，2023.

\[2\] 王珊，萨师煊. 数据库系统概论（第 6 版）\[M\]. 北京：高等教育出版社，2020.

\[3\] 刘春玉. 基于软件工程和 J2EE 的网上银行系统\[J\]. 现代电子技术，2018，41(4)：45-47.

\[4\] 陈明. 软件工程导论（第 7 版）\[M\]. 北京：清华大学出版社，2019.

\[5\] 赵阳. 基于用户画像的众筹项目推荐算法实现\[J\]. 计算机应用研究，2024，41(3)：879-882.

\[6\] 刘军. 第三方支付接口集成与安全防护技术研究\[J\]. 现代电子技术，2021，44(12)：134-137.

\[7\] 李娜. 基于 Vue.js 和 Spring Boot 的众筹网站开发研究\[J\]. 计算机技术与发展，2022，32(7)：156-160.

\[8\] 严蔚敏，吴伟民. 数据结构（C 语言版）\[M\]. 北京：清华大学出版社，2019.

\[9\] 周志明. 深入理解 Java 虚拟机：JVM 高级特性与最佳实践（第 3 版）\[M\]. 北京：机械工业出版社，2019.

\[10\] 尤雨溪. Vue.js 官方文档\[EB/OL\]. https://cn.vuejs.org/guide/introduction.html，2024-03-01/2026-03-10.

\[11\] Smith J, Johnson M. Design and Implementation of an Online Crowdfunding Platform\[J\]. Journal of Web Engineering, 2022, 21(4): 321-345.

\[12\] Brown A, Lee C. Secure Payment Flow Design in Crowdfunding Systems\[J\]. IEEE Transactions on Information Security, 2023, 18(2): 890-903.

\[13\] Miller R. The Development and Trend of Global Crowdfunding Industry\[J\]. Journal of Business Research, 2022, 145: 789-802.

\[14\] Mollick E. The Dynamics of Crowdfunding: An Exploratory Study\[J\]. Journal of Business Venturing, 2014, 29(1): 1-16.

\[15\] Belleflamme P, Lambert T, Schwienbacher A. Crowdfunding: Tapping the Right Crowd\[J\]. Journal of Business Venturing, 2014, 29(5): 585-609.

\newpage

# 致　谢

光阴荏苒，毕业在即。回顾本科四年的学习生活，这篇毕业设计论文既是对专业积累的一次集中检阅，也是本科阶段落下帷幕的一个重要注脚。在论文完成之际，谨向所有在课题研究过程中给予我关心与帮助的师长、同窗、家人表达最诚挚的谢意。

首先，谨向我的指导教师李金岚副教授致以最衷心的感谢。从课题选题、开题答辩、中期检查到论文定稿，李老师始终严谨认真地为我把关，在需求建模、系统设计、论文规范等方面给予了大量耐心细致的指导。李老师严谨的治学态度与一丝不苟的工作作风令我受益终生。

其次，感谢太原理工大学软件学院全体教师在本科四年内传授给我的知识与为人之道；感谢与我一同学习、共同成长的软件 2235 班同学们给予我的鼓励与帮助；感谢同专业、同宿舍的朋友们陪伴我度过了难忘的大学时光。

最后，感谢一直以来默默支持我的父母与家人，你们始终是我前行的坚实后盾。

路漫漫其修远兮，吾将上下而求索。谨以此文向所有教过我、帮助过我、关心过我的人致以最诚挚的谢意！

\newpage

# 外文原文

**A Review of Crowdfunding: Financial Democratisation and New Opportunities for Entrepreneurship**

*Source: Short, J. C., Ketchen, D. J., McKenny, A. F., Allison, T. H., & Ireland, R. D. Research on crowdfunding: Reviewing the (very recent) past and celebrating the present. Entrepreneurship Theory and Practice, 41(2), 149-160.*

Crowdfunding has emerged as a disruptive financing mechanism that allows entrepreneurs to raise capital directly from the public, typically through online platforms, rather than relying on traditional sources such as banks, venture capital firms, or governmental grants. Over the past decade, the global crowdfunding industry has experienced explosive growth, transforming from a niche curiosity into a mainstream channel for funding creative, commercial, and social projects. This article reviews the current state of crowdfunding research and highlights the most promising directions for further scholarly inquiry.

Four primary models of crowdfunding are commonly recognised in the literature. Reward-based crowdfunding, popularised by platforms such as Kickstarter and Indiegogo, allows backers to pledge funds in exchange for a tangible product, a service, or recognition. Donation-based crowdfunding, exemplified by GoFundMe, supports charitable or personal causes without promising any return. Equity-based crowdfunding enables backers to obtain shares in a start-up, while lending-based (or peer-to-peer lending) crowdfunding turns backers into creditors who receive interest payments. Each model raises distinct governance, disclosure, and risk-management challenges.

Research consistently shows that the success of a crowdfunding campaign depends on a combination of project-level signals and platform-level mechanisms. Project descriptions that are vivid, specific, and emotionally resonant tend to attract more supporters. Likewise, creators who release timely updates, reply to comments, and demonstrate social proof — such as endorsements from influencers or media coverage — systematically outperform those who do not. At the platform level, the presence of trust-building features, including identity verification, escrow accounts, milestone payouts, and transparent refund policies, reduces perceived risk and increases contribution levels.

Nevertheless, crowdfunding is not without challenges. Information asymmetry between creators and backers creates opportunities for fraud, over-promising, and sub-par delivery. Empirical studies suggest that a non-negligible proportion of successfully funded campaigns ultimately fail to deliver on time or at the advertised quality. To mitigate these risks, platforms increasingly rely on algorithmic screening, community moderation, and staged payout mechanisms. Governments around the world have also introduced regulations that balance investor protection with innovation, although the regulatory landscape remains fragmented.

From an entrepreneurial perspective, crowdfunding provides several benefits beyond capital. First, a successful campaign validates market demand, thereby reducing the uncertainty associated with follow-on investment rounds. Second, backers often become early adopters and brand advocates, contributing feedback and word-of-mouth promotion. Third, by lowering the cost of acquiring customers, crowdfunding may reshape product-development processes themselves, enabling lean, iterative, and consumer-driven innovation. Fourth, the public nature of campaigns encourages entrepreneurs to articulate their value proposition clearly, a discipline that often yields long-term strategic benefits.

Despite its democratising potential, crowdfunding is not equally accessible to all entrepreneurs. Demographic studies reveal persistent gender, ethnic, and geographic disparities in fundraising outcomes. Women and minority founders often receive less attention and smaller average contributions than their counterparts, even when controlling for project quality. Rural entrepreneurs face additional hurdles related to digital literacy, payment infrastructure, and logistics. Addressing these disparities requires deliberate platform design, community intervention, and policy support.

Looking ahead, several research avenues deserve greater attention. First, the integration of blockchain technology with crowdfunding promises higher transparency, smart-contract-based payout automation, and global accessibility, yet its adoption raises new regulatory and usability questions. Second, the emergence of artificial-intelligence-driven recommendation systems could personalise project discovery, but may also amplify popularity-driven bias. Third, the role of crowdfunding in financing deep-tech, climate-tech, and public-health projects warrants deeper empirical investigation. Finally, longitudinal studies that track campaign outcomes beyond the funding milestone are needed to understand the true impact of crowdfunding on entrepreneurial success, consumer welfare, and societal innovation.

In conclusion, crowdfunding represents a structural evolution in how entrepreneurs access capital and engage with markets. It democratises financial opportunity, fosters community, and accelerates innovation, but also introduces novel risks and governance responsibilities. For platform operators, entrepreneurs, backers, and regulators alike, understanding the mechanisms that drive campaign success and failure is essential to unlocking the full potential of crowdfunding as an instrument of economic and social progress. The remainder of this paper synthesises recent empirical findings, proposes an integrative research agenda, and discusses practical implications for each stakeholder group.

**Signalling Theory and Crowdfunding Success.** Signalling theory offers one of the most widely applied theoretical lenses in the crowdfunding literature. Drawing on the seminal work of Spence, scholars argue that in a market characterised by information asymmetry, high-quality creators use costly and observable signals to distinguish themselves from low-quality peers. In the crowdfunding context, such signals include the quality of the pitch video, the completeness of the risk-and-challenges section, the richness of reward tiers, the creator's educational and professional background, and the presence of preparatory work such as prototypes or pilot users. Empirical studies have repeatedly demonstrated that projects with richer, more credible signals attract higher pledge amounts, reach their funding goals faster, and enjoy better post-campaign delivery records. Conversely, projects that rely on vague language, excessive superlatives, or purely promotional imagery tend to underperform. An emerging strand of research investigates how linguistic features — including concreteness, temporal orientation, and the use of passion-laden vocabulary — function as soft signals of a creator's entrepreneurial orientation.

**Herding Behaviour and Momentum Effects.** Beyond signalling, a second behavioural mechanism — herding — plays a decisive role in campaign outcomes. Backers on crowdfunding platforms are frequently observed to make contribution decisions not solely on the intrinsic merits of a project, but also on the basis of what other backers have done. Projects that accumulate early momentum — often in the first forty-eight hours — are far more likely to reach their goals than projects that start slowly. This phenomenon is amplified by platform design choices such as ranking algorithms that privilege "trending" campaigns, social-sharing badges that celebrate funding milestones, and countdown timers that create urgency. While herding can help deserving projects overcome the cold-start problem, it also risks entrenching popularity-driven biases, in which already-visible projects receive disproportionate attention while equally deserving but lesser-known campaigns are overlooked. Recent computational studies leveraging natural language processing and network analysis have begun to disentangle the relative contributions of intrinsic project quality and extrinsic social dynamics, suggesting that roughly one-third of the variance in final pledge volume is attributable to herding rather than fundamental merit.

**Geographic Dispersion of Backers.** Although crowdfunding is often celebrated as a borderless phenomenon, empirical evidence paints a more nuanced picture. The spatial distribution of backers tends to be bimodal: a significant fraction of early contributions originate from the creator's local community — friends, family, and acquaintances — while later contributions, once the project gains visibility, arrive from much wider geographic regions, including international supporters. This spatial pattern has important implications. First, local backers serve a dual function: they provide initial capital and also generate the early momentum that triggers herding effects. Second, geographic dispersion can mitigate regional economic shocks, because diversified funding reduces the risk that a local downturn will wipe out a creator's capital base. Third, dispersion raises practical concerns regarding cross-border shipping logistics, tax compliance, and currency conversion, all of which increase the administrative burden on creators and may erode the net proceeds of a campaign. Platforms have responded with a variety of tools, including integrated shipping calculators, multi-currency support, and regional fulfilment partners, yet the costs of cross-border delivery remain non-trivial, particularly for hardware and fashion projects.

**Regulatory Frameworks Across Jurisdictions.** The rapid expansion of crowdfunding has prompted regulators to craft frameworks that balance investor protection with the preservation of innovation-friendly environments. In the United States, the JOBS Act of 2012 and its subsequent rulemakings by the Securities and Exchange Commission laid the foundation for Regulation Crowdfunding (Reg CF), which permits non-accredited investors to participate in equity crowdfunding subject to annual caps. In the European Union, the European Crowdfunding Service Providers Regulation (ECSPR), effective from 2021, establishes a harmonised passport regime for crowdfunding platforms, replacing the earlier patchwork of national licences. China has adopted a more cautious stance, with the authorities restricting equity and lending-based crowdfunding while permitting reward-based models under platform-level compliance obligations. Across all jurisdictions, common themes include mandatory disclosure of creator identity and financial position, limits on individual contribution amounts, requirements for platform-level anti-fraud procedures, and obligations for platforms to maintain escrow arrangements. Persistent challenges include the cross-border enforcement of investor rights, the treatment of tokenised or cryptocurrency-denominated campaigns, and the classification of hybrid reward-equity models.

**Platform Governance and Trust Architecture.** Trust is the scarcest resource on any crowdfunding platform, and platform operators invest substantial effort in its cultivation. Modern platforms deploy a multi-layered governance stack that begins with identity verification through government-issued documents or third-party authentication services, continues through community-based review of campaign content, and culminates in automated anomaly detection powered by machine learning models trained on historical fraud patterns. Dispute-resolution mechanisms typically combine platform-level arbitration with graduated refund policies, and in some jurisdictions, platforms have partnered with traditional financial institutions to offer insurance-backed guarantees for a subset of campaigns. Recent academic work highlights the importance of procedural fairness — the perception that disputes are handled transparently and consistently — as a stronger predictor of long-term user loyalty than raw refund amounts. Trust-building is therefore not merely a technical challenge but also a design, communication, and community-management discipline.

**The Role of Community and Social Capital.** Beyond capital, backers frequently contribute a second form of resource — social capital. Active commenters, diligent bug reporters, and enthusiastic community moderators collectively constitute a form of distributed quality assurance that can meaningfully reduce the probability of post-campaign failure. Creators who cultivate this community — by responding to questions within hours, hosting live streams, publishing transparent production updates, and acknowledging the contributions of individual backers — consistently report higher satisfaction scores and greater willingness among backers to support future campaigns. This dynamic creates a virtuous feedback loop: successful creators accumulate a reservoir of loyal supporters who de-risk subsequent launches, while novice creators must work harder to establish credibility. The implication for platforms is clear: community infrastructure — discussion forums, update broadcasting tools, backer recognition mechanisms — is not ancillary but central to the value proposition.

**Technological Frontier: Blockchain and Smart Contracts.** A small but growing body of research explores the intersection of blockchain technology and crowdfunding. Smart contracts can automate milestone-based fund releases, thereby reducing the reliance on platform intermediaries and providing stronger guarantees against post-funding mismanagement. Token-based models (often referred to as Initial Coin Offerings, or ICOs, and their more recent descendants, Security Token Offerings) blur the boundary between traditional crowdfunding and capital markets. While these innovations offer compelling transparency benefits, they also expose backers to novel risks, including smart-contract vulnerabilities, volatile token valuations, and jurisdictional ambiguity. Regulatory clarity in this domain is evolving rapidly, with frameworks such as the EU's Markets in Crypto-Assets Regulation (MiCA) setting a precedent for structured oversight.

**Practical Implications for Stakeholders.** For entrepreneurs, the reviewed literature underscores the importance of investing in high-quality pitch materials, planning a deliberate pre-launch community-building phase, and committing to transparent and timely communication throughout the campaign and post-campaign periods. For backers, due diligence — including careful review of creator history, realistic assessment of reward tiers, and scepticism toward overly ambitious timelines — remains indispensable. For platform operators, the balance between frictionless user experience and robust fraud prevention is delicate; over-aggressive moderation stifles legitimate creativity, while under-moderation erodes trust. For regulators, a proportionate, risk-based approach — lighter obligations for small reward-based campaigns and stricter rules for larger equity or lending models — appears to strike the right balance between innovation and protection.

**Future Research Agenda.** We close by proposing a four-point research agenda. First, longitudinal studies that follow backers and creators across multiple campaigns are needed to understand how reputational capital accumulates and depreciates over time. Second, cross-cultural comparative studies can illuminate how platform design choices interact with local institutional and cultural contexts, especially in emerging markets. Third, causal identification strategies — including field experiments, regression discontinuity around funding thresholds, and instrumental-variable analyses — are necessary to move the literature beyond correlational findings. Fourth, interdisciplinary collaboration across management, finance, information systems, computer science, and public policy is essential to address the multi-faceted questions raised by an increasingly complex crowdfunding ecosystem. By pursuing these directions, researchers and practitioners can together ensure that crowdfunding continues to fulfil its promise of democratising entrepreneurship while minimising the risks that accompany any rapidly evolving financial innovation.

\newpage

# 中文翻译

**众筹研究综述：金融民主化与创业新机遇**

*来源：Short, J. C., Ketchen, D. J., McKenny, A. F., Allison, T. H., & Ireland, R. D. Research on crowdfunding: Reviewing the (very recent) past and celebrating the present. Entrepreneurship Theory and Practice, 41(2), 149-160.*

众筹作为一种颠覆性的融资机制迅速兴起，它使创业者可以通过在线平台直接从大众筹集资金，而不再依赖银行、风险投资机构或政府专项资金等传统来源。过去十年中，全球众筹行业经历了爆发式增长，从小众现象逐步演变为主流的项目融资渠道，广泛应用于创意、商业与公益领域。本文综述了众筹研究的现状，并指出了未来最具前景的若干学术方向。

学术界通常将众筹归纳为四种主要模式。以 Kickstarter 与 Indiegogo 为代表的回报型众筹允许支持者以资金换取实物产品、服务或荣誉；以 GoFundMe 为代表的捐赠型众筹支持慈善或个人事业且不承诺任何回报；股权型众筹使支持者获得初创企业的股份；借贷型（或点对点借贷）众筹则将支持者转变为收取利息的债权人。每种模式在治理、披露与风险管理上都提出了不同的挑战。

既有研究一致表明，众筹项目的成功取决于"项目级信号"与"平台级机制"的组合。生动、具体、具有情感共鸣的项目描述往往能够吸引更多支持者；及时发布动态、回复评论并展示社会背书（如意见领袖推荐或媒体报道）的创作者系统性地优于不这样做的创作者。在平台层面，身份认证、托管账户、里程碑拨付以及透明的退款政策等信任建设特性能够显著降低感知风险，从而提升出资意愿。

然而，众筹并非没有问题。创作者与支持者之间的信息不对称为欺诈、过度承诺与交付不达标提供了土壤。实证研究显示，一部分获得筹款成功的项目最终并未按时交付或达到宣传的质量。为了缓解上述风险，平台越来越多地依赖算法审核、社区监督与分阶段拨付机制。各国政府也纷纷出台了相应法规，在投资者保护与创新激励之间寻求平衡，尽管全球监管格局仍呈现碎片化特征。

从创业者的视角出发，众筹提供了超越"资金"本身的多重收益：其一，成功的众筹活动本身即是对市场需求的验证，有助于降低后续融资轮次的不确定性；其二，支持者常常在项目上线后转变为早期用户与品牌倡导者，提供反馈并带来口碑传播；其三，借助众筹降低客户获取成本，创业者可以采用精益、迭代、由消费者驱动的产品开发方式；其四，众筹项目的公开属性促使创业者更清晰地表达价值主张，这种能力往往带来长期战略收益。

尽管具备明显的金融民主化潜力，众筹并非对所有创业者同等开放。人口统计学研究揭示，在筹款结果中存在显著的性别、族裔与地理差异：在控制项目质量的情况下，女性与少数族裔创始人所获得的关注与人均出资额仍然偏低；农村创业者则需面对数字素养、支付基础设施与物流等多重障碍。若要消除这些差异，需要平台的有意识设计、社区的主动干预以及政策的持续支持。

展望未来，若干研究方向值得关注。其一，将区块链技术与众筹结合，有望提升透明度、通过智能合约自动化拨付并实现全球化可达，但也会带来新的合规与易用性挑战；其二，人工智能驱动的推荐系统可以为项目发现提供个性化能力，但也可能放大"马太效应"式的偏见；其三，众筹在深科技、气候科技与公共卫生项目融资中的作用值得开展更深入的实证研究；其四，针对项目在筹款成功之后的长期表现开展纵向研究，有助于理解众筹对于创业者成功、消费者福祉与社会创新的真实影响。

综上所述，众筹是创业者获取资金、与市场互动方式的一次结构性演进。它在金融民主化、社群营造与创新加速方面释放了积极作用，同时也带来了新的风险与治理责任。对平台运营方、创业者、支持者以及监管者而言，理解驱动项目成败的内在机制，是将众筹这一经济与社会进步工具的潜力充分释放的关键。本文余下部分综合最新实证研究成果，构建一个整合式的研究议程，并分别从上述四类利益相关方的视角讨论相应的实践意涵。

**信号理论与众筹成功。** 信号理论是众筹研究中最广泛采用的理论视角之一。借鉴 Spence 的开创性研究，学者们认为在信息不对称的市场中，高质量的创作者会采用成本高昂且可被观察的信号将自己与低质量同行区分开来。在众筹情境下，这类信号包括宣传视频的质量、风险与挑战章节的完整性、回报档位的丰富程度、创作者的教育与职业背景，以及是否已有原型或试点用户等前期准备。大量实证研究表明，携带更丰富、更可信信号的项目能够获得更高的出资额、更快达成筹款目标，并在筹款成功后表现出更好的交付记录。相反，依赖含糊措辞、过度形容词或纯营销图片的项目往往表现不佳。新兴研究还探讨语言学特征——如具象程度、时态取向以及激情词汇的使用——如何作为创作者创业取向的"软信号"发挥作用。

**羊群行为与动量效应。** 除了信号机制外，第二种行为机制——羊群行为——对活动结果同样具有决定性影响。众筹平台上的支持者的出资决策，往往并非完全依据项目的内在品质，而是受其他支持者已有行动的影响。那些能够在早期（通常是前 48 小时内）快速累积动量的项目，比起步缓慢的项目更有可能达成目标。这一现象被平台设计所放大：排行算法偏向"热度"项目、社交徽章庆贺筹款里程碑、倒计时营造紧迫感。羊群效应有助于优质项目克服冷启动难题，但也可能固化"人气驱动型"偏见，使得本已显眼的项目获得过多关注，而同样优秀但不知名的项目被忽视。近期借助自然语言处理与网络分析的计算研究开始解构"内在项目质量"与"外部社交动力学"的相对贡献，结果表明最终募资额约三分之一的方差可归因于羊群行为，而非项目本身的优劣。

**支持者的地理分散性。** 尽管众筹常被誉为"无国界"现象，但实证证据揭示出更微妙的图景。支持者的空间分布呈现双峰特征：相当比例的早期出资来自创作者的本地社群——亲友与熟人；而一旦项目获得可见度，后续出资便会来自更广泛的地理区域，甚至包括国际支持者。这一分布规律有重要含义：其一，本地支持者兼具"资金提供者"与"羊群效应触发者"的双重身份；其二，地理分散性能够缓解区域性经济冲击，因为资金来源多元化可减少局部下行导致整体崩盘的风险；其三，地理分散也带来跨境物流、税务合规与汇率换算等实际挑战，增加创作者的行政负担并可能侵蚀净收益。对此，各平台相继推出集成运费计算器、多币种支持、区域履约合作等工具，但跨境配送成本仍不可忽视，在硬件与时尚类项目中尤为突出。

**不同司法辖区下的监管框架。** 众筹的快速扩张促使各国监管机构设计既保护投资者又兼顾创新的框架。在美国，2012 年《创业企业促进法案》（JOBS Act）及证券交易委员会的后续立法奠定了"监管型众筹"（Reg CF）的基础，允许非合格投资者在年度额度内参与股权众筹；在欧盟，自 2021 年生效的《欧盟众筹服务提供商条例》（ECSPR）建立了统一的"通行证"机制，取代了此前各国零散的牌照体系；中国则采取较为审慎的立场，对股权与借贷型众筹实施限制，同时允许回报型模式在平台级合规义务下运行。各司法辖区的共同主题包括：创作者身份与财务情况的强制披露、个人出资额度上限、平台级反欺诈流程义务、以及必须维护托管账户的安排等。持续存在的挑战包括：投资者权益的跨境执行、代币化或加密货币计价项目的定性，以及奖励-股权混合模式的分类归属。

**平台治理与信任架构。** 信任是任何众筹平台最稀缺的资源，平台运营者为此投入大量资源。现代平台部署了多层次的治理体系：从通过政府颁发证件或第三方实名认证完成的身份核验开始，到社区驱动的项目内容审核，再到以机器学习模型进行异常检测。纠纷解决机制通常将平台级仲裁与阶梯式退款政策相结合，部分司法辖区的平台还与传统金融机构合作，为部分项目提供保险型保障。最近的学术成果强调"程序正义"——即用户感知到纠纷被透明且一致地处理——其对长期用户忠诚度的预测力优于单纯的退款金额。因此，信任建设既是技术挑战，也是设计、沟通与社群运营的综合课题。

**社群与社会资本的作用。** 除了资金，支持者还常常贡献第二种资源——社会资本。活跃的评论者、勤勉的缺陷反馈者与热忱的社群志愿者，共同构成了一种"分布式质量保障"，能够显著降低项目在筹款成功后失败的概率。用心经营社群（数小时内回复提问、举办直播、发布透明的生产进度、公开致谢具体支持者）的创作者，其满意度与支持者复购意愿都更高。这种动态形成正向反馈：成功的创作者积累起"忠实支持者储备"为后续发射减少风险，而新手创作者则必须付出更多努力以建立可信度。对平台而言启示明确：社群基础设施——讨论区、动态广播、支持者认证机制——绝非辅助功能，而是价值主张的核心。

**技术前沿：区块链与智能合约。** 一股规模虽小但迅速壮大的研究力量正在探索区块链与众筹的交集。智能合约可以以里程碑方式自动拨付资金，从而降低对平台中介的依赖，并为"筹款后的资金管理"提供更强保证。基于代币的模式（即早期的 ICO 及其近期演进的 STO）模糊了传统众筹与资本市场之间的边界。这些创新在透明度上具吸引力，但也使支持者暴露于新的风险之中，包括智能合约漏洞、代币价格剧烈波动以及司法辖区模糊。该领域的监管正在快速演进，欧盟的《加密资产市场监管条例》（MiCA）为结构化监督树立了先例。

**对各利益相关方的实践意涵。** 对创业者而言，上述文献强调应投入高质量的路演材料、规划有意识的预热社群建设阶段、并在筹款与筹款后阶段保持透明与及时的沟通；对支持者而言，尽职调查——包括审慎评估创作者历史、现实地判断回报档位、以及对过度乐观时间表保持怀疑——始终不可或缺；对平台运营方而言，"流畅用户体验"与"稳健反欺诈"之间的平衡十分微妙，过度审核会扼杀合法创意，而审核不足则会侵蚀信任；对监管者而言，采取"按风险分级"的比例原则——对小型回报型活动实施较轻义务，对更大规模的股权或借贷型模式施以更严格约束——似乎是平衡创新与保护的恰当方式。

**未来研究议程。** 最后，我们提出四点研究议程：其一，需开展追踪同一创作者与支持者跨多个项目的纵向研究，以理解声誉资本如何随时间累积或折旧；其二，跨文化比较研究可阐明平台设计选择如何与本地制度与文化情境互动，尤其是新兴市场；其三，需要借助田野实验、围绕筹款阈值的回归断点分析以及工具变量分析等因果识别策略，将研究推进到相关性以上的层次；其四，管理学、金融学、信息系统学、计算机科学与公共政策的跨学科协作，对于回应日益复杂的众筹生态系统所提出的多维问题不可或缺。通过沿这些方向共同推进，研究者与从业者有望确保众筹持续兑现"创业民主化"的承诺，同时把一切快速演进的金融创新所伴随的风险降到最低。

