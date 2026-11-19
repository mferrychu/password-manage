# MyBox 项目介绍

> 版本：v1.0 (2026.08.01) ｜ 平台：Android ｜ 包名：`com.mybox` ｜ 最低支持：Android 7.0 (API 24) ｜ 目标 SDK：Android 14 (API 34)

## 一、项目概述

**MyBox** 是一款本地优先（Local-First）、零网络依赖的个人数据管理应用，专注于两大核心场景：**密码安全管理** 与 **重要事件管理**。所有用户数据均存储在本地设备中，不上传任何服务器，密码字段采用 AES-256-GCM 加密存储，主密码采用 PBKDF2WithHmacSHA256 加盐哈希，是一款真正"数据自主可控"的轻量级安全工具。

应用以 **Kotlin + Jetpack Compose** 为开发框架，遵循 **Material Design 3** 设计语言，提供 5 套精心搭配的主题色，体积控制在 10MB 以内，运行流畅、视觉精致。

## 二、应用功能

### 2.1 密码管理模块

- **新增 / 编辑 / 删除密码**：支持标题（必填）、账号（选填）、密码（必填）、备注（选填）四字段
- **列表分页加载**：基于 Paging 3 实现，每页 10 条，加载有 500ms 延迟以体现分页效果
- **实时搜索**：在标题、账号、备注三个字段中模糊匹配
- **密码可见性切换**：使用眼睛图标切换显示/隐藏，图标不被文字遮挡
- **弱密码检测**：自动识别长度 < 8 位、纯数字、纯字母三类弱密码，并在统计卡片中提示
- **二次确认删除**：所有删除操作均带确认对话框，防止误删
- **复制功能（密码字段除外）**：密码字段无复制功能，仅可见性切换，进一步防泄漏

### 2.2 事件管理模块

- **新增 / 编辑 / 删除事件**：包含日期（YYYY-MM-DD）、事件说明（必填）、卡片颜色（自定义）三字段
- **时间维度统计**：自动统计未来事件数与历史事件数
- **实时搜索**：基于事件描述字段模糊匹配
- **卡片色彩个性化**：每条事件可指定独立颜色（默认 #FFFFFF）

### 2.3 安全与解锁

- **主密码首次设置**：首次安装后强制设置主密码，最小长度 4 位，禁止纯空白
- **解锁页覆盖层**：进入主页前需解锁；从后台切回前台时，若启用自动锁定则重新锁定
- **生物识别快捷解锁**：支持指纹 / 3D 面部识别（BIOMETRIC_STRONG，仅 Class 3），首次进入主页后弹出一次启用引导，可在设置中关闭
- **修改主密码**：设置页入口，重新哈希存储
- **自动锁定**：可选开关，切到后台即锁；进行导出/导入操作时不触发锁定

### 2.4 数据导入 / 导出

- **加密导出**：用户输入导出密码，基于 PBKDF2 + AES-256-GCM 加密为 `.enc` 文件
- **加密导入**：导入时校验密码，错误则提示；导入操作使用 Room `withTransaction` 保证原子性（全部成功或全部回滚）
- **文件大小限制**：单文件超过 10MB 直接拒绝，防止 OOM
- **版本与签名校验**：导入时校验 `appName` 与 `version`，拒绝不兼容数据

### 2.5 个性化与体验

- **5 套主题**：紫色（默认）、科技蓝、绿色、玫瑰粉、芥末黄
- **状态栏自适应**：根据当前页面与主题动态调整状态栏颜色与图标深浅
- **SplashScreen 启动屏**：使用 AndroidX SplashScreen API
- **空状态友好提示**：列表为空时展示 Info 图标与提示文案
- **Snackbar 反馈**：所有增删改操作均有成功/失败提示

## 三、技术栈

| 维度 | 技术选型 |
|------|----------|
| 语言 | Kotlin（JVM Target 1.8） |
| UI 框架 | Jetpack Compose + Compose BOM 2024.10.00 |
| 设计语言 | Material Design 3（material3） |
| 图标库 | Material Icons Extended |
| 导航 | Navigation Compose 2.7.6 |
| 数据库 | Room 2.6.1（含 Paging 3 集成） |
| 分页 | Paging 3.2.1（compose + runtime） |
| 依赖注入 | Dagger Hilt 2.51.1（KSP 注解处理） |
| 序列化 | Gson 2.10.1 |
| 生物识别 | AndroidX Biometric 1.1.0 |
| 启动屏 | core-splashscreen 1.0.1 |
| 协程 | kotlinx-coroutines 1.7.3 |
| 生命周期 | lifecycle-runtime-ktx / process 2.6.2 |
| 构建工具 | Gradle 8.13 + Kotlin DSL + KSP |
| 加密算法 | AES-256-GCM（数据）+ PBKDF2WithHmacSHA256（密码哈希） |
| 密钥存储 | Android Keystore |
| 压缩发布 | R8 + 资源压缩 + ProGuard |

## 四、架构设计

### 4.1 整体架构：单 Activity + Compose Navigation

```
MainActivity (FragmentActivity, Hilt 入口)
   └── MainScreen (Compose 根组合)
         └── MyBoxTheme (主题包装)
               └── MainContent
                     ├── AppNavHost (导航容器)
                     │     ├── SetupPasswordScreen
                     │     ├── PasswordListScreen (主页)
                     │     ├── AddEditPasswordScreen
                     │     ├── PasswordDetailScreen
                     │     ├── EventListScreen
                     │     ├── AddEditEventScreen
                     │     ├── EventDetailScreen
                     │     ├── SettingsScreen
                     │     ├── ChangePasswordScreen
                     │     └── AboutScreen
                     ├── BottomNavBar (底部导航)
                     └── UnlockScreen (锁定覆盖层)
```

### 4.2 分层职责

| 层 | 职责 | 关键类 |
|----|------|--------|
| **UI 层** `ui/` | Composable 组件、屏幕、主题、组件 | `screens/*`、`components/*`、`theme/*` |
| **ViewModel 层** `viewmodel/` | 状态管理、业务逻辑、生命周期监听 | `MainViewModel`（单 ViewModel 统管，Hilt 注入） |
| **数据层** `data/` | 实体、DAO、数据库、偏好设置 | `model/*`、`dao/*`、`database/AppDatabase`、`preferences/AppPreferences` |
| **工具层** `util/` | 加解密、导入导出、生物识别 | `EncryptUtils`、`ExportImportUtils`、`BiometricHelper` |
| **依赖注入** `di/` | Hilt Module 提供数据库与 DAO 单例 | `DatabaseModule` |
| **导航** `navigation/` | 路由定义与 NavHost 配置 | `NavGraph`、`Screen` |

### 4.3 关键架构决策

1. **单 ViewModel 设计**：`MainViewModel` 持有所有状态与业务方法，避免多 ViewModel 间状态同步成本；UI 通过 `collectAsState` 订阅 `StateFlow`
2. **状态驱动 UI**：所有数据通过 `Flow`/`StateFlow` 暴露，配合 `flatMapLatest` 实现搜索响应
3. **`AppInitState` 统一初始化状态**：包含 `isInitializing`、`masterPasswordHash`、`isLocked` 三字段，决定启动导航目标
4. **SplashScreen 条件保持**：通过 `setKeepOnScreenCondition` 在初始化未完成时持续显示，避免闪现设置密码页
5. **锁定覆盖层模式**：`UnlockScreen` 以覆盖层形式叠加在 `NavHost` 之上，而非独立路由，确保解锁后回到原页面且不被销毁
6. **`ProcessLifecycleOwner` 监听前后台切换**：实现自动锁定逻辑，文件操作进行中跳过锁定
7. **Room 迁移策略**：保留 `MIGRATION_1_2`（事件表新增 color 字段），并提供 `fallbackToDestructiveMigration` 兜底

## 五、安全设计

### 5.1 多层加密体系

| 场景 | 算法 | 说明 |
|------|------|------|
| 密码字段加密存储 | **AES-256-GCM** | 密钥保管在 Android Keystore，IV 与密文拼接后 Base64 |
| 主密码哈希存储 | **PBKDF2WithHmacSHA256** | 100,000 次迭代 + 32 字节随机盐，存储格式 `saltBase64:hashBase64` |
| 导入导出文件加密 | **PBKDF2 + AES-256-GCM** | 用户密码派生密钥，盐与 IV 随机生成，存储格式 `salt:iv:ciphertext` |

### 5.2 密钥与权限安全

- **Android Keystore 硬件级保护**：密钥不离开 Keystore，硬件隔离的 Keymaster/Keymint 后端
- **`allowBackup="false"`**：禁止系统自动备份，防止通过 ADB 备份导出数据
- **`USE_BIOMETRIC` 权限**：仅声明生物识别权限，无网络/存储等敏感权限
- **BIOMETRIC_STRONG 策略**：仅接受 Class 3 指纹/3D 面部，拒绝弱认证（如 2D 面部）
- **生物识别不绕过主密码**：必须先设置主密码；启用生物识别需先验证生物特征
- **截图屏蔽**：密码输入页面系统自动屏蔽截图（Android 标准行为）

### 5.3 应用行为安全

- **返回键拦截**：锁定状态下拦截系统返回键，防止绕过解锁页
- **弱密码检测**：长度 < 8、纯数字、纯字母三类密码自动标记
- **导入原子性**：`Room.withTransaction` 保证全部成功或全部回滚
- **导入文件大小校验**：> 10MB 直接拒绝，Toast 提示
- **导入版本校验**：检查 `appName` 与 `version` 字段，拒绝不兼容数据
- **生物识别开关智能禁用**：设备不支持时设置页开关自动禁用并提示

### 5.4 用户操作安全

- **所有删除操作二次确认**
- **密码字段仅可见性切换，无复制功能**
- **主密码最小长度 4 位 + 非空校验**
- **标题、事件描述 trim 校验**，账号与备注保存时 trim
- **密码字段 `isNotBlank` 校验**，禁止纯空白密码

## 六、项目优势

### 6.1 安全优势

- **本地零网络**：除系统 API 外不与任何服务器通信，从架构上杜绝数据外泄
- **硬件级密钥保护**：Android Keystore 隔离密钥
- **行业级算法**：AES-256-GCM 与 PBKDF2 均为 NIST 推荐算法
- **多场景防护**：覆盖存储、传输（导出）、解锁三大场景

### 6.2 体验优势

- **现代 Compose UI**：响应式、声明式，渲染高效
- **5 套主题随心切换**：紫色 / 蓝 / 绿 / 粉 / 黄，状态栏与背景自适应
- **生物识别快捷解锁**：进 App 自动唤起指纹弹窗
- **分页加载 + 实时搜索**：百条数据亦流畅
- **Snackbar 即时反馈**：操作结果一目了然
- **空状态友好提示**：避免空白页面困惑

### 6.3 工程优势

- **APK ≤ 10MB**：精简依赖，R8 压缩
- **Hilt 依赖注入**：单例 DAO/Database，测试友好
- **KSP 替代 KAPT**：编译更快
- **Kotlin DSL Gradle**：类型安全的构建配置
- **ProGuard 混淆发布**：release 包开启 minify + shrinkResources，体积更小更安全
- **签名配置隔离**：release 签名密钥库独立，密码通过 `gradle.properties` 注入

### 6.4 数据主权优势

- **完全自主**：数据从未离开设备
- **加密导出迁移**：换机或备份通过加密文件传递
- **无第三方账号体系**：无需注册登录，即装即用

## 七、模块文件结构

```
app/src/main/
├── java/com/mybox/
│   ├── MainActivity.kt                 # 入口 Activity（FragmentActivity）
│   ├── MyBoxApplication.kt             # HiltAndroidApp 入口
│   ├── data/
│   │   ├── dao/                        # PasswordDao / EventDao / MasterPasswordDao
│   │   ├── database/AppDatabase.kt    # Room 数据库 (version=2)
│   │   ├── model/                      # Password / Event / MasterPassword / ExportData
│   │   └── preferences/AppPreferences.kt  # SharedPreferences 偏好管理
│   ├── di/DatabaseModule.kt           # Hilt 模块
│   ├── navigation/NavGraph.kt         # 路由与 NavHost
│   ├── ui/
│   │   ├── components/                 # BottomNavBar / CommonComponents / StatCard
│   │   ├── screens/                    # 11 个屏幕组件
│   │   └── theme/                      # Color / Theme / Type（5 套主题）
│   ├── util/
│   │   ├── BiometricHelper.kt         # 生物识别封装
│   │   ├── EncryptUtils.kt            # Keystore + PBKDF2 加解密
│   │   └── ExportImportUtils.kt       # 导入导出（含加密）
│   └── viewmodel/MainViewModel.kt     # 单 ViewModel 统管状态与业务
└── res/
    ├── values/strings.xml             # app_name = "MyBox"
    ├── values/themes.xml              # Splash 主题
    └── xml/                            # backup_rules / data_extraction_rules
```

## 八、关键数据流

### 8.1 启动流程

```
Application 启动 → Hilt 注入 → SplashScreen 显示
    → MainViewModel.init: 读取 master_password 表
        → 已设置：startDestination = PasswordList（locked=true）
        → 未设置：startDestination = SetupPassword
    → SplashScreen 在 isInitializing=false 时消失
    → 渲染 NavHost，UnlockScreen 覆盖层显示
```

### 8.2 解锁流程

```
用户输入密码 / 触发生物识别
    → MainViewModel.unlock(password) 或 unlockByBiometric()
        → EncryptUtils.verify(password, storedHash)
        → 成功：isLocked=false，UnlockScreen 消失，露出主页
        → 失败：保持锁定，Toast 提示
```

### 8.3 数据导出流程

```
用户点击导出 → 输入导出密码
    → 系统文件选择器 (ACTION_CREATE_DOCUMENT)
    → ExportImportUtils.exportData：解密所有密码字段 → Gson 序列化
    → ExportImportUtils.encryptExportData：PBKDF2 派生密钥 + AES-256-GCM 加密
    → 写入 .enc 文件 → Toast 反馈条数
```

## 九、版本与构建信息

| 项 | 值 |
|----|----|
| versionCode | 1 |
| versionName | 1.0 (2026.08.01) |
| compileSdk | 34 |
| minSdk | 24 |
| targetSdk | 34 |
| Compose Compiler | 1.5.8 |
| 资源配置 | zh, zh-rCN |
| Release 构建 | minify=true, shrinkResources=true, 自定义签名 |

## 十、总结

MyBox 是一款"小而美、轻而强"的个人数据管理应用：

- **小**：APK ≤ 10MB，依赖精简，资源仅保留中文
- **美**：Material Design 3 + 5 套主题，状态栏与背景自适应
- **轻**：单 ViewModel + Compose 声明式 UI，无冗余框架
- **强**：AES-256-GCM + PBKDF2 + Android Keystore + BIOMETRIC_STRONG 多重防护

它在**安全**与**易用**之间取得了平衡：以本地存储 + 硬件加密保障数据主权，以生物识别 + 自动锁定提升日常体验，是一款适合隐私敏感用户的轻量级密码与事件管理工具。

---

*本文档基于 MyBox v1.0 源码生成，反映项目截至 2026.08.01 的实现状态。*
*本项目采用 PolyForm Noncommercial 1.0.0 许可，允许个人学习、研究、 hobby 项目，以及慈善、教育、政府等非营利机构使用，商业用途需另行获得授权。*
