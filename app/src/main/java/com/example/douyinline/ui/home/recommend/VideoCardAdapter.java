package com.example.douyinline.ui.home.recommend;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.douyinline.R;
import com.example.douyinline.bean.VideoBean;

import java.util.List;

/**
 * 推荐页双列视频流RecyclerView适配器
 */
public class VideoCardAdapter extends RecyclerView.Adapter<VideoCardAdapter.VideoCardViewHolder> {
    private Context context;
    private List<VideoBean> videoList;
    private OnItemClickListener listener;

    // 点击事件的回调接口
    public interface OnItemClickListener {
        void onItemClick(VideoBean video, int position);
    }

    // 设置添加监听器的方法
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // 设置获取数据的方法
    public void setVideoList(List<VideoBean> videoList) {
        this.videoList = videoList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VideoCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_video_card, parent, false);
        return new VideoCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoCardViewHolder holder, int position) {
        // 视频列表空值检查
        if(videoList == null || videoList.isEmpty() || position < 0 || position >= videoList.size()){
            return;
        }
        // 视频对象空值检查
        VideoBean videoBean = videoList.get(position);
        if(videoBean == null){
            return;
        }

        // 设置视频图片
        holder.ivCover.setImageResource(videoBean.getCoverResourceId());

        // 根据图片宽高比设置封面显示比例
        setCoverAspectRatio(holder, videoBean.getCoverResourceId());

        // 图片裁切 - 使用centerCrop对视频封面进行中心裁切
        RequestOptions options = new RequestOptions()
                .transform(new CenterCrop());

        Glide.with(holder.itemView.getContext())
                .load(videoBean.getCoverResourceId())
                .apply(options)
                .into(holder.ivCover);

        // 设置作者头像
        holder.ivAuthorAvatar.setImageResource(videoBean.getAuthorDetail().getAuthorAvatar());

        // 设置文字
        holder.tvTitle.setText(videoBean.getTitle());
        holder.tvLikeCount.setText(String.valueOf(videoBean.getLikeCount()));
        holder.tvAuthorName.setText(videoBean.getAuthorDetail().getAuthorName());

        // 设置点击事件
        holder.itemView.setOnClickListener(v->{
            if(listener != null){
                int currentPosition = holder.getBindingAdapterPosition();
                listener.onItemClick(videoList.get(currentPosition), currentPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return videoList == null ? 0 : videoList.size(); // 防止空指针
    }

    @Override
    public void onViewRecycled(@NonNull VideoCardViewHolder holder) {
        super.onViewRecycled(holder);
        // 清除 Glide 的加载请求，避免复用时显示旧图片
        Glide.with(holder.itemView.getContext()).clear(holder.ivAuthorAvatar);
    }

    /**
     * 根据图片的实际宽高比设置封面显示比例
     * 长图（高>宽）：3:4
     * 宽图（宽>高）：4:3
     */
    private void setCoverAspectRatio(@NonNull VideoCardViewHolder holder, int coverRes) {
        try {
            // 获取图片的实际宽高
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true; // 只获取尺寸，不加载完整图片
            BitmapFactory.decodeResource(context.getResources(), coverRes, options);

            int imageWidth = options.outWidth;
            int imageHeight = options.outHeight;

            if (imageWidth > 0 && imageHeight > 0) {
                // 获取屏幕宽度，计算每个item的宽度（2列布局，每个item有4dp的margin）
                DisplayMetrics metrics = context.getResources().getDisplayMetrics();
                int screenWidth = metrics.widthPixels;
                // CardView margin: 4dp * 2 (左右) = 8dp，两个item = 16dp
                // 转换为px: 16dp * density
                float density = metrics.density;
                int marginPx = (int) (16 * density); // 两个item的总margin
                int itemWidth = (screenWidth - marginPx) / 2;

                // 判断是长图还是宽图
                float aspectRatio;
                if (imageHeight > imageWidth) {
                    // 长图（竖图），使用3:4比例
                    aspectRatio = 3f / 4f;
                } else {
                    // 宽图（横图），使用4:3比例
                    aspectRatio = 4f / 3f;
                }

                // 计算高度
                int targetHeight = (int) (itemWidth / aspectRatio);

                // 设置ImageView的宽高
                ViewGroup.LayoutParams params = holder.ivCover.getLayoutParams();
                params.width = itemWidth;
                params.height = targetHeight;
                holder.ivCover.setLayoutParams(params);
            }
        } catch (Exception e) {
            // 如果获取图片尺寸失败，使用默认比例
            e.printStackTrace();
        }
    }
    public static class VideoCardViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        ImageView ivAuthorAvatar;
        ImageView ivLikeIcon;
        TextView tvTitle;
        TextView tvAuthorName;
        TextView tvLikeCount;

        public VideoCardViewHolder(@NonNull View itemView){
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_video_cover_recommend);
            ivAuthorAvatar = itemView.findViewById(R.id.iv_author_avatar_recommend);
            ivLikeIcon = itemView.findViewById(R.id.iv_like_icon_video_recommend);
            tvTitle = itemView.findViewById(R.id.tv_video_title_recommend);
            tvAuthorName = itemView.findViewById(R.id.tv_author_name_recommend);
            tvLikeCount = itemView.findViewById(R.id.tv_like_count_recommend);

        }

    }
}
