package com.example.douyinline.ui.home;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Home页面viewpager适配器
 */
public class HomeViewPagerAdapter extends FragmentStateAdapter {
    private List<Fragment> fragmentList = new ArrayList<>();
    private List<String> titleList = new ArrayList<>();
    public HomeViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getItemCount() {
        return fragmentList.size();
    }

    /**
     * 添加fragment
     * @param fragment fragment
     * @param title 标题
     */
    public void addFragment(Fragment fragment, String title) {
        fragmentList.add(fragment);
        titleList.add(title);
    }

    /**
     * 获取标题
     * @param position 位置
     * @return 标题
     */
    public String getTitle(int position) {
        return titleList.get(position);
    }
}
