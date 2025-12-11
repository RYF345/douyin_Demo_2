package com.example.douyinline.ui.video;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.MainThread;
import androidx.media3.exoplayer.ExoPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * ExoPlayer 播放器池
 * 用于复用播放器实例，避免频繁创建和销毁
 * 所有操作必须在主线程执行
 */
public class PlayerPool {
    private static final String TAG = "PlayerPool";
    private final List<ExoPlayer> availablePlayers = new ArrayList<>();
    private final List<ExoPlayer> allPlayers = new ArrayList<>();  // 跟踪所有创建的播放器
    private final Context appContext;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    public final int MAX_PLAYERS = 2;  // 使用延迟加载策略，只需要 2 个播放器
    private boolean isInitialized = false;

    public PlayerPool(Context context) {
        this.appContext = context.getApplicationContext();
    }

    /**
     * 初始化ExoPlayer实例池
     */
    @MainThread
    public void initPool() {
        if (isInitialized) {
            android.util.Log.w(TAG, "PlayerPool 已经初始化过了");
            return;
        }
        for (int i = 0; i < MAX_PLAYERS; i++) {
            ExoPlayer player = createPlayer();
            availablePlayers.add(player);
            allPlayers.add(player);
        }
        isInitialized = true;
        android.util.Log.d(TAG, "PlayerPool 初始化完成，创建了 " + MAX_PLAYERS + " 个播放器");
    }

    /**
     * 创建一个新的 ExoPlayer 实例
     */
    private ExoPlayer createPlayer() {
        return new ExoPlayer.Builder(appContext)
                .build();
    }

    /**
     * 获取一个可用的ExoPlayer实例
     * @return 可用的ExoPlayer实例，如果池为空则创建新的
     */
    @MainThread
    public ExoPlayer acquirePlayer() {
        ExoPlayer player;
        if (!availablePlayers.isEmpty()) {
            player = availablePlayers.remove(0);
            android.util.Log.d(TAG, "从池中获取播放器，剩余: " + availablePlayers.size());
        } else {
            // 池为空，创建新的播放器
            player = createPlayer();
            allPlayers.add(player);
            android.util.Log.w(TAG, "池为空，创建新播放器，总数: " + allPlayers.size());
        }
        // 确保播放器状态干净
        resetPlayer(player);
        return player;
    }

    /**
     * 归还播放器到池中
     * @param player 要归还的播放器
     */
    @MainThread
    public void releasePlayer(ExoPlayer player) {
        if (player == null) {
            return;
        }
        
        // 先重置播放器状态
        resetPlayer(player);
        
        // 如果池未满且播放器有效，归还到池中
        if (availablePlayers.size() < MAX_PLAYERS && allPlayers.contains(player)) {
            if (!availablePlayers.contains(player)) {
                availablePlayers.add(player);
                android.util.Log.d(TAG, "播放器归还到池中，可用: " + availablePlayers.size());
            }
        } else {
            // 池已满或播放器不在跟踪列表中，直接释放
            player.release();
            allPlayers.remove(player);
            android.util.Log.d(TAG, "播放器已释放，总数: " + allPlayers.size());
        }
    }

    /**
     * 重置播放器状态，确保下次使用时状态干净
     * @param player 要重置的播放器
     */
    @MainThread
    public void resetPlayer(ExoPlayer player) {
        if (player == null) {
            return;
        }
        try {
            // 停止播放
            player.stop();
            // 清除所有媒体项
            player.clearMediaItems();
            // 移除所有监听器（如果有的话，需要在外部管理）
            // 重置播放位置
            player.seekTo(0);
        } catch (Exception e) {
            android.util.Log.e(TAG, "重置播放器失败: " + e.getMessage());
        }
    }

    /**
     * 获取可用播放器数量
     */
    public int getAvailableCount() {
        return availablePlayers.size();
    }

    /**
     * 获取总播放器数量
     */
    public int getTotalCount() {
        return allPlayers.size();
    }

    /**
     * 释放所有播放器
     */
    @MainThread
    public void releaseAllPlayers() {
        for (ExoPlayer player : allPlayers) {
            try {
                player.release();
            } catch (Exception e) {
                android.util.Log.e(TAG, "释放播放器失败: " + e.getMessage());
            }
        }
        availablePlayers.clear();
        allPlayers.clear();
        isInitialized = false;
        android.util.Log.d(TAG, "所有播放器已释放");
    }
}