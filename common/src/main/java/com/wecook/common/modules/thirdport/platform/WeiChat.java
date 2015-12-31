package com.wecook.common.modules.thirdport.platform;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.tencent.mm.sdk.constants.Build;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXMusicObject;
import com.tencent.mm.sdk.modelmsg.WXTextObject;
import com.tencent.mm.sdk.modelmsg.WXVideoObject;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.wecook.common.R;
import com.wecook.common.modules.property.SharePreferenceProperties;
import com.wecook.common.modules.thirdport.object.IShareObject;
import com.wecook.common.modules.thirdport.object.OrderInfo;
import com.wecook.common.modules.thirdport.platform.base.BasePlatform;
import com.wecook.common.utils.HttpUtils;
import com.wecook.common.utils.StringUtils;

import cz.msebera.android.httpclient.Header;

/**
 * 微信
 */
public class WeiChat extends BasePlatform {

    protected IWXAPI mApi;

    protected boolean mTimeline;//是否分享到朋友圈
    protected boolean mRegisterApp;//是否注册到微信中

    private IWXAPIEventHandler mEventHandler;
    private static WeChatSession mWeChatSession;
    private WeChatUserInfo mWeChatUserInfo;

    public WeiChat(Context context) {
        super(context);
    }

    public void setEnableTimeline(boolean showTimeline) {
        mTimeline = showTimeline;
    }

    /**
     * 是否支持SSO调用
     *
     * @return
     */
    @Override
    public boolean isSupportSSOShare() {
        return mApi != null && mApi.isWXAppInstalled() && mApi.isWXAppSupportAPI();
    }

    /**
     * 是否支持以Web形式进行分享
     *
     * @return
     */
    @Override
    public boolean isSupportWebShare() {
        return false;
    }

    @Override
    public boolean isSupportPay() {
        return mApi != null && mApi.getWXAppSupportAPI() >= Build.PAY_SUPPORTED_SDK_INT;
    }

    /**
     * 是否安装软件
     *
     * @return
     */
    @Override
    public boolean isInstalledApp() {
        return mApi != null && mApi.isWXAppInstalled();
    }

    @Override
    public boolean isValidSession() {
        return mWeChatSession.isSessionValid();
    }

    @Override
    public boolean isNeedUpdate() {
        return mApi != null && mApi.isWXAppSupportAPI();
    }

    @Override
    public Object getUserInfo() {
        return mWeChatUserInfo;
    }

    /**
     * 检查API-SDK是否有效
     *
     * @return
     */
    @Override
    public boolean onCreate() {
        mApi = WXAPIFactory.createWXAPI(getContext(), getShareAppId(), false);
        if (mApi != null) {
            mRegisterApp = mApi.registerApp(getShareAppId());
        }
        mWeChatSession = WeChatSession.load();
        mWeChatUserInfo = WeChatUserInfo.load();
        return true;
    }

    @Override
    public void onLogin() {
        if (mApi != null) {
            mRegisterApp = mApi.registerApp(getShareAppId());
            mEventHandler = new IWXAPIEventHandler() {
                @Override
                public void onReq(BaseReq baseReq) {

                }

                @Override
                public void onResp(BaseResp baseResp) {
                    boolean success = (baseResp.errCode == BaseResp.ErrCode.ERR_OK);
                    String message = baseResp.errStr;

                    switch (baseResp.errCode) {
                        case BaseResp.ErrCode.ERR_OK:
                            message = getString(R.string.share_status_auth_success);
                            break;
                        case BaseResp.ErrCode.ERR_USER_CANCEL:
                            message = getString(R.string.share_errcode_auth_cancel);
                            break;
                        case BaseResp.ErrCode.ERR_AUTH_DENIED:
                            message = getString(R.string.share_status_auth_deny);
                            break;
                        default:
                            if (message == null || message.equals("")) {
                                message = getString(R.string.share_errcode_unknown);
                            }
                            break;
                    }

                    if (success) {
                        if (baseResp instanceof SendAuth.Resp) {
                            String url = ((SendAuth.Resp) baseResp).url;
                            if (!StringUtils.isEmpty(url) && url.startsWith(getShareAppId())) {
                                Uri uri = Uri.parse(url);
                                String code = uri.getQueryParameter("code");
                                requestSessionToken(code);
                            }
                        }
                    } else {
                        performLogin(false, message);
                    }
                }
            };
            SendAuth.Req req = new SendAuth.Req();
            req.scope = "snsapi_userinfo";
            req.state = getName();
            mApi.sendReq(req);
        }
    }

    /**
     * 请求会话
     *
     * @param code
     */
    private void requestSessionToken(String code) {
        AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams requestParams = new RequestParams();
        requestParams.put("code", code);
        requestParams.put("secret", getShareAppSecret());
        requestParams.put("appid", getShareAppId());
        requestParams.put("grant_type", "authorization_code");
        httpClient.get(getShareOAuthUrl(), requestParams,
                new TextHttpResponseHandler() {
                    @Override
                    public void onFailure(int statusCode, Header[] headers,
                                          String responseString, Throwable throwable) {
                        performLogin(false, responseString);
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers,
                                          String responseString) {

                        Gson gson = new Gson();
                        mWeChatSession = gson.fromJson(responseString, WeChatSession.class);
                        mWeChatSession.save();
                        requestUserInfo();
                    }
                }
        );
    }

    /**
     * 请求用户信息
     */
    private void requestUserInfo() {
        AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams requestParams = new RequestParams();
        requestParams.put("access_token", mWeChatSession.accessToken);
        requestParams.put("openid", mWeChatSession.openid);
        httpClient.get("https://api.weixin.qq.com/sns/userinfo", requestParams,
                new TextHttpResponseHandler() {
                    @Override
                    public void onFailure(int statusCode, Header[] headers,
                                          String responseString, Throwable throwable) {
                        performLogin(false, responseString);
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String responseString) {
                        Gson gson = new Gson();
                        mWeChatUserInfo = gson.fromJson(responseString, WeChatUserInfo.class);
                        mWeChatUserInfo.save();
                        performLogin(true, "");
                    }
                }
        );
    }

    @Override
    public void onLogout() {
        if (mApi != null) {
            mApi.unregisterApp();
        }
        WeChatUserInfo.clear();
        WeChatSession.clear();
    }

    @Override
    public void onPay(OrderInfo orderInfo) {
        super.onPay(orderInfo);
        if (checkProduct(orderInfo) && orderInfo instanceof WeChatOrderInfo) {
            mEventHandler = new IWXAPIEventHandler() {
                @Override
                public void onReq(BaseReq baseReq) {

                }

                @Override
                public void onResp(BaseResp baseResp) {
                    boolean success = (baseResp.errCode == BaseResp.ErrCode.ERR_OK);
                    String message = baseResp.errStr;
                    if (baseResp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
                        switch (baseResp.errCode) {
                            case BaseResp.ErrCode.ERR_OK:
                                message = "支付成功";
                                break;
                            case BaseResp.ErrCode.ERR_USER_CANCEL:
                                message = "支付取消";
                                break;
                            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                                message = "支付失败";
                                break;
                        }
                        performPay(success, message);
                    } else {
                        performPay(false, "支付失败");
                    }
                }
            };
            PayReq req = new PayReq();
            req.appId = getShareAppId();
            req.nonceStr = ((WeChatOrderInfo) orderInfo).getNoncestr();
            req.packageValue = ((WeChatOrderInfo) orderInfo).getPackageValue();
            req.partnerId = ((WeChatOrderInfo) orderInfo).getPartnerid();
            req.sign = ((WeChatOrderInfo) orderInfo).getSign();
            req.timeStamp = ((WeChatOrderInfo) orderInfo).getTimestamp();
            req.prepayId = ((WeChatOrderInfo) orderInfo).getPrepayid();
            mApi.sendReq(req);
        } else {
            performPay(false, "商品数据为空");
        }
    }

    /**
     * 检查商品
     *
     * @param orderInfo
     * @return
     */
    private boolean checkProduct(OrderInfo orderInfo) {
        return orderInfo != null;
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
        if (mApi != null && mEventHandler != null && mApi.handleIntent(intent, mEventHandler)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean shareValidateCheck(IShareObject... shareObjects) {

        if (shareObjects == null || shareObjects.length == 0) {
            logE("数据为NULL或者空");
            notifyEvent(getString(R.string.share_invalidate_datas));
            return false;
        }

        IShareObject shareObject = shareObjects[0];

        if (shareObject == null) {
            logE("第一条数据为NULL");
            notifyEvent(getString(R.string.share_invalidate_datas));
            return false;
        }

        if (StringUtils.isEmpty(shareObject.getTitle())) {
            notifyEvent(getString(R.string.share_text_title_empty));
            return false;
        }

        IShareObject.TYPE type = shareObject.getType();
        boolean result = true;
        switch (type) {
            case TYPE_TEXT:
                String content = shareObject.getMessage();
                if (StringUtils.isEmpty(content)) {
                    notifyEvent(getString(R.string.share_text_empty));
                    result = false;
                }
                break;
            case TYPE_MUSIC:
                String musicUrl = shareObject.getMediaUrl();
                String musicLowBandUrl = shareObject.getLowBandMediaUrl();

                if (StringUtils.isEmpty(musicLowBandUrl) && StringUtils.isEmpty(musicUrl)) {
                    notifyEvent(getString(R.string.share_music_invalidate_url));
                    result = false;
                }
                break;
            case TYPE_VIDEO:
                String videoUrl = shareObject.getMediaUrl();
                String videoLowBandUrl = shareObject.getLowBandMediaUrl();

                if (StringUtils.isEmpty(videoLowBandUrl) && StringUtils.isEmpty(videoUrl)) {
                    notifyEvent(getString(R.string.share_video_invalidate_url));
                    result = false;
                }
                break;
            case TYPE_IMAGE:
                byte[] bitmap = shareObject.getThumbnailBytes();
                String[] urls = shareObject.getOnlineThumbnailUrl();
                if (bitmap == null && (urls == null || urls.length == 0)) {
                    notifyEvent(getString(R.string.share_image_empty));
                    result = false;
                } else if (bitmap == null && urls != null) {
                    notifyEvent(getString(R.string.share_image_only_for_local));
                    result = false;
                }
                break;
            case TYPE_WEBURL:
                String url = shareObject.getRedirectUrl();
                if (StringUtils.isEmpty(url) || !HttpUtils.isValidHttpUri(url)) {
                    notifyEvent(getString(R.string.share_webpage_invalidate_url));
                    result = false;
                }
                break;
        }
        return result;
    }

    /**
     * 开始执行分享过程
     *
     * @param shareObjects
     */
    @Override
    public void onShare(IShareObject... shareObjects) {
        mEventHandler = new IWXAPIEventHandler() {
            @Override
            public void onReq(BaseReq baseReq) {

            }

            @Override
            public void onResp(BaseResp baseResp) {
                boolean success = (baseResp.errCode == BaseResp.ErrCode.ERR_OK);
                String message = baseResp.errStr;

                switch (baseResp.errCode) {
                    case BaseResp.ErrCode.ERR_OK:
                        message = getString(R.string.share_status_success);
                        break;
                    case BaseResp.ErrCode.ERR_USER_CANCEL:
                        message = getString(R.string.share_status_cancel);
                        break;
                    case BaseResp.ErrCode.ERR_AUTH_DENIED:
                        message = getString(R.string.share_status_auth_deny);
                        break;
                    default:
                        if (message == null || message.equals("")) {
                            message = getString(R.string.share_errcode_unknown);
                        }
                        break;
                }

                performShare(success, message);
            }
        };

        IShareObject shareObject = shareObjects[0];

        IShareObject.TYPE type = shareObject.getType();

        WXMediaMessage.IMediaObject mediaObject = null;
        switch (type) {
            case TYPE_TEXT:
                mediaObject = new WXTextObject();
                ((WXTextObject) mediaObject).text = shareObject.getMessage();
                break;
            case TYPE_MUSIC:
                mediaObject = new WXMusicObject();
                ((WXMusicObject) mediaObject).musicUrl = shareObject.getRedirectUrl();
                ((WXMusicObject) mediaObject).musicLowBandUrl = shareObject.getRedirectUrl();
                ((WXMusicObject) mediaObject).musicDataUrl = shareObject.getMediaUrl();
                ((WXMusicObject) mediaObject).musicLowBandDataUrl = shareObject.getLowBandMediaUrl();
                break;
            case TYPE_VIDEO:
                mediaObject = new WXVideoObject();
                ((WXVideoObject) mediaObject).videoUrl = shareObject.getMediaUrl();
                ((WXVideoObject) mediaObject).videoLowBandUrl = shareObject.getLowBandMediaUrl();
                break;
            case TYPE_IMAGE:
                mediaObject = new WXImageObject(shareObject.getThumbnailBytes());
                break;
            case TYPE_WEBURL:
                mediaObject = new WXWebpageObject(shareObject.getRedirectUrl());
                break;
        }

        if (mediaObject == null) {
            return;
        }

        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = mediaObject;
        msg.title = shareObject.getTitle();
        if (type == IShareObject.TYPE.TYPE_TEXT) {
            msg.description = shareObject.getMessage();
        } else {
            msg.description = shareObject.getSecondTitle();
        }

        byte[] thumbnail = shareObject.getThumbnailBytes();
        if (thumbnail != null && thumbnail.length != 0 && type != IShareObject.TYPE.TYPE_TEXT) {
            msg.thumbData = thumbnail;
        }

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction(type.getValue());
        req.message = msg;
        req.scene = mTimeline ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
        mApi.sendReq(req);
        performShare(true, "");
    }

    private String buildTransaction(String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    public static class WeChatSession {
        @SerializedName("access_token")
        private String accessToken;
        @SerializedName("refresh_token")
        private String refreshToken;
        @SerializedName("expires_in")
        private long expireTime;
        @SerializedName("openid")
        private String openid;
        @SerializedName("session_time")
        private long sessionTime;

        public void save() {
            sessionTime = System.currentTimeMillis();

            Gson gson = new Gson();
            SharePreferenceProperties.set("wexin_session", gson.toJson(this));
        }

        public static WeChatSession load() {
            String json = (String) SharePreferenceProperties.get("wexin_session", "");
            Gson gson = new Gson();
            return gson.fromJson(json, WeChatSession.class);
        }

        public boolean isSessionValid() {
            if (System.currentTimeMillis() - sessionTime > expireTime * 1000) {
                return false;
            }
            return true;
        }

        public static void clear() {
            SharePreferenceProperties.set("wexin_session", "");
        }
    }

    public static class WeChatUserInfo {

        @SerializedName("openid")
        public String openid;

        @SerializedName("nickname")
        public String nickname;

        @SerializedName("sex")
        public int sex;

        @SerializedName("city")
        public String city;

        @SerializedName("country")
        public String country;

        @SerializedName("headimgurl")
        public String headimgurl;

        @SerializedName("unionid")
        public String unionid;

        public String token;

        public void save() {
            Gson gson = new Gson();
            SharePreferenceProperties.set("wexin_user_info", gson.toJson(this));
            token = mWeChatSession.accessToken;
        }

        public static WeChatUserInfo load() {
            String json = (String) SharePreferenceProperties.get("wexin_user_info", "");
            Gson gson = new Gson();
            WeChatUserInfo info = gson.fromJson(json, WeChatUserInfo.class);
            if (info != null) {
                info.token = mWeChatSession.accessToken;
            }
            return info;
        }

        public static void clear() {
            SharePreferenceProperties.set("wexin_user_info", "");
        }

    }

    public static class WeChatOrderInfo extends OrderInfo {
        private String noncestr;

        private String partnerid;

        private String prepayid;

        private String timestamp;

        private String sign;

        private String packageValue;

        public String getNoncestr() {
            return noncestr;
        }

        public void setNoncestr(String noncestr) {
            this.noncestr = noncestr;
        }

        public String getPartnerid() {
            return partnerid;
        }

        public void setPartnerid(String partnerid) {
            this.partnerid = partnerid;
        }

        public String getPrepayid() {
            return prepayid;
        }

        public void setPrepayid(String prepayid) {
            this.prepayid = prepayid;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        public String getSign() {
            return sign;
        }

        public void setSign(String sign) {
            this.sign = sign;
        }

        public String getPackageValue() {
            return packageValue;
        }

        public void setPackageValue(String packageValue) {
            this.packageValue = packageValue;
        }
    }
}
