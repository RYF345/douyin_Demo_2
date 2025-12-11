package com.example.douyinline.ui.video;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.douyinline.R;
import com.example.douyinline.bean.VideoBean;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.w3c.dom.Comment;


public class CommentBottomSheetFragment extends BottomSheetDialogFragment {
    private TextView tvCommentCount;
    private RecyclerView rvCommentList;
    private ImageView ivClose;
    private EditText editComment;
    private TextView tvSendComment;
    private ProgressBar progressBar;
    private static final String ARG_VIDEO = "video";
    private CommentViewModel commentViewModel;
    private VideoBean video;
    private boolean isLoading = false;
    private CommentSheetAdapter commentAdapter;
    private boolean hasMore = false;

    /**
     * 创建实例
     * @param video 输入视频对象，确认评论来源
     * @return fragment
     */
    public static CommentBottomSheetFragment newInstance(VideoBean video){
        CommentBottomSheetFragment fragment = new CommentBottomSheetFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_VIDEO, video);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();

        // 设置屏幕弹窗高度
        Dialog dialog = getDialog();
        if (dialog != null) {
            // 找到底部的 FrameLayout (它是 BottomSheet 的容器)
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);

            if (bottomSheet != null) {
                // 设置初始高度 (例如屏幕高度的 75%)
                DisplayMetrics displayMetrics = requireActivity().getResources().getDisplayMetrics();
                int height = (int) (displayMetrics.heightPixels * 0.75); // 0.75 即 75%

                bottomSheet.getLayoutParams().height = height; // 强行设置 View 高度

                // 获取 Behavior 并设置为展开状态
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);

                // 设置展开高度（PeekHeight）也为 75%
                behavior.setPeekHeight(height);
                // 默认状态设为“展开”
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video_play_comment_sheet, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvCommentCount = view.findViewById(R.id.tv_comment_count_comment_sheet);
        rvCommentList = view.findViewById(R.id.rv_comment_list_comment_sheet);
        ivClose = view.findViewById(R.id.iv_close_comment_sheet);
        editComment = view.findViewById(R.id.edit_comment_comment_sheet);
        tvSendComment = view.findViewById(R.id.tv_send_comment_comment_sheet);
        progressBar = view.findViewById(R.id.progress_loading_comment_sheet);

        progressBar.setVisibility(View.GONE);
        commentViewModel = new ViewModelProvider(this).get(CommentViewModel.class);
        if (getArguments() != null){
            video = (VideoBean) getArguments().getSerializable(ARG_VIDEO);
            if (video != null){
                tvCommentCount.setText(video.getCommentCount() + "条评论");
                commentViewModel.setVideo(video);
            }else{
                tvCommentCount.setText("0条评论");
            }
        }

        // 初始化监听器
        initListeners();

        // 初始RecyclerView
        initRecyclerView();

        // 观察ViewModel中数据变化
        observeViewModel();

        // 更新评论列表
        if (video != null){
            loadComments(video);
        }
    }

    private void initListeners() {
        // 关闭按钮监听
        ivClose.setOnClickListener(v -> dismiss());
        // 发送按钮监听
        tvSendComment.setOnClickListener(v -> {
            String comment = editComment.getText().toString().trim();
            if (!comment.isEmpty()) {
                commentViewModel.sendComment(comment);
                editComment.setText("");
                editComment.clearFocus();
                Toast.makeText(getContext(), "评论已发送", Toast.LENGTH_SHORT).show();
                // 更新标题显示的评论数
                video.setCommentCount(video.getCommentCount() + 1);
                tvCommentCount.setText(video.getCommentCount() + "条评论");
                // 发送新评论后滚动到顶部显示
                rvCommentList.scrollToPosition(0);
            } else {
                Toast.makeText(getContext(), "请输入评论内容", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void initRecyclerView() {
        commentAdapter = new CommentSheetAdapter();
        rvCommentList.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCommentList.setAdapter(commentAdapter);

        // 上拉加载更多
        rvCommentList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(!isLoading && dy > 0){
                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (layoutManager != null) {
                        int visibleItemCount = layoutManager.getChildCount();
                        int totalItemCount = layoutManager.getItemCount();
                        int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 2) {
                            isLoading = true;
                            progressBar.setVisibility(View.VISIBLE);
                            commentViewModel.loadMoreComments();
                        }
                    }
                }
            };
        });
    }

    private void observeViewModel() {
        commentViewModel.getCommentListLiveData().observe(this, comments -> {
            if (comments != null) {
                commentAdapter.refreshData(comments);
            }
        });
        commentViewModel.getLoading().observe(this, loading -> {
            if (loading != null) {
                progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
                // 根据ViewModel的加载状态来同步Fragment的isLoading标志
                this.isLoading = loading;
            }
        });
    }

    /**
     * 加载评论
     * @param video 视频对象
     */
    private void loadComments(VideoBean video) {
        commentViewModel.loadCommentsFirst(video);
    }
}