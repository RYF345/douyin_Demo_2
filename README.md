# DouyinLine

<p align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp" alt="DouyinLine Logo" width="120"/>
</p>

<p align="center">
  <b>短视频流Demo</b>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-green.svg" alt="Platform"/>
  <img src="https://img.shields.io/badge/Language-Java-orange.svg" alt="Language"/>
  <img src="https://img.shields.io/badge/MinSDK-24-blue.svg" alt="MinSDK"/>
  <img src="https://img.shields.io/badge/Architecture-MVVM-purple.svg" alt="Architecture"/>
</p>

---

## ✨ 功能特性
- **全屏沉浸式播放** - 支持垂直滑动切换视频，沉浸式观看体验
- **双列瀑布流推荐** - 首页采用瀑布流布局展示视频封面
- **社交互动** - 支持点赞、收藏、评论交互功能
- **评论系统** - 底部弹窗展示评论列表，支持发送评论
- **下拉刷新** - 支持下拉刷新和上拉加载更多
- **双击点赞** - 双击视频屏幕触发点赞动画
- **转场** - 从推荐页到播放页的封面过渡动画

---

## 核心界面

| 推荐页 | 全屏播放 | 评论弹窗 |
|:-----:|:-------:|:-------:|
| 双列瀑布流 | 沉浸式播放 | 底部评论 |

---

## 技术架构

### 整体架构

```
┌─────────────────────────────────────────────────────────┐
│                      UI Layer                           │
│   Activity / Fragment / Adapter / ViewHolder            │
├─────────────────────────────────────────────────────────┤
│                   ViewModel Layer                       │
│   RecommendViewModel / VideoPlayViewModel / ...         │
├─────────────────────────────────────────────────────────┤
│                  Repository Layer                       │
│              VideoRepository (Singleton)                │
├─────────────────────────────────────────────────────────┤
│                    Model Layer                          │
│         VideoBean / AuthorBean / CommentBean            │
└─────────────────────────────────────────────────────────┘
```

### 技术选型

| 技术领域 | 选型 | 版本 |
|---------|------|------|
| 开发语言 | Java | 11 |
| 视频播放 | Media3 ExoPlayer | Latest |
| 图片加载 | Glide | 4.16.0 |
| 架构组件 | ViewModel + LiveData | 2.6.2 |
| 页面切换 | ViewPager2 | 1.0.0 |
| 列表组件 | RecyclerView | 1.3.2 |
| UI 设计 | Material Design | 1.9.0 |
| 下拉刷新 | SmartRefreshLayout | 2.1.0 |

---

## 📁 项目结构

```
app/src/main/java/com/example/douyinline/
├── bean/                           # 数据模型
│   ├── VideoBean.java              # 视频实体
│   ├── AuthorBean.java             # 作者实体
│   └── CommentBean.java            # 评论实体
├── repository/                     # 数据仓库
│   └── VideoRepository.java        # 视频数据管理（单例）
├── event/                          # 事件类
│   └── NavigationEvent.java        # 页面导航事件
├── ui/                             # UI 层
│   ├── home/                       # 首页模块
│   │   ├── HomeFragment.java
│   │   ├── HomeViewPagerAdapter.java
│   │   └── recommend/              # 推荐页
│   │       ├── RecommendFragment.java
│   │       ├── RecommendViewModel.java
│   │       └── VideoCardAdapter.java
│   ├── video/                      # 视频播放模块
│   │   ├── VideoPlayActivity.java      # 全屏播放页
│   │   ├── VideoPlayViewModel.java     # 播放页 ViewModel
│   │   ├── VideoPlayerPagerAdapter.java # 视频列表适配器
│   │   ├── PlayerPool.java             # 播放器对象池
│   │   ├── CommentBottomSheetFragment.java # 评论弹窗
│   │   ├── CommentViewModel.java       # 评论 ViewModel
│   │   └── CommentSheetAdapter.java    # 评论列表适配器
│   ├── friends/                    # 朋友页（占位）
│   ├── message/                    # 消息页（占位）
│   └── me/                         # 个人页（占位）
└── MainActivity.java               # 主入口
```

---

## 环境要求

- **Android Studio**: Arctic Fox 或更高版本
- **JDK**: 11 或更高版本
- **Gradle**: 8.x
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 36

---

## 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/yourusername/douyinline.git
cd douyinline
```

### 2. 打开项目

使用 Android Studio 打开项目目录，等待 Gradle 同步完成。

### 3. 运行应用

连接 Android 设备或启动模拟器，点击 **Run** 按钮运行应用。

---

## 核心实现

### 播放器对象池 (PlayerPool)

为避免频繁创建销毁 ExoPlayer 导致的性能问题和 MediaCodec 资源耗尽，项目实现了播放器对象池：

```java
public class PlayerPool {
    private final List<ExoPlayer> availablePlayers = new ArrayList<>();
    public final int MAX_PLAYERS = 2;  // 延迟加载策略
    
    public ExoPlayer acquirePlayer() {
        // 从池中获取或创建新的播放器
    }
    
    public void releasePlayer(ExoPlayer player) {
        // 重置状态后归还到池中
    }
}
```

### 视频滑动切换

使用 ViewPager2 + RecyclerView.Adapter 实现垂直滑动切换：

```java
// 垂直滚动方向
vpFullVideo.setOrientation(ViewPager2.ORIENTATION_VERTICAL);

// 预加载相邻页面
vpFullVideo.setOffscreenPageLimit(1);

// 页面切换监听
vpFullVideo.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
    @Override
    public void onPageSelected(int position) {
        adapter.playAt(position);  // 切换播放
    }
});
```

### 双击点赞动画

```java
GestureDetector gestureDetector = new GestureDetector(context, 
    new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            showLikeAnimation(holder, e.getX(), e.getY());
            return true;
        }
    });
```

---

## 依赖库

```kotlin
dependencies {
    // 视频播放
    implementation(libs.media3.ui)
    implementation(libs.media3.exoplayer)
    
    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata:2.6.2")
    
    // ViewPager2
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    
    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    
    // 图片加载
    implementation("com.github.bumptech.glide:glide:4.16.0")
    
    // Material Design
    implementation("com.google.android.material:material:1.9.0")
    
    // 下拉刷新
    implementation("io.github.scwang90:refresh-layout-kernel:2.1.0")
    implementation("io.github.scwang90:refresh-header-classics:2.1.0")
    implementation("io.github.scwang90:refresh-footer-classics:2.1.0")
}
```

---

## 功能概览

- 首页双列瀑布流推荐
- 全屏沉浸式视频播放
- 垂直滑动切换视频
- 点赞 / 收藏功能
- 评论底部弹窗
- 下拉刷新 / 加载更多
- 双击点赞动画
- 页面转场优化

---
