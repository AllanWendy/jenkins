package cn.wecook.app.main.home.setting;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wecook.common.core.debug.DebugCenter;
import com.wecook.common.core.debug.Logger;
import com.wecook.common.modules.property.PhoneProperties;
import com.wecook.common.utils.AndroidUtils;
import com.wecook.common.utils.MorseCodeUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.uikit.alarm.ToastAlarm;
import com.wecook.uikit.fragment.BaseTitleFragment;

import cn.wecook.app.R;
import cn.wecook.app.WecookConfig;
import cn.wecook.app.features.debug.ListDebugFragment;

/**
 * 关于
 * Created by LK on 2015/9/21.
 */
public class AboutFragment extends BaseTitleFragment {
    private static final String META_DATA_DEBUG_VERSION = "DEBUG_VERSION";
    private static final int MAX_CLICK_TIME = 20;

    private int mClickTime;
    private String debugVersion;


    private TextView tvDebug, tvVersion;
    private LinearLayout layoutDebug;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return LayoutInflater.from(getContext()).inflate(R.layout.view_about, null);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getTitleBar().setBackListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back();
            }
        });
        setTitle("关于味库");

        tvDebug = (TextView) view.findViewById(R.id.app_about_debug_text);
        tvVersion = (TextView) view.findViewById(R.id.app_about_version_code);
        layoutDebug = (LinearLayout) view.findViewById(R.id.app_about_item_debug_layout);


        initView(R.id.app_about_item_business);//商务合作
        initView(R.id.app_about_item_share);//分享
        initView(R.id.app_about_item_praise);//好评
        initView(R.id.app_about_item_debug);//DEBUG

        view.findViewById(R.id.app_about_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                debug();
            }
        });
        updateDebugView();
    }

    /**
     * 初始化View
     * 资源布局
     *
     * @param rid
     */
    private void initView(int rid) {
        //Title
        TextView textView = (TextView) getView().findViewById(rid).findViewById(R.id.app_my_feature_name);
        String title = "";
        switch (rid) {
            case R.id.app_about_item_business:
                title = "商务合作";
                break;
            case R.id.app_about_item_share:
                title = "分享味库给小伙伴";
                break;
            case R.id.app_about_item_praise:
                title = "给味库好评";
                break;
            case R.id.app_about_item_debug:
                title = "Debug模式";
                break;
        }
        textView.setText(title);
        //监听
        if (rid != R.id.app_about_item_debug) {
            getView().findViewById(rid).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (v.getId()) {
                        case R.id.app_about_item_business://商务合作
                            next(BusinessCooperationFragment.class);
                            break;
                        case R.id.app_about_item_share://分享
                            shareApp();
                            break;
                        case R.id.app_about_item_praise://好评
                            requestStar();
                            break;
                    }
                }
            });
        }

    }

    /**
     * 分享APP
     */
    private void shareApp() {
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.app_share_app_content));
            startActivity(intent);
        } catch (Throwable throwable) {
        }
    }

    /**
     * 请求市场评分
     */
    private void requestStar() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=" + PhoneProperties.getPackageName()));
            startActivity(intent);
        } catch (Throwable e) {
            ToastAlarm.show("没有找到符合的应用市场，快去下载一个吧...");
        }
    }

    private void updateDebugView() {
        String versionNameString = PhoneProperties.getVersionName();
        if (!StringUtils.isEmpty(debugVersion) && mClickTime > MAX_CLICK_TIME) {
            versionNameString = debugVersion;
        }

        if (WecookConfig.getInstance().isTest()) {
            versionNameString += " 内测版";
        } else if (DebugCenter.isDebugable()) {
            versionNameString += " 调试版";
        }
        tvVersion.setText("味库 Wecook V" + versionNameString);
    }

    private void debug() {
        if (DebugCenter.isDebugable()) {
            mClickTime = MAX_CLICK_TIME + 1;
        }

        mClickTime++;
        if (mClickTime < MAX_CLICK_TIME && mClickTime >= MAX_CLICK_TIME / 2) {
            ToastAlarm.show("再点" + (MAX_CLICK_TIME - mClickTime) + "次进入Debug模式");
        }
        if (mClickTime > MAX_CLICK_TIME) {
            debugVersion = String.valueOf(AndroidUtils.getMetaDataFromApplication(getContext(), META_DATA_DEBUG_VERSION));
            updateDebugView();
            layoutDebug.setVisibility(View.VISIBLE);
            tvDebug.setVisibility(View.VISIBLE);
            tvDebug.setText("");
            final String pwd = MorseCodeUtils.translateToCode("c");
            if (DebugCenter.isDebugable()) {
                layoutDebug.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        next(ListDebugFragment.class);
                    }
                });
            } else {
                MorseCodeUtils.clear();
                tvDebug.setText("");
                MorseCodeUtils.morseCode(getContext(), layoutDebug, new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        String code = MorseCodeUtils.getMorseCode();
                        Logger.d("code : " + code + " pwd : " + pwd);
                        tvDebug.setText(code);
                        if (pwd.equals(code)) {
                            next(ListDebugFragment.class);
                        }
                        return false;
                    }
                });
            }
        }
    }
}
