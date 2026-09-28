# 拾歌/Random Poetry Screen
**[English](README_EN.md), [中文](README.md)**

一个纯本地、无服务端的“类屏保”安卓原生应用。打开应用时，以 Decryption Effect（解密动画）风格随机显示诗歌，占据手机屏幕，为用户在无意识地想要使用手机娱乐时，提供一个缓冲窗口。

## 核心功能

- **随机诗歌显示**：以解密动画风格逐字揭示随机生成的诗歌，屏幕常亮
- **诗歌生成**：支持随机段落、随机组合行、随机组合段落和行三种生成方式
- **诗集管理**：创建诗集，编写/导入诗歌素材，支持按行/按段落存储
- **文件导入**：支持 TXT（按行拆分）和 Markdown（按标题拆分）格式导入
- **配色风格**：终端绿、黑白、青蓝、琥珀橙四种极客风配色
- **时间显示**：刷新倒计时、应用运行时间、番茄钟倒计时、实时时间
- **默认诗集**：内置「随机诗歌」（1189条）和「无害预言」（1500条）两个默认诗集

## 技术栈

| 项目 | 选型 |
|------|------|
| 语言 | Kotlin |
| UI | Jetpack Compose + Material3 |
| 架构 | MVVM |
| 导航 | Navigation Compose |
| 持久化 | JSON 文件（Gson）+ DataStore Preferences |
| 最低 SDK | 24（Android 7.0） |
| 目标 SDK | 36 |

## 项目结构

```
app/src/main/java/online/dicemeow/dev_randompoetryscreen/
├── MainActivity.kt              # 应用入口
├── AppContainer.kt               # 依赖容器
├── data/
│   ├── model/Models.kt           # 数据模型
│   ├── store/
│   │   ├── JsonDataStore.kt      # 诗集数据持久化
│   │   └── PreferencesStore.kt   # 用户配置持久化
│   ├── repository/PoetryRepository.kt
│   ├── generator/PoetryGenerator.kt
│   └── importer/                 # 文件导入解析
├── ui/
│   ├── theme/                    # 主题、配色、字体
│   ├── components/DecryptionText.kt  # 解密动画组件
│   ├── navigation/               # 路由与导航
│   ├── main/                     # 主界面
│   ├── anthology/                # 诗集管理
│   ├── editor/                   # 文本编辑
│   └── config/                   # 配置界面
└── res/
    ├── font/ark_pixel_12px.ttf   # 像素字体
    ├── raw/
    │   ├── default_poetry.json   # 默认诗集：随机诗歌
    │   └── prophet.json          # 默认诗集：无害预言
    └── values/strings.xml        # 应用名称：拾歌
```

## 构建

### 环境要求

- Android Studio
- JDK 11+
- Android SDK 36（路径配置在 `local.properties` 的 `sdk.dir`）

### 打包 APK

```bash
# Debug APK
./gradlew assembleDebug

# Release APK
./gradlew assembleRelease
```

生成的 APK 位于 `app/build/outputs/apk/`，文件名格式为 `RandomPoetryScreen-{debug|release}.apk`。

## 文档

- [PRD.md](Documents/PRD.md) - 产品需求文档
- [LLD.md](Documents/LLD.md) - 底层设计文档
- [BUG_LOG.md](Documents/BUG_LOG.md) - Bug 记录

## 权限

本应用不申请任何系统权限，不获取用户信息，不联网。

## 许可

MIT
