package cn.wecook.app.features.barcode;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.umeng.analytics.MobclickAgent;
import com.wecook.common.core.debug.Logger;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.KitchenApi;
import com.wecook.sdk.api.model.FoodResource;
import com.wecook.sdk.policy.LogConstant;
import com.wecook.uikit.activity.BaseSwipeActivity;
import com.wecook.uikit.fragment.BaseFragment;

import java.util.HashMap;
import java.util.Map;

import cn.wecook.app.R;
import cn.wecook.app.dialog.ConfirmDialog;
import cn.wecook.app.dialog.LoadingDialog;
import cn.wecook.app.main.dish.DishRecentPurchaseFragment;
import cn.wecook.app.main.kitchen.KitchenResourceDetailFragment;

/**
 * 扫码功能
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/15/14
 */
public class BarCodeActivity extends BaseSwipeActivity {

    private static final int REQUEST_CODE_BAR_CODE = 0;
    private BarCodeFragment mFragment;
    private LoadingDialog mLoading;
    private boolean mScanning;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_BAR_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Logger.i("barcode", "result ok");
                if (data != null) {
                    final String code = data.getStringExtra(BarCodeScanActivity.EXTRA_BAR_CODE);
                    if (!StringUtils.isEmpty(code)) {
                        Logger.i("barcode", "request barcode detail with code : " + code);
                        Uri uri = Uri.parse(code);
                        if (uri != null && null != uri.getHost() && uri.getHost().equals("www.wecook.cn") && null != uri.getPath() && uri.getPath().equals("/download/")) {
                            //wecook 有效url
                            String param = uri.getQueryParameter("src");
                            if ("cplb".equals(param)) {
                                //跳转最近购买列表
                                next(DishRecentPurchaseFragment.class);
                                return;
                            }
                            ConfirmDialog confirmDialog = new ConfirmDialog(getContext(), "请扫描菜品包装上的二维码，查看做法");
                            confirmDialog.show();
                        } else {
                            showLoading();
                            KitchenApi.getBarcodeDetail(code, new ApiCallback<FoodResource>() {
                                @Override
                                public void onResult(FoodResource result) {
                                    Map<String, String> keys = new HashMap<String, String>();
                                    keys.put(LogConstant.KEY_BARCODE, code);
                                    if (result != null && result.available()) {
                                        Logger.i("barcode", "get detail!!");
                                        keys.put(LogConstant.KEY_RESULT, LogConstant.RESULT_OK);
                                        Bundle bundle = new Bundle();
                                        bundle.putSerializable(KitchenResourceDetailFragment.EXTRA_DATA, result);
                                        mFragment.next(KitchenResourceDetailFragment.class, bundle);
                                    } else {
                                        Logger.i("barcode", "no detail!!");
                                        keys.put(LogConstant.KEY_RESULT, LogConstant.RESULT_FAIL);
                                        mFragment.showEmpty();
                                    }
                                    MobclickAgent.onEvent(getContext(), LogConstant.UBS_BARCODESEARCH_COUNT, keys);
                                    hideLoading();
                                    mScanning = false;
                                }
                            });
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
            mFragment = BaseFragment.getInstance(BarCodeFragment.class);
            return mFragment;
        }
        startScan();
        mFragment = BaseFragment.getInstance(BarCodeFragment.class);
        return mFragment;
    }

    private void startScan() {
        mScanning = true;
        Intent intent = new Intent(this, BarCodeScanActivity.class);
        startActivityForResult(intent, REQUEST_CODE_BAR_CODE);
    }

    @Override
    public boolean back(Bundle data) {
        BaseFragment fragment = getPreFragment();
        if (fragment != null && fragment instanceof BarCodeFragment) {
            //如果是扫码详情页面的返回，则直接关闭扫码功能
            Logger.i("barcode", "back!! from Detail");
            finish();
            return true;
        }

        fragment = getCurrentFragment();
        //如果是扫码结果没有数据，则返回按钮会再次进入扫描
        if (fragment != null && fragment instanceof BarCodeFragment) {
            Logger.i("barcode", "restart scan");
            startScan();
        }
        return true;
    }
}
