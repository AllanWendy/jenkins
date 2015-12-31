package com.wecook.common.modules.thirdport;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.utils.Utility;
import com.wecook.common.R;

/**
 * 微博授权对话框
 *
 * @author zhoulu
 * @date 13-12-26
 */
public class SinaAuthDialog extends Dialog {

    static FrameLayout.LayoutParams FILL = new FrameLayout.LayoutParams(-1, -1);
    private String url;
    private RelativeLayout webViewContainer;
    private WebView mWebView;
    private RelativeLayout mContent;
    private WeiboAuthListener mListener;

    public SinaAuthDialog(Context context, String url, WeiboAuthListener mListener) {
        super(context, R.style.Theme_Dialog_Transparent);
        this.url = url;
        this.mListener = mListener;
    }

    public static void clearCookies(Context context) {
        CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.removeSessionCookie();
        cookieManager.removeAllCookie();
        CookieSyncManager.getInstance().sync();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(1);
        getWindow().setFeatureDrawableAlpha(0, 0);
        this.mContent = new RelativeLayout(getContext());
        setUpWebView();

        addContentView(this.mContent, new ViewGroup.LayoutParams(-1, -1));
    }

    private void handleRedirectUrl(WebView view, String url) {
        if (url == null) {
            return;
        }

        Bundle values = Utility.parseUrl(url);

        if (values != null) {
            String error = values.getString("error");
            String error_code = values.getString("error_code");

            if ((error == null) && (error_code == null))
                this.mListener.onComplete(values);
            else if ("access_denied".equals(error)) {
                this.mListener.onCancel();
            } else {
                this.mListener.onWeiboException(null);
            }
        }
    }

    private void onBack() {
        try {
            if (this.mWebView != null) {
                this.mWebView.stopLoading();
                this.mWebView.destroy();
            }
        } catch (Exception localException) {
        }
        dismiss();
    }

    private void setUpWebView() {
        this.webViewContainer = new RelativeLayout(getContext());
        this.mWebView = new WebView(getContext());
        this.mWebView.setVerticalScrollBarEnabled(false);
        this.mWebView.setHorizontalScrollBarEnabled(false);
        this.mWebView.getSettings().setJavaScriptEnabled(true);
        this.mWebView.getSettings().setSavePassword(false);
        this.mWebView.setWebViewClient(new WeiboWebViewClient());
        this.mWebView.requestFocus();
        clearCookies(getContext());

        this.mWebView.loadUrl(url);
        this.mWebView.setLayoutParams(FILL);
        this.mWebView.setVisibility(View.INVISIBLE);

        this.mContent.setBackgroundColor(getContext().getResources().getColor(android.R.color.transparent));

        webViewContainer.setBackgroundColor(getContext().getResources().getColor(android.R.color.transparent));

        RelativeLayout.LayoutParams lp0 = new RelativeLayout.LayoutParams(-1, -1);
        this.webViewContainer.addView(this.mWebView, lp0);
        this.webViewContainer.setGravity(17);

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(-1, -1);
        this.mContent.addView(this.webViewContainer, lp);

    }

    private class WeiboWebViewClient extends WebViewClient {
        private WeiboWebViewClient() {
        }

        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d("Weibo-WebView", "Redirect URL: " + url);
            if (url.startsWith("sms:")) {
                Intent sendIntent = new Intent("android.intent.action.VIEW");
                sendIntent.putExtra("address", url.replace("sms:", ""));
                sendIntent.setType("vnd.android-dir/mms-sms");
                SinaAuthDialog.this.getContext().startActivity(sendIntent);
                return true;
            }
            return super.shouldOverrideUrlLoading(view, url);
        }

        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            SinaAuthDialog.this.dismiss();
        }

        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.d("Weibo-WebView", "onPageStarted URL: " + url);
            if (url.startsWith("http://wecook.cn")) {
                SinaAuthDialog.this.handleRedirectUrl(view, url);
                view.stopLoading();
                SinaAuthDialog.this.dismiss();
                return;
            }
            super.onPageStarted(view, url, favicon);
        }

        public void onPageFinished(WebView view, String url) {
            Log.d("Weibo-WebView", "onPageFinished URL: " + url);
            super.onPageFinished(view, url);
            mWebView.setVisibility(View.VISIBLE);
        }

        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
        }
    }

}
