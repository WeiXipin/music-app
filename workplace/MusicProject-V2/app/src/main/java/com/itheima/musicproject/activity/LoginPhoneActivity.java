package com.itheima.musicproject.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;

import com.itheima.musicproject.MainActivity;
import com.itheima.musicproject.R;
import com.itheima.musicproject.api.Api;
import com.itheima.musicproject.domain.Session;
import com.itheima.musicproject.domain.User;
import com.itheima.musicproject.domain.event.LoginSuccessEvent;
import com.itheima.musicproject.domain.response.DetailResponse;
import com.itheima.musicproject.reactivex.HttpListener;
import com.itheima.musicproject.util.StringUtil;
import com.itheima.musicproject.util.ToastUtil;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class LoginPhoneActivity extends BaseTitleActivity{

    @BindView(R.id.et_phone)
    EditText et_phone;

    @BindView(R.id.et_password)
    EditText et_password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_phone);
    }

    @OnClick(R.id.bt_login)
    public void bt_login() {


        String phone = et_phone.getText().toString();
        if (StringUtils.isBlank(phone)) {
            ToastUtil.showSortToast(getActivity(), R.string.hint_phone);
            return;
        }

        if (!StringUtil.isPhone(phone)) {
            ToastUtil.showSortToast(getActivity(), R.string.hint_error_phone);
            return;
        }

        String password = et_password.getText().toString();
        if (StringUtils.isBlank(password)) {
            ToastUtil.showSortToast(getActivity(), R.string.hint_password);
            return;
        }

        if (!StringUtil.isPassword(password)) {
            ToastUtil.showSortToast(getActivity(), R.string.hint_error_password_format);
            return;
        }

        User user = new User();
        user.setPhone(phone);
        user.setPassword(password);
        user.setType(User.TYPE_PHONE);

        Api.getInstance().login(user)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HttpListener<DetailResponse<Session>>(getActivity()) {
                    @Override
                    public void onSucceeded(DetailResponse<Session> data) {
                        super.onSucceeded(data);
                        next(data.getData());
                    }
                });
    }

    /**
     * 登陆完成后，保存相关信息，并跳转到主界面
     *
     * @param data
     */
    private void next(Session data) {

        sp.setToken(data.getToken());
        sp.setUserId(data.getId());
        sp.setIMToken(data.getIm_token());
        startActivityAfterFinishThis(MainActivity.class);

        EventBus.getDefault().post(new LoginSuccessEvent());
    }
}
