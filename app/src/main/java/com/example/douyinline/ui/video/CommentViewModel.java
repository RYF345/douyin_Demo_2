package com.example.douyinline.ui.video;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.douyinline.R;
import com.example.douyinline.bean.AuthorBean;
import com.example.douyinline.bean.CommentBean;
import com.example.douyinline.bean.VideoBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class CommentViewModel extends ViewModel {
    private final MutableLiveData<List<CommentBean>> commentListLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private VideoBean video;
    private final int ADD_COMMENT_COUNT = 10;
    private boolean hasMore = false;
    private int hasLoaded = 0;  // 已加载的评论数量
    private MutableLiveData<Integer> commentCountToTal = new MutableLiveData<>(0);

    private List<CommentBean> currentComments = new ArrayList<>(0);
    private MutableLiveData<Integer> currentVideoPosition = new MutableLiveData<>(0);

    public MutableLiveData<Boolean> getLoading() {
        return isLoading;
    }

    /**
     * 获取评论列表LiveData
     * @return 评论列表LiveData
     */
    public MutableLiveData<List<CommentBean>> getCommentListLiveData() {
        return commentListLiveData;
    }

    /**
     * 获取评论总数
     * @return 评论总数
     */
    public MutableLiveData<Integer> getCommentCountToTal() {
        return commentCountToTal;
    }

    /**
     * 获取当前视频位置
     * @return 当前视频位置
     */
    public MutableLiveData<Integer> getCurrentVideoPosition() {
        return currentVideoPosition;
    }

    /**
     * 首次加载数据
     */
    public void loadCommentsFirst(VideoBean video) {
        // 防止重复加载
        if (Boolean.TRUE.equals(isLoading.getValue())) {return;}
        
        // 模拟加载评论数据
        getCommentCountToTal().setValue(video.getCommentCount());
        int shouldLoadCount = video.getCommentCount() - hasLoaded;
        if (shouldLoadCount <= 0) {
            hasMore = false;
            return;
        }
        
        isLoading.setValue(true);
        hasMore = true;
        // 加载评论
        List<CommentBean> comments = generateComments(shouldLoadCount);
        hasLoaded += comments.size();
        currentComments.addAll(comments);
        if(hasLoaded >= video.getCommentCount()) {
            hasMore = false;
        }
        commentListLiveData.setValue(currentComments);
        isLoading.setValue(false);
    }
    /**
     * 加载更多数据
     */
    public void loadMoreComments() {
        if (!hasMore) {return;}
        if (Boolean.TRUE.equals(isLoading.getValue())) {return;}
        isLoading.setValue(true);
        
        // 使用Handler在后台延迟执行
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            int shouldLoadCount = video.getCommentCount() - hasLoaded;
            // 模拟加载评论数据
            List<CommentBean> comments = generateComments(shouldLoadCount);
            hasLoaded += comments.size();
            currentComments.addAll(comments);
            if(hasLoaded >= video.getCommentCount()) {
                hasMore = false;
            }
            commentListLiveData.setValue(currentComments);
            isLoading.setValue(false);
        }, 500);
    }
    /**
     * 设置当前视频
     * 切换视频时需要重置评论相关状态
     */
    public void setVideo(VideoBean video) {
        this.video = video;
        // 重置评论状态，防止不同视频共享同一份评论数据
        this.currentComments.clear();
        this.hasLoaded = 0;
        this.hasMore = false;
        this.commentListLiveData.setValue(new ArrayList<>());
    }

    /**
     * 模拟业务：发送评论
     * @param content 评论内容
     */
    public void sendComment(String content) {
        if (content == null || content.isEmpty()) {
            return;
        }
        getCommentCountToTal().setValue(getCommentCountToTal().getValue() + 1);
        // 获取当前评论列表
        List<CommentBean> comments = commentListLiveData.getValue();

        // 创建新评论
        CommentBean newComment = new CommentBean(
                false, // 默认未点赞
                0, // 点赞数
                "刚刚", // 时间
                new AuthorBean("我", R.drawable.img_avatar_1), // 作者
                content); // 评论内容

        // 添加到评论列表
        if (comments == null) {
            comments = new ArrayList<>();
        }
        comments.add(0, newComment);
        commentListLiveData.setValue(comments);
    }
    /**
     * 模拟生成评论数据
     * @return 评论列表
     */
    private List<CommentBean> generateComments(int count) {
        List<CommentBean> comments = new ArrayList<>();
        String[] names = {"小明", "小红", "张三", "李四", "王五", "小美", "大壮", "阿花", "老王", "小李"};
        String[] contents = {
                "哈哈哈太搞笑了！",
                "这也太可爱了吧",
                "已关注，期待更多作品",
                "666666",
                "第一次看就被圈粉了",
                "支持支持！",
                "这个创意太棒了",
                "笑死我了",
                "每天都要来看一遍",
                "这就是我想要的生活",
                "太有才了",
                "哇，学到了！",
                "这不得火？",
                "直接关注了",
                "我的快乐源泉"
        };
        String[] times = {"刚刚", "1分钟前", "5分钟前", "10分钟前", "30分钟前",
                "1小时前", "2小时前", "3小时前", "昨天", "2天前"};
        int[] avatars = {
                R.drawable.img_avatar_1, R.drawable.img_avatar_2, R.drawable.img_avatar_3,
                R.drawable.img_avatar_4, R.drawable.img_avatar_5, R.drawable.img_avatar_6,
                R.drawable.img_avatar_7, R.drawable.img_avatar_8, R.drawable.img_avatar_9,
                R.drawable.img_avatar_10
        };

        int load_count = Math.min(count, ADD_COMMENT_COUNT);
        Random random = new Random();
        for (int i = 0; i < load_count; i++) {
            CommentBean comment = new CommentBean(
                    false, // 默认未点赞
                    random.nextInt(1000), // 随机点赞数
                    times[random.nextInt(times.length)], // 随机时间
                    new AuthorBean(
                        names[random.nextInt(names.length)],
                        avatars[random.nextInt(avatars.length)]), // 随机作者
                    contents[random.nextInt(contents.length)] // 随机评论内容

            );
            comments.add(comment);
        }
        return comments;
    }
}
