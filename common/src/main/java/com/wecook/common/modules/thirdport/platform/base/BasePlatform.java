package com.wecook.common.modules.thirdport.platform.base;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.wecook.common.R;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.filemaster.FileMaster;
import com.wecook.common.modules.thirdport.PlatformConfiguration;
import com.wecook.common.modules.thirdport.PlatformManager;
import com.wecook.common.modules.thirdport.PlatformNotify;
import com.wecook.common.modules.thirdport.object.IShareObject;
import com.wecook.common.modules.thirdport.object.OrderInfo;
import com.wecook.common.utils.FileUtils;
import com.wecook.common.utils.StringUtils;


/**
 * 社交组件抽象基类
 *
 * @author zhoulu
 * @date 13-12-11
 */
public abstract class BasePlatform implements IPlatform {

    public static final String ACTION_LOGIN = "com.wecook.common.action.LOGIN";//登录
    public static final String ACTION_LOGOUT = "com.wecook.common.action.LOGOUT";//登出
    public static final String ACTION_SHARE = "com.wecook.common.action.SHARE";//分享
    public static final String ACTION_CREATE = "com.wecook.common.action.CREATE";//创建api
    public static final String ACTION_PAY = "com.wecook.common.action.PAY";//支付

    public static final String TYPE_ACTIVITY_RESULT = "activity_result";
    public static final String TYPE_NEW_INTENT = "new_intent";

    public static final String EXTRA_RESULT = "result";
    public static final String EXTRA_MESSAGE = "message";
    public static final String EXTRA_RESULT_CODE = "resultCode";
    public static final String EXTRA_REQUEST_CODE = "requestCode";
    public static final String EXTRA_TYPE = "type";

    private Context mContext;
    private IShareObject mShareObject;
    private OrderInfo mOrderInfo;
    private IPlatformEventListener mEventListener;

    private String mCurrentAction;//当前执行的功能

    private PlatformConfiguration mConfig;

    protected int mType;

    public BasePlatform(Context context) {
        mContext = context;
        mConfig = PlatformManager.getInstance().getConfig();
    }

    public PlatformConfiguration getConfig() {
        return mConfig;
    }

    public void setContext(Context context) {
        mContext = context;
    }

    public void setShareObject(IShareObject object) {
        mShareObject = object;
    }

    public IShareObject getShareObject() {
        return mShareObject;
    }

    public OrderInfo getmOrderInfo() {
        return mOrderInfo;
    }

    public void setmOrderInfo(OrderInfo mOrderInfo) {
        this.mOrderInfo = mOrderInfo;
    }

    protected Context getContext() {
        return mContext;
    }

    protected String getString(int resId) {
        return mContext.getString(resId);
    }

    @Override
    public boolean isNeedUpdate() {
        return false;
    }

    @Override
    public boolean isValidSession() {
        return false;
    }

    @Override
    public void setEventListener(IPlatformEventListener eventListener) {
        mEventListener = eventListener;
    }

    public String getCurrentAction() {
        return mCurrentAction;
    }

    public void setCurrentAction(String action) {
        mCurrentAction = action;
    }

    @Override
    public int getShareMaxLength(IShareObject shareObject) {
        return Integer.MAX_VALUE;
    }

    @Override
    public final int getIcon() {
        return mConfig.getPlatformIcon(mType);
    }

    @Override
    public final String getShareAppId() {
        return mConfig.getShareConfig().getAppID(mType);
    }

    @Override
    public String getShareAppSecret() {
        return mConfig.getShareConfig().getAppSecret(mType);
    }

    @Override
    public final String getName() {
        return mConfig.getPlatformName(mType);
    }

    @Override
    public final String getShareOAuthUrl() {
        return mConfig.getShareConfig().getOAuthUrl(mType);
    }

    @Override
    public final String getShareRedirectUrl() {
        return mConfig.getShareConfig().getRedirectUrl(mType);
    }

    @Override
    public final String getShareScope() {
        return mConfig.getShareConfig().getScope(mType);
    }

    @Override
    public void setType(int type) {
        mType = type;
    }

    public int getType() {
        return mType;
    }

    @Override
    public Object getUserInfo() {
        return null;
    }

    /**
     * 设置分享结果
     *
     * @param success
     * @param message
     */
    protected final void performShare(boolean success, String message) {
        //发送通知分享结果
        notifyEvent(mContext, createShareIntent(success, message));
    }

    /**
     * 设置创建api结果
     *
     * @param success
     * @param message
     */
    protected final void performCreate(boolean success, String message) {
        //发送通知创建api结果
        notifyEvent(mContext, createCreateIntent(success, message));
    }

    /**
     * 设置登录结果
     *
     * @param success
     * @param message
     */
    protected final void performLogin(boolean success, String message) {
        //发送通知创建api结果
        notifyEvent(mContext, createLoginIntent(success, message));
    }

    /**
     * 设置登出结果
     *
     * @param success
     * @param message
     */
    protected final void performLogout(boolean success, String message) {
        //发送通知创建api结果
        notifyEvent(mContext, createLogoutIntent(success, message));
    }

    /**
     * 设置支付结果
     *
     * @param success
     * @param message
     */
    protected final void performPay(boolean success, String message) {
        //发送通知支付结果
        notifyEvent(mContext, createPayIntent(success, message));
    }

    private Intent createShareIntent(boolean success, String message) {
        Intent intent = new Intent(ACTION_SHARE);
        intent.putExtra(EXTRA_RESULT, success);
        intent.putExtra(EXTRA_MESSAGE, message);
        return intent;
    }

    private Intent createCreateIntent(boolean success, String message) {
        Intent intent = new Intent(ACTION_CREATE);
        intent.putExtra(EXTRA_RESULT, success);
        intent.putExtra(EXTRA_MESSAGE, message);
        return intent;
    }

    private Intent createLoginIntent(boolean success, String message) {
        Intent intent = new Intent(ACTION_LOGIN);
        intent.putExtra(EXTRA_RESULT, success);
        intent.putExtra(EXTRA_MESSAGE, message);
        return intent;
    }

    private Intent createLogoutIntent(boolean success, String message) {
        Intent intent = new Intent(ACTION_LOGOUT);
        intent.putExtra(EXTRA_RESULT, success);
        intent.putExtra(EXTRA_MESSAGE, message);
        return intent;
    }

    private Intent createPayIntent(boolean success, String message) {
        Intent intent = new Intent(ACTION_PAY);
        intent.putExtra(EXTRA_RESULT, success);
        intent.putExtra(EXTRA_MESSAGE, message);
        return intent;
    }

    private void notifyEvent(final Context context, Intent intent) {
        String message = dispatchShareListener(intent);
        notifyEvent(message);

        onEvent(context, intent);
    }

    public String dispatchShareListener(Intent intent) {
        if (intent == null) {
            return "";
        }

        boolean success = intent.getBooleanExtra(EXTRA_RESULT, false);
        String message = intent.getStringExtra(EXTRA_MESSAGE);

        if (mEventListener != null) {
            if (ACTION_SHARE.equals(intent.getAction())) {
                mEventListener.onResponseShare(this, success, message);
            } else if (ACTION_LOGIN.equals(intent.getAction())) {
                mEventListener.onResponseLogin(this, success, message);
            } else if (ACTION_LOGOUT.equals(intent.getAction())) {
                mEventListener.onResponseLogout(this, success, message);
            } else if (ACTION_PAY.equals(intent.getAction())) {
                mEventListener.onResponsePay(this, success, message);
            }
        }
        return message;
    }

    public void notifyEvent(final String message) {

        if (mConfig.enableShowNotify()) {
            UIHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (!StringUtils.isEmpty(message)) {
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }

    /**
     * 检查是否支持
     *
     * @return
     */
    public boolean checkSupport() {
        boolean created = onCreate();
        String message = "";
        if (isSupportSSOShare() || isSupportWebShare()) {
            performCreate(created, message);
            return true;
        } else {
            if (isInstalledApp()) {
                if (isNeedUpdate()) {
                    message = mContext.getString(R.string.share_errcode_need_update, getName());
                } else {
                    message = mContext.getString(R.string.share_errcode_unsupport, getName());
                }
            } else {
                message = mContext.getString(R.string.share_errcode_uninstalled, getName());
            }
            performCreate(created, message);
        }
        return false;
    }

    /**
     * 执行分享过程
     */
    public final void doShare() {
        mCurrentAction = ACTION_SHARE;
        //0.检查是否获得api
        if (checkSupport()) {
            //1.检查有效数据
            if (validateCheckSdcard(mShareObject) && shareValidateCheck(mShareObject)) {
                //2.发送数据
                onShare(mShareObject);
            } else {
                performShare(false, "");
            }
        } else {
            performShare(false, "");
        }
    }

    /**
     * 当分享带有图片的内容时候，检查当前sdcard的状态是否满足要求
     *
     * @param shareObject
     * @return
     */
    private boolean validateCheckSdcard(IShareObject shareObject) {
        if (shareObject == null)
            return false;
        switch (shareObject.getType()) {
            case TYPE_IMAGE:
            case TYPE_VIDEO:
            case TYPE_MUSIC:

                if (!FileMaster.isSdcardMounted()
                        || !FileMaster.isSdcardWritable()
                        || !FileMaster.isSdcardExist()) {
                    PlatformNotify.showShortToast(getContext(), R.string.share_errcode_sdcard_badly);
                    return false;
                }

                if (!FileUtils.checkSDCardHasEnoughSpace(FileUtils.ONE_MB)) {
                    PlatformNotify.showShortToast(getContext(), R.string.share_errcode_sdcard_no_space);
                    return false;
                }

                break;
        }
        return true;
    }

    /**
     * 执行登录
     */
    public final void doLogin() {
        mCurrentAction = ACTION_LOGIN;
        //0.检查是否获得api
        if (checkSupport()) {
            //1.登录
            onLogin();
        } else {
            performLogin(false, "");
        }
    }

    /**
     * 执行登出
     */
    public final void doLogout() {
        mCurrentAction = ACTION_LOGOUT;
        //0.检查是否获得api
        if (checkSupport()) {
            //1.登出
            onLogout();
        } else {
            performLogout(false, "");
        }
    }

    /**
     * 执行支付
     */
    public final void doPay(OrderInfo orderInfo) {
        mOrderInfo = orderInfo;
        mCurrentAction = ACTION_PAY;
        if (checkPaySupport()) {
            onPay(mOrderInfo);
        } else {
            performPay(false, "不支持支付");
        }
    }

    /**
     * 检查是否可支持支付功能
     *
     * @return
     */
    private boolean checkPaySupport() {
        return checkSupport()
                && isSupportSSOShare()
                && isInstalledApp()
                && isSupportPay();
    }

    @Override
    public boolean isSupportPay() {
        return false;
    }

    @Override
    public void onPay(OrderInfo orderInfo) {

    }

    @Override
    public boolean isSupportSSOShare() {
        return false;
    }

    @Override
    public boolean isSupportWebShare() {
        return false;
    }

    @Override
    public boolean isInstalledApp() {
        return false;
    }

    @Override
    public boolean onCreate() {
        return false;
    }

    @Override
    public void onLogin() {

    }

    @Override
    public void onLogout() {

    }

    @Override
    public boolean onEvent(Context context, Intent intent) {
        return false;
    }

    @Override
    public void onShare(IShareObject... shareObject) {

    }

    @Override
    public boolean shareValidateCheck(IShareObject... shareObject) {
        return false;
    }

    /**
     * 统一日志输出
     *
     * @param s
     */
    protected void log(String s) {
        Log.i("common-platform", "[" + ((Object) this).getClass().getSimpleName() + "] " + s);
    }

    protected void logE(String s) {
        Log.e("common-platform", "[" + ((Object) this).getClass().getSimpleName() + "] " + s);
    }
}
