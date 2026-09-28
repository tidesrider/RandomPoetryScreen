# Bug 记录文档 (BUG_LOG)

## 目录
- [Bug #001: TextImporter 调用不存在的 EpubParser.extractText 方法](#bug-001-textimporter-调用不存在的-epubparserextracttext-方法)

---

## Bug #001: TextImporter 调用不存在的 EpubParser.extractText 方法

### 基本信息
| 项目 | 内容 |
|------|------|
| **编号** | BUG-001 |
| **日期** | 2026-07-08 |
| **严重程度** | 高 (编译错误，项目无法构建) |
| **状态** | ✅ 已修复 |

### 问题描述
在 Android Studio 中执行调试时，编译器报错：
```
Unresolved reference 'extractText'
File: app/src/main/java/online/dicemeow/dev_randompoetryscreen/data/importer/TextImporter.kt:59:31
```

### 原因分析
1. `TextImporter.kt` 第59行调用了 `EpubParser.extractText(content)`
2. 查看 [EpubParser.kt](file:///d:/dev_RandomPoetryScreen/app/src/main/java/online/dicemeow/dev_randompoetryscreen/data/importer/EpubParser.kt)，`EpubParser` 类中**不存在**名为 `extractText` 的公共方法
3. 深层原因：EPUB 文件是二进制 ZIP 格式，需要读取 `ByteArray` 解析，而 `TextImporter.import()` 接收的是 `String content` 参数，不适合处理 EPUB

### 修复方案
从 `TextImporter` 中移除 EPUB 的文本解析逻辑：
- 移除错误的 `importEpub()` 方法
- 在 `import()` 的 `when` 语句中，EPUB 分支直接返回错误提示

**正确的 EPUB 处理路径**已在 [FileImportService.kt](file:///d:/dev_RandomPoetryScreen/app/src/main/java/online/dicemeow/dev_randompoetryscreen/data/importer/FileImportService.kt) 中实现：
```
FileImportService.importFile(uri, fileName, splitMode)
  → FileType.EPUB → readBytes(uri) → EpubParser.parse(bytes, splitMode)
  → FileType.TXT/MARKDOWN → readText(uri) → TextImporter.import(text, fileName, splitMode, fileType)
```

### 涉及文件
| 文件 | 修改类型 |
|------|----------|
| [TextImporter.kt](file:///d:/dev_RandomPoetryScreen/app/src/main/java/online/dicemeow/dev_randompoetryscreen/data/importer/TextImporter.kt) | 修改 |

### 修复前代码 (错误)
```kotlin
private fun importEpub(content: String, splitMode: SplitMode): ImportResult {
    val text = EpubParser.extractText(content)  // ❌ 方法不存在
    return when (splitMode) {
        ...
        SplitMode.BY_TITLE -> EpubParser.splitByTitle(text)  // ❌ 方法签名不匹配
    }
}
```

### 修复后代码 (正确)
```kotlin
fun import(content: String, fileName: String, splitMode: SplitMode, fileType: FileType): ImportResult {
    return when (fileType) {
        FileType.TXT -> importTxt(content, splitMode)
        FileType.MARKDOWN -> importMarkdown(content, splitMode)
        FileType.EPUB -> ImportResult(emptyList(), message = "EPUB文件需要二进制解析")
    }
}
```

---

## Bug #002: OutlinedTextField containerColor 参数在新版 Compose 中不可用

### 基本信息
| 项目 | 内容 |
|------|------|
| **编号** | BUG-002 |
| **日期** | 2026-07-08 |
| **严重程度** | 高 (编译错误) |
| **状态** | ✅ 已修复 |

### 问题描述
```
None of the following candidates is applicable: 
fun OutlinedTextField(state: TextFieldState, ...)
fun OutlinedTextField(value: String, onValueChange: (String) -> Unit, ...)
fun OutlinedTextField(value: TextFieldValue, onValueChange: (TextFieldValue) -> Unit, ...)
File: EditScreen.kt:114:13
```

### 原因分析
在 Compose Material3 BOM 2026.02.01 中，`OutlinedTextField` 的 API 发生变化：
- `containerColor` 不再是直接参数
- 需要通过 `colors = OutlinedTextFieldDefaults.colors(containerColor = ...)` 设置

### 修复方案
1. 添加 import: `import androidx.compose.material3.OutlinedTextFieldDefaults`
2. 将 `containerColor = MaterialTheme.colorScheme.surface` 替换为:
   ```kotlin
   colors = OutlinedTextFieldDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
   ```

### 涉及文件
| 文件 | 修改类型 |
|------|----------|
| [EditScreen.kt](file:///d:/dev_RandomPoetryScreen/app/src/main/java/online/dicemeow/dev_randompoetryscreen/ui/editor/EditScreen.kt) | 修改 |

---

## Bug #003: PreferencesStore 中 enumValueOfOrNull 无法解析

### 基本信息
| 项目 | 内容 |
|------|------|
| **编号** | BUG-003 |
| **日期** | 2026-07-08 |
| **严重程度** | 高 (编译错误) |
| **状态** | ✅ 已修复 |

### 问题描述
```
Unresolved reference 'enumValueOfOrNull'
File: app/src/main/java/online/dicemeow/dev_randompoetryscreen/data/store/PreferencesStore.kt:43:32
```

### 原因分析
1. `PreferencesStore` 类中定义了私有方法 `enumValueOfOrNull`（第89行）
2. 在 `userConfig` Flow 的 `map` 转换中调用 `enumValueOfOrNull` 时，Lambda 内部的 `this` 指向的是 Flow 的收集器上下文，而非 `PreferencesStore` 实例
3. 编译器无法找到该方法，因为它不在当前作用域内

### 修复方案
在所有调用 `enumValueOfOrNull` 的地方添加 `this.` 前缀，显式指定调用对象为 `PreferencesStore` 实例：
```kotlin
this.enumValueOfOrNull<PoetryGenerationMethod>(...)
```

### 涉及文件
| 文件 | 修改类型 |
|------|----------|
| [PreferencesStore.kt](file:///d:/dev_RandomPoetryScreen/app/src/main/java/online/dicemeow/dev_randompoetryscreen/data/store/PreferencesStore.kt) | 修改 |

### 修复前后代码对比
```kotlin
// 修复前
generationMethod = enumValueOfOrNull<PoetryGenerationMethod>(
    prefs[Keys.GENERATION_METHOD]
) ?: PoetryGenerationMethod.RANDOM_PARAGRAPH

// 修复后
generationMethod = this.enumValueOfOrNull<PoetryGenerationMethod>(
    prefs[Keys.GENERATION_METHOD]
) ?: PoetryGenerationMethod.RANDOM_PARAGRAPH
```

---

## Bug #004: PRD 需求更新-2026-07-08 优化需求未完整实现

### 基本信息
| 项目 | 内容 |
|------|------|
| **编号** | BUG-004 |
| **日期** | 2026-07-08 |
| **严重程度** | 中 (功能未按需求实现) |
| **状态** | ✅ 已修复 |

### 问题描述
根据 PRD.md "需求更新-2026-07-08" 章节，以下优化需求未完整实现：
1. **文本显示优化**：主界面文本仅实现左右居中，没有上下居中
2. **长文本分块滚动**：长文本没有分块滚动渲染，超出屏幕高度的文本不可见
3. **配色方案全局生效**：配色方案仅影响"配置""诗集""时间"等少数元素，没有全局生效

### 原因分析
1. **文本上下居中**：`MainScreen` 中使用 `Box(Alignment.Center)` 包裹文本组件，但 `DecryptionTextScrollable` 使用 `fillMaxWidth()` 导致垂直方向未居中
2. **长文本分块滚动**：`DecryptionTextScrollable` 组件只是简单调用 `DecryptionText`，没有实现文本分块和滚动逻辑
3. **配色方案全局生效**：主题系统使用固定的 `GeekColorScheme`，没有根据用户配置动态切换 `MaterialTheme.colorScheme`

### 修复方案
1. **文本上下居中**：在 `MainScreen` 中使用嵌套 `Box`，外层 `Box` 使用 `Alignment.Center`，内层 `Box` 使用 `contentAlignment = Alignment.Center`
2. **长文本分块滚动**：重构 `DecryptionTextScrollable`，添加以下逻辑：
   - 将文本按行数分块（每块6行）
   - 使用 `Layout` 自定义布局和 `Animatable` 实现滚动动画
   - 当前块解密完成后等待显示间隔，然后滚动到下一块
3. **配色方案全局生效**：
   - 修改 `Color.kt`，添加 `getMaterialColorScheme()` 函数，为每种配色风格生成完整的 `ColorScheme`
   - 修改 `Theme.kt`，主题函数接收 `colorSchemeStyle` 参数
   - 修改 `MainActivity.kt`，读取用户配置并传递给主题

### 涉及文件
| 文件 | 修改类型 |
|------|----------|
| [Color.kt](file:///d:/dev_RandomPoetryScreen/app/src/main/java/online/dicemeow/dev_randompoetryscreen/ui/theme/Color.kt) | 修改 |
| [Theme.kt](file:///d:/dev_RandomPoetryScreen/app/src/main/java/online/dicemeow/dev_randompoetryscreen/ui/theme/Theme.kt) | 修改 |
| [MainActivity.kt](file:///d:/dev_RandomPoetryScreen/app/src/main/java/online/dicemeow/dev_randompoetryscreen/MainActivity.kt) | 修改 |
| [MainScreen.kt](file:///d:/dev_RandomPoetryScreen/app/src/main/java/online/dicemeow/dev_randompoetryscreen/ui/main/MainScreen.kt) | 修改 |
| [DecryptionText.kt](file:///d:/dev_RandomPoetryScreen/app/src/main/java/online/dicemeow/dev_randompoetryscreen/ui/components/DecryptionText.kt) | 修改 |

---

## Bug 记录格式模板

### Bug #XXX: [标题]

| 项目 | 内容 |
|------|------|
| **编号** | BUG-XXX |
| **日期** | YYYY-MM-DD |
| **严重程度** | 高/中/低 |
| **状态** | 🔴 未修复 / 🟡 修复中 / ✅ 已修复 |

### 问题描述
[描述问题现象和报错信息]

### 原因分析
[分析问题根本原因]

### 修复方案
[描述修复思路和方案]

### 涉及文件
| 文件 | 修改类型 |
|------|----------|
| [文件名](file:///path/to/file) | 修改/新增/删除 |

### 修复前后代码对比
```kotlin
// 修复前
...

// 修复后
...
```
