# 随机诗歌屏幕（Random Poetry Screen）底层设计文档 Low Level Design

## 1. 系统架构

### 1.1 技术栈
| 项目 | 选型 |
|------|------|
| 语言 | Kotlin |
| UI 框架 | Jetpack Compose + Material3 |
| 导航 | Navigation Compose |
| 状态管理 | ViewModel + StateFlow |
| 本地数据持久化 | JSON 文件 (Gson 序列化, 存储于应用内部存储) |
| 用户配置持久化 | DataStore Preferences |
| 最低 SDK | 24 (Android 7.0) |
| 目标 SDK | 36 |
| 构建工具 | Gradle 9.4.1 + AGP 9.2.1 |

### 1.2 架构模式
采用 **MVVM (Model-View-ViewModel)** 架构：
- **Model**: 数据模型 + Repository + Store
- **ViewModel**: 每个 Screen 对应一个 ViewModel，持有 UI 状态 (StateFlow)
- **View**: 纯 Composable 函数，通过 `collectAsState()` 订阅 ViewModel 状态

### 1.3 依赖注入
采用轻量级手动 DI，通过 `AppContainer` 单例持有所有服务实例：
```
AppContainer
├── PoetryRepository (单例, 管理诗集数据)
├── PreferencesStore (管理用户配置)
├── FileImportService (文件导入解析)
└── RdpoemFileService (.rdpoem 导入导出)
```
ViewModel 通过 `ViewModelFactories` 对象工厂创建，工厂从 `AppContainer` 获取依赖。

## 2. 项目文件结构

```
app/src/main/java/online/dicemeow/dev_randompoetryscreen/
├── MainActivity.kt                      # 应用入口, 初始化 AppContainer
├── AppContainer.kt                      # 依赖容器
├── data/
│   ├── model/
│   │   └── Models.kt                    # 数据模型 (Anthology, PoetryEntry, 枚举, 配置)
│   ├── store/
│   │   ├── JsonDataStore.kt             # JSON 文件持久化 (诗集数据)
│   │   └── PreferencesStore.kt          # DataStore 偏好存储 (用户配置)
│   ├── repository/
│   │   └── PoetryRepository.kt          # 诗集数据仓库 (CRUD + 导入导出)
│   ├── generator/
│   │   └── PoetryGenerator.kt           # 诗歌随机生成算法
│   └── importer/
│       ├── TextImporter.kt              # 文本导入统一入口 (txt/md)
│       ├── MarkdownParser.kt            # Markdown 标题拆分解析
│       ├── EpubParser.kt                # EPUB 解析 (ZIP + XHTML)
│       ├── FileImportService.kt         # 文件 Uri 读取 + 路由
│       └── RdpoemFileService.kt         # .rdpoem 文件读写
├── ui/
│   ├── theme/
│   │   ├── Color.kt                     # 极客绿黑配色
│   │   ├── Type.kt                      # 等宽字体排版
│   │   └── Theme.kt                     # Material3 暗色主题
│   ├── components/
│   │   └── DecryptionText.kt            # 解密效果动画组件
│   ├── navigation/
│   │   ├── Routes.kt                    # 路由定义
│   │   └── AppNavigation.kt             # NavHost 导航图
│   ├── main/
│   │   ├── MainViewModel.kt             # 主界面 VM (诗歌循环 + 时间)
│   │   └── MainScreen.kt                # 主界面 (诗歌显示 + 屏幕常亮)
│   ├── anthology/
│   │   ├── AnthologyListViewModel.kt    # 诗集列表 VM
│   │   ├── AnthologyListScreen.kt       # 诗集列表界面
│   │   ├── AnthologyContentViewModel.kt # 诗集内容 VM
│   │   ├── AnthologyContentScreen.kt    # 诗集内容界面
│   │   └── ImportTargetDialog.kt        # 导入目标选择对话框
│   ├── editor/
│   │   ├── EditViewModel.kt             # 编辑器 VM (查找/替换/保存)
│   │   └── EditScreen.kt                # 文本编辑器界面
│   ├── config/
│   │   ├── ConfigViewModel.kt           # 配置 VM
│   │   └── ConfigScreen.kt              # 配置界面
│   └── ViewModelFactories.kt            # ViewModel 工厂集合
└── res/
    ├── font/
    │   └── ark_pixel_12px.ttf            # 像素字体
    ├── raw/
    │   ├── default_poetry.json           # 默认诗集: "随机诗歌" (1189条)
    │   └── prophet.json                  # 默认诗集: "无害预言" (1500条)
    ├── values/
    │   ├── themes.xml                   # XML 主题 (暗色背景)
    │   ├── colors.xml                   # XML 颜色
    │   └── strings.xml                  # 字符串资源
    └── AndroidManifest.xml              # 清单 (configChanges, 无额外权限)
```

## 3. 数据模型设计

### 3.1 核心数据模型
```kotlin
// 诗歌条目 (行 或 段落)
data class PoetryEntry(
    val id: String,           // UUID
    val anthologyId: String,  // 所属诗集 ID
    val type: EntryType,      // LINE | PARAGRAPH
    val content: String       // 文本内容
) {
    val lineCount: Int        // 计算属性: LINE=1, PARAGRAPH=换行数+1
        get() = ...
}

// 诗集
data class Anthology(
    val id: String,           // UUID
    val name: String,         // 诗集名称
    val entries: List<PoetryEntry>  // 条目列表
)

// 应用全部数据
data class AppData(
    val anthologies: List<Anthology>
)
```

### 3.2 枚举类型
| 枚举 | 值 | 说明 |
|------|-----|------|
| `EntryType` | LINE, PARAGRAPH | 条目类型 |
| `SplitMode` | BY_LINE, BY_TITLE, NO_SPLIT | 导入拆分模式 |
| `PoetryGenerationMethod` | RANDOM_PARAGRAPH, RANDOM_COMBINE_LINES, RANDOM_COMBINE_PARAGRAPH_AND_LINES | 诗歌生成方式 |
| `TimeDisplayType` | REFRESH_COUNTDOWN, APP_RUNTIME, POMODORO_COUNTDOWN, REAL_TIME | 时间显示类型 |

### 3.3 用户配置模型
```kotlin
data class UserConfig(
    val displayIntervalSec: Int = 15,           // 显示间隔 (秒)
    val displaySpeedMsPerChar: Int = 60,        // 显示速度 (毫秒/字符)
    val lineCountRange: PoetryLineCountRange,   // 行数范围 [1, 99]
    val selectedAnthologyIds: Set<String>,      // 随机范围 (空=全部)
    val generationMethod: PoetryGenerationMethod,
    val timeDisplayType: TimeDisplayType,
    val pomodoroDurationMin: Int = 25           // 番茄钟时长
)
```

## 4. 持久化设计

### 4.1 诗集数据存储 (JsonDataStore)
- **格式**: JSON 文件, 存储于 `context.filesDir/poetry_data.json`
- **序列化**: Gson, 使用中间 Raw 类隔离存储格式与领域模型
- **并发控制**: `@Synchronized` 保证线程安全
- **.rdpoem 格式**: 与内部存储格式相同的 JSON, 文件后缀 `.rdpoem`
- **默认数据**: 首次启动时, 若数据文件不存在, 加载两个默认诗集:
  - `res/raw/default_poetry.json` → "随机诗歌" (1189条诗歌素材)
  - `res/raw/prophet.json` → "无害预言" (1500条荒诞预言素材)
  - 通过 `loadDefaultAnthology()` 方法分别加载后合并

### 4.2 用户配置存储 (PreferencesStore)
- **技术**: AndroidX DataStore Preferences
- **名称**: `user_config`
- **数据流**: `Flow<UserConfig>`, 支持响应式配置更新
- **更新方式**: `suspend fun update(transform: (UserConfig) -> UserConfig)`

### 4.3 导入导出
- **导出**: `PoetryRepository.exportData()` → JSON 字符串 → `RdpoemFileService.exportToUri()` 写入 SAF Uri
- **导入**: `RdpoemFileService.importFromUri()` 读取 → `PoetryRepository.importData(json, merge=true)` 合并导入
- **合并策略**: ID 冲突时重新生成 ID, 保留原数据

## 5. 诗歌生成算法 (PoetryGenerator)

### 5.1 随机段落 (RANDOM_PARAGRAPH)
1. 筛选所有 PARAGRAPH 类型条目
2. 若无段落, 退化为从所有条目随机取 1 个
3. 随机取 1 个段落的 content 返回

### 5.2 随机组合行 (RANDOM_COMBINE_LINES)
1. 筛选所有 LINE 类型条目
2. 若无行, 将段落按换行拆分为行
3. 在 [min, max] 范围内随机取目标行数 (不超过池大小)
4. 打乱后取前 N 行, 以换行符连接

### 5.3 随机组合段落和行 (RANDOM_COMBINE_PARAGRAPH_AND_LINES)
遵循 PRD 指定算法:
1. 在 [min, max] 内随机确定目标行数 `sum_r`
2. 统计所有段落的行数众数 `p_mode`
3. 计算 `p_num = int(sum_r / p_mode)`
4. 随机取 [0, p_num] 个段落
5. 若段落行数和 < sum_r: 随机取行补齐, 段落与行打乱组合
6. 若段落行数和 >= sum_r: 仅用取出的段落组合

## 6. 文件导入解析设计

### 6.1 导入流程
```
用户点击"导入文件" → SAF OpenMultipleDocuments → 获取 Uri 列表
→ 获取文件名 → 判断文件类型集合 → 显示拆分模式选择对话框
→ 用户选择拆分模式 → 显示目标诗集选择对话框
→ 用户选择/新建诗集 → 逐文件解析+拆分 → 存入诗集 → 提示结果
```

### 6.2 文件类型与拆分模式映射
| 文件类型 | 支持的拆分模式 |
|----------|---------------|
| .txt | 按行拆分, 不拆分 |
| .md | 按行拆分, 按标题拆分, 不拆分 |
| .epub | 按行拆分, 按标题拆分, 不拆分 |
| 混合类型 | 取所有类型支持的交集 |

### 6.3 Markdown 标题拆分 (MarkdownParser)
1. 正则匹配一级标题 `^# .+$` 和二级标题 `^## .+$`
2. 无标题 → 提示用户 "未识别到标题格式，将按行拆分"
3. 一级标题 > 1 个 → 以一级标题为分隔符拆分为段落
4. 否则若有二级标题 → 以二级标题为分隔符拆分为段落
5. 每个段落包含标题行 + 标题下正文

### 6.4 EPUB 解析 (EpubParser)
EPUB 本质为 ZIP 包, 解析流程:
1. `ZipInputStream` 解压, 读取 `META-INF/container.xml`
2. 从 container.xml 获取 OPF 文件路径
3. 解析 OPF: 提取 manifest items + spine 顺序, 确定 XHTML 文件阅读顺序
4. 按 spine 顺序读取 XHTML 文件
5. HTML 文本提取: 将 `<br>`, `</p>`, `</div>`, `</h1-6>` 转为换行, 剥离标签, 解码 HTML 实体
6. 标题拆分: 识别 `<h1>`, `<h2>` 标签, 按标题分块, 逻辑同 Markdown

### 6.5 .rdpoem 格式
- 本质为 JSON, 与 `poetry_data.json` 格式一致
- 包含 `version`, `anthologies` (每个含 `id`, `name`, `entries`)
- 每个 entry 含 `id`, `type` ("LINE"/"PARAGRAPH"), `content`

## 7. Decryption Effect 动画设计

### 7.1 动画原理
1. **占位阶段**: 目标文本区域全部用随机字符填充, 随机字符每 50ms 变换一次 (闪烁)
2. **解密阶段**: 从第一个字符开始, 以 `displaySpeedMsPerChar` 的速度逐个揭示真实字符
3. **完成阶段**: 所有字符揭示完成, 停止闪烁

### 7.2 实现细节 (DecryptionText.kt)
- **随机字符集**: 包含 ASCII、半角片假名、汉字 (日月風雲雨雪花鳥山川海空等), 突出诗意与极客感
- **状态**: `revealedCount` (已揭示字符数) + `scrambleFrame` (闪烁帧计数)
- **协程结构**: 
  - `LaunchedEffect(targetText)` 启动两个并发任务:
    - 闪烁任务: 每 50ms `scrambleFrame++`
    - 揭示任务: 每 `speedMsPerChar` ms `revealedCount++`
  - 揭示完成后 `cancel()` 闪烁任务
- **AnnotatedString**: 已揭示部分用 `revealedColor`, 未揭示部分用 35% 透明度的同色
- **动态字号** (`DecryptionTextSized`): 根据可用宽高、文本行数和最大行长计算字号, 确保不同屏幕尺寸适配

### 7.3 字号计算算法
```
sizeByWidth = (可用宽度dp * 0.85) / (最大行长 * 0.62)
sizeByHeight = (可用高度dp * 0.85) / (行数 * 1.6)
fontSize = min(sizeByWidth, sizeByHeight, 32).coerceAtLeast(8)
```

### 7.4 长文本分块滚动设计

#### 7.4.1 分块策略
当文本总行数超过屏幕可显示行数时，需要将文本分块：
1. **文本测量**: 在 `DecryptionText` 内部使用 `TextMeasurer` 测量文本的实际显示高度（包含自动换行）
2. **分块规则**: 每块包含 `screenLineCount * 0.85` 行（保留15%作为滚动过渡区）
3. **滚动方向**: 从下往上滚动，新内容从下方进入，旧内容从上方退出

#### 7.4.2 动画状态机
```
状态流转:
  IDLE → REVEALING_BLOCK1 → PAUSED_AFTER_BLOCK1 → SCROLLING → REVEALING_BLOCK2 → ... → COMPLETED
```
| 状态 | 说明 |
|------|------|
| IDLE | 初始状态，等待开始 |
| REVEALING_BLOCK_N | 第N块文本正在执行Decryption Effect动画 |
| PAUSED_AFTER_BLOCK_N | 第N块揭示完成，等待显示间隔后开始滚动 |
| SCROLLING | 向上滚动，下一块内容进入屏幕 |
| COMPLETED | 所有块显示完成，进入下一首诗歌循环 |

#### 7.4.3 滚动实现
- 使用 `AnimatedScrollableContainer` 或 `Modifier.verticalScroll` + `Animatable` 实现平滑滚动
- 滚动距离 = 当前块高度 + 过渡区高度
- 滚动时长 = 500ms（固定，确保流畅感）
- 滚动完成后，下一块的占位字符立即开始闪烁

#### 7.4.4 空格动画优化
- 当前问题：空格不参与闪烁，提前暴露单词结构
- 解决方案：空格位置使用特殊占位字符（如 `·` 或随机半角符号）参与闪烁，揭示时替换为真实空格
- 在 `DecryptionText` 的字符处理逻辑中，将空格视为普通字符参与动画

## 8. 主界面循环与时间显示

### 8.1 诗歌显示循环 (MainViewModel)
```
循环开始:
  → 读取最新配置 → 生成诗歌 → 设置 currentPoem + isRevealing=true
  → 等待 (诗歌长度 * 显示速度 + 500ms) → isRevealing=false
  → 记录 intervalStartTime → 等待 displayIntervalSec 秒
  → 回到循环开始
若无数据: 显示提示, 3秒后重试
```
- 每次循环迭代读取 `_uiState.value.config`, 配置变更自动生效
- `generatePoem()` 每次从 `repository.data.value` 读取最新数据

### 8.2 时间显示类型
| 类型 | 计算方式 |
|------|---------|
| 实时时间 | `SimpleDateFormat("HH:mm:ss")` 格式化当前时间 |
| 应用运行时间 | `System.currentTimeMillis() - appStartTime`, 格式 `HH:MM:SS` |
| 刷新倒计时 | `displayIntervalSec - (now - intervalStartTime) / 1000` 秒 |
| 番茄钟倒计时 | 25分钟工作+25分钟休息循环, 格式 `MM:SS` |

### 8.3 屏幕常亮
- `MainScreen` 中通过 `DisposableEffect` 设置 `FLAG_KEEP_SCREEN_ON`
- 进入主界面时添加 flag, 离开时清除 flag
- 仅主界面保持常亮, 其他界面正常息屏

### 8.4 主界面按钮交互
- **默认状态**: 左上角"诗集"按钮和右上角"设置"按钮默认隐藏 (`alpha(0f)`)
- **唤出方式**: 点击屏幕任意位置切换按钮显示状态 (`buttonsVisible` 状态变量)
- **隐藏方式**: 再次点击屏幕或点击按钮后自动隐藏
- **显示效果**: 按钮显示时 `alpha(0.7f)`，与背景形成对比但不突兀

## 9. 导航设计

### 9.1 路由表
| 路由 | 路径 | 参数 | 说明 |
|------|------|------|------|
| Main | `main` | - | 主界面 |
| AnthologyList | `anthology_list` | - | 诗集列表 |
| AnthologyContent | `anthology_content/{anthologyId}` | anthologyId | 诗集内容 |
| Edit | `edit/{anthologyId}/{entryId}` | anthologyId, entryId("new"=新建) | 文本编辑 |
| Config | `config` | - | 配置 |

### 9.2 页面跳转流程
```
Main → AnthologyList (点击左上角编辑图标)
Main → Config (点击右上角设置图标)
AnthologyList → AnthologyContent (点击诗集)
AnthologyList → Edit (新建诗集后直接进入编辑)
AnthologyContent → Edit (点击条目或+按钮)
所有页面 → 返回上一页 (popBackStack)
```

### 9.3 配置界面输入框逻辑
- **本地状态**: 每个数字输入框使用独立的 `inputValue` 本地状态，不直接绑定到 ViewModel
- **输入验证**: `onValueChange` 允许输入空字符串或有效数字，拒绝非数字字符
- **失焦处理**: `onFocusChanged` 监听失焦事件，若输入为空或非数字，自动设置为默认值 `1`
- **涉及输入框**: 显示时间间隔、显示速度、诗歌行数范围（最小/最大）、番茄钟时长

## 10. UI 设计规范

### 10.1 主题
- **配色**: 支持四种配色风格，通过 `ColorSchemeStyle` 枚举切换：
  - 终端绿 (`#0D0D0D` 背景, `#00FF41` 主色)
  - 黑白 (`#1A1A1A` 背景, `#FFFFFF` 主色)
  - 青蓝 (`#0A0A1A` 背景, `#00FFFF` 主色)
  - 琥珀橙 (`#1A1000` 背景, `#FFB000` 主色)
- **字体**: `ArkPixelFontFamily` (像素字体, 源文件: `res/font/ark_pixel_12px.ttf`, 基于 ARK Pixel 12px 等宽中文字体)
- **动态配色**: 通过 `getMaterialColorScheme()` 函数生成完整的 Material3 `ColorScheme`，所有界面元素使用 `MaterialTheme.colorScheme` 而非硬编码颜色
- **主题**: 仅暗色主题, 关闭动态颜色 (Dynamic Color)

### 10.2 组件风格
- 卡片: `RoundedCornerShape(4.dp)`, `surface` 背景色
- 按钮: 自定义 `PixelButton` (方角, monospace 文字)
- 文本前缀: `>` 表示标题, `//` 表示注释/提示, `[L]`/`[P]` 表示条目类型
- 对话框: monospace 字体, 绿色标题

## 11. 权限与隐私
- **无额外权限**: 使用 SAF (Storage Access Framework) 选择文件, 无需 `READ_EXTERNAL_STORAGE`
- **无网络权限**: 纯本地应用, 无网络请求
- **无用户信息收集**: 不获取任何用户隐私数据
- 数据存储于应用沙盒 (`context.filesDir`), 卸载后自动清除

## 12. 依赖清单
| 依赖 | 版本 | 用途 |
|------|------|------|
| androidx-compose-bom | 2026.02.01 | Compose 版本统一管理 |
| androidx-navigation-compose | 2.7.7 | 导航 |
| androidx-lifecycle-viewmodel-compose | 2.7.0 | ViewModel 集成 |
| androidx-datastore-preferences | 1.0.0 | 用户配置持久化 |
| gson | 2.10.1 | JSON 序列化 |
| material-icons-extended | (BOM 管理) | 扩展图标集 |

## 13. 扩展性设计
- **Decryption Effect**: `DecryptionText` 组件接受 `style`, `revealedColor` 参数, 预留后续添加其他显示效果的接口
- **诗歌生成**: `PoetryGenerator` 为 `object`, 可轻松添加新的生成方法
- **文件导入**: `TextImporter` + `FileType` 枚举, 可扩展新文件格式
- **时间显示**: `TimeDisplayType` 枚举, 可添加新的时间显示类型

## 14. 优化记录 (2026-07-15)

### 14.1 UI用户可读性优化
- **拆分方式选项文字**: 将技术术语改为用户易懂的描述：
  - `BY_LINE` → "这是拼贴诗素材"
  - `BY_TITLE` → "这是一本诗集"
  - `NO_SPLIT` → "这是一首诗"

### 14.2 数据更新
- **默认诗集数据**: 从100条扩展为全部1189条诗歌素材，确保用户首次使用时有丰富的诗歌内容

### 14.3 UI配色优化
- **诗集界面**: 所有硬编码颜色（`TerminalGreen`, `TerminalGreenDim`）替换为 `MaterialTheme.colorScheme.primary` 和 `MaterialTheme.colorScheme.onSurfaceVariant`，实现配色风格全局生效

### 14.4 UI交互优化
- **导入按钮拆分**: 将单个"导入文件"按钮拆分为"导入txt文件"和"导入md文件"两个独立按钮
- **自动拆分模式**: txt文件默认使用 `BY_LINE` 模式，md文件默认使用 `BY_TITLE` 模式
- **简化流程**: 移除拆分方式选择对话框，根据文件类型自动确定拆分模式

### 14.5 主界面优化
- **按钮交互**: 按钮默认隐藏（`alpha(0f)`），点击屏幕唤出（`alpha(0.7f)`），再次点击或点击按钮后隐藏
- **状态栏padding**: 使用 `statusBarsPadding()` 修饰符防止按钮与顶部状态栏重叠

### 14.6 新增默认诗集 - 无害预言
- **资源文件**: `res/raw/prophet.json` (1500条)
- **内容类型**: 无害、荒诞、无意义的预言和指令
- **设计原则**: 不涉及传统迷信、不制造焦虑、不预示获利
- **加载逻辑**: 新增 `loadDefaultAnthology()` 方法支持加载多个默认诗集并合并
- **更新文件**: `JsonDataStore.kt` - `loadDefaultData()` 方法扩展为加载两个默认诗集
