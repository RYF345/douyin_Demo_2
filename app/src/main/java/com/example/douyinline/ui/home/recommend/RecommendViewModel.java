package com.example.douyinline.ui.home.recommend;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.MutableLiveData;


import com.example.douyinline.bean.VideoBean;
import com.example.douyinline.event.NavigationEvent;
import com.example.douyinline.repository.VideoRepository;

import java.util.List;
import java.util.ArrayList;

/**
 * 推荐页ViewModel
 * 负责处理业务逻辑，管理UI状态
 */
public class RecommendViewModel extends ViewModel {
    private VideoRepository videoRepository;

    // LiveData 用于存储推荐视频数据变化
    private MutableLiveData<List<VideoBean>> videoListLiveData;
    // LiveData 用于显示Toast
    private MutableLiveData<String> toastMessageLiveData;
    // 刷新状态
    private MutableLiveData<Boolean> refreshStatusLiveData = new MutableLiveData<>(false);
    // 加载更多状态
    private MutableLiveData<Boolean> loadMoreStatusLiveData = new MutableLiveData<>(false);
    // 是否还有更多数据状态
    private MutableLiveData<Boolean> hasMoreDataLiveData = new MutableLiveData<>(true);
    // 出错状态
    private MutableLiveData<String> errorStatusLiveData = new MutableLiveData<>();
    // 导航事件
    private MutableLiveData<NavigationEvent> navigateToVideoLiveData = new MutableLiveData<>();

    /**
     * 构造函数
     */
   
    public RecommendViewModel() {
        videoRepository = VideoRepository.getInstance();
        videoListLiveData = new MutableLiveData<>(new ArrayList<>());
        toastMessageLiveData = new MutableLiveData<>();
    }

    /**
     * 获取视频列表的LiveData
     * Fragment观察该LiveData，当数据变化时，会自动更新UI
     * @return 视频列表的LiveData
     */
    public MutableLiveData<List<VideoBean>> getVideoListLiveData() {
        return videoListLiveData;
    }

    /**
     * 获取Toast消息的LiveData
     * Fragment观察该LiveData，当有消息时，会自动显示Toast
     * @return Toast消息的LiveData
     */
    public MutableLiveData<String> getToastMessageLiveData() {
        return toastMessageLiveData;
    }

    /**
     * 获取刷新状态的LiveData
     * @return 刷新状态的LiveData
     */
    public MutableLiveData<Boolean> getRefreshStatusLiveData() {
        return refreshStatusLiveData;
    }

    /**
     * 获取加载更多状态的LiveData
     * @return 加载更多状态的LiveData
     */
    public MutableLiveData<Boolean> getLoadMoreStatusLiveData() {
        return loadMoreStatusLiveData;
    }

    /**
     * 获取是否还有更多数据状态的LiveData
     * @return 是否还有更多数据状态的LiveData
     */
    public MutableLiveData<Boolean> getHasMoreDataLiveData() {
        return hasMoreDataLiveData;
    }

    /**
     * 获取出错状态的LiveData
     * @return 出错状态的LiveData
     */
    public MutableLiveData<String> getErrorStatusLiveData() {
        return errorStatusLiveData;
    }

    /**
     * 获取导航事件的LiveData
     * @return 导航事件的LiveData
     */
    public MutableLiveData<NavigationEvent> getNavigateToVideoLiveData() {
        return navigateToVideoLiveData;
    }

    /**
     * 加载推荐视频列表方法
     * 调用VideoRepository的getRecommendVideos方法获取数据
     */
    public void loadRecommendVideos() {
        // 从数据库或网络获取视频数据
        List<VideoBean> videoList = videoRepository.getRecommendVideos();
        if(videoList == null || videoList.isEmpty()){
            toastMessageLiveData.setValue("没有获取到推荐视频数据");
        }else{
            videoListLiveData.setValue(videoList);
        }
    }

    /**
     * 处理视频卡片点击事件方法
     * 跳转到视频详情页
     * @param video 点击的视频对象
     * @param position 点击的位置
     */
    public void onVideoCardClick(VideoBean video, int position) {
        if (video == null){ return;}
        // 发送导航事件，包含视频的位置信息和封面资源 ID（用于转场动画）
        navigateToVideoLiveData.setValue(new NavigationEvent(position, video.getCoverResourceId()));
    }

    /**
     * 刷新推荐视频列表方法
     * 调用VideoRepository的refreshShuffle方法刷新数据
     */
    public void refreshRecommendVideos() {
        refreshStatusLiveData.setValue(true);
        try {
            // 从数据库或网络获取视频数据
            List<VideoBean> videoList = videoRepository.refreshShuffle();
            videoListLiveData.setValue(videoList);
            hasMoreDataLiveData.setValue(true);
        }catch(Exception e){
            errorStatusLiveData.setValue("刷新推荐视频数据失败：" + e.getMessage());
        }finally{
            refreshStatusLiveData.setValue(false);
        }
    }

    /**
     * 加载更多推荐视频列表方法
     * 调用VideoRepository的loadMoreShuffle方法加载更多数据
     */
    public void loadMoreRecommendVideos() {
        loadMoreStatusLiveData.setValue(true);
        try {
            // 从数据库或网络获取视频数据
            List<VideoBean> videoList = videoRepository.loadMoreShuffle();
            videoListLiveData.setValue(videoList);
            hasMoreDataLiveData.setValue(true);
        }catch(Exception e){
            errorStatusLiveData.setValue("加载更多推荐视频数据失败：" + e.getMessage());
        }finally{
            loadMoreStatusLiveData.setValue(false);
        }
    }

}
