package cn.wecook.app.main.home.wallet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.WalletApi;
import com.wecook.sdk.api.model.base.DataModel;
import com.wecook.sdk.userinfo.UserProperties;
import com.wecook.uikit.fragment.BaseTitleFragment;
import com.wecook.uikit.widget.EmptyView;
import com.wecook.uikit.widget.TitleBar;

import cn.wecook.app.R;
import cn.wecook.app.main.home.user.UserLoginActivity;

/**
 * 我的钱包
 * Created by LK on 2015/10/15.
 */
public class WalletFragment extends BaseTitleFragment {
    private final int TEXTSIZE = 14;
    private final int TEXTSIZE_SMALL = 12;

    private View mView;
    private LinearLayout layout_cj;//菜金
    private LinearLayout layout_coupons;//优惠券
    private EmptyView mEmptyView;

    private String walletCount = "0";//钱包余额
    private String couponCount = "0";//优惠券余额

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setFixed(true);
    }

    @Override
    public void onCardIn(Bundle data) {
        super.onCardIn(data);
        setFixed(true);
    }

    @Override
    public void onRefreshList() {
        super.onRefreshList();
        onStartUILoad();
    }

    @Override
    public boolean back(Bundle data) {
        setFixed(false);
        return super.back(data);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_wallet, null);
        layout_cj = (LinearLayout) mView.findViewById(R.id.app_wallet_cj);
        mEmptyView = (EmptyView) mView.findViewById(R.id.app_wallet_empty);
        layout_coupons = (LinearLayout) mView.findViewById(R.id.app_wallet_coupons);
        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //设置TitleBar
        TitleBar titleBar = getTitleBar();
        titleBar.setBackListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back();
            }
        });
        titleBar.setBackgroundColor(getResources().getColor(R.color.uikit_white));
        titleBar.getTitleView().setText("我的钱包");
        //菜金
        initItemView(layout_cj, "菜金");
        //优惠券
        initItemView(layout_coupons, "优惠券");
    }

    /**
     * 初始化ItemView
     *
     * @param layout
     * @param title
     */
    private void initItemView(LinearLayout layout, final String title) {
        //默认状态
        TextView textViewRight = (TextView) layout.findViewById(R.id.app_my_feature_sub_name);
        textViewRight.setTextColor(getResources().getColor(R.color.uikit_777));
        textViewRight.setText("加载中...");
        textViewRight.setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXTSIZE_SMALL);
        textViewRight.setVisibility(View.VISIBLE);
        //初始化
        TextView textView = (TextView) layout.findViewById(R.id.app_my_feature_name);
        textView.setTextColor(getResources().getColor(R.color.uikit_333));
        textView.setText(title);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXTSIZE);
    }

    /**
     * @param layout
     * @param subTitle
     */
    private void updItemView(LinearLayout layout, String subTitle, boolean setListener) {
        if (null != subTitle && !"".equals(subTitle)) {
            TextView textView = (TextView) layout.findViewById(R.id.app_my_feature_sub_name);
            textView.setTextColor(getResources().getColor(R.color.uikit_333));
            textView.setText(subTitle);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXTSIZE);
            textView.setVisibility(View.VISIBLE);
            if (setListener) {
                layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switch (v.getId()) {
                            case R.id.app_wallet_cj://菜金
                                Bundle bundle = new Bundle();
                                bundle.putString(VegetableMoneyFragment.PARAMTS_MONEY, walletCount);
                                next(VegetableMoneyFragment.class, bundle);
                                break;
                            case R.id.app_wallet_coupons://优惠券
                                Bundle bundle2 = new Bundle();
                                bundle2.putString(CouponsFragment.PARAMTS_SHOW_MODE, CouponsFragment.MODE_AVAILABLE);
                                bundle2.putString(CouponsFragment.EXTRA_TITLE, "优惠券");
                                next(CouponsFragment.class, bundle2);
                                break;
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onStartUILoad() {
        super.onStartUILoad();
        showLoading();
        WalletApi.getWalletRemainder(new ApiCallback<DataModel>() {
            @Override
            public void onResult(DataModel result) {
                if (result.available()) {
                    hideEmptyView();
                    JsonObject resultJson = result.getElement().getAsJsonObject();
                    if (resultJson.has("result")) {
                        resultJson = resultJson.getAsJsonObject("result");
                    }
                    if (resultJson.has("money")) {
                        walletCount = resultJson.get("money").getAsString();
                    }
                    if (resultJson.has("coupons_count")) {
                        couponCount = resultJson.get("coupons_count").getAsString();
                    }

                    updItemView(layout_cj, StringUtils.getPriceText(Float.parseFloat(walletCount)), true);
                    updItemView(layout_coupons, couponCount + "张", true);
                    hideLoading();
                } else {
                    showEmptyView();
                }
            }
        });
    }

    private void showEmptyView() {
        if (mEmptyView != null && mEmptyView.getVisibility() != View.VISIBLE) {
            mEmptyView.setVisibility(View.VISIBLE);
            if (UserProperties.isLogin()) {
                mEmptyView.setTitle("数据为空");
                mEmptyView.setSecondTitle("与服务器失联了，请点击重试");
                mEmptyView.setActionButton("重试", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onStartUILoad();
                    }
                });
            } else {
                mEmptyView.setTitle("状态错误");
                mEmptyView.setSecondTitle("需要登录之后才能查看");
                mEmptyView.setActionButton("点击登录", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), UserLoginActivity.class);
                        startActivity(intent);
                    }
                });
            }
        }
        hideLoading();
    }

    private void hideEmptyView() {
        if (mEmptyView != null) {
            mEmptyView.setVisibility(View.GONE);
        }
    }
}
