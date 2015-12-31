package cn.wecook.app.main.home.wallet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.utils.ScreenUtils;
import com.wecook.sdk.api.legacy.UserApi;
import com.wecook.sdk.api.legacy.WalletApi;
import com.wecook.sdk.api.model.Coupon;
import com.wecook.uikit.fragment.ApiModelListFragment;
import com.wecook.uikit.widget.EmptyView;
import com.wecook.uikit.widget.pulltorefresh.PullToRefreshBase;

import cn.wecook.app.R;
import cn.wecook.app.features.webview.WebViewActivity;
import cn.wecook.app.features.webview.WebViewFragment;
import cn.wecook.app.utils.TitleBarUtils;

/**
 * 优惠券
 * Created by LK on 2015/10/15.
 */
public class CouponsFragment extends ApiModelListFragment<Coupon> {
    /**
     * 展现模式（过期/不过期）
     */
    public static final String PARAMTS_SHOW_MODE = "mode";
    /**
     * 过期
     */
    public static final String MODE_UNAVAILABLE = "unavailable";
    /**
     * 可用
     */
    public static final String MODE_AVAILABLE = "available";

    private String mode = MODE_AVAILABLE;

    private View mView;
    private TextView mLoadOldBtn;//查看过期优惠券

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle bundle = getArguments();
        if (bundle.containsKey(PARAMTS_SHOW_MODE)) {
            mode = bundle.getString(PARAMTS_SHOW_MODE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_coupons, null);
        mLoadOldBtn = (TextView) mView.findViewById(R.id.app_coupons_load_more);
        mLoadOldBtn.setVisibility(View.GONE);
        mLoadOldBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString(CouponsFragment.PARAMTS_SHOW_MODE, CouponsFragment.MODE_UNAVAILABLE);
                bundle.putString(CouponsFragment.EXTRA_TITLE, "最近过期优惠券");
                next(CouponsFragment.class, bundle);
            }
        });
        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getRefreshListLayout().setMode(PullToRefreshBase.Mode.BOTH);
        getTitleBar().setBackgroundColor(getResources().getColor(R.color.uikit_white));
        if (mode.equals(MODE_AVAILABLE)) {
            TitleBarUtils.setRemind(getContext(), getTitleBar(), "使用说明", true, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putString(WebViewFragment.EXTRA_URL, UserApi.BCP_COUPON_INFO);
                    bundle.putString(WebViewFragment.EXTRA_TITLE, "使用说明");
                    startActivity(new Intent(getContext(), WebViewActivity.class), bundle);
                }
            });
        }
    }

    @Override
    protected void requestList(int page, int pageSize, ApiCallback<ApiModelList<Coupon>> callback) {
        if (mode.equals(MODE_AVAILABLE)) {
            WalletApi.getAvailableCouponList(page, pageSize, "-1", callback);
        } else {
            WalletApi.getDisableCouponList(page, pageSize, callback);
        }

    }

    @Override
    public void onStartUILoad() {
        super.onStartUILoad();
        WalletApi.getDisableCouponList(1, 20, new ApiCallback<ApiModelList<Coupon>>() {
            @Override
            public void onResult(ApiModelList<Coupon> result) {
                if (null != result && null != result.getList() && result.getList().size() > 0) {
                    mLoadOldBtn.setVisibility(mode.equals(MODE_AVAILABLE) ? View.VISIBLE : View.GONE);
                }
            }
        });
    }

    @Override
    protected int getItemLayoutId() {
        return R.layout.listview_item_coupon;
    }

    @Override
    protected void updateItemView(View view, int position, int viewType, Coupon data, Bundle extra) {
        super.updateItemView(view, position, viewType, data, extra);
        View leftDiv = view.findViewById(R.id.app_coupon_left_div);
        View rightDiv = view.findViewById(R.id.app_coupon_right_div);
        TextView moneyIcon = (TextView) view.findViewById(R.id.app_coupon_money_logo);
        TextView moneyTv = (TextView) view.findViewById(R.id.app_coupon_money);
        TextView title = (TextView) view.findViewById(R.id.app_coupon_title);
        TextView subtitle = (TextView) view.findViewById(R.id.app_coupon_subtitle);
        ImageView bgLogo = (ImageView) view.findViewById(R.id.app_coupon_logo);

        if (mode.equals(MODE_AVAILABLE)) {
            //金额颜色
            moneyIcon.setTextColor(getResources().getColor(R.color.uikit_red));
            moneyTv.setTextColor(getResources().getColor(R.color.uikit_red));
            //字色
            title.setTextColor(getResources().getColor(R.color.uikit_777));
            subtitle.setTextColor(getResources().getColor(R.color.uikit_777));
            //背景
            bgLogo.setBackgroundResource(R.drawable.app_pic_coupon_grey_wecook);
            //边
            leftDiv.setBackgroundResource(R.drawable.app_bg_coupon_red_left_div);
            rightDiv.setBackgroundResource(R.drawable.app_bg_coupon_red_right_div);
        } else {
            //金额颜色
            moneyIcon.setTextColor(getResources().getColor(R.color.uikit_aaa));
            moneyTv.setTextColor(getResources().getColor(R.color.uikit_aaa));
            //字色
            title.setTextColor(getResources().getColor(R.color.uikit_aaa));
            subtitle.setTextColor(getResources().getColor(R.color.uikit_aaa));
            //背景
            bgLogo.setBackgroundResource(R.drawable.app_pic_coupon_grey_overdue);
            //边
            leftDiv.setBackgroundResource(R.drawable.app_bg_coupon_grey_left_div);
            rightDiv.setBackgroundResource(R.drawable.app_bg_coupon_grey_right_div);
        }

        //money
        moneyTv.setText((int) data.getMoney() + "");
        //Title
        title.setText(data.getSubDesc());
        //SubTitle
        subtitle.setText("有效期至" + data.getExpiry_date());
    }

    @Override
    protected void onNewView(int viewType, View view) {
        super.onNewView(viewType, view);
        int width = ScreenUtils.getScreenWidthInt();
        float ratio = (float) 115.00 / (float) 355.00;
        int height = (int) (width * ratio);
        AbsListView.LayoutParams layoutParams = (AbsListView.LayoutParams) view.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new AbsListView.LayoutParams(width, height);
            view.setLayoutParams(layoutParams);
        } else {
            layoutParams.width = width;
            layoutParams.height = height;
            view.setLayoutParams(layoutParams);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        onStartUILoad();
    }

    @Override
    protected void onUpdateEmptyView() {
        super.onUpdateEmptyView();
        EmptyView emptyView = getEmptyView();
        if (null != emptyView) {
            emptyView.setBackgroundColor(getResources().getColor(R.color.uikit_grey));
            emptyView.setImageViewResourceIdOfTop(R.drawable.app_ic_empty_coupon);
            emptyView.getTitle().setVisibility(View.GONE);
            if (mode.equals(MODE_AVAILABLE)) {
                emptyView.getSecondTitle().setText("您暂无可使用的优惠券哦");
            } else {
                emptyView.getSecondTitle().setText("您暂无过期的优惠券");
            }
        }
    }
}
