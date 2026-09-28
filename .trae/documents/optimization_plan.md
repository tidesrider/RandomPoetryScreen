# 优化需求实现计划

## 1. 需求分析总结

| 序号 | 需求 | 类型 | 涉及文件 |
|------|------|------|----------|
| 1 | 拆分方式选项重命名（"按行拆分"→"这是拼贴诗素材"等） | UI优化 | AnthologyListScreen.kt |
| 2 | 将poem.txt所有行（1189行）录入default_poetry.json | 数据更新 | default_poetry.json |
| 3 | 诗集界面字体颜色随配色风格变化 | Bug修复 | AnthologyListScreen.kt |
| 4 | 导入按钮拆分为"导入txt文件"和"导入md文件" | UI交互优化 | AnthologyListScreen.kt |
| 5 | 主界面按钮与顶部状态栏重叠问题 | UI优化 | MainScreen.kt |

## 2. 详细实现步骤

### 2.1 需求1：拆分方式选项重命名

**修改文件**：`app/src/main/java/online/dicemeow/dev_randompoetryscreen/ui/anthology/AnthologyListScreen.kt`

**修改位置**：第336-342行

**修改内容**：
```kotlin
// 修改前
val splitOptions = modes.map { mode ->
    when (mode) {
        SplitMode.BY_LINE -> "按行拆分" to SplitMode.BY_LINE
        SplitMode.BY_TITLE -> "按标题拆分" to SplitMode.BY_TITLE
        SplitMode.NO_SPLIT -> "不拆分(整体存储)" to SplitMode.NO_SPLIT
    }
}

// 修改后
val splitOptions = modes.map { mode ->
    when (mode) {
        SplitMode.BY_LINE -> "这是拼贴诗素材" to SplitMode.BY_LINE
        SplitMode.BY_TITLE -> "这是一本诗集" to SplitMode.BY_TITLE
        SplitMode.NO_SPLIT -> "这是一首诗" to SplitMode.NO_SPLIT
    }
}
```

### 2.2 需求2：将poem.txt所有行录入default_poetry.json

**修改文件**：`app/src/main/res/raw/default_poetry.json`

**处理步骤**：
1. 读取 `/poem_source/poem.txt` 文件（共1189行）
2. 将每一行作为一条 LINE 类型的 PoetryEntry
3. 生成完整的JSON结构，包含一个名为"随机诗歌"的诗集

**数据结构**：
```json
{
  "version": "1.0",
  "anthologies": [
    {
      "id": "default-anthology",
      "name": "随机诗歌",
      "entries": [
        {
          "id": "entry-0001",
          "type": "LINE",
          "content": "淋湿的钟声"
        },
        {
          "id": "entry-0002",
          "type": "LINE", 
          "content": "贩卖月光的旧书店"
        }
        // ... 共1189条
      ]
    }
  ]
}
```

### 2.3 需求3：诗集界面字体颜色随配色风格变化

**修改文件**：`app/src/main/java/online/dicemeow/dev_randompoetryscreen/ui/anthology/AnthologyListScreen.kt`

**修改内容**：
1. 删除第60-61行的硬编码颜色导入
   ```kotlin
   import online.dicemeow.dev_randompoetryscreen.ui.theme.TerminalGreen
   import online.dicemeow.dev_randompoetryscreen.ui.theme.TerminalGreenDim
   ```
2. 将所有 `TerminalGreen` 替换为 `MaterialTheme.colorScheme.primary`
3. 将所有 `TerminalGreenDim` 替换为 `MaterialTheme.colorScheme.onSurfaceVariant`

**涉及位置**：
- 第142行：TopAppBar标题颜色
- 第147行：返回图标颜色
- 第154行：合并图标颜色
- 第158行：取消图标颜色
- 第162行：导入.rdpoem图标颜色
- 第165行：导出图标颜色
- 第178行：FloatingActionButton内容颜色
- 第213行：暂无诗集提示文字颜色
- 第251行：新建诗集对话框标题颜色
- 第280行：重命名对话框标题颜色
- 第307行：合并对话框标题颜色
- 第345行：拆分方式选择对话框标题颜色
- 第417行：导入提示对话框标题颜色
- 第428行：导出提示对话框标题颜色
- 第440行：删除对话框标题颜色
- 第491行：诗集名称颜色
- 第497行：条目数量颜色
- 第502行：重命名图标颜色
- 第505行：删除图标颜色
- 第527行：PixelButton文字颜色

### 2.4 需求4：导入按钮拆分为两个

**修改文件**：`app/src/main/java/online/dicemeow/dev_randompoetryscreen/ui/anthology/AnthologyListScreen.kt`

**修改内容**：
1. 将第189-202行的单个"导入文件"按钮替换为两个按钮
2. 第一个按钮：导入txt文件，仅打开txt格式
3. 第二个按钮：导入md文件，仅打开md格式
4. 移除拆分方式选择对话框，根据按钮类型自动确定拆分模式

**修改位置**：
- 第189-202行：替换导入按钮区域
- 第89-107行：修改importLauncher逻辑，改为两个独立的launcher
- 第334-379行：简化或移除拆分方式选择对话框

### 2.5 需求5：主界面按钮与顶部状态栏重叠问题

**修改文件**：`app/src/main/java/online/dicemeow/dev_randompoetryscreen/ui/main/MainScreen.kt`

**分析**：
- 问题原因：主界面按钮使用 `Alignment.TopStart` 和 `Alignment.TopEnd` 定位在屏幕顶部，但没有考虑状态栏高度
- 解决方案：使用 `WindowInsets.statusBars` 获取状态栏高度，为按钮区域添加顶部padding

**修改内容**：
```kotlin
// 添加导入
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.window.WindowInsets
import androidx.compose.ui.window.statusBarsPadding

// 修改按钮区域padding
Row(
    modifier = Modifier
        .align(Alignment.TopStart)
        .padding(top = 12.dp, start = 12.dp)
        .statusBarsPadding()  // 添加状态栏padding
        .alpha(if (buttonsVisible) 0.7f else 0f)
)
```

## 3. 潜在风险与注意事项

### 3.1 default_poetry.json 文件大小
- poem.txt 共1189行，生成的JSON文件约100KB左右
- Android raw资源文件无大小限制，不会影响应用性能

### 3.2 导入按钮拆分后的逻辑
- 需要确保两种文件类型导入后都能正确处理
- 需要维护原有的目标诗集选择流程

### 3.3 状态栏padding兼容性
- `statusBarsPadding()` 是Compose的标准API，兼容所有支持的Android版本
- 需要确保主界面使用了 `WindowCompat.setDecorFitsSystemWindows(window, false)`

## 4. 验证计划

| 需求 | 验证方式 |
|------|----------|
| 1 | 在诗集界面导入文件，确认拆分方式选项文字已更新 |
| 2 | 首次启动应用，确认默认诗集包含1189条诗歌素材 |
| 3 | 切换配色风格，确认诗集界面所有文字颜色随之变化 |
| 4 | 确认诗集界面有两个独立的导入按钮，分别导入txt和md文件 |
| 5 | 在开启状态栏的设备上测试，确认按钮不被状态栏遮挡 |

## 5. 实施顺序

1. 需求2（数据更新）- 优先更新默认数据
2. 需求3（配色修复）- 修复视觉问题
3. 需求1（选项重命名）- UI文本优化
4. 需求4（按钮拆分）- UI交互优化
5. 需求5（状态栏问题）- UI布局优化