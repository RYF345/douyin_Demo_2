package com.example.douyinline.ui.video;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.douyinline.R;
import com.example.douyinline.bean.VideoBean;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.widget.ImageView;

import java.util.List;

/**
 * 全屏沉浸式视频播放页面
 * 
 * 核心流程：
 * 1. 初始化 PlayerPool（预创建播放器实例）
 * 2. 初始化 ViewPager2 和 Adapter
 * 3. 通过 OnPageChangeCallback 控制当前播放的视频
 * 4. 生命周期管理：onPause 暂停，onResume 恢复
 */
public class VideoPlayActivity extends AppCompatActivity {
    private static final String TAG = "VideoPlayActivity";
    
    private VideoPlayViewModel viewModel;
    private CommentViewModel commentViewModel;
    private ViewPager2 vpFullVideo;
    private VideoPlayerPagerAdapter adapter;
    private int currentPosition = 0;
    
    // 封面占位图（用于转场，避免黑屏）
    private ImageView ivCoverPlaceholder;
    private boolean isCoverHidden = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 设置全屏沉浸式
        setupFullScreen();
        
        setContentView(R.layout.activity_video_play);

        int startPosition = getIntent().getIntExtra("startPosition", 0);
        int coverResId = getIntent().getIntExtra("coverResId", 0);
        this.currentPosition = startPosition;

        // 初始化封面占位图（避免转场黑屏）
        initCoverPlaceholder(coverResId);

        // 初始化 ViewModel
        viewModel = new ViewModelProvider(this).get(VideoPlayViewModel.class);
        viewModel.initFromRepository(startPosition);
        viewModel.initPlayerPool(this);
        commentViewModel = new ViewModelProvider(this).get(CommentViewModel.class);

        // 初始化 ViewPager2
        initViewPager(startPosition);

        // 观察数据
        observeViewModel();
        
        android.util.Log.d(TAG, "onCreate: startPosition=" + startPosition);
    }

    /**
     * 初始化封面占位图
     * 在视频加载完成前显示封面，避免黑屏
     */
    private void initCoverPlaceholder(int coverResId) {
        ivCoverPlaceholder = findViewById(R.id.iv_cover_placeholder);
        if (coverResId != 0) {
            // 使用 Glide 加载封面图
            Glide.with(this)
                    .load(coverResId)
                    .into(ivCoverPlaceholder);
            ivCoverPlaceholder.setVisibility(View.VISIBLE);
        } else {
            ivCoverPlaceholder.setVisibility(View.GONE);
        }
    }

    /**
     * 淡出封面占位图（视频准备好后调用）
     */
    public void hideCoverPlaceholder() {
        if (isCoverHidden || ivCoverPlaceholder == null) {
            return;
        }
        isCoverHidden = true;
        
        // 淡出动画
        ivCoverPlaceholder.animate()
                .alpha(0f)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        ivCoverPlaceholder.setVisibility(View.GONE);
                    }
                })
                .start();
    }

    /**
     * 设置全屏沉浸式模式
     */
    private void setupFullScreen() {
        // 设置全屏
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            // 隐藏状态栏和导航栏
            controller.hide(WindowInsetsCompat.Type.systemBars());
            // 设置行为：滑动时短暂显示
            controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }
        // 保持屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    private void initViewPager(int startPosition) {
        vpFullVideo = findViewById(R.id.vp_full_video);
        
        // 设置垂直滚动
        vpFullVideo.setOrientation(ViewPager2.ORIENTATION_VERTICAL);

        // 设置预加载页面数（左右各1页，加上当前页共3页，正好匹配播放器池大小）
        vpFullVideo.setOffscreenPageLimit(1);

        // 设置适配器
        List<VideoBean> videoList = viewModel.getVideoListLiveData().getValue();
        adapter = new VideoPlayerPagerAdapter(viewModel.getPlayerPool(), videoList);
        vpFullVideo.setAdapter(adapter);

        // 设置视频准备好的监听器（用于淡出封面占位图）
        adapter.setOnVideoReadyListener(() -> hideCoverPlaceholder());

        // 设置初始位置，不带滚动动画
        vpFullVideo.setCurrentItem(startPosition, false);
        
        // 初始播放位置（需要延迟一帧，确保 ViewHolder 已经 attached）
        vpFullVideo.post(() -> {
            adapter.playAt(startPosition);
        });

        // 设置页面切换监听
        vpFullVideo.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                android.util.Log.d(TAG, "onPageSelected: " + position);
                currentPosition = position;
                viewModel.onPageSelected(position);
                // 切换播放
                adapter.playAt(position);
            }
        });

        // 设置交互监听
        adapter.setOnInteractionListener(new VideoPlayerPagerAdapter.OnInteractionListener() {
            @Override
            public void onLikeClick(int position) {
                viewModel.toggleLikeClick(position);
            }
            @Override
            public void onDoubleTapLike(int position) {
                List<VideoBean> videoList = viewModel.getVideoListLiveData().getValue();
                if (videoList != null && videoList.size() > position && position >= 0) {
                    // 仅在未点赞时点赞
                    if (!videoList.get(position).isLiked()) {
                        viewModel.toggleLikeClick(position);
                    }
                }
            }
            @Override
            public void onCollectClick(int position) {
                viewModel.toggleCollectClick(position);
            }

            @Override
            public void onShareClick(int position) {
                // TODO：分享按钮监听
            }

            @Override
            public void onCommentClick(int position) {
                List<VideoBean> videoList = viewModel.getVideoListLiveData().getValue();
                if (videoList != null && videoList.size() > position && position >= 0) {
                    VideoBean video = videoList.get(position);
                    // 显示评论BottomSheet
                    if (video != null) {
                        CommentBottomSheetFragment commentSheet= CommentBottomSheetFragment.newInstance(video, position);
                        commentSheet.show(getSupportFragmentManager(), "commentSheet");
                    }else{
                        CommentBottomSheetFragment commentSheet= new CommentBottomSheetFragment();
                        android.util.Log.e(TAG, "onCommentClick: video is null");
                        commentSheet.show(getSupportFragmentManager(), "commentSheet");
                    }
                }
            }
        });
    }

    private void observeViewModel() {
        // 观察视频列表变化（用于后续动态加载更多视频时更新列表）
        viewModel.getVideoListLiveData().observe(this, videoList -> {
            if (videoList != null && adapter != null) {
                adapter.setVideoList(videoList);
            }
        });

        // 观察点赞事件
        viewModel.getLikeEventLiveData().observe(this, new Observer<VideoPlayViewModel.LikeEvent>() {
            @Override
            public void onChanged(VideoPlayViewModel.LikeEvent likeEvent) {
                if (likeEvent != null) {
                    VideoPlayerPagerAdapter.VideoPlayerViewHolder holder = adapter.getViewHolder(likeEvent.getPosition());
                    if (holder != null) {
                        // 更新点赞UI
                        adapter.updateLikeUI(holder, likeEvent.isLiked());
                        holder.tvLikeCount.setText(formatCount(likeEvent.getLikeCount()));
                    }
                }
            }
        });

        // 观察收藏事件
        viewModel.getCollectEventLiveData().observe(this, new Observer<VideoPlayViewModel.CollectEvent>() {
            @Override
            public void onChanged(VideoPlayViewModel.CollectEvent collectEvent) {
                if (collectEvent != null) {
                    VideoPlayerPagerAdapter.VideoPlayerViewHolder holder = adapter.getViewHolder(collectEvent.getPosition());
                    if (holder != null) {
                        // 更新收藏UI
                        adapter.updateCollectUI(holder, collectEvent.isCollected());
                        holder.tvCollectCount.setText(formatCount(collectEvent.getCollectCount()));
                    }
                }
            }
        });

        // 观察评论事件
        commentViewModel.getCommentCountToTal().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer count) {
                int position = commentViewModel.getCurrentVideoPosition().getValue();
                if (position > 0 && position < adapter.getItemCount()) {
                    VideoPlayerPagerAdapter.VideoPlayerViewHolder holder = adapter.getViewHolder(position);
                    if(holder != null){
                        holder.tvCommentCount.setText(formatCount(count));
                    }
                }

            }
        });
    }
    //
    private String formatCount(int count) {
        if (count >= 10000) {
            return String.format("%.1fw", count / 10000.0);
        }
        return String.valueOf(count);
    }

    @Override
    protected void onResume() {
        super.onResume();
        android.util.Log.d(TAG, "onResume");
        // 恢复播放
        if (adapter != null) {
            adapter.resumeCurrent();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        android.util.Log.d(TAG, "onPause");
        // 暂停播放
        if (adapter != null) {
            adapter.pauseCurrent();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        android.util.Log.d(TAG, "onDestroy");
        // PlayerPool 的释放由 ViewModel.onCleared() 处理
    }
}
