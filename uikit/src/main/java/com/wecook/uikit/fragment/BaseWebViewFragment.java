package com.wecook.uikit.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.GeolocationPermissions;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.wecook.common.app.AppLink;
import com.wecook.common.core.debug.Logger;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.jsbridge.WebViewJavascriptBridge;
import com.wecook.common.modules.network.NetworkState;
import com.wecook.common.modules.property.PhoneProperties;
import com.wecook.common.utils.StringUtils;
import com.wecook.common.utils.WebViewUtil;
import com.wecook.uikit.R;
import com.wecook.uikit.widget.EmptyView;
import com.wecook.uikit.widget.hybrid.HybridWebView;

import org.apache.http.HttpStatus;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLDecoder;

/**
 * @author kevin
 */
@SuppressLint("HandlerLeak")
public abstract class BaseWebViewFragment extends BaseTitleFragment implements OnClickListener {
    public static final String ACTION_INTENT_WEB_PAGE_LOADED = "intent_web_page_loaded";
    public static final int REQUEST_CODE_FILE_CHOOSE = 100;
    private final String TAG = "jsbridge";

    public static final String EXTRA_URL = "extra_data_url";
    private static final String APP_CALL = "wecook://";

    private static final int STATE_NONE = -1;
    private static final int STATE_LOADING = 0;
    private static final int STATE_LOAD_SUCCESS = 1;
    private static final int STATE_EMPTY = 2;
    private static final int STATE_LOAD_FAIL = 3;

    private static final int ERROR_CODE_SUCCESS = HttpStatus.SC_OK;

    private HybridWebView mWebView;
    private ViewGroup mOperatorView;
    private WebViewJavascriptBridge mJsbridge;

    private int mState = STATE_NONE;
    private int mErrorCode = ERROR_CODE_SUCCESS;
    private String mCurrentUrl;
    private WeakReference<Activity> mActivityRef;

    private EmptyView mEmptyView;

    private ValueCallback mFileChooser;
    private boolean isMultFileChooser;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mCurrentUrl = bundle.getString(EXTRA_URL);
        }
        mActivityRef = new WeakReference<>(activity);
        setFixed(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_FILE_CHOOSE) {

            if (mFileChooser != null) {
                if (data != null) {
                    Uri uri = data.getData();
                    Logger.d(TAG, "REQUEST_CODE_FILE_CHOOSE:[uri]" + uri);
                    if (uri != null) {
                        Cursor cursor = getContext().getContentResolver()
                                .query(uri, new String[]{MediaStore.Images.Media.DATA}, null, null, null);
                        if (cursor != null) {
                            int pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                            cursor.moveToFirst();
                            if (pathIndex >= 0) {
                                String imagePath = cursor.getString(pathIndex);
                                File file = new File(imagePath);
                                Uri fileUri = Uri.fromFile(file);
                                if (isMultFileChooser) {
                                    mFileChooser.onReceiveValue(new Uri[]{fileUri});
                                } else {
                                    mFileChooser.onReceiveValue(fileUri);
                                }
                            }
                        }
                    }
                } else {
                    mFileChooser.onReceiveValue(null);
                }
            }
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mEmptyView = (EmptyView) view.findViewById(R.id.uikit_empty);
        if (StringUtils.isEmpty(mCurrentUrl)) {
            mEmptyView.setCanRefresh(false);
        }
        mEmptyView.setRefreshListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });
        mOperatorView = (ViewGroup) view.findViewById(R.id.uikit_webview_operator);
        View operatorView = onCreateOperatorView(mOperatorView);
        if (operatorView != null && operatorView.getParent() == null) {
            mOperatorView.addView(operatorView);
        }

        mWebView = (HybridWebView) view.findViewById(R.id.uikit_webview);
        mWebView.setWebViewClient(new InnerWebViewClient());
        mWebView.setWebChromeClient(new InnerWebChromeClient());
        mWebView.requestFocus();
        mWebView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });

        mJsbridge = new WebViewJavascriptBridge(getActivity(), mWebView);
        checkUserAgent(getWebView());
        loadUrl(mCurrentUrl);
    }

    public abstract View onCreateOperatorView(ViewGroup parent);

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return LayoutInflater.from(getContext()).inflate(R.layout.uikit_fragment_webview, null);
    }

    @Override
    public void onClick(View v) {
        if (mWebView == null) {
            getActivity().onBackPressed();
        }
    }

    public WebViewJavascriptBridge getJsbridge() {
        return mJsbridge;
    }

    public HybridWebView getWebView() {
        return mWebView;
    }

    /**
     * 后退
     */
    public boolean backward() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return false;
    }

    /**
     * 前进
     */
    public boolean forward() {
        if (mWebView.canGoForward()) {
            mWebView.goForward();
            return true;
        }
        return false;
    }

    /**
     * 刷新
     */
    public void refresh() {
        loadUrl(mCurrentUrl);
    }

    public void loadAssetHtml(String name) {
        if (!StringUtils.isEmpty(name)) {
            String assetUrl = "file:///android_asset/" + name;
            loadUrl(assetUrl);
        }
    }

    public EmptyView getEmptyView() {
        return mEmptyView;
    }

    /**
     * 跳转
     *
     * @param url
     */
    public void loadUrl(String url) {
        if (StringUtils.isEmpty(url)) {
            mEmptyView.setVisibility(View.VISIBLE);
            return;
        } else {
            mEmptyView.setVisibility(View.GONE);
        }

        mState = STATE_LOADING;
        mErrorCode = ERROR_CODE_SUCCESS;
        mCurrentUrl = url;
        if (!NetworkState.available()) {
            mState = STATE_LOAD_FAIL;
        } else {
            CookieSyncManager.createInstance(getActivity());
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            CookieSyncManager.getInstance().sync();

            try {
                if (mWebView != null) {
                    mWebView.loadUrl(url);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroyView() {
        if (mWebView != null) {
            try {
                mWebView.clearView();
                mWebView.freeMemory();
                mWebView.destroy();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        super.onDestroyView();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()) {
            backward();
            return true;
        }
        return super.onKeyDown(keyCode, event);

    }

    public boolean isWebLoadSuccess() {
        return mState == STATE_LOAD_SUCCESS;
    }

    /**
     * Web与应用间调起处理
     *
     * @param url
     */
    private void dealWithCallApp(String url) {
        AppLink.sendLink(url);
    }

    public class InnerWebChromeClient extends WebChromeClient {

        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            mFileChooser = filePathCallback;
            isMultFileChooser = true;
            chooseImage();
            return true;
        }

        public void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType, String capture) {
            mFileChooser = uploadFile;
            chooseImage();
        }

        public void onSelectionStart(WebView view) {

        }

        /**
         * 选择图片
         */
        private void chooseImage() {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            getActivity().startActivityForResult(Intent.createChooser(intent, ""), REQUEST_CODE_FILE_CHOOSE);
        }

        /**
         * 开启Html5地理位置定位
         */
        @Override
        public void onGeolocationPermissionsShowPrompt(String origin,
                                                       GeolocationPermissions.Callback callback) {
            callback.invoke(origin, true, false);
            super.onGeolocationPermissionsShowPrompt(origin, callback);
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            if (getTitleBar() != null) {
                getTitleBar().setProgress(newProgress);
            }
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message,
                                 JsResult result) {
            Logger.i(TAG, "[onJsAlert] : " + message);
            if (result == null) {
                return true;
            }
            result.confirm();
            Toast.makeText(view.getContext(), message, Toast.LENGTH_LONG).show();
            return true;
        }

        /**
         * 拦截js 的 prompt方法
         */
        @Override
        public boolean onJsPrompt(WebView view, String url, String message,
                                  String value, JsPromptResult result) {
            Logger.i(TAG, "[onJsPrompt] : " + message);

            try {
                message = URLDecoder.decode(message, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            result.cancel();
            WebViewUtil.checkExtendRedirect(mActivityRef, view, message);
            Logger.i(TAG, "message : " + message);
            Logger.i(TAG, "value : " + value);
            return true;
        }

        @Override
        public boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result) {
            Logger.i(TAG, "[onJsBeforeUnload] url:" + url + " message:" + message);
            return super.onJsBeforeUnload(view, url, message, result);
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            Logger.i(TAG, "[onReceivedTitle] title:" + title);
            setTitle(title);
        }

        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            Logger.i(TAG, "[onConsoleMessage] console:" + consoleMessage.message());
            return super.onConsoleMessage(consoleMessage);
        }
    }

    private class InnerWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
            Logger.i(TAG, "shouldOverrideKeyEvent ");
            return super.shouldOverrideKeyEvent(view, event);
        }

        @Override
        public void onUnhandledInputEvent(WebView view, InputEvent event) {
            super.onUnhandledInputEvent(view, event);
            Logger.i(TAG, "onUnhandledInputEvent ");
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Logger.i(TAG, "onPageStarted ");
            showLoading();
            checkUserAgent(view);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView webview, String url) {
            Logger.i(TAG, "shouldOverrideUrlLoading url : " + url);
            if (url.equals(mCurrentUrl)) {
                refresh();
                return true;
            }
            if (!WebViewUtil.checkExtendRedirect(mActivityRef, webview, url)) {
                if (url.startsWith(APP_CALL)) {
                    dealWithCallApp(url);
                } else {
                    loadUrl(url);
                }
            }
            return true;
        }

        @Override
        public void onPageFinished(final WebView view, String url) {
            super.onPageFinished(view, url);
            if (mErrorCode != ERROR_CODE_SUCCESS)
                return;
            Logger.i(TAG, "onPageFinished ");
            UIHandler.postOnce(new Runnable() {
                @Override
                public void run() {
                    mJsbridge.loadWebViewJavascriptBridgeJs(view);
                    mState = STATE_LOAD_SUCCESS;
                    mCurrentUrl = mWebView.getUrl();
                    Intent intent = new Intent(ACTION_INTENT_WEB_PAGE_LOADED);
                    intent.putExtra(EXTRA_URL, mCurrentUrl);
                    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
                    hideLoading();
                }
            });

        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            Logger.w(TAG, "onReceivedError errorCode : " + errorCode);
            mErrorCode = errorCode;
            mState = STATE_LOAD_FAIL;
            hideLoading();
        }
    }

    /**
     * 检查用户代理，如果有变化，会导致reload当前页面
     *
     * @param view
     */
    private void checkUserAgent(WebView view) {
        WebSettings settings = view.getSettings();
        if (settings != null) {
            String userAgent = settings.getUserAgentString();
            if (!StringUtils.isEmpty(userAgent) && !StringUtils.containWith(userAgent, "Wecook")) {
                userAgent += " Wecook/" + PhoneProperties.getVersionName();
            }
            if (StringUtils.containWith(userAgent, "NetType")) {
                userAgent.replaceAll(" NetType/* ", " NetType/" + NetworkState.getNetworkType() + " ");
            } else {
                userAgent += " NetType/" + NetworkState.getNetworkType() + " ";
            }

            Logger.d(TAG, "user agent : " + userAgent);
            settings.setUserAgentString(userAgent);
        }
    }
}
