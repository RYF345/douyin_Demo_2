package com.example.douyinline.repository;

import com.example.douyinline.R;
import com.example.douyinline.bean.AuthorBean;
import com.example.douyinline.bean.VideoBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

/**
 * 视频数据仓库
 * 负责数据的获取和管理
 */
public class VideoRepository {
    private final List<VideoBean> videoList = new ArrayList<>();
    private static final int[] VIDEO_POOL = {R.raw.video_1, R.raw.video_2, R.raw.video_3, R.raw.video_4, R.raw.video_5,
            R.raw.video_6, R.raw.video_7, R.raw.video_8, R.raw.video_9, R.raw.video_10};
    private static final int[] AVATAR_POOL = {R.drawable.img_avatar_1, R.drawable.img_avatar_2, R.drawable.img_avatar_3,
            R.drawable.img_avatar_4, R.drawable.img_avatar_5, R.drawable.img_avatar_6,
            R.drawable.img_avatar_7, R.drawable.img_avatar_8, R.drawable.img_avatar_9,
            R.drawable.img_avatar_10};

    private static final int[] COVER_POOL = {R.drawable.img_cover_1, R.drawable.img_cover_2, R.drawable.img_cover_3,
            R.drawable.img_cover_4, R.drawable.img_cover_5, R.drawable.img_cover_6,
            R.drawable.img_cover_7, R.drawable.img_cover_8, R.drawable.img_cover_9,
            R.drawable.img_cover_10};
    private static final String[] TITLE_POOL = {
            "理想中的女生合租生活，大概就是这样吧",
            "当冬季风暴按下城市的暂停键",
            "镜头恐惧？学她如何用姿态说话",
            "土耳其的斑斓，一半在天上，一半在地下",
            "闯入上帝遗落的棋盘：伦索伊斯马拉汉塞斯。",
            "当夜幕降临，城市才开始真正地呼吸。",
            "把城市公园，拍成了4K壁纸级电影。",
            "便携烤箱，是都市人的浪漫烧烤自由。",
            "漫步在华盛顿的秋日里",
            "来场网球",
    };
    private static final String[] AUTHOR_POOL = {
            "半只废柴", "赛博唐僧在线念经", "碳酸小饼干", "404号", "月亮今晚不营业",
            "村口烫头王姐", "小熊维尼", "老衲洗头用飘柔", "凌晨三点猫", "糖分过载"
    };
    private static final int INIT_VIDEO_COUNT = 10;

    // 视频仓库单例实例
    private static volatile VideoRepository INSTANCE;

    public static VideoRepository getInstance() {
        if (INSTANCE == null) {
            synchronized (VideoRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new VideoRepository();
                }
            }
        }
        return INSTANCE;
    }

    // 视频池数据，用于推荐视频的随机展示
    private final List<VideoBean> poolData = generateVideos();
    
    /**
     * 视频列表随机乱序
     */
    private List<Integer> shuffleList = new ArrayList<>();
    private void reshuffle(){
       shuffleList = new ArrayList<>();
       for(int i = 0; i < poolData.size(); i++){
           shuffleList.add(i);
       }
       Collections.shuffle(shuffleList);
    }

    /**
     * 获取推荐视频数据
     * @return 推荐视频数据列表
     */
    public synchronized List<VideoBean> getRecommendVideos() {
        if(videoList.isEmpty()){
            // 模拟从数据库或网络获取视频数据
            videoList.addAll(generateVideos());
        }
        return new ArrayList<>(videoList);
    }

    /**
     * 刷新推荐视频数据
     * @return 推荐视频数据列表
     */
    public synchronized List<VideoBean> refreshShuffle(){
        videoList.clear();
        reshuffle();
        for(int i = 0; i < poolData.size(); i++){
            videoList.add(poolData.get(shuffleList.get(i)));
        }
        return new ArrayList<>(videoList);
    }
    
    /**
     * 加载更多推荐视频数据
     * @return 推荐视频数据列表
     */
    public synchronized List<VideoBean> loadMoreShuffle(){
        reshuffle();
        for(int i = 0; i < poolData.size(); i++){
            videoList.add(poolData.get(shuffleList.get(i)));
        }
        return new ArrayList<>(videoList);
    }

    /**
     * 生成模拟视频数据
     * @return 模拟视频数据列表
     */
    public static List<VideoBean> generateVideos() {
        List<VideoBean> videos = new ArrayList<>();
        // 添加视频数据
        for (int i = 0; i < INIT_VIDEO_COUNT; i++) {
            int likeCount = (int) (Math.random() * 1000);
            int commentCount = (int) (Math.random() * 1000);
            int shareCount = (int) (Math.random() * 1000);
            int collectCount = (int) (Math.random() * 1000);
            AuthorBean authorDetail = new AuthorBean(
                    AUTHOR_POOL[i],
                    AVATAR_POOL[i]
            );
            videos.add(new VideoBean(
                    COVER_POOL[i],
                    VIDEO_POOL[i],
                    TITLE_POOL[i],
                    likeCount,
                    commentCount,
                    shareCount,
                    collectCount,
                    authorDetail
            ));
        }
        return videos;
    }

    
}
