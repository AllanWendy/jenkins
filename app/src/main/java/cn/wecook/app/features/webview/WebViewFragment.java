package cn.wecook.app.features.webview;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.wecook.common.core.debug.Logger;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.modules.jsbridge.WebViewJavascriptBridge;
import com.wecook.common.modules.location.LocationServer;
import com.wecook.common.modules.network.NetworkState;
import com.wecook.common.modules.property.PhoneProperties;
import com.wecook.common.modules.thirdport.SimplePlatformEventListener;
import com.wecook.common.modules.thirdport.platform.base.IPlatform;
import com.wecook.common.utils.JsonUtils;
import com.wecook.common.utils.ScreenUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.policy.UpdatePolicy;
import com.wecook.sdk.userinfo.UserProperties;
import com.wecook.uikit.adapter.UIAdapter;
import com.wecook.uikit.fragment.BaseWebViewFragment;
import com.wecook.uikit.widget.TitleBar;
import com.wecook.uikit.window.PopListWindow;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cn.wecook.app.R;
import cn.wecook.app.dialog.AppUpdateDialog;
import cn.wecook.app.features.thirdport.ThirdPortDelivery;
import cn.wecook.app.main.dish.DishActivity;
import cn.wecook.app.main.home.user.UserRapidLoginFragment;

/**
 * 网页
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/18/14
 */
public class WebViewFragment extends BaseWebViewFragment {

    private static final String JS_METHOD_GET_APP_INFO = "getAppInfo";
    private static final String JS_METHOD_GET_USER_INFO = "getUserInfo";
    private static final String JS_METHOD_GET_PAY_COMPONENTS = "getPayComponents";
    private static final String JS_METHOD_GET_LOCATION = "getLocation";

    private static final String JS_METHOD_SHOW_OPTION_MENU = "showOptionMenu";
    private static final String JS_METHOD_HIDE_OPTION_MENU = "hideOptionMenu";
    private static final String JS_METHOD_GET_OPTIONS = "menu:option";
    private static final String JS_METHOD_UPDATE_OPTIONS = "updateOptions";
    private static final String JS_METHOD_CLICK_OPTION = "doOptionMenu";
    private static final String JS_METHOD_SET_BACK_TYPE = "setBackType";
    private static final String JS_METHOD_SHOW_CLOSE_MENU = "showCloseMenu";
    private static final String JS_METHOD_HIDE_CLOSE_MENU = "hideCloseMenu";
    private static final String JS_METHOD_SHOW_SHARE_MENU = "showShareMenu";
    private static final String JS_METHOD_HIDE_SHARE_MENU = "hideShareMenu";
    private static final String JS_METHOD_SHOW_SHOPCART_MENU = "showShopcartMenu";
    private static final String JS_METHOD_HIDE_SHOPCART_MENU = "hideShopcartMenu";
    private static final String JS_METHOD_UPDATE_TITLE = "updateTitle";
    private static final String JS_METHOD_CHECK_VERSION = "checkVersion";

    private static final String JS_METHOD_SHARE_DIALOG = "shareDialog";
    private static final String JS_METHOD_SHARE_DO = "doShare";
    private static final String JS_METHOD_GET_SHARE_WEIXIN = "menu:share:wxfriends";
    private static final String JS_METHOD_GET_SHARE_WEIBO = "menu:share:weibo";
    private static final String JS_METHOD_GET_SHARE_WEIXIN_FRIEND = "menu:share:wxtimeline";

    private static final int BACK_TYPE_APP = 1;
    private static final int BACK_TYPE_WEBVIEW = 2;
    private static final int BACK_TYPE_WEBVIEW_INTERNAL = 3;
    private static final String TAG = "jsbridge";

    private String mShareType;

    private BroadcastReceiver mUserReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UserProperties.INTENT_LOGIN.equals(intent.getAction())) {
                if (mInGetUserInfo) {
                    mInGetUserInfo = false;
                    if (mJsCallback != null) {
                        String userJson = UserProperties.toJson();
                        mJsCallback.callback(userJson);
                    }
                }
            }
        }
    };

    private WebViewJavascriptBridge mBridge;
    private WebView mWeView;
    private TextView mClosePage;
    private TitleBar.ActionCoveredImageView mShareMenu;
    private TitleBar.ActionCoveredImageView mShopcartMenu;
    private TitleBar.ActionCoveredImageView mOptionMenu;

    private boolean mInGetUserInfo;
    private WebViewJavascriptBridge.WVJBResponseCallback mJsCallback;
    private int mBackType;
    private List<Option> mMenuOptions = new ArrayList<Option>();

    private ArrayList<WebShareData> shareDatas = new ArrayList<WebShareData>();
    private WebShareData shareData;
    private boolean mIsJustShowBtn;

    private PopListWindow mOptionWindow;
    private OptionAdapter mOptionAdapter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public View onCreateOperatorView(ViewGroup parent) {
        return null;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBridge = getJsbridge();
        mWeView = getWebView();
        initJsBridge();

        mClosePage = getTitleBar().getBackTitleView();
        mClosePage.setText("关闭");
        mClosePage.setTextColor(getResources().getColor(R.color.uikit_font_orange));
        mClosePage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishFragment();
            }
        });
        mClosePage.setVisibility(View.GONE);

        mShareMenu = new TitleBar.ActionCoveredImageView(getContext(), R.drawable.app_bt_share_highlight);
        mShareMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shareData != null && WebShareData.SHARE_TO_WEIXIN_FRIENDS.equals(shareData.getShareTo())) {
                    if ("weixin".equals(mShareType)) {
                        ThirdPortDelivery.shareOnlyWeixin(getContext(), shareDatas);
                    } else {
                        ThirdPortDelivery.share(getContext(), shareDatas);
                    }
                }
            }
        });

        mShopcartMenu = new TitleBar.ActionCoveredImageView(getContext(), R.drawable.app_ic_shop_cart_nobg);
        mShopcartMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), DishActivity.class);
                intent.putExtra(DishActivity.EXTRA_PAGE, DishActivity.PAGE_SHOP_CART);
                startActivity(intent);
            }
        });

        mOptionMenu = new TitleBar.ActionCoveredImageView(getContext(), R.drawable.app_ic_more);
        mOptionMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBridge.callHandler(JS_METHOD_GET_OPTIONS);
            }
        });

        mShareMenu.setVisibility(View.GONE);
        mShopcartMenu.setVisibility(View.GONE);
        mOptionMenu.setVisibility(View.GONE);

        getTitleBar().addActionView(mShareMenu);
        getTitleBar().addActionView(mShopcartMenu);
        getTitleBar().addActionView(mOptionMenu);

        IntentFilter filter = new IntentFilter();
        filter.addAction(UserProperties.INTENT_LOGIN);
        filter.addAction(UserProperties.INTENT_LOGOUT);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mUserReceiver, filter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mUserReceiver);
    }

    private void initJsBridge() {

        mBridge.setSimpleJBHandler(new WebViewJavascriptBridge.WVJBHandler() {
            @Override
            public void handle(final String data, WebViewJavascriptBridge.WVJBResponseCallback jsCallback) {
                Logger.i(TAG, "[WebViewJavascriptBridge..init] data:" + data);
                UIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (JS_METHOD_HIDE_OPTION_MENU.equals(data)) {
                            mOptionMenu.setVisibility(View.GONE);
                        } else if (JS_METHOD_SHOW_OPTION_MENU.equals(data)) {
                            mOptionMenu.setVisibility(View.VISIBLE);
                        } else if (JS_METHOD_SHOW_CLOSE_MENU.equals(data)) {
                            mClosePage.setVisibility(View.VISIBLE);
                        } else if (JS_METHOD_HIDE_CLOSE_MENU.equals(data)) {
                            mClosePage.setVisibility(View.GONE);
                        } else if (JS_METHOD_SHOW_SHOPCART_MENU.equals(data)) {
                            mShopcartMenu.setVisibility(View.VISIBLE);
                        } else if (JS_METHOD_HIDE_SHOPCART_MENU.equals(data)) {
                            mShopcartMenu.setVisibility(View.GONE);
                        } else if (JS_METHOD_HIDE_SHARE_MENU.equals(data)) {
                            mShareMenu.setVisibility(View.GONE);
                        }
                    }
                });

            }
        });

        mBridge.registerHandler(JS_METHOD_GET_APP_INFO, new WebViewJavascriptBridge.WVJBHandler() {
            @Override
            public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback jsCallback) {
                Logger.i(TAG, JS_METHOD_GET_APP_INFO + " data:" + data);
                String phoneJson = PhoneProperties.toJson();
                //获取APP信息
                jsCallback.callback(phoneJson);
            }
        });

        mBridge.registerHandler(JS_METHOD_GET_USER_INFO, new WebViewJavascriptBridge.WVJBHandler() {
            @Override
            public void handle(String data, final WebViewJavascriptBridge.WVJBResponseCallback jsCallback) {
                Logger.i(TAG, JS_METHOD_GET_USER_INFO + " data:" + data);

                if (!UserProperties.isLogin()) {
                    UIHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            next(UserRapidLoginFragment.class);
                        }
                    });

                    mInGetUserInfo = true;
                    mJsCallback = jsCallback;
                } else {
                    String userJson = UserProperties.toJson();
                    Logger.i(TAG, JS_METHOD_GET_USER_INFO + " userJson:" + userJson);
                    //获取用户信息
                    jsCallback.callback(userJson);
                }

            }
        });

        mBridge.registerHandler(JS_METHOD_GET_PAY_COMPONENTS, new WebViewJavascriptBridge.WVJBHandler() {
            @Override
            public void handle(String data, final WebViewJavascriptBridge.WVJBResponseCallback jsCallback) {
                Logger.i(TAG, JS_METHOD_GET_PAY_COMPONENTS + " data:" + data);
                String orderData = "";
                if (JsonUtils.isJsonObject(data)) {
                    try {
                        JSONObject jsonObject = JsonUtils.getJSONObject(data);
                        if (jsonObject != null) {
                            orderData = jsonObject.optString("id");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                final String payTaskId = orderData;
                if (StringUtils.isEmpty(payTaskId)) {
                    jsCallback.callback("{\"status\": 0, \"info\":\" get order id fail.\"}");
                    return;
                }

                //调用支付接口
                ThirdPortDelivery.pay(getContext(), payTaskId, new SimplePlatformEventListener() {
                    @Override
                    public void onResponsePay(IPlatform platform, boolean success, String message) {
                        super.onResponsePay(platform, success, message);
                        if (success) {
                            jsCallback.callback("{\"status\": 1, \"info\":\"success\"}");
                        } else {
                            jsCallback.callback("{\"status\": 0, \"info\":\"" + message + "\"}");
                        }
                    }
                });
            }
        });

        mBridge.registerHandler(JS_METHOD_GET_LOCATION, new WebViewJavascriptBridge.WVJBHandler() {
            @Override
            public void handle(String data, final WebViewJavascriptBridge.WVJBResponseCallback jsCallback) {
                Logger.i(TAG, JS_METHOD_GET_LOCATION + " data:" + data);
                LocationServer.asInstance().location(new LocationServer.OnLocationResultListener() {
                    @Override
                    public void onLocationResult(boolean success, BDLocation location) {
                        if (success) {
                            jsCallback.callback("{\"status\": 1, \"info\":\"success\", \"result\":{"
                                    + "\"lon\": \"" + LocationServer.asInstance().getLon() + "\","
                                    + "\"lat\": \"" + LocationServer.asInstance().getLat() + "\","
                                    + "\"network\": \"" + NetworkState.getNetworkType() + "\"" +
                                    "}}");
                        } else {
                            jsCallback.callback("{\"status\": 0, \"info\":\"location fail\"}");
                        }
                    }
                });
            }
        });

        mBridge.registerHandler(JS_METHOD_SET_BACK_TYPE, new WebViewJavascriptBridge.WVJBHandler() {
            @Override
            public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback jsCallback) {
                Logger.i(TAG, JS_METHOD_SET_BACK_TYPE + " data:" + data);
                if (!StringUtils.isEmpty(data)) {
                    try {
                        JSONObject jsonObject = JsonUtils.getJSONObject(data);
                        mBackType = StringUtils.parseInt(jsonObject.optString("type"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        mBridge.registerHandler(JS_METHOD_UPDATE_TITLE, new WebViewJavascriptBridge.WVJBHandler() {
            @Override
            public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback jsCallback) {
                Logger.i(TAG, JS_METHOD_UPDATE_TITLE + " data:" + data);
                if (!StringUtils.isEmpty(data)) {
                    try {
                        if (JsonUtils.isJsonObject(data)) {
                            JSONObject jsonObject = JsonUtils.getJSONObject(data);
                            final String title = jsonObject.optString("title");
                            UIHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    TitleBar titleBar = getTitleBar();
                                    if (titleBar != null) {
                                        titleBar.setTitle(title);
                                    }
                                }
                            });

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        mBridge.registerHandler(JS_METHOD_UPDATE_OPTIONS, new WebViewJavascriptBridge.WVJBHandler() {
            @Override
            public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback jsCallback) {
                Logger.i(TAG, JS_METHOD_UPDATE_OPTIONS + " data:" + data);
                if (!StringUtils.isEmpty(data)) {
                    try {
                        mMenuOptions.clear();
                        if (JsonUtils.isJsonArray(data)) {
                            JsonArray array = JsonUtils.getJsonArray(data);
                            Iterator<JsonElement> iterator = array.iterator();
                            while (iterator.hasNext()) {
                                Option option = new Option();
                                JsonElement element = iterator.next();
                                if (element.isJsonObject()) {
                                    JsonObject object = element.getAsJsonObject();
                                    if (object.has("title")) {
                                        option.name = object.get("title").getAsString();
                                    }
                                    if (object.has("linkto")) {
                                        option.key = object.get("linkto").getAsString();
                                    }
                                    if (object.has("icon")) {
                                        option.iconUrl = object.get("icon").getAsString();
                                    }
                                }
                                mMenuOptions.add(option);
                            }

                            if (!mMenuOptions.isEmpty()) {
                                UIHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        updateOptionList();
                                    }
                                });
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        mBridge.registerHandler(JS_METHOD_SHARE_DIALOG, new WebViewJavascriptBridge.WVJBHandler() {
            @Override
            public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback jsCallback) {
                Logger.i(TAG, JS_METHOD_SHARE_DIALOG + " data:" + data);
                mShareType = "";
                if (!StringUtils.isEmpty(data)) {
                    try {
                        JsonObject object = JsonUtils.getJsonObject(data);
                        if (object != null) {
                            if (object.has("type")) {
                                mShareType = object.get("type").getAsString();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                mIsJustShowBtn = false;
                shareDatas.clear();
                mBridge.callHandler(JS_METHOD_GET_SHARE_WEIBO);
                mBridge.callHandler(JS_METHOD_GET_SHARE_WEIXIN);
                mBridge.callHandler(JS_METHOD_GET_SHARE_WEIXIN_FRIEND);

            }
        });

        mBridge.registerHandler(JS_METHOD_SHARE_DO, new WebViewJavascriptBridge.WVJBHandler() {
            @Override
            public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback jsCallback) {
                Logger.i(TAG, JS_METHOD_SHARE_DO + " data:" + data);
                shareData = WebShareData.parse(data);
                shareDatas.add(shareData);

                if (!mIsJustShowBtn && WebShareData.SHARE_TO_WEIXIN_FRIENDS.equals(shareData.getShareTo())) {
                    if ("weixin".equals(mShareType)) {
                        ThirdPortDelivery.shareOnlyWeixin(getContext(), shareDatas);
                    } else {
                        ThirdPortDelivery.share(getContext(), shareDatas);
                    }
                }

            }
        });

        /**
         * 检查版本
         */
        mBridge.registerHandler(JS_METHOD_CHECK_VERSION, new WebViewJavascriptBridge.WVJBHandler() {
            @Override
            public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback jsCallback) {
                Logger.i(TAG, JS_METHOD_CHECK_VERSION + " data:" + data);
                UIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        UpdatePolicy.get().checkUpdateVersion(getContext(), new AppUpdateDialog(getContext()), true);
                    }
                });

            }
        });

        /**
         * 添加显示分享按钮
         */
        mBridge.registerHandler(JS_METHOD_SHOW_SHARE_MENU, new WebViewJavascriptBridge.WVJBHandler() {
            @Override
            public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback jsCallback) {
                Logger.i(TAG, JS_METHOD_SHOW_SHARE_MENU + " data:" + data);
                mShareType = "";
                if (!StringUtils.isEmpty(data)) {
                    try {
                        JsonObject object = JsonUtils.getJsonObject(data);
                        if (object != null) {
                            if (object.has("type")) {
                                mShareType = object.get("type").getAsString();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                mIsJustShowBtn = true;
                shareDatas.clear();
                mBridge.callHandler(JS_METHOD_GET_SHARE_WEIBO);
                mBridge.callHandler(JS_METHOD_GET_SHARE_WEIXIN);
                mBridge.callHandler(JS_METHOD_GET_SHARE_WEIXIN_FRIEND);


                UIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mShareMenu.setVisibility(View.VISIBLE);
                    }
                });
            }
        });

    }

    /**
     * 更新选项列表
     */
    protected void updateOptionList() {
        if (mOptionWindow != null) {
            mOptionWindow.dismiss();
        }
        mOptionWindow = new PopListWindow(getContext());
        mOptionAdapter = new OptionAdapter(getContext(), mMenuOptions);

        List<String> optionNames = new ArrayList<String>();
        for (Option option : mMenuOptions) {
            optionNames.add(option.name);
        }
        mOptionWindow.setContentBackgroundColor(getResources().getColor(R.color.uikit_transparent));
        mOptionWindow.setWindowBackground(getResources().getDrawable(R.drawable.app_bg_option_more));
        mOptionWindow.setListAdapter(mOptionAdapter, optionNames, true);
        mOptionWindow.setItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String itemName = mOptionWindow.getItemName(position);
                for (Option option : mMenuOptions) {
                    if (itemName.equals(option.name)) {
                        String key = option.key;
                        clickOption(key);
                        break;
                    }
                }
            }
        });
        mOptionWindow.showAtViewSide(mOptionMenu, -mOptionMenu.getMeasuredWidth(), ScreenUtils.dip2px(getContext(), 0));
    }

    /**
     * 点击选项
     *
     * @param key
     */
    private void clickOption(String key) {
        if (!StringUtils.isEmpty(key)) {
            mBridge.callHandler(JS_METHOD_CLICK_OPTION, "{'key':" + key + "}");
        }
        mOptionWindow.dismiss();
    }

    @Override
    public boolean back(Bundle data) {
        Logger.d(TAG, "back type : " + mBackType);
        switch (mBackType) {
            //返回页面app的上级
            case BACK_TYPE_APP: {
                return super.finishFragment();
            }
            //返回webview的上级
            case BACK_TYPE_WEBVIEW: {
                if (backward()) {
                    return true;
                }
                break;
            }
            //返回webview的H5内部上级
            case BACK_TYPE_WEBVIEW_INTERNAL: {
                mBridge.callHandler("wkBackHandler");
                return true;
            }

            default:
                if (backward()) {
                    return true;
                }
                break;
        }
        return super.finishFragment();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && back()) {
            return true;
        }
        return super.onKeyDown(keyCode, event);

    }

    private static class Option {
        String name;
        String key;
        int icon;
        String iconUrl;
    }

    private static class OptionAdapter extends UIAdapter<Option> {

        public OptionAdapter(Context context, List<Option> data) {
            super(context, R.layout.listview_item_option, data);
        }

        @Override
        public void updateView(int position, int viewType, Option data, Bundle extra) {
            super.updateView(position, viewType, data, extra);

            ImageView iconView = (ImageView) findViewById(R.id.app_option_icon);
            TextView nameView = (TextView) findViewById(R.id.app_option_name);

            if (data.icon != 0) {
                iconView.setVisibility(View.VISIBLE);
                iconView.setImageDrawable(getContext().getResources().getDrawable(data.icon));
            }

            if (!StringUtils.isEmpty(data.iconUrl)) {
                iconView.setVisibility(View.VISIBLE);
                ImageFetcher.asInstance().loadSimple(data.iconUrl, iconView);
            }


            nameView.setText(data.name);
        }
    }

    public static class WebShareData implements Serializable {

        public static final String SHARE_TO_WEIXIN = "wxfriends";
        public static final String SHARE_TO_WEIXIN_FRIENDS = "wxtimeline";
        public static final String SHARE_TO_WEIBO = "weibo";

        @SerializedName("img_url")
        private String image;

        @SerializedName("link")
        private String redirectUrl;

        @SerializedName("title")
        private String title;

        @SerializedName("desc")
        private String desc;

        @SerializedName("shareto")
        private String shareTo;

        public String getImage() {
            return image;
        }

        public String getRedirectUrl() {
            return redirectUrl;
        }

        public String getTitle() {
            return title;
        }

        public String getDesc() {
            return desc;
        }

        public String getShareTo() {
            return shareTo;
        }

        public static WebShareData parse(String json) {
            return JsonUtils.getModel(json, WebShareData.class);
        }
    }
}
