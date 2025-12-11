package com.example.douyinline.ui.home;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.douyinline.R;
import com.example.douyinline.ui.home.others.EmptyFragment;
import com.example.douyinline.ui.home.recommend.RecommendFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * 展示主页面的Fragment
 */
public class HomeFragment extends Fragment {
    private TabLayout tabLayout;
    private ViewPager2 viewPager2;
    private HomeViewPagerAdapter homeViewPagerAdapter;
    private TabLayoutMediator tabLayoutMediator;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 初始化控件
        tabLayout = view.findViewById(R.id.tbl_top_tab_bar_home);
        viewPager2 = view.findViewById(R.id.vp_home);

        // 初始化适配器
        homeViewPagerAdapter = new HomeViewPagerAdapter(requireActivity());

        // 添加“推荐页”
        homeViewPagerAdapter.addFragment(new RecommendFragment(), "推荐");
        // 添加“其他页”
        homeViewPagerAdapter.addFragment(new EmptyFragment(), "直播");
        homeViewPagerAdapter.addFragment(new EmptyFragment(), "游戏");
        homeViewPagerAdapter.addFragment(new EmptyFragment(), "娱乐");
        homeViewPagerAdapter.addFragment(new EmptyFragment(), "二次元");
        homeViewPagerAdapter.addFragment(new EmptyFragment(), "体育");
        homeViewPagerAdapter.addFragment(new EmptyFragment(), "户外");
        homeViewPagerAdapter.addFragment(new EmptyFragment(), "美食");

        // 设置适配器
        viewPager2.setAdapter(homeViewPagerAdapter);

        // 设置tablayout和viewpager2联动
        new TabLayoutMediator(tabLayout, viewPager2, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setText(homeViewPagerAdapter.getTitle(position));
            }
        }).attach();

        // 设置tab滚动模式以防标签过多
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);

    }
}