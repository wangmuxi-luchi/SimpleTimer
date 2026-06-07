# Simple Timer 工程说明文档

## 1. 项目概述

Simple Timer 是一款基于 Android 平台的时间记录应用，专注于帮助用户记录和管理日常时间花费。应用采用现代 MVVM 架构，支持分类管理、时间记录、数据统计和备份恢复等核心功能。

## 2. 技术栈

| 分类 | 技术 | 版本 |
|------|------|------|
| 语言 | Kotlin | 1.9+ |
| 框架 | Android Jetpack | - |
| 数据库 | Room | 2.6+ |
| 响应式 | Coroutines + Flow | - |
| UI绑定 | Data Binding | - |

## 3. 项目结构

```
app/src/main/java/com/wy/simple_timer/
├── MainActivity.kt              # 主界面入口
├── TimeRecordActivity.kt        # 时间记录页面
├── EventEditActivity.kt         # 事件编辑页面
├── CategoryDetailActivity.kt    # 分类详情页面
├── CategoryManagementActivity.kt # 分类管理页面（备用）
├── EventListActivity.kt         # 事件列表页面（备用）
├── MyBroadcastReceiver.kt       # 广播接收器
├── DatabaseManagementService.kt # 数据库操作服务（主文件）
├── SimpleTimerApplication.kt    # 应用入口
├── adapter/                     # 适配器模块
│   ├── BaseCategoryAdapterRV.kt    # 分类适配器基类
│   ├── BaseEventAdapterRV.kt       # 事件适配器基类
│   ├── CategoryAdapterCM.kt        # 分类管理适配器
│   ├── CategoryAdapterCMF.kt       # 分类管理Fragment适配器
│   ├── CategoryAdapterTR.kt        # 时间记录分类适配器
│   ├── EventAdapterEL.kt           # 事件列表适配器
│   └── WorkMode.kt                 # 工作模式枚举
├── custom_views/                # 自定义视图
│   ├── AutoHeightGridView.kt       # 自动高度网格视图
│   ├── CategoryDialog.kt           # 分类对话框
│   ├── CustomDatePickerDialog.kt   # 自定义日期选择器
│   ├── CustomRecycleView.kt        # 自定义RecyclerView
│   └── DateRangeTextView.kt        # 日期范围显示
├── database/                    # 数据库模块
│   ├── Category.kt                 # 分类实体类
│   ├── CategoryDao.kt              # 分类数据访问接口
│   ├── DateConverter.kt            # 日期类型转换器
│   ├── Event.kt                    # 事件实体类
│   ├── EventDao.kt                 # 事件数据访问接口
│   └── MyDatabase.kt               # 数据库实例管理
├── fragment/                    # 碎片模块
│   ├── CategoryManagementFragment.kt # 分类管理碎片
│   ├── CategoryPickerFragment.kt     # 分类选择碎片
│   ├── EventListFragment.kt          # 事件列表碎片
│   └── TimePickerFragment.kt         # 时间选择碎片
├── service/                     # 服务模块
│   └── DatabaseManagementService.kt  # 数据库操作服务
├── ui/theme/                    # UI主题
│   ├── Color.kt                    # 颜色定义
│   ├── Theme.kt                    # 主题配置
│   └── Type.kt                     # 字体样式
├── utils/                       # 工具类
│   ├── Calendar.kt                 # 日期工具
│   ├── ItemTouchCallbackCM.kt      # 分类管理拖拽回调
│   ├── ItemTouchCallbackCMF.kt     # 分类Fragment拖拽回调
│   └── SpacingItemDecoration.kt    # 间距装饰器
└── viewModel/                   # 视图模型
    ├── CategoryViewModel.kt        # 分类视图模型
    ├── CategoryViewModelDaoHelper.kt # 分类Dao助手接口
    ├── CategoryWEIViewModel.kt     # 分类+事件统计视图模型
    ├── EventViewModel.kt           # 事件视图模型
    └── EventViewModelDaoHelper.kt  # 事件Dao助手接口
```

## 4. 文件功能说明

### 4.1 Activity 层

#### MainActivity.kt
- **职责**：应用主入口，承载事件列表和分类管理两个Fragment
- **功能**：
  - 管理底部导航切换
  - 处理数据备份/恢复操作
  - 监听日期变化广播
  - 协调两个Fragment之间的通信

#### TimeRecordActivity.kt
- **职责**：时间记录页面，用于创建新的时间事件
- **功能**：
  - 集成时间选择器和分类选择器
  - 支持备注输入
  - 验证时间逻辑（开始时间 <= 结束时间）
  - 通过Service保存事件到数据库

#### EventEditActivity.kt
- **职责**：事件编辑页面，用于修改已有事件
- **功能**：
  - 根据eventId加载事件详情
  - 支持修改时间、分类、备注
  - 提供删除事件功能
  - 通过ViewModel更新数据库

#### CategoryDetailActivity.kt
- **职责**：分类详情页面，展示分类统计信息
- **功能**：
  - 展示分类名称、颜色
  - 统计该分类下的事件总数、总时长、天数、日均时长
  - 提供编辑、归档、删除操作

### 4.2 Fragment 层

#### EventListFragment.kt
- **职责**：事件列表展示碎片
- **功能**：
  - 根据时间范围筛选事件
  - 支持事件点击跳转编辑
  - 响应分类选择状态变化

#### CategoryManagementFragment.kt
- **职责**：分类管理展示碎片
- **功能**：
  - 展示分类列表及时间统计
  - 支持拖拽排序
  - 左滑快速记录、右滑多选
  - 管理分类选中状态

#### CategoryPickerFragment.kt
- **职责**：分类选择碎片
- **功能**：
  - 展示所有可用分类
  - 支持单选分类
  - 返回选中分类ID

#### TimePickerFragment.kt
- **职责**：时间选择碎片
- **功能**：
  - 提供开始时间和结束时间选择
  - 支持时间验证
  - 返回Date类型时间

### 4.3 ViewModel 层

#### EventViewModel.kt
- **职责**：事件数据管理
- **功能**：
  - 管理事件数据流
  - 提供事件增删改查接口
  - 通过Dao助手访问数据库

#### CategoryViewModel.kt
- **职责**：分类数据管理
- **功能**：
  - 管理分类数据流
  - 提供分类增删改查接口

#### CategoryWEIViewModel.kt
- **职责**：分类+事件统计数据管理（Category With Event Info）
- **功能**：
  - 组合分类和事件数据流
  - 计算分类时间统计（事件数、总时长、天数、占比）
  - 支持归档/未归档数据筛选

### 4.4 数据库层

#### MyDatabase.kt
- **职责**：Room数据库实例管理
- **功能**：
  - 创建单例数据库实例
  - 提供EventDao和CategoryDao访问

#### Category.kt
- **职责**：分类实体定义
- **字段**：id、categoryName、categoryColor、position、archived、parentId

#### Event.kt
- **职责**：事件实体定义
- **字段**：id、startTime、endTime、categoryId、notes

#### CategoryDao.kt
- **职责**：分类数据访问接口
- **方法**：
  - insertCategory：插入分类
  - updateCategory：更新分类
  - deleteCategory：删除分类
  - getUnarchivedCategoriesOrderedByPosition：获取未归档分类（按位置排序）
  - getArchivedCategoriesOrderedByPosition：获取归档分类

#### EventDao.kt
- **职责**：事件数据访问接口
- **方法**：
  - insertEvent：插入事件
  - updateEvent：更新事件
  - deleteEvent：删除事件
  - getEventsByCategory：按分类查询事件
  - getEventsInRange：按时间范围查询事件
  - getAllEvents：查询所有事件

### 4.5 Service 层

#### DatabaseManagementService.kt
- **职责**：后台数据库操作服务
- **功能**：
  - 处理分类增删改操作
  - 处理事件增删改操作
  - 支持级联删除分类及子分类
  - 数据备份（JSON格式）
  - 数据恢复（增量合并）

### 4.6 Adapter 层

#### CategoryAdapterCMF.kt
- **职责**：分类管理Fragment的RecyclerView适配器
- **功能**：
  - 展示分类名称、颜色、时间统计
  - 支持拖拽排序
  - 管理选中状态
  - 可视化时间占比进度条

#### EventAdapterEL.kt
- **职责**：事件列表的RecyclerView适配器
- **功能**：
  - 展示事件时间范围、分类、备注
  - 根据分类颜色显示标识

### 4.7 工具类

#### Calendar.kt
- **职责**：日期处理工具
- **功能**：
  - 重置时间到周期起始点
  - 日期比较扩展方法

#### ItemTouchCallbackCMF.kt
- **职责**：RecyclerView拖拽和滑动回调
- **功能**：
  - 处理Item拖拽排序
  - 处理左右滑动操作

## 5. 核心功能模块

### 5.1 时间记录模块
- **入口**：MainActivity -> TimeRecordActivity
- **流程**：选择时间范围 → 选择分类 → 添加备注 → 保存

### 5.2 事件管理模块
- **入口**：MainActivity -> EventListFragment -> EventEditActivity
- **功能**：查看事件列表、编辑事件、删除事件

### 5.3 分类管理模块
- **入口**：MainActivity -> CategoryManagementFragment
- **功能**：添加/编辑/删除分类、拖拽排序、快速记录

### 5.4 数据统计模块
- **功能**：
  - 分类时间占比可视化
  - 分类详情统计（事件数、时长、天数）
  - 时间范围筛选统计

### 5.5 数据备份恢复模块
- **功能**：
  - 一键备份到JSON文件
  - 从JSON文件恢复数据
  - 增量合并保留现有数据

## 6. 架构设计

```
┌─────────────────────────────────────────────────────────┐
│                    UI Layer (Activity/Fragment)         │
├─────────────────────────────────────────────────────────┤
│                  ViewModel Layer                        │
│         ┌─────────────┬─────────────┐                  │
│         │EventViewModel│CategoryViewModel│              │
│         │              │CategoryWEIViewModel│           │
│         └─────────────┴─────────────┘                  │
├─────────────────────────────────────────────────────────┤
│                    Data Layer (Room)                    │
│         ┌─────────────┬─────────────┐                  │
│         │  EventDao   │ CategoryDao │                  │
│         └─────────────┴─────────────┘                  │
├─────────────────────────────────────────────────────────┤
│              DatabaseManagementService                  │
└─────────────────────────────────────────────────────────┘
```

## 7. 数据流向

1. **用户操作** → Activity/Fragment → ViewModel → Dao → Database
2. **数据更新** → Database → Flow → ViewModel → Activity/Fragment → UI

## 8. 关键技术点

- **MVVM架构**：解耦UI与业务逻辑，提高可测试性
- **Flow响应式**：数据变化自动推送，实时更新UI
- **后台Service**：数据库操作异步执行，避免阻塞主线程
- **事务管理**：确保数据一致性，尤其是级联操作
- **增量备份**：支持数据合并，避免覆盖现有数据

## 9. 待完善功能

| 功能 | 状态 | 说明 |
|------|------|------|
| 归档功能 | 待修复 | CategoryDetailActivity中归档逻辑待完善 |
| 子分类管理 | 待实现 | Category实体支持parentId，但UI未完整支持 |
| 数据统计图表 | 待添加 | 可增加可视化统计图表 |