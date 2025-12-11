package com.example.douyinline.ui.video;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.douyinline.bean.CommentBean;
import com.example.douyinline.bean.VideoBean;
import com.example.douyinline.repository.VideoRepository;

import java.util.List;

/**
 * 视频播放 ViewModel
 * 1. 管理视频列表数据
 * 2. 管理当前播放索引
 * 3. 处理页面切换逻辑
 * 4. 处理交互事件（点赞、分享等）
 */
public class VideoPlayViewModel extends ViewModel {
    private MutableLiveData<List<VideoBean>> videoListLiveData = new MutableLiveData<>();
    private MutableLiveData<Integer> currentVideoPositionLiveData = new MutableLiveData<>();

    private MutableLiveData<LikeEvent> likeEventLiveData = new MutableLiveData<>();
    private MutableLiveData<CollectEvent> collectEventLiveData = new MutableLiveData<>();


    private PlayerPool playerPool;
    private int currentIndex = 0;

    @Override
    protected void onCleared() {
        super.onCleared();
        if (playerPool != null) {
            playerPool.releaseAllPlayers();
        }
    }

    /**
     * 初始化数据
     * @param startPosition 初始播放位置
     */
    public void initFromRepository(int startPosition) {
        this.currentIndex = startPosition;
        List<VideoBean> list = VideoRepository.getInstance().getRecommendVideos();
        videoListLiveData.setValue(list);
        currentVideoPositionLiveData.setValue(startPosition);
    }

    /**
     * 初始化ExoPlayer实例池
     */
    public void initPlayerPool(Context context) {
        if(playerPool == null){
            playerPool = new PlayerPool(context);
        }
        playerPool.initPool();
    }

    /**
     * 获取视频池
     */
    public PlayerPool getPlayerPool() {
        return playerPool;
    }

    /**
     * 页面切换时调用
     * @param position 切换到的位置
     */
    public void onPageSelected(int position) {
        currentIndex = position;
        currentVideoPositionLiveData.setValue(position);
    }

    /**
     * 点赞事件
     * @param position 被点赞的视频
     */
    public void toggleLikeClick(int position) {
        List<VideoBean> list = videoListLiveData.getValue();
        if (list != null && list.size() > position && position >= 0) {
            VideoBean video = list.get(position);

            // 切换点赞状态
            boolean newLikedState = !video.isLiked();
            video.setLiked(newLikedState);

            // 更新点赞数
            if(newLikedState){
                video.setLikeCount(video.getLikeCount() + 1);
            }else{
                video.setLikeCount(Math.max(0, video.getLikeCount() - 1));
            }

            // 发送点赞事件更新UI
            likeEventLiveData.setValue(new LikeEvent(position, video.isLiked(), video.getLikeCount()));
        }
    }

   /**
    * 收藏事件
    * @param position 被收藏的视频
    */
    public void toggleCollectClick(int position) {
        List<VideoBean> list = videoListLiveData.getValue();
        if (list != null && list.size() > position && position >= 0) {
            VideoBean video = list.get(position);
            
            // 切换收藏状态
            boolean newCollectedState = !video.isCollected();
            video.setCollected(newCollectedState);

            // 更新收藏数
            if(newCollectedState){
                video.setCollectCount(video.getCollectCount() + 1);
            }else{
                video.setCollectCount(Math.max(0, video.getCollectCount() - 1));
            }

            // 发送收藏事件更新UI
            collectEventLiveData.setValue(new CollectEvent(position, video.isCollected(), video.getCollectCount()));
        }
    }

    /**
     * 收藏事件
     * @param video 被分享的视频
     */
    public void onShareClick(VideoBean video) {
        // TODO: 分享逻辑
    }

    // Getter 方法
    public MutableLiveData<List<VideoBean>> getVideoListLiveData() {
        return videoListLiveData;
    }

    public MutableLiveData<Integer> getCurrentVideoPositionLiveData() {
        return currentVideoPositionLiveData;
    }
    public MutableLiveData<LikeEvent> getLikeEventLiveData() {
        return likeEventLiveData;
    }
    public MutableLiveData<CollectEvent> getCollectEventLiveData() {
        return collectEventLiveData;
    }

    // 内部事件类
    public static class LikeEvent {
        private final int position;
        private final boolean isLiked;
        private final int likeCount;
        public LikeEvent(int position, boolean isLiked, int likeCount) {
            this.position = position;
            this.isLiked = isLiked;
            this.likeCount = likeCount;
        }

        public int getPosition() {
            return position;
        }

        public boolean isLiked() {
            return isLiked;
        }

        public int getLikeCount() {
            return likeCount;
        }
    }

    public static class CollectEvent {
        private final int position;
        private final boolean isCollected;
        private final int collectCount;
        public CollectEvent(int position, boolean isCollected, int collectCount) {
            this.position = position;
            this.isCollected = isCollected;
            this.collectCount = collectCount;
        }

        public int getPosition() {
            return position;
        }

        public boolean isCollected() {
            return isCollected;
        }

        public int getCollectCount() {
            return collectCount;
        }
    }
}
