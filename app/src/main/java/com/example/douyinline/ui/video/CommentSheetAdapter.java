package com.example.douyinline.ui.video;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.douyinline.R;
import com.example.douyinline.bean.CommentBean;

import java.util.ArrayList;
import java.util.List;

/**
 * 评论列表适配器
 * 1. 刷新数据
 * 2. 添加单条数据
 */
public class CommentSheetAdapter extends RecyclerView.Adapter<CommentSheetAdapter.CommentViewHolder> {
    private List<CommentBean> commentsList;

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        CommentBean comment = commentsList.get(position);
        // 设置文字
        holder.tvUserName.setText(comment.getAuthorBean().getAuthorName());
        holder.tvContent.setText(comment.getCommentContent());
        holder.tvTime.setText(comment.getCommentTime());
        holder.tvLikeCount.setText(String.valueOf(comment.getCommentLikeCount()));

        // 设置头像
        holder.ivAvatar.setImageResource(comment.getAuthorBean().getAuthorAvatar());
    }

    @Override
    public int getItemCount() {
        return commentsList == null ? 0 : commentsList.size();
    }

    /**
     * 刷新数据
     * @param commentsList 评论列表
     */
    public void refreshData(List<CommentBean> commentsList) {
        this.commentsList = commentsList;
        notifyDataSetChanged();
    }

    /**
     * 添加单条数据
     */
    public void addCommentTop(CommentBean comment) {
        if (commentsList != null) {
            commentsList.add(0, comment);
            notifyItemInserted(0);
        }else{
            commentsList = new ArrayList<>();
            commentsList.add(0, comment);
            notifyItemInserted(0);
        }
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        private TextView tvUserName;
        private TextView tvContent;
        private TextView tvTime;
        private TextView tvLikeCount;
        private ImageView ivAvatar;
        private ImageView ivLike;

        public CommentViewHolder(View itemView) {
            super(itemView);
            // 初始化视图
            tvUserName = itemView.findViewById(R.id.tv_user_name_comment_sheet);
            tvContent = itemView.findViewById(R.id.tv_content_comment_sheet);
            tvTime = itemView.findViewById(R.id.tv_time_comment_sheet);
            tvLikeCount = itemView.findViewById(R.id.tv_like_count_comment_sheet);
            ivAvatar = itemView.findViewById(R.id.iv_avatar_comment_sheet);
        }
    }

}
