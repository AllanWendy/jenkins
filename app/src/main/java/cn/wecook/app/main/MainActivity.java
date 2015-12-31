package cn.wecook.app.main;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;
import android.view.Window;

import com.wecook.common.app.AppLink;
import com.wecook.common.core.debug.Logger;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.messager.XMPushMessager;
import com.wecook.common.utils.SecurityUtils;
import com.wecook.sdk.api.model.Restaurant;
import com.wecook.sdk.policy.MessageQueuePolicy;
import com.wecook.sdk.policy.UpdatePolicy;
import com.wecook.sdk.userinfo.UserProperties;
import com.wecook.uikit.activity.BaseSwipeActivity;
import com.wecook.uikit.alarm.ToastAlarm;
import com.wecook.uikit.fragment.BaseFragment;
import com.wecook.uikit.tools.SmartBarUtils;
import com.zhuge.analysis.stat.ZhugeSDK;
import com.zhuge.push.msg.ZGPush;

import org.json.JSONObject;

import java.util.List;

import cn.wecook.app.R;
import cn.wecook.app.WecookApp;
import cn.wecook.app.dialog.AppUpdateDialog;
import cn.wecook.app.launch.LaunchActivity;
import cn.wecook.app.main.search.SearchActivity;

/**
 * @author kevin
 * @version v1.0
 * @since 2014-10/8/14
 */
public class MainActivity extends BaseSwipeActivity {

    public static final String ACTION_RELOAD = "action_reload_activity";

    private static final int DELAY_MILLIS = 2000;

    private boolean enableMeizu = false;

    private List<Restaurant> food_feed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //检查新版本
        UpdatePolicy.get().checkUpdateVersion(this, new AppUpdateDialog(this));

        Intent intent = getIntent();
        if (intent != null) {
            //由H5开启软件，先进入启动页面
            if (AppLink.SCHEME_APP.equals(intent.getScheme())) {
                startActivity(new Intent(this, LaunchActivity.class));
                finish();
                return;
            }
        }

//        UmengUpdateAgent.setUpdateOnlyWifi(true);
//        UmengUpdateAgent.setUpdateAutoPopup(false);
//        UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {
//            @Override
//            public void onUpdateReturned(int updateStatus, UpdateResponse updateResponse) {
//                switch (updateStatus) {
//                    case UpdateStatus.Yes: // has update
//                        SharePreferenceProperties.set(PhoneProperties.CHANNEL, PhoneProperties.getChannel());
//                        UmengUpdateAgent.showUpdateDialog(getContext(), updateResponse);
//                        break;
//                    case UpdateStatus.No: // has no update
//                        break;
//                    case UpdateStatus.NoneWifi: // none wifi
//                        break;
//                    case UpdateStatus.Timeout: // time out
//                        break;
//                }
//            }
//        });
//        UmengUpdateAgent.update(MainActivity.this);

        if (UserProperties.isLogin()) {

            XMPushMessager.setAlias(MainActivity.this, SecurityUtils.encodeByMD5(UserProperties.getUserId()));
            XMPushMessager.subscribe(MainActivity.this, SecurityUtils.encodeByMD5(UserProperties.getUserId()));

            JSONObject jsonObject = UserProperties.getUserJSONObject();
            ZhugeSDK.getInstance().identify(getApplicationContext(), UserProperties.getUserId(), jsonObject);
        }

        ZGPush.getInstance().msgStatistics(this);
        Logger.d("launch_app", "onCreate time:" + System.currentTimeMillis());
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onPreCreate() {
        super.onPreCreate();
        /**
         * 魅族SmartBar适配
         */
        if (Build.VERSION.SDK_INT >= 14) {
            final ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                if ("meizu".equalsIgnoreCase(Build.BRAND) && enableMeizu) {
                    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
                    actionBar.setHomeButtonEnabled(true);
                    actionBar.setDisplayOptions(0);
                    final ActionBar.Tab mSearchTab = actionBar.newTab();
                    ActionBar.TabListener mTabListener = new ActionBar.TabListener() {

                        @Override
                        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {

                        }

                        @Override
                        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

                        }

                        @Override
                        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
                            if (tab == mSearchTab) {
                                Intent intent = new Intent(getContext(), SearchActivity.class);
                                startActivity(intent);
                            }
                        }
                    };
                    actionBar.addTab(mSearchTab.setIcon(R.drawable.uikit_ic_food_search).setTabListener(mTabListener));
                    SmartBarUtils.setActionBarTabsShowAtBottom(actionBar, true);
                    SmartBarUtils.setActionBarViewCollapsable(actionBar, true);
                } else {
                    actionBar.hide();
                }
            }
        } else {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
    }

    @Override
    protected BaseFragment onCreateFragment(Bundle savedInstanceState) {
        setIsAppRootActivity(true);
        Intent intent = getIntent();
        if (intent != null) {
            if (savedInstanceState == null) {
                savedInstanceState = new Bundle();
            }
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                savedInstanceState.putAll(bundle);
            }
        }
        return BaseFragment.getInstance(MainFragment.class, savedInstanceState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_RELOAD));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean catchKeyDown = super.onKeyDown(keyCode, event);
        if (!catchKeyDown) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {

                ToastAlarm.show(R.string.app_exit_toast, DELAY_MILLIS).among(new Runnable() {
                    @Override
                    public void run() {
                        WecookApp.quitApp();
                    }
                });
            }
        }

        return catchKeyDown;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MessageQueuePolicy.getInstance().requestNewMessage();
    }

    @Override
    protected void onStop() {
        super.onStop();
        UIHandler.stopLoops();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //请求新消息
        MessageQueuePolicy.getInstance().requestNewMessage();
    }


    public List<Restaurant> getFood_feed() {
        return food_feed;
    }

    public void setFood_feed(List<Restaurant> food_feed) {
        this.food_feed = food_feed;
    }

}
