package com.itheima.musicproject;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.itheima.musicproject.activity.BaseTitleActivity;
import com.itheima.musicproject.activity.Facedetect;
import com.itheima.musicproject.activity.LoginActivity;
import com.itheima.musicproject.activity.SettingsActivity;
import com.itheima.musicproject.activity.UserDetailActivity;
import com.itheima.musicproject.adapter.HomeAdapter;
import com.itheima.musicproject.api.Api;
import com.itheima.musicproject.domain.User;
import com.itheima.musicproject.domain.event.LoginSuccessEvent;
import com.itheima.musicproject.domain.event.LogoutSuccessEvent;
import com.itheima.musicproject.domain.response.DetailResponse;
import com.itheima.musicproject.reactivex.HttpListener;
import com.itheima.musicproject.util.UserUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends BaseTitleActivity implements View.OnClickListener, ViewPager.OnPageChangeListener {

    private DrawerLayout drawer_Layout;
    ImageView iv_avatar;
    TextView tv_nickname;
    TextView tv_description;
    private ViewPager vp;
    private HomeAdapter adapter;
    private ImageView iv_music;
    private ImageView iv_recommend;
    private ImageView iv_video;
    private LinearLayout ll_settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getPermission();

    }

    private void getPermission(){
        //??????????????????
        int REQUEST_EXTERNAL_STORAGE = 1;
        String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        };
        int permission = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }


    @Override
    protected void initViews() {
        super.initViews();
        //??????????????????????????????????????????????????????????????????????????????
        EventBus.getDefault().register(this);
        
        vp = findViewById(R.id.vp);

        iv_music = findViewById(R.id.iv_music);
        iv_recommend = findViewById(R.id.iv_recommend);
        iv_video = findViewById(R.id.iv_video);

        ll_settings = findViewById(R.id.ll_settings);

        drawer_Layout = findViewById(R.id.drawer_layout);
        iv_avatar = findViewById(R.id.iv_avatar);
        tv_nickname = findViewById(R.id.tv_nickname);
        tv_description = findViewById(R.id.tv_description);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer_Layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer_Layout.addDrawerListener(toggle);
        toggle.syncState();


        //??????????????????
        vp.setOffscreenPageLimit(3);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void loginSuccessEvent(LoginSuccessEvent event) {
        showUserInfo();
    }

    @Override
    protected void initDatas() {
        super.initDatas();

        adapter = new HomeAdapter(getActivity(), getSupportFragmentManager());
        vp.setAdapter(adapter);

        ArrayList<Integer> datas = new ArrayList<>();
        datas.add(0);
        datas.add(1);
        datas.add(2);
        adapter.setDatas(datas);

        showUserInfo();
    }

    @Override
    protected void initListener() {
        super.initListener();

        iv_music.setOnClickListener(this);
        iv_recommend.setOnClickListener(this);
        iv_video.setOnClickListener(this);

        vp.addOnPageChangeListener(this);
        //???????????????????????????????????????????????????????????????????????????
        vp.setCurrentItem(1);

        ll_settings.setOnClickListener(this);

    }
    private void showData(User data) {
        //???????????????????????????????????????????????????????????????????????????????????????????????????
        UserUtil.showUser(getActivity(),data,iv_avatar,tv_nickname,tv_description);
    }





    @OnClick(R.id.iv_avatar)
    public void avatarClick() {
        closeDrawer();

        if (sp.isLogin()) {
            startActivityExtraId(UserDetailActivity.class, sp.getUserId());
        } else {
            startActivity(LoginActivity.class);
        }}


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.iv_music:
                vp.setCurrentItem(0, true);
                break;
            case R.id.iv_recommend:
                vp.setCurrentItem(1, true);
                break;
            case R.id.iv_video:
                startActivity(Facedetect.class);
                break;
            case R.id.ll_settings:
                startActivity(SettingsActivity.class);
                closeDrawer();
                break;

        }
    }

    private void closeDrawer() {
        drawer_Layout.closeDrawer(Gravity.START);
    }



    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if (position == 0) {
            iv_music.setImageResource(R.drawable.ic_play_selected);
            iv_recommend.setImageResource(R.drawable.ic_music);
            iv_video.setImageResource(R.drawable.ic_video);
        } else if (position == 1) {
            iv_music.setImageResource(R.drawable.ic_play);
            iv_recommend.setImageResource(R.drawable.ic_music_selected);
            iv_video.setImageResource(R.drawable.ic_video);
        } else {
            iv_music.setImageResource(R.drawable.ic_play);
            iv_recommend.setImageResource(R.drawable.ic_music);
            iv_video.setImageResource(R.drawable.ic_video_selected);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void logoutSuccessEvent(LogoutSuccessEvent event) {
        showUserInfo();
    }

    private void showUserInfo() {
        //???????????????????????????????????????????????????????????????????????????
        if (sp.isLogin()) {
            //????????????????????????
            Api.getInstance().userDetail(sp.getUserId())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new HttpListener<DetailResponse<User>>(getActivity()) {
                        @Override
                        public void onSucceeded(DetailResponse<User> data) {
                            super.onSucceeded(data);
                            showData(data.getData());
                        }
                    });

        } else {
            UserUtil.showNotLoginUser(getActivity(), iv_avatar, tv_nickname, tv_description);
        }
    }
    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
