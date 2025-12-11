package com.example.douyinline.ui.home.recommend;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.douyinline.R;
import com.example.douyinline.bean.VideoBean;
import com.example.douyinline.event.NavigationEvent;
import com.example.douyinline.ui.video.VideoPlayActivity;
import com.scwang.smart.refresh.layout.api.RefreshLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * 推荐页Fragment
 */
public class RecommendFragment extends Fragment {
    private VideoCardAdapter videoCardAdapter;
    private RecyclerView recyclerView;
    private RecommendViewModel viewModel;
    private RefreshLayout refreshLayout;

    public RecommendFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recommend, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化ViewModel
        viewModel = new ViewModelProvider(this).get(RecommendViewModel.class);

        // 获取 recyclerView 控件
        recyclerView = view.findViewById(R.id.recyclerView);
        refreshLayout = view.findViewById(R.id.refreshLayout);

        // 初始化 RecyclerView
        initRecyclerView();

        // 观察ViewModel的数据变化
        observeViewModel();

        // 初始化刷新布局
        initRefreshLayout();
    }
    /**
     * 初始化刷新布局
     */
    private void initRefreshLayout() {
        refreshLayout.setEnableLoadMore(true);
        refreshLayout.setEnableRefresh(true);
        // 监听刷新事件
        refreshLayout.setOnRefreshListener(refreshLayout -> {
            // 调用ViewModel的方法加载推荐视频列表
            viewModel.refreshRecommendVideos();
            refreshLayout.finishRefresh();
        });
        // 监听加载更多事件
        refreshLayout.setOnLoadMoreListener(refreshLayout -> {
            // 调用ViewModel的方法加载更多推荐视频列表
            viewModel.loadMoreRecommendVideos();
            refreshLayout.finishLoadMore();
        });
        // 首次进入刷新列表
        viewModel.refreshRecommendVideos();
    }
    /**
     * 初始化 RecyclerView
     */
    public void initRecyclerView() {
        // 创建 RecyclerView 的适配器
        videoCardAdapter = new VideoCardAdapter();

        // 设置点击监听 - 将点击事件传递给ViewModel处理
        videoCardAdapter.setOnItemClickListener(new VideoCardAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(VideoBean video, int position) {
                viewModel.onVideoCardClick(video, position);
            }
        });

        // 创建 RecyclerView 的布局管理器
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);

        // 防止瀑布流跳动
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(videoCardAdapter);

    }

    /**
     * 观察ViewModel的数据变化，更新UI
     */
    public void observeViewModel() {
        // 观察视频列表变化
        viewModel.getVideoListLiveData().observe(getViewLifecycleOwner(), new Observer<List<VideoBean>>() {
            @Override
            public void onChanged(List<VideoBean> videoList) {
                if (videoList != null) {
                    videoCardAdapter.setVideoList(videoList);
                }
            }
        });
        
        // 观察Toast消息变化
        viewModel.getToastMessageLiveData().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String toastMessage) {
                if(toastMessage != null){
                    Toast.makeText(requireContext(), toastMessage, Toast.LENGTH_SHORT).show();
                }
            }
        });


        // 观察是否还有更多数据变化
        viewModel.getHasMoreDataLiveData().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean hasMoreData) {
                if (refreshLayout != null) {
                    refreshLayout.setEnableLoadMore(Boolean.TRUE.equals(hasMoreData));
                }
            }
        });

        // 观察导航事件变化
        viewModel.getNavigateToVideoLiveData().observe(getViewLifecycleOwner(), new Observer<NavigationEvent>() {
            @Override
            public void onChanged(NavigationEvent navigationEvent) {
                if (navigationEvent != null) {
                    Intent intent = new Intent(requireContext(), VideoPlayActivity.class);
                    intent.putExtra("startPosition", navigationEvent.getPosition());
                    intent.putExtra("coverResId", navigationEvent.getCoverResId());
                    startActivity(intent);
                    // 禁用默认转场动画，使封面过渡更自然
                    requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            }
        });
    }
}
