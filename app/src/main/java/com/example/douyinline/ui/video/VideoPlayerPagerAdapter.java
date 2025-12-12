package com.example.douyinline.ui.video;

import android.content.Context;
import android.net.Uri;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.Log;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.douyinline.R;
import com.example.douyinline.bean.VideoBean;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 视频播放页面 ViewPager2 适配器
 * 
 * 核心逻辑（延迟加载策略）：
 * 1. onBindViewHolder: 只绑定 UI 数据，不准备播放器
 * 2. playAt: 外部调用，只为当前播放位置准备播放器并播放 (AI实现)
 * 3. prepareAndPlay 等: 切换页面时，释放旧播放器，为新页面准备播放器 (AI实现)
 * 这样可以避免同时准备多个 4K 视频导致 MediaCodec 资源不足
 */
public class VideoPlayerPagerAdapter extends RecyclerView.Adapter<VideoPlayerPagerAdapter.VideoPlayerViewHolder> {
    private static final String TAG = "VideoPlayerAdapter";
    
    private List<VideoBean> videoList = new ArrayList<>();
    private Context context;
    private PlayerPool playerPool;
    private int currentPlayingPosition = -1;  // 当前应该播放的位置

    // 交互事件的回调接口
    public interface OnInteractionListener {
        void onLikeClick(int position);
        void onDoubleTapLike(int position);
        void onCollectClick(int position);
        void onShareClick(int position);
        void onCommentClick(int position);
    }

    // 视频准备好的回调接口（用于转场动画）
    public interface OnVideoReadyListener {
        void onFirstVideoReady();
    }

    private OnVideoReadyListener videoReadyListener;
    private boolean hasNotifiedFirstVideoReady = false;

    public void setOnVideoReadyListener(OnVideoReadyListener listener) {
        this.videoReadyListener = listener;
    }

    private OnInteractionListener interactionListener;

    // 设置交互事件监听器
    public void setOnInteractionListener(OnInteractionListener listener) {
        this.interactionListener = listener;
    }
    
    // 用于跟踪每个位置对应的 ViewHolder（仅当 attached 时有效）
    private final Map<Integer, VideoPlayerViewHolder> attachedHolders = new HashMap<>();

    public VideoPlayerPagerAdapter(PlayerPool pool, List<VideoBean> videoList) {
        this.playerPool = pool;
        if (videoList != null) {
            this.videoList = videoList;
        }
    }

    @NonNull
    @Override
    public VideoPlayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (context == null) {
            context = parent.getContext();
        }
        View view = LayoutInflater.from(context).inflate(R.layout.item_full_screen_video, parent, false);
        return new VideoPlayerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoPlayerViewHolder holder, int position) {
        VideoBean video = videoList.get(position);
        android.util.Log.e(TAG, "onBindViewHolder: position=" + position + ", title=" + video.getTitle());
        
        // 记录当前绑定的位置
        holder.boundPosition = position;
        
        // 只绑定 UI 数据，不准备播放器（延迟加载）
        bindVideoInfo(holder, video);
        
        // 设置点击暂停/播放
        setupGestureListener(holder);

        // 设置交互事件监听
        setupInteractionListener(holder);
        
        // 显示播放图标（因为还没开始播放）
        holder.ivPlayIcon.setVisibility(View.VISIBLE);
    }

    /**
     * 绑定视频信息到 UI
     */
    private void bindVideoInfo(VideoPlayerViewHolder holder, VideoBean video) {
        holder.tvTitle.setText(video.getTitle());
        holder.tvUserName.setText("@" + video.getAuthorDetail().getAuthorName());
        holder.tvLikeCount.setText(formatNumber(video.getLikeCount()));
        holder.tvCommentCount.setText(formatNumber(video.getCommentCount()));
        holder.tvCollectCount.setText(formatNumber(video.getCollectCount()));
        holder.tvShareCount.setText(formatNumber(video.getShareCount()));
        holder.ivAuthorAvatar.setImageResource(video.getAuthorDetail().getAuthorAvatar());

        // 更新点赞UI
        updateLikeUI(holder, video.isLiked());

        // 更新收藏UI
        updateCollectUI(holder, video.isCollected());

        // 启动头像旋转动画
        if (holder.ivAuthorAvatar != null && holder.avatarAnimation!= null) {
            holder.ivAuthorAvatar.startAnimation(holder.avatarAnimation);
        }

    }

    /**
     * 更新点赞UI
     * @param holder ViewHolder
     * @param isLiked 是否点赞
     */
    public void updateLikeUI(VideoPlayerViewHolder holder, boolean isLiked) {
        if (isLiked) {
            // 点赞状态，颜色变为粉色
            holder.ivLike.setColorFilter(0xFFFF4081); // 粉红色
        } else {
            // 未点赞状态，颜色变为白色
            holder.ivLike.setColorFilter(0xFFFFFFFF); // 粉红色
        }
    }

    /**
     * 更新收藏UI
     * @param holder ViewHolder
     * @param isCollected 是否收藏
     */
    public void updateCollectUI(VideoPlayerViewHolder holder, boolean isCollected) {
        if (isCollected) {
            holder.ivStar.setColorFilter(0xFFFFD700); // 金黄色
        } else {
            holder.ivStar.setColorFilter(0xFFFFFFFF); // 白色
        }
    }

    /**
     * 设置手势监听
     * @param holder ViewHolder
     */
    private void setupGestureListener(VideoPlayerViewHolder holder) {
        GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                // 单击：暂停/播放
                android.util.Log.e(TAG, "单击: position=" + holder.boundPosition);
                togglePlayPause(holder);
                return true;
            }
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                // 双击：点赞
                if (interactionListener != null) {
                    // 显示点赞动画
                    android.util.Log.e(TAG, "双击点赞: position=" + holder.boundPosition);
                    VideoBean currentVideo = videoList.get(currentPlayingPosition);
                    if (!currentVideo.isLiked()) {
                        showLikeAnimation(holder, e.getX(), e.getY());
                    }
                    interactionListener.onDoubleTapLike(holder.boundPosition);
                }
                return true;
            }
        });
        holder.itemView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });
    }

    /**
     * 切换播放/暂停
     * @param holder ViewHolder
     */
    private void togglePlayPause(VideoPlayerViewHolder holder) {
        ExoPlayer currentPlayer = (ExoPlayer) holder.playerView.getPlayer();
        // 如果没有播放器，且是当前播放位置，尝试准备并播放
        if (currentPlayer == null) {
            if (holder.boundPosition == currentPlayingPosition) {
                android.util.Log.e(TAG, "点击时无播放器，准备播放: position=" + holder.boundPosition);
                prepareAndPlay(holder, holder.boundPosition);
            }
            return;
        }
        if (currentPlayer.isPlaying()) {
            currentPlayer.pause();
            holder.ivPlayIcon.setVisibility(View.VISIBLE);
            android.util.Log.e(TAG, "用户点击暂停: position=" + holder.boundPosition);
        } else {
            currentPlayer.play();
            holder.ivPlayIcon.setVisibility(View.GONE);
            android.util.Log.e(TAG, "用户点击播放: position=" + holder.boundPosition);
        }
    }

    /**
     * 设置点赞、收藏按钮监听
     * @param holder ViewHolder
     */
    private void setupInteractionListener(VideoPlayerViewHolder holder) {
        // 点赞按钮监听
        holder.ivLike.setOnClickListener(v -> {
            if (interactionListener != null) {
                interactionListener.onLikeClick(holder.boundPosition);
            }
        });
        // 收藏按钮监听
        holder.ivStar.setOnClickListener(v -> {
            if (interactionListener != null) {
                interactionListener.onCollectClick(holder.boundPosition);
            }
        });
        // 评论按钮监听
        holder.ivComment.setOnClickListener(v -> {
            if (interactionListener != null) {
                interactionListener.onCommentClick(holder.boundPosition);
            }
        });
    }

    /**
     * 显示双击点赞动画
     * @param holder ViewHolder
     * @param x 点击位置的x坐标
     * @param y 点击位置的y坐标
     */
    private void showLikeAnimation(VideoPlayerViewHolder holder, float x, float y) {
        // 创建一个心形图标并显示动画
        ImageView heart = new ImageView(context);
        heart.setImageResource(R.drawable.ic_like_fill);
        heart.setColorFilter(android.graphics.Color.parseColor("#FF4081"));
        
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(200, 200);
        params.leftMargin = (int) x - params.width/2;
        params.topMargin = (int) y - params.height/2;
        
        ViewGroup parent = (ViewGroup) holder.playerView.getParent();
        parent.addView(heart, params);
        
        // 放大 + 淡出动画
        heart.setScaleX(0f);
        heart.setScaleY(0f);
        heart.setAlpha(1f);
        
        heart.animate()
            .scaleX(1.5f)
            .scaleY(1.5f)
            .alpha(0f)
            .setDuration(500)
            .withEndAction(() -> parent.removeView(heart))
            .start();
    }

    /**
     * 格式化数字显示
     * @param number 数字
     * @return 格式化后的数字
     */
    private String formatNumber(int number) {
        if (number >= 10000) {
            return String.format("%.1fw", number / 10000.0);
        }
        return String.valueOf(number);
    }

    /**
     * 通知第一个视频已准备好（仅通知一次）
     * 用于淡出封面占位图，实现平滑转场
     */
    private void notifyFirstVideoReady() {
        if (!hasNotifiedFirstVideoReady && videoReadyListener != null) {
            hasNotifiedFirstVideoReady = true;
            videoReadyListener.onFirstVideoReady();
            android.util.Log.e(TAG, "首个视频准备完成，通知淡出封面");
        }
    }

    /**
     * 获取指定位置的 ViewHolder 
     */
    public VideoPlayerViewHolder getViewHolder(int position) {
        return attachedHolders.get(position);
    }
    /**
     * 为 ViewHolder 准备播放器和视频并开始播放
     * 只在需要播放时才调用（延迟加载）
     */
    @OptIn(markerClass = UnstableApi.class)
    private void prepareAndPlay(VideoPlayerViewHolder holder, int position) {
        PlayerView playerView = holder.playerView;
        
        // 如果已有播放器且是当前位置，直接播放
        ExoPlayer existingPlayer = (ExoPlayer) playerView.getPlayer();
        if (existingPlayer != null) {
            int state = existingPlayer.getPlaybackState();
            android.util.Log.e(TAG, "已有播放器: position=" + position + ", state=" + getStateName(state));
            
            if (state == Player.STATE_READY) {
                existingPlayer.play();
                holder.ivPlayIcon.setVisibility(View.GONE);
                android.util.Log.e(TAG, "复用已有播放器直接播放: position=" + position);
                return;
            } else if (state == Player.STATE_BUFFERING) {
                existingPlayer.setPlayWhenReady(true);
                holder.ivPlayIcon.setVisibility(View.GONE);
                android.util.Log.e(TAG, "播放器正在缓冲，设置 playWhenReady: position=" + position);
                return;
            }
            // 如果是 IDLE 或 ERROR 状态，需要重新准备
        }
        
        // 释放旧播放器
        releasePlayerFromHolder(holder);
        
        // 获取新的播放器
        ExoPlayer player = playerPool.acquirePlayer();
        playerView.setPlayer(player);
        
        android.util.Log.e(TAG, "准备新播放器: position=" + position);
        
        // 设置播放器属性
        player.setRepeatMode(Player.REPEAT_MODE_ONE);  // 单个视频循环播放
        
        // 创建并保存播放状态监听器
        WeakReference<VideoPlayerViewHolder> holderRef = new WeakReference<>(holder);
        final int boundPos = position;
        Player.Listener listener = new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                VideoPlayerViewHolder h = holderRef.get();
                if (h == null) return;
                
                ExoPlayer p = (ExoPlayer) h.playerView.getPlayer();
                boolean playWhenReady = p != null ? p.getPlayWhenReady() : false;
                boolean isPlaying = p != null ? p.isPlaying() : false;
                
                android.util.Log.e(TAG, "状态变化: position=" + boundPos 
                    + ", state=" + getStateName(playbackState)
                    + ", playWhenReady=" + playWhenReady
                    + ", isPlaying=" + isPlaying
                    + ", currentPlayingPosition=" + currentPlayingPosition);
                
                if (playbackState == Player.STATE_READY) {
                    // 如果这个位置是当前应该播放的位置，确保开始播放
                    if (boundPos == currentPlayingPosition && p != null) {
                        if (!isPlaying) {
                            android.util.Log.e(TAG, "STATE_READY 触发播放: position=" + boundPos);
                            p.play();
                            h.ivPlayIcon.setVisibility(View.GONE);
                        }
                        // 通知第一个视频已准备好（用于淡出封面占位图）
                        notifyFirstVideoReady();
                    }
                }
            }
            
            @Override
            public void onPlayerError(androidx.media3.common.PlaybackException error) {
                android.util.Log.e(TAG, "播放器错误: position=" + boundPos + ", error=" + error.getMessage());
                VideoPlayerViewHolder h = holderRef.get();
                if (h != null) {
                    h.ivPlayIcon.setVisibility(View.VISIBLE);
                }
            }
        };
        
        // 保存监听器引用
        holder.playerListener = listener;
        player.addListener(listener);
        
        // 准备媒体
        Uri videoUri = Uri.parse("android.resource://" + context.getPackageName() + "/" 
                + videoList.get(position).getVideoResourceId());
        MediaItem mediaItem = MediaItem.fromUri(videoUri);
        player.setMediaItem(mediaItem);
        
        // 准备播放器并设置自动播放
        player.setPlayWhenReady(true);  // 准备好就播放
        player.prepare();
        
        holder.ivPlayIcon.setVisibility(View.GONE);
        
        android.util.Log.e(TAG, "播放器准备中: position=" + position);
    }

    /**
     * ViewHolder 附加到窗口时调用
     */
    @Override
    public void onViewAttachedToWindow(@NonNull VideoPlayerViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        int position = holder.boundPosition;
        android.util.Log.e(TAG, "onViewAttachedToWindow: position=" + position);
        
        // 记录 attached 的 holder
        attachedHolders.put(position, holder);
    }

    /**
     * ViewHolder 从窗口分离时调用
     */
    @Override
    public void onViewDetachedFromWindow(@NonNull VideoPlayerViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        int position = holder.boundPosition;
        android.util.Log.e(TAG, "onViewDetachedFromWindow: position=" + position);
        
        // 移除记录
        attachedHolders.remove(position);
        
        // 释放播放器（关键：detach 时释放，避免资源占用）
        releasePlayerFromHolder(holder);
    }

    /**
     * 释放 ViewHolder 中的播放器
     */
    private void releasePlayerFromHolder(VideoPlayerViewHolder holder) {
        ExoPlayer player = (ExoPlayer) holder.playerView.getPlayer();
        if (player != null) {
            android.util.Log.e(TAG, "释放播放器: position=" + holder.boundPosition 
                + ", state=" + getStateName(player.getPlaybackState()));
            
            // 移除监听器
            if (holder.playerListener != null) {
                player.removeListener(holder.playerListener);
                holder.playerListener = null;
            }
            // 归还播放器到池中
            playerPool.releasePlayer(player);
            holder.playerView.setPlayer(null);
        }
    }
    
    /**
     * 获取播放器状态名称（用于日志）
     */
    private String getStateName(int state) {
        switch (state) {
            case Player.STATE_IDLE: return "IDLE";
            case Player.STATE_BUFFERING: return "BUFFERING";
            case Player.STATE_READY: return "READY";
            case Player.STATE_ENDED: return "ENDED";
            default: return "UNKNOWN(" + state + ")";
        }
    }

    /**
     * 外部调用：切换到指定位置播放
     * @param position 要播放的位置
     */
    public void playAt(int position) {
        android.util.Log.e(TAG, "playAt: " + currentPlayingPosition + " -> " + position);
        
        // 释放之前位置的播放器（关键：先释放再获取新的，避免资源冲突）
        if (currentPlayingPosition != -1 && currentPlayingPosition != position) {
            VideoPlayerViewHolder oldHolder = attachedHolders.get(currentPlayingPosition);
            if (oldHolder != null) {
                releasePlayerFromHolder(oldHolder);
                oldHolder.ivPlayIcon.setVisibility(View.VISIBLE);
            }
        }
        
        // 更新当前播放位置
        currentPlayingPosition = position;
        
        // 为新位置准备播放器并播放
        VideoPlayerViewHolder newHolder = attachedHolders.get(position);
        if (newHolder != null) {
            prepareAndPlay(newHolder, position);
        }
    }

    /**
     * 暂停当前播放
     */
    public void pauseCurrent() {
        if (currentPlayingPosition != -1) {
            VideoPlayerViewHolder holder = attachedHolders.get(currentPlayingPosition);
            if (holder != null) {
                ExoPlayer player = (ExoPlayer) holder.playerView.getPlayer();
                if (player != null) {
                    player.pause();
                    holder.ivPlayIcon.setVisibility(View.VISIBLE);
                    android.util.Log.e(TAG, "暂停当前: position=" + currentPlayingPosition);
                }
            }
        }
    }

    /**
     * 恢复当前播放
     */
    public void resumeCurrent() {
        if (currentPlayingPosition != -1) {
            VideoPlayerViewHolder holder = attachedHolders.get(currentPlayingPosition);
            if (holder != null) {
                ExoPlayer player = (ExoPlayer) holder.playerView.getPlayer();
                if (player != null && player.getPlaybackState() == Player.STATE_READY) {
                    player.play();
                    holder.ivPlayIcon.setVisibility(View.GONE);
                    android.util.Log.e(TAG, "恢复当前: position=" + currentPlayingPosition);
                } else {
                    // 播放器不存在或状态不对，重新准备
                    prepareAndPlay(holder, currentPlayingPosition);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return videoList == null ? 0 : videoList.size();
    }

    /**
     * 设置视频列表
     */
    public void setVideoList(List<VideoBean> videoList) {
        this.videoList = videoList;
        notifyDataSetChanged();
    }

    /**
     * 视频播放页面 ViewHolder
     */
    public static class VideoPlayerViewHolder extends RecyclerView.ViewHolder {
        PlayerView playerView;
        ImageView ivPlayIcon;
        ImageView ivLike;
        ImageView ivStar;
        ImageView ivComment;
        ImageView ivShare;
        ImageView ivAuthorAvatar;
        TextView tvUserName, tvTitle;
        TextView tvLikeCount, tvCommentCount, tvCollectCount, tvShareCount;
        
        // 记录当前绑定的位置
        int boundPosition = -1;
        
        // 保存播放器监听器引用，用于后续移除
        Player.Listener playerListener;

        // 头像旋转动画
        Animation avatarAnimation;

        public VideoPlayerViewHolder(@NonNull View itemView) {
            super(itemView);
            playerView = itemView.findViewById(R.id.playerView);
            playerView.setResizeMode(androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH);
            android.widget.FrameLayout.LayoutParams lp = (android.widget.FrameLayout.LayoutParams) playerView.getLayoutParams();
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            lp.gravity = android.view.Gravity.CENTER;
            playerView.setLayoutParams(lp);
            ivPlayIcon = itemView.findViewById(R.id.iv_play_icon);
            ivLike = itemView.findViewById(R.id.iv_like_full);
            ivStar = itemView.findViewById(R.id.iv_star);
            ivComment = itemView.findViewById(R.id.iv_comment);
            ivShare = itemView.findViewById(R.id.iv_share);
            ivAuthorAvatar = itemView.findViewById(R.id.iv_author_avatar_full_screen_video);
            tvUserName = itemView.findViewById(R.id.tv_user_name_full);
            tvTitle = itemView.findViewById(R.id.tv_title_full);
            tvLikeCount = itemView.findViewById(R.id.tv_like_count_full);
            tvCommentCount = itemView.findViewById(R.id.tv_comment_count_full);
            tvCollectCount = itemView.findViewById(R.id.tv_collect_count_full);
            tvShareCount = itemView.findViewById(R.id.tv_share_count_full);

            // 初始化头像旋转动画
            avatarAnimation = android.view.animation.AnimationUtils.loadAnimation(
                    itemView.getContext(), R.anim.avatar_rotate);
        }
    }
}
