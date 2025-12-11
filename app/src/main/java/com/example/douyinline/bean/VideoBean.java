package com.example.douyinline.bean;

import java.io.Serializable;

/**
 * 视频类
 */
public class VideoBean implements Serializable {
    private int coverResourceId;
    private int videoResourceId;
    private String title;
    private int likeCount;
    private int commentCount;
    private int shareCount;
    private int collectCount;

    private boolean isLiked = false;
    private boolean isCollected = false;

    private AuthorBean authorDetail;

    public VideoBean(int coverResourceId, int videoResourceId, String title, int likeCount, int commentCount, int shareCount, int collectCount, AuthorBean authorDetail) {
        this.coverResourceId = coverResourceId;
        this.videoResourceId = videoResourceId;
        this.title = title;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.shareCount = shareCount;
        this.collectCount = collectCount;
        this.authorDetail = authorDetail;
    }

    public AuthorBean getAuthorDetail() {
        return authorDetail;
    }

    public void setAuthorDetail(AuthorBean authorDetail) {
        this.authorDetail = authorDetail;
    }

    public int getCoverResourceId() {
        return coverResourceId;
    }

    public void setCoverResourceId(int coverResourceId) {
        this.coverResourceId = coverResourceId;
    }

    public int getVideoResourceId() {
        return videoResourceId;
    }

    public void setVideoResourceId(int videoResourceId) {
        this.videoResourceId = videoResourceId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public int getShareCount() {
        return shareCount;
    }

    public void setShareCount(int shareCount) {
        this.shareCount = shareCount;
    }

    public int getCollectCount() {
        return collectCount;
    }

    public void setCollectCount(int collectCount) {
        this.collectCount = collectCount;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public boolean isCollected() {
        return isCollected;
    }

    public void setCollected(boolean collected) {
        isCollected = collected;
    }
}
