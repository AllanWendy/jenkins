package com.wecook.common.modules.thirdport.platform;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.MusicObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.VideoObject;
import com.sina.weibo.sdk.api.WebpageObject;
import com.sina.weibo.sdk.api.WeiboMessage;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboDownloadListener;
import com.sina.weibo.sdk.api.share.IWeiboHandler;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.ProvideMultiMessageForWeiboResponse;
import com.sina.weibo.sdk.api.share.SendMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuth;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.constant.WBConstants;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.AsyncWeiboRunner;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.net.WeiboParameters;
import com.sina.weibo.sdk.utils.Utility;
import com.wecook.common.R;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.property.SharePreferenceProperties;
import com.wecook.common.modules.thirdport.PlatformManager;
import com.wecook.common.modules.thirdport.SinaAuthDialog;
import com.wecook.common.modules.thirdport.object.IShareObject;
import com.wecook.common.modules.thirdport.platform.base.BasePlatform;
import com.wecook.common.utils.HttpUtils;
import com.wecook.common.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * 新浪微博
 */
public class WeiBlog extends BasePlatform implements IWeiboHandler.Response {

    private static final String REQ_ACTION = "com.sina.weibo.sdk.action.ACTION_SDK_REQ_ACTIVITY";

    public static final int MAX_LENGTH_OF_CONTENT = 140;//最大140个文字，280个字母数字
    private static final String TAG = WeiBlog.class.getSimpleName();
    public static final String API_SEND_MSG_WITH_PIC = "https://upload.api.weibo.com/2/statuses/upload.json";
    public static final String API_GET_USER_INFO = "https://api.weibo.com/2/users/show.json";
    public static final String API_SHORT_URL = "https://api.weibo.com/2/short_url/shorten.json";
    public static final String API_ACCESS_TOKEN_URL = "https://api.weibo.com/oauth2/access_token";

    private IWeiboShareAPI mWeiboShareAPI;
    /**
     * 微博 Web 授权类，提供登陆等功能
     */
    private WeiboAuth mWeiboAuth;

    private SsoHandler mSsoHandler;
    private UserInfo mUserInfo;

    /**
     * 封装了 "access_token"，"expires_in"，"refresh_token"，并提供了他们的管理功能
     */
    private static Oauth2AccessToken mAccessToken;
    private boolean mIsSharing;
    private boolean mIsSsoHanding;

    private boolean mIsInShareAction;
    public WeiBlog(Context context) {
        super(context);
    }

    @Override
    public int getShareMaxLength(IShareObject shareObject) {
        int urllength = (int) StringUtils.chineseLength(shareObject.getRedirectUrl());
        if (urllength < 20) {
            return MAX_LENGTH_OF_CONTENT - 1 - urllength;
        }
        return MAX_LENGTH_OF_CONTENT - 20;//短链接
    }

    @Override
    public boolean isValidSession() {
        mAccessToken = AccessTokenKeeper.readAccessToken(getContext());
        if (mAccessToken != null) {
            return mAccessToken.isSessionValid();
        }
        return super.isValidSession();
    }

    /**
     * 是否支持以SSO进行分享
     *
     * @return
     */
    @Override
    public boolean isSupportSSOShare() {
        if (!getConfig().enablePlatformSSO(PlatformManager.PLATFORM_WEBLOG)) {
            return false;
        }
        int supportApiLevel = mWeiboShareAPI.getWeiboAppSupportAPI();
        return isInstalledApp() && supportApiLevel >= 0;
    }

    /**
     * 是否支持以Web形式进行分享
     *
     * @return
     */
    @Override
    public boolean isSupportWebShare() {
        return true;
    }

    /**
     * 是否安装软件
     *
     * @return
     */
    @Override
    public boolean isInstalledApp() {
        PackageManager pm = getContext().getPackageManager();
        boolean isInstalled = mWeiboShareAPI != null && mWeiboShareAPI.isWeiboAppInstalled();
        try {
            String qqPackageName = "com.sina.weibo";
            PackageInfo pi = pm.getPackageInfo(qqPackageName, PackageManager.GET_ACTIVITIES);
            int enableFlag = pm.getApplicationEnabledSetting(qqPackageName);
            if (pi != null && enableFlag == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                    || enableFlag == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT) {
                isInstalled = true;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            isInstalled = false;
        }
        return isInstalled;
    }

    /**
     * 检查API-SDK是否有效
     *
     * @return
     */
    @Override
    public boolean onCreate() {
        // 创建微博 SDK 接口实例
        mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(getContext(), getShareAppId(), false);
        mWeiboShareAPI.registerApp();

        mUserInfo = UserInfo.load();
        mAccessToken = AccessTokenKeeper.readAccessToken(getContext());

        mWeiboAuth = new WeiboAuth(getContext(), getShareAppId(), getShareRedirectUrl(), getShareScope());

        mSsoHandler = new SsoHandler((android.app.Activity) getContext(), mWeiboAuth);
        // 如果未安装微博客户端，设置下载微博对应的回调
        if (!mWeiboShareAPI.isWeiboAppInstalled()) {
            mWeiboShareAPI.registerWeiboDownloadListener(new IWeiboDownloadListener() {
                @Override
                public void onCancel() {
                    Toast.makeText(getContext(),
                            R.string.share_status_cancel_download_weibo,
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
        return true;
    }

    @Override
    public void onLogin() {
        final WeiboAuthListener authListener = new WeiboAuthListener() {
            @Override
            public void onComplete(Bundle bundle) {

                if (mIsSsoHanding) {
                    mAccessToken = Oauth2AccessToken.parseAccessToken(bundle);
                    if (mAccessToken.isSessionValid()) {
                        AccessTokenKeeper.writeAccessToken(getContext(), mAccessToken);
                        requestUserInfo();
                    } else {
                        performLogin(false, getString(R.string.share_status_auth_deny));
                    }

                } else {
                    String code = bundle.getString("code");
                    if (!StringUtils.isEmpty(code)) {
                        requestAccessToken(code, new RequestListener() {
                            @Override
                            public void onComplete(String s) {
                                mAccessToken = Oauth2AccessToken.parseAccessToken(s);
                                if (mAccessToken.isSessionValid()) {
                                    AccessTokenKeeper.writeAccessToken(getContext(), mAccessToken);
                                    requestUserInfo();
                                } else {
                                    performLogin(false, getString(R.string.share_status_auth_deny));
                                }
                            }

                            @Override
                            public void onWeiboException(WeiboException e) {
                                performLogin(false, getString(R.string.share_status_auth_deny));
                            }
                        });
                    } else {
                        performLogin(false, getString(R.string.share_status_auth_deny));
                    }
                }

            }

            @Override
            public void onWeiboException(WeiboException e) {
                performLogin(false, getString(R.string.share_status_auth_deny));
            }

            @Override
            public void onCancel() {
                performLogin(false, getString(R.string.share_status_cancel_auth));
            }
        };

        if (isInstalledApp()) {
            mIsSsoHanding = true;
            mSsoHandler.authorize(authListener);
        } else {
            WeiboParameters requestParams = new WeiboParameters();
            requestParams.put(WBConstants.AUTH_PARAMS_CLIENT_ID, getShareAppId());
            requestParams.put(WBConstants.AUTH_PARAMS_RESPONSE_TYPE, "code");
            requestParams.put(WBConstants.AUTH_PARAMS_SCOPE, getShareScope());
            requestParams.put(WBConstants.AUTH_PARAMS_DISPLAY, "mobile");
            requestParams.put(WBConstants.AUTH_PARAMS_REDIRECT_URL, getShareRedirectUrl());
            final String url = getShareOAuthUrl() + "?" + requestParams.encodeUrl();

            UIHandler.post(new Runnable() {
                @Override
                public void run() {
                    final SinaAuthDialog dialog = new SinaAuthDialog(getContext(), url, authListener);
                    dialog.setOwnerActivity((android.app.Activity) getContext());
                    dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            performLogin(false, "");
                        }
                    });
                    dialog.show();
                }
            });
        }

    }

    private void requestAccessToken(String code, RequestListener listener) {
        WeiboParameters requestParams = new WeiboParameters();
        requestParams.put(WBConstants.AUTH_PARAMS_CLIENT_ID, getShareAppId());
        requestParams.put(WBConstants.AUTH_PARAMS_CLIENT_SECRET, getShareAppSecret());
        requestParams.put(WBConstants.AUTH_PARAMS_CODE, code);
        requestParams.put(WBConstants.AUTH_PARAMS_REDIRECT_URL, getShareRedirectUrl());
        requestParams.put(WBConstants.AUTH_PARAMS_GRANT_TYPE, "authorization_code");

        AsyncWeiboRunner.requestAsync(API_ACCESS_TOKEN_URL, requestParams, "POST", listener);
    }

    /**
     * 请求用户信息
     */
    private void requestUserInfo() {
        WeiboParameters params = new WeiboParameters();
        params.put("uid", mAccessToken.getUid());
        params.put("access_token", mAccessToken.getToken());
        AsyncWeiboRunner.requestAsync(API_GET_USER_INFO,
                params, "GET", new RequestListener() {
                    @Override
                    public void onComplete(String response) {
                        mUserInfo = new UserInfo();
                        mUserInfo.save(response);
                        performLogin(true, "");
                    }

                    @Override
                    public void onWeiboException(WeiboException e) {
                        performLogin(false, getString(R.string.share_status_auth_deny));
                    }
                }
        );
    }

    @Override
    public void onLogout() {
        AccessTokenKeeper.clear(getContext());
        mAccessToken = AccessTokenKeeper.readAccessToken(getContext());
        if (mUserInfo != null) {
            mUserInfo.clear();
        }
        performLogout(true, "");
    }

    /**
     * 消息响应监听
     *
     * @param context
     * @param intent
     * @return
     */
    @Override
    public boolean onEvent(Context context, Intent intent) {
        String action = intent.getAction();
        log("onEvent action = " + action);
        boolean success = intent.getBooleanExtra(EXTRA_RESULT, false);
        String type = intent.getStringExtra(EXTRA_TYPE);
        int requestCode = intent.getIntExtra(EXTRA_REQUEST_CODE, 0);
        int resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0);
        if (REQ_ACTION.equals(action)
                && (mWeiboShareAPI.handleWeiboResponse(intent, this) || handleResponse(intent))) {
            return true;
        } else if (ACTION_SHARE.equals(action) && !success) {
            intent.putExtra(EXTRA_RESULT, false);
            intent.putExtra(EXTRA_MESSAGE, "");
            dispatchShareListener(intent);
            return true;

        } else if ((ACTION_SHARE.equals(action) || mIsSsoHanding) && TYPE_ACTIVITY_RESULT.equals(type)) {
            mSsoHandler.authorizeCallBack(requestCode, resultCode, intent);
            mIsSsoHanding = false;
        } else if (ACTION_LOGIN.equals(action) && success && mIsInShareAction) {
            doShare();
            mIsInShareAction = false;
        }
        return false;
    }

    private boolean handleResponse(Intent intent) {
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                int error_code = extras.getInt("_weibo_resp_errcode");
                BaseResponse response = new ProvideMultiMessageForWeiboResponse();
                response.fromBundle(extras);
                response.errCode = error_code;
                onResponse(response);
                return true;
            }
        }
        return false;
    }

    @Override
    public void onResponse(BaseResponse baseResponse) {
        switch (baseResponse.errCode) {
            case WBConstants.ErrorCode.ERR_OK:
                performShare(true, getString(R.string.share_status_success));
                break;
            case WBConstants.ErrorCode.ERR_CANCEL:
                performShare(false, getString(R.string.share_status_cancel));
                break;
            case WBConstants.ErrorCode.ERR_FAIL:
                performShare(false, baseResponse.errMsg);
                break;
        }
    }

    /**
     * 开始执行分享过程
     *
     * @param shareObjects
     */
    @Override
    public void onShare(IShareObject... shareObjects) {

        if (isSupportSSOShare()) {
            if (mWeiboShareAPI.checkEnvironment(true)) {
                mWeiboShareAPI.registerApp();
                int supportApi = mWeiboShareAPI.getWeiboAppSupportAPI();
                if (supportApi >= 10351 /*ApiUtils.BUILD_INT_VER_2_2*/) {
                    sendMultiMessage(shareObjects);
                } else {
                    if (shareObjects.length != 0) {
                        sendSingleMessage(shareObjects[0]);
                    }
                }
            }
        } else if (isSupportWebShare()) {
            if (mAccessToken.isSessionValid()) {
                if (shareObjects.length != 0) {
                    sendSingleMessageByOpenApi(shareObjects[0]);
                }
            } else {
                mIsInShareAction = true;
                doLogin();
            }
        }

    }

    private void sendSingleMessageByOpenApi(final IShareObject shareObject) {

        if (mIsSharing) {
            return;
        }

        mIsSharing = true;

        if (StringUtils.isEmpty(shareObject.getRedirectUrl())) {
            shareShoreUrl(shareObject, "");
            return;
        }

        final WeiboParameters params = new WeiboParameters();
        params.put("access_token", mAccessToken.getToken());
        params.put("url_long", shareObject.getRedirectUrl());

        final RequestListener shortListener = new RequestListener() {
            @Override
            public void onComplete(String response) {
                String shortUrl = shareObject.getRedirectUrl();
                try {
                    JSONObject object = new JSONObject(response);
                    JSONArray array = object.getJSONArray("urls");
                    int count = array.length();
                    for (int i = 0; i < count; i++) {
                        JSONObject obj = array.getJSONObject(i);
                        boolean ret = obj.getBoolean("result");
                        if (!ret) {
                            continue;
                        } else {
                            shortUrl = obj.getString("url_short");
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                shareShoreUrl(shareObject, shortUrl);
            }

            @Override
            public void onWeiboException(WeiboException e) {
                mIsSharing = false;
            }

        };
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            AsyncWeiboRunner.requestAsync(API_SHORT_URL,
                    params, "GET", shortListener);
        } else {
            AsyncWeiboRunner.requestByThread(API_SHORT_URL,
                    params, "GET", shortListener);
        }
    }

    private void shareShoreUrl(IShareObject shareObject, String shortUrl) {
        final WeiboParameters params = new WeiboParameters();
        params.put("access_token", mAccessToken.getToken());
        params.put("visible", 0);
        String cutString = shareObject.getMessage();
        cutString += shortUrl;
        try {
            params.put("status", URLEncoder.encode(cutString, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Bitmap source = shareObject.getBitmap();
        params.put("pic", source);

        AsyncWeiboRunner.requestAsync(API_SEND_MSG_WITH_PIC,
                params, "POST", new RequestListener() {
                    @Override
                    public void onComplete(String response) {
                        log("onComplete = " + response);
                        performShare(true, getString(R.string.share_status_success));
                        mIsSharing = false;
                    }

                    @Override
                    public void onWeiboException(WeiboException e) {
                        log("onWeiboException = " + e.getMessage());
                        performShare(false, getString(R.string.share_status_fail));
                        mIsSharing = false;
                    }

                }
        );

    }


    /**
     * 可发送多个内容
     *
     * @param shareObjects
     */
    private void sendMultiMessage(IShareObject[] shareObjects) {
        if (shareObjects == null) {
            return;
        }

        WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
        for (IShareObject shareObject : shareObjects) {

            if (shareObject == null) {
                continue;
            }

            IShareObject.TYPE type = shareObject.getType();

            Bitmap bitmap = shareObject.getBitmap();
            if (bitmap == null) {
                try {
                    byte[] bytes = shareObject.getThumbnailBytes();
                    if (bytes != null && bytes.length != 0) {
                        bitmap = BitmapFactory.decodeByteArray(shareObject.getThumbnailBytes(),
                                0, bytes.length);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }

            // 1. 初始化微博的分享消息
            switch (type) {
                case TYPE_TEXT:
                    TextObject mediaObject = new TextObject();
                    mediaObject.text = shareObject.getMessage();
                    weiboMessage.textObject = mediaObject;
                    break;
                case TYPE_WEBURL: {
                    WebpageObject webpageObject = new WebpageObject();
                    webpageObject.actionUrl = shareObject.getRedirectUrl();
                    webpageObject.identify = Utility.generateGUID();
                    webpageObject.defaultText = shareObject.getMessage();
                    if (bitmap != null) {
                        webpageObject.setThumbImage(bitmap);
                    }
                    webpageObject.title = shareObject.getTitle();
                    webpageObject.description = shareObject.getSecondTitle();
                    weiboMessage.mediaObject = webpageObject;

                    TextObject messageForUrl = new TextObject();
                    messageForUrl.text = shareObject.getMessage();
                    weiboMessage.textObject = messageForUrl;
                    break;
                }
                case TYPE_IMAGE: {
                    TextObject messageForImage = new TextObject();
                    messageForImage.text = shareObject.getMessage();
                    weiboMessage.textObject = messageForImage;

                    ImageObject imageObject = new ImageObject();
                    if (bitmap != null) {
                        imageObject.setThumbImage(bitmap);
                    }
                    weiboMessage.imageObject = imageObject;
                    break;
                }
                case TYPE_MUSIC:
                    TextObject messageForAudio = new TextObject();
                    messageForAudio.text = shareObject.getMessage();
                    weiboMessage.textObject = messageForAudio;

                    ImageObject imageObjectForAudio = new ImageObject();
                    if (bitmap != null) {
                        imageObjectForAudio.setImageObject(bitmap);
                    }

                    weiboMessage.imageObject = imageObjectForAudio;

                    MusicObject musicObject = new MusicObject();
                    musicObject.actionUrl = shareObject.getRedirectUrl();
                    musicObject.identify = Utility.generateGUID();
                    musicObject.title = shareObject.getTitle();
                    musicObject.description = shareObject.getSecondTitle();

                    musicObject.duration = shareObject.getContentSize();
                    if (bitmap != null) {
                        musicObject.setThumbImage(bitmap);
                    }
                    musicObject.dataHdUrl = shareObject.getMediaUrl();
                    musicObject.dataUrl = shareObject.getLowBandMediaUrl();
                    weiboMessage.mediaObject = musicObject;
                    break;
                case TYPE_VIDEO:
                    TextObject messageForVideo = new TextObject();
                    messageForVideo.text = shareObject.getMessage();
                    weiboMessage.textObject = messageForVideo;

                    VideoObject videoObject = new VideoObject();
                    videoObject.actionUrl = shareObject.getMessage();
                    videoObject.identify = Utility.generateGUID();
                    videoObject.title = shareObject.getTitle();
                    videoObject.description = shareObject.getSecondTitle();

                    videoObject.duration = shareObject.getContentSize();
                    if (bitmap != null) {
                        videoObject.setThumbImage(bitmap);
                    }
                    videoObject.dataHdUrl = shareObject.getMediaUrl();
                    videoObject.dataUrl = shareObject.getLowBandMediaUrl();
                    weiboMessage.mediaObject = videoObject;
                    break;
            }

        }

        // 2. 初始化从第三方到微博的消息请求
        SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
        // 用transaction唯一标识一个请求
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.multiMessage = weiboMessage;
        try {
            if (weiboMessage.checkArgs()) {
                // 3. 发送请求消息到微博，唤起微博分享界面
                mWeiboShareAPI.sendRequest(request);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendSingleMessage(IShareObject shareObject) {
        WeiboMessage weiboMessage = new WeiboMessage();

        IShareObject.TYPE type = shareObject.getType();

        Bitmap bitmap = null;
        try {
            byte[] bytes = shareObject.getThumbnailBytes();
            if (bytes != null && bytes.length != 0) {
                bitmap = BitmapFactory.decodeByteArray(shareObject.getThumbnailBytes(), 0, bytes.length);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        // 1. 初始化微博的分享消息
        switch (type) {
            case TYPE_TEXT:
                TextObject mediaObject = new TextObject();
                mediaObject.text = shareObject.getMessage();
                weiboMessage.mediaObject = mediaObject;
                break;
            case TYPE_WEBURL:
                WebpageObject webpageObject = new WebpageObject();
                webpageObject.actionUrl = shareObject.getRedirectUrl();
                webpageObject.identify = Utility.generateGUID();
                if (bitmap != null) {
                    webpageObject.setThumbImage(bitmap);
                }
                webpageObject.title = shareObject.getTitle();
                webpageObject.description = shareObject.getSecondTitle();
                webpageObject.defaultText = shareObject.getMessage();
                weiboMessage.mediaObject = webpageObject;
                break;
            case TYPE_MUSIC:
                MusicObject musicObject = new MusicObject();
                musicObject.actionUrl = shareObject.getRedirectUrl();
                musicObject.identify = Utility.generateGUID();
                musicObject.title = shareObject.getTitle();
                musicObject.description = shareObject.getSecondTitle();

                musicObject.duration = shareObject.getContentSize();
                if (bitmap != null) {
                    musicObject.setThumbImage(bitmap);
                }
                musicObject.dataHdUrl = shareObject.getMediaUrl();
                musicObject.dataUrl = shareObject.getLowBandMediaUrl();
                weiboMessage.mediaObject = musicObject;

                break;
            case TYPE_IMAGE:
                ImageObject imageObject = new ImageObject();
                if (bitmap != null) {
                    imageObject.setImageObject(bitmap);
                }
                weiboMessage.mediaObject = imageObject;
                break;
            case TYPE_VIDEO:

                VideoObject videoObject = new VideoObject();
                videoObject.actionUrl = shareObject.getRedirectUrl();
                videoObject.identify = Utility.generateGUID();
                videoObject.title = shareObject.getTitle();
                videoObject.description = shareObject.getSecondTitle();

                videoObject.duration = shareObject.getContentSize();
                if (bitmap != null) {
                    videoObject.setThumbImage(bitmap);
                }
                videoObject.dataHdUrl = shareObject.getMediaUrl();
                videoObject.dataUrl = shareObject.getLowBandMediaUrl();
                weiboMessage.mediaObject = videoObject;
                break;
        }

        // 2. 初始化从第三方到微博的消息请求
        SendMessageToWeiboRequest request = new SendMessageToWeiboRequest();
        // 用transaction唯一标识一个请求
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.message = weiboMessage;

        try {
            if (weiboMessage.checkArgs()) {
                // 3. 发送请求消息到微博，唤起微博分享界面
                mWeiboShareAPI.sendRequest(request);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean shareValidateCheck(IShareObject... shareObjects) {
        if (shareObjects == null || shareObjects.length == 0) {
            logE("数据为NULL或者空");
            notifyEvent(getString(R.string.share_invalidate_datas));
            return false;
        }

        boolean validate = true;
        for (IShareObject shareObject : shareObjects) {
            if (shareObject == null) {
                notifyEvent(getString(R.string.share_invalidate_datas));
                return false;
            }

            IShareObject.TYPE type = shareObject.getType();
            // 1. 初始化微博的分享消息
            switch (type) {
                case TYPE_WEBURL:
                    if (StringUtils.isEmpty(shareObject.getRedirectUrl()) || !HttpUtils.isValidHttpUri(shareObject.getRedirectUrl())) {
                        notifyEvent(getString(R.string.share_webpage_invalidate_url));
                        validate = false;
                    } else if (shareObject.getThumbnailBytes() == null && shareObject.getBitmap() == null) {
                        notifyEvent(getString(R.string.share_image_empty));
                        validate = false;
                    }
                    break;
                case TYPE_TEXT:
                    if (StringUtils.isEmpty(shareObject.getMessage())) {
                        notifyEvent(getString(R.string.share_text_empty));
                        validate = false;
                    } else if (StringUtils.chineseLength(shareObject.getMessage()) > MAX_LENGTH_OF_CONTENT - 1) {
                        notifyEvent(getString(R.string.share_text_too_long));
                        validate = false;
                    }
                    break;
                case TYPE_IMAGE:
                    if (shareObject.getThumbnailBytes() == null) {
                        notifyEvent(getString(R.string.share_image_empty));
                        validate = false;
                    }
                    break;
                case TYPE_MUSIC:

                    if (StringUtils.isEmpty(shareObject.getMediaUrl()) && StringUtils.isEmpty(shareObject.getLowBandMediaUrl())) {
                        notifyEvent(getString(R.string.share_music_invalidate_url));
                        validate = false;
                    } else if (!HttpUtils.isValidHttpUri(shareObject.getMediaUrl()) && !HttpUtils.isValidHttpUri(shareObject.getLowBandMediaUrl())) {
                        notifyEvent(getString(R.string.share_music_invalidate_url));
                        validate = false;
                    } else if (StringUtils.isEmpty(shareObject.getRedirectUrl()) || !HttpUtils.isValidHttpUri(shareObject.getRedirectUrl())) {
                        notifyEvent(getString(R.string.share_music_invalidate_redirect_url));
                        validate = false;
                    } else if (shareObject.getContentSize() <= 0) {
                        notifyEvent(getString(R.string.share_music_invalidate_duration));
                        validate = false;
                    } else if (StringUtils.isEmpty(shareObject.getTitle())) {
                        notifyEvent(getString(R.string.share_music_title_empty));
                        validate = false;
                    }

                    break;
                case TYPE_VIDEO:

                    if (StringUtils.isEmpty(shareObject.getMediaUrl())) {//播放url
                        notifyEvent(getString(R.string.share_video_invalidate_url));
                        validate = false;
                    } else if (!HttpUtils.isValidHttpUri(shareObject.getMediaUrl())) {//播放url
                        notifyEvent(getString(R.string.share_video_invalidate_url));
                        validate = false;
                    } else if (StringUtils.isEmpty(shareObject.getRedirectUrl()) || !HttpUtils.isValidHttpUri(shareObject.getRedirectUrl())) {//详情url
                        notifyEvent(getString(R.string.share_video_invalidate_redirect_url));
                        validate = false;
                    } else if (shareObject.getContentSize() <= 0) {
                        notifyEvent(getString(R.string.share_video_invalidate_duration));//时长
                        validate = false;
                    } else if (StringUtils.isEmpty(shareObject.getTitle())) {//title
                        notifyEvent(getString(R.string.share_video_title_empty));
                        validate = false;
                    }

                    break;
            }
            return validate;
        }
        return validate;
    }

    /**
     * 该类定义了微博授权时所需要的参数。
     *
     * @author SINA
     * @since 2013-10-07
     */
    public static class AccessTokenKeeper {
        private static final String PREFERENCES_NAME = "com_weibo_sdk_android";

        private static final String KEY_UID = "uid";
        private static final String KEY_ACCESS_TOKEN = "access_token";
        private static final String KEY_EXPIRES_IN = "expires_in";

        /**
         * 保存 Token 对象到 SharedPreferences。
         *
         * @param context 应用程序上下文环境
         * @param token   Token 对象
         */
        public static void writeAccessToken(Context context, Oauth2AccessToken token) {
            if (null == context || null == token) {
                return;
            }

            SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString(KEY_UID, token.getUid());
            editor.putString(KEY_ACCESS_TOKEN, token.getToken());
            editor.putLong(KEY_EXPIRES_IN, token.getExpiresTime());
            editor.commit();
        }

        /**
         * 从 SharedPreferences 读取 Token 信息。
         *
         * @param context 应用程序上下文环境
         * @return 返回 Token 对象
         */
        public static Oauth2AccessToken readAccessToken(Context context) {
            if (null == context) {
                return null;
            }

            Oauth2AccessToken token = new Oauth2AccessToken();
            SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
            token.setUid(pref.getString(KEY_UID, ""));
            token.setToken(pref.getString(KEY_ACCESS_TOKEN, ""));
            token.setExpiresTime(pref.getLong(KEY_EXPIRES_IN, 0));
            return token;
        }

        /**
         * 清空 SharedPreferences 中 Token信息。
         *
         * @param context 应用程序上下文环境
         */
        public static void clear(Context context) {
            if (null == context) {
                return;
            }

            SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
            SharedPreferences.Editor editor = pref.edit();
            editor.clear();
            editor.commit();
        }
    }

    @Override
    public Object getUserInfo() {
        return mUserInfo;
    }

    public static class UserInfo {
        @SerializedName("name")
        public String name;

        @SerializedName("idstr")
        public String uid;

        @SerializedName("gender")
        public String gender;

        @SerializedName("avatar_hd")
        public String avatar;

        public String token;

        public void save(String json) {
            SharePreferenceProperties.set("weibo_user_info", json);
            UserInfo userInfo = load();
            if (userInfo != null) {
                uid = userInfo.uid;
                name = userInfo.name;
                gender = userInfo.gender;
                avatar = userInfo.avatar;
                token = mAccessToken.getToken();
            }
        }

        public boolean isGirl() {
            return "f".equals(gender);
        }

        public boolean isBoy() {
            return "m".equals(gender);
        }

        public static UserInfo load() {
            Gson gson = new Gson();
            String json = (String) SharePreferenceProperties.get("weibo_user_info", "");
            UserInfo info = gson.fromJson(json, UserInfo.class);
            if (info != null && mAccessToken != null) {
                info.token = mAccessToken.getToken();
            }
            return info;
        }

        public void clear() {
            save("");
        }
    }
}
