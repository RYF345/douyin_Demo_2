package com.example.douyinline;

import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.douyinline.ui.friends.FriendsFragment;
import com.example.douyinline.ui.home.HomeFragment;
import com.example.douyinline.ui.me.MeFragment;
import com.example.douyinline.ui.message.MessageFragment;

public class MainActivity extends AppCompatActivity {
    private TextView btnHomepage, btnFriends, btnMessage, btnMe;
    private HomeFragment homeFragment;
    private FriendsFragment friendsFragment;
    private MessageFragment messageFragment;
    private MeFragment meFragment;
    private Fragment currentFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 初始化Fragment页面
        initFragment();

        // 初始化底部导航栏
        initBottomNavigation();

    }

    /**
     * 初始化所有Fragment页面
     */
    private void initFragment(){
        // 初始化所有页面
        homeFragment = new HomeFragment();
        friendsFragment = new FriendsFragment();
        messageFragment = new MessageFragment();
        meFragment = new MeFragment();
    }
    /**
     * 初始化底部导航栏
     */
    private void initBottomNavigation() {
        btnHomepage = findViewById(R.id.tv_home_main);
        btnFriends = findViewById(R.id.tv_friends_main);
        btnMessage = findViewById(R.id.tv_message_main);
        btnMe = findViewById(R.id.tv_me_main);

        // 为每一个按钮设置监听器
        btnHomepage.setOnClickListener(v -> {switchToFragment(0);});
        btnFriends.setOnClickListener(v -> {switchToFragment(1);});
        btnMessage.setOnClickListener(v -> {switchToFragment(2);});
        btnMe.setOnClickListener(v -> {switchToFragment(3);});

        // 默认显示首页
        switchToFragment(0);
        // 更新底部导航栏样式
        updateBottomNavigation(0);
    }

    /**
     * 切换到指定的Fragment
     * @param index 0-首页，1-朋友，2-消息，3-我的
     */
    private void switchToFragment(int index) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        Fragment targetFragment = null;
        // 根据index选择目标Fragment
        switch (index){
            case 0:
                targetFragment = homeFragment;
                break;
            case 1:
                targetFragment = friendsFragment;
                break;
            case 2:
                targetFragment = messageFragment;
                break;
            case 3:
                targetFragment = meFragment;
                break;
        }

        if (targetFragment != null && targetFragment != currentFragment){
            // 如果当前有Fragment，先隐藏它
            if (currentFragment != null) {
                transaction.hide(currentFragment);
            }

            // 如果目标Fragment未添加，则添加并显示
            if (!targetFragment.isAdded()){
                transaction.add(R.id.fl_container_main, targetFragment);
            } else {
                transaction.show(targetFragment);
            }
            transaction.commit();
            currentFragment = targetFragment;

            // 更新底部导航栏样式
            updateBottomNavigation(index);
        }
    }
    /**
     * 更新底部导航栏的样式
     */
    private void updateBottomNavigation(int index){
        int unselectedColor = ContextCompat.getColor(this, R.color.gray);
        int selectedColor = ContextCompat.getColor(this, R.color.black);

        // 设置字体颜色
        btnHomepage.setTextColor(index == 0 ? selectedColor : unselectedColor);
        btnFriends.setTextColor(index == 1 ? selectedColor : unselectedColor);
        btnMessage.setTextColor(index == 2 ? selectedColor : unselectedColor);
        btnMe.setTextColor(index == 3 ? selectedColor : unselectedColor);

    }

}