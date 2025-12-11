package com.example.douyinline.ui.video;

import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.datasource.RawResourceDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.douyinline.R;
import com.example.douyinline.bean.VideoBean;

/**
 * 单个视频播放 Fragment
 * 1. 播放/暂停视频
 * 2. 显示视频信息（作者、描述、点赞数等）
 * 3. 处理用户交互（点赞、评论、分享按钮点击）
 */
public class VideoPlayerFragment extends Fragment {
    private static final String ARG_VIDEO = "video";

    private VideoBean video;
    private PlayerView playerView;
    private ExoPlayer player;
    private ImageView ivPlayIcon;
    private TextView tvAuthorName;
    private TextView tvTitle;
    private TextView tvLikeCount;
    private TextView tvCommentCount;
    private TextView tvShareCount;
    private TextView tvCollectCount;
    private ImageView ivLike;
    private ImageView ivComment;
    private ImageView ivShare;
    private ImageView ivCollect;
    private ImageView ivAuthorAvatar;

    private boolean isPlaying = false;
    private boolean isPrepared = false;

    public static VideoPlayerFragment newInstance(VideoBean video) {
        VideoPlayerFragment fragment = new VideoPlayerFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_VIDEO, video);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_video_player, container, false);
        playerView = root.findViewById(R.id.playerView);
        ivPlayIcon = root.findViewById(R.id.iv_play_icon);
        tvAuthorName = root.findViewById(R.id.tv_user_name_full);
        tvTitle = root.findViewById(R.id.tv_title_full);
        tvLikeCount = root.findViewById(R.id.tv_like_count_full);
        tvCommentCount = root.findViewById(R.id.tv_comment_count_full);
        tvShareCount = root.findViewById(R.id.tv_share_count_full);
        tvCollectCount = root.findViewById(R.id.tv_collect_count_full);
        ivAuthorAvatar = root.findViewById(R.id.iv_author_avatar);

        if (getArguments() != null) {
            video = (VideoBean) getArguments().getSerializable(ARG_VIDEO);
        }

        // 绑定视频数据到UI
        bindVideoDataToUI();

        // 初始化播放器
        initPlayer();

        // 设置点击监听
        setupClickListener(root);

        return root;
    }
    /**
     * 初始化播放器
     */
    private void initPlayer() {
        if (video == null) {
            return;
        }
        player = new ExoPlayer.Builder(requireContext()).build();
        playerView.setPlayer(player);
        playerView.setUseController(false);  // 禁用默认控制器
        playerView.setControllerHideOnTouch(false);
        playerView.setOnClickListener(null);

        Uri uri = RawResourceDataSource.buildRawResourceUri(video.getVideoResourceId());
        MediaItem mediaItem = MediaItem.fromUri(uri);
        player.setMediaItem(mediaItem);
        player.setRepeatMode(Player.REPEAT_MODE_ONE);  // 循环播放
        player.prepare();
        // 不要在这里播放，等待 Activity 调用 startPlayback()
        isPrepared = true;

        // 监听播放状态变化
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(@Player.State int playbackState) {
                if (playbackState == Player.STATE_READY) {
                    isPrepared = true;
                }
            }

            @Override
            public void onIsPlayingChanged(boolean playing) {
                isPlaying = playing;
                updatePlayIcon();
            }
        });
    }

     /**
     * 设置点击监听 - 点击屏幕暂停/播放
     */
     private void setupClickListener(View root) {
        root.setOnClickListener(v -> {
            togglePlayPause();
        });
    }

    /**
     * 切换播放/暂停状态
     */
    public void togglePlayPause() {
        if (player == null) return;
        
        if (player.isPlaying()) {
            player.pause();
            showPlayIcon();
        } else {
            player.play();
            hidePlayIcon();
        }
    }

    /**
     * 更新播放图标显示状态
     */
    private void updatePlayIcon() {
        if (ivPlayIcon == null) return;
        
        if (isPlaying) {
            hidePlayIcon();
        } else {
            showPlayIcon();
        }
    }

    private void showPlayIcon() {
        if (ivPlayIcon != null) {
            ivPlayIcon.setVisibility(View.VISIBLE);
        }
    }

    private void hidePlayIcon() {
        if (ivPlayIcon != null) {
            ivPlayIcon.setVisibility(View.GONE);
        }
    }
    /**
     * 由 Activity 调用，开始播放
     */
    public void startPlayback() {
        if (player != null) {
            player.play();
            hidePlayIcon();
        }
    }

    /**
     * 由 Activity 调用，暂停播放
     */
    public void pausePlayback() {
        if (player != null) {
            player.pause();
        }
    }

    /**
     * 绑定视频数据到UI
     */
    private void bindVideoDataToUI() {
        if (video != null) {
            tvTitle.setText(video.getTitle());
            tvLikeCount.setText(String.valueOf(video.getLikeCount()));
            tvCommentCount.setText(String.valueOf(video.getCommentCount()));
            tvShareCount.setText(String.valueOf(video.getShareCount()));
            tvCollectCount.setText(String.valueOf(video.getCollectCount()));
            if (video.getAuthorDetail() != null) {
                tvAuthorName.setText(video.getAuthorDetail().getAuthorName());
                ivAuthorAvatar.setImageResource(video.getAuthorDetail().getAuthorAvatar());
            }
        }
    }

  
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (player != null) {
            player.release();
            player = null;
        }
        playerView = null;
        isPrepared = false;
    }
}