package com.example.douyinline.bean;

import java.io.Serializable;

public class CommentBean implements Serializable {
    private String commentContent;
    private AuthorBean authorBean;
    private String commentTime;
    private int commentLikeCount;
    private boolean isLiked;

    public CommentBean(boolean isLiked, int commentLikeCount, String commentTime, AuthorBean authorBean, String commentContent) {
        this.isLiked = isLiked;
        this.commentLikeCount = commentLikeCount;
        this.commentTime = commentTime;
        this.authorBean = authorBean;
        this.commentContent = commentContent;
    }

    public String getCommentContent() {
        return commentContent;
    }

    public void setCommentContent(String commentContent) {
        this.commentContent = commentContent;
    }

    public AuthorBean getAuthorBean() {
        return authorBean;
    }

    public void setAuthorBean(AuthorBean authorBean) {
        this.authorBean = authorBean;
    }

    public String getCommentTime() {
        return commentTime;
    }

    public void setCommentTime(String commentTime) {
        this.commentTime = commentTime;
    }

    public int getCommentLikeCount() {
        return commentLikeCount;
    }

    public void setCommentLikeCount(int commentLikeCount) {
        this.commentLikeCount = commentLikeCount;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }
}
