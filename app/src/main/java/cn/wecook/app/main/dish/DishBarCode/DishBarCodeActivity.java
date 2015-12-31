package cn.wecook.app.main.dish.DishBarCode;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.wecook.common.core.debug.Logger;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.userinfo.UserProperties;
import com.wecook.uikit.activity.BaseSwipeActivity;
import com.wecook.uikit.fragment.BaseFragment;

import cn.wecook.app.R;
import cn.wecook.app.dialog.LoadingDialog;
import cn.wecook.app.main.dish.DishRecentPurchaseFragment;
import cn.wecook.app.main.home.user.UserLoginActivity;

/**
 * 扫码功能
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/15/14
 */
public class DishBarCodeActivity extends BaseSwipeActivity {

    private static final int REQUEST_CODE_BAR_CODE = 0;
    private DishBarCodeFragment mFragment;
    private LoadingDialog mLoading;
    private boolean mScanning;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_BAR_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Logger.i("barcode", "result ok");
                if (data != null) {
                    final String code = data.getStringExtra(DishBarCodeScanActivity.EXTRA_BAR_CODE);
                    if (!StringUtils.isEmpty(code)) {
                        Logger.i("barcode", "request barcode detail with code : " + code);
                        Uri uri = Uri.parse(code);
                        if (uri != null && uri.getHost().equals("www.wecook.cn") && uri.getPath().equals("/download/")) {
                            //wecook 有效url
                            String param = uri.getQueryParameter("src");
                            if ("cplb".equals(param)) {
                                //跳转最近购买列表
                                next(DishRecentPurchaseFragment.class);
                                return;
                            }
                        }
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Logger.i("barcode", "result cancel");
                finish();
                mScanning = false;
            }
        }
    }

    private void showLoading() {
        hideLoading();
        mLoading = new LoadingDialog(this);
        mLoading.setText(R.string.app_tip_bar_code_scan_loading);
        mLoading.show();
    }

    private void hideLoading() {
        if (mLoading != null && mLoading.isShowing()) {
            mLoading.cancel();
        }
    }

    @Override
    protected BaseFragment onCreateFragment(Bundle savedInstanceState) {
        Logger.i("barcode", "onCreateFragment has scanning ? " + mScanning);
        if (mScanning) {
            mFragment = BaseFragment.getInstance(DishBarCodeFragment.class);
            return mFragment;
        }
        startScan();
        mFragment = BaseFragment.getInstance(DishBarCodeFragment.class);
        return mFragment;
    }

    private void startScan() {
        mScanning = true;
        Intent intent = new Intent(this, DishBarCodeScanActivity.class);
        startActivityForResult(intent, REQUEST_CODE_BAR_CODE);
    }

    @Override
    public boolean back(Bundle data) {
        BaseFragment fragment = getPreFragment();
        if (fragment != null && fragment instanceof DishBarCodeFragment) {
            //如果是扫码详情页面的返回，则直接关闭扫码功能
            Logger.i("barcode", "back!! from Detail");
            finish();
            return true;
        }

        fragment = getCurrentFragment();
        //如果是扫码结果没有数据，则返回按钮会再次进入扫描
        if (fragment != null && fragment instanceof DishBarCodeFragment) {
            Logger.i("barcode", "restart scan");
            startScan();
        }
        return super.back(data);

    }
}
