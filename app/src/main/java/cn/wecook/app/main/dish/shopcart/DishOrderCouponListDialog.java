package cn.wecook.app.main.dish.shopcart;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.utils.ScreenUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.WalletApi;
import com.wecook.sdk.api.model.Coupon;
import com.wecook.sdk.policy.DishPolicy;
import com.wecook.uikit.adapter.UIAdapter;
import com.wecook.uikit.alarm.DialogAlarm;
import com.wecook.uikit.fragment.BaseFragment;
import com.wecook.uikit.loader.LoadMore;
import com.wecook.uikit.loader.LoadMoreImpl;
import com.wecook.uikit.loader.UILoader;
import com.wecook.uikit.widget.pulltorefresh.PullToRefreshListView;

import java.util.List;

import cn.wecook.app.R;

/**
 * 对话框优惠券列表
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/7/25
 */
public class DishOrderCouponListDialog extends DialogAlarm {

    private List<Coupon> mCouponList;
    private ItemAdapter mAdapter;
    private LoadMore<List<Coupon>> mLoadMore;

    private AdapterView.OnItemClickListener mItemClick;

    private BaseFragment mFragment;

    public DishOrderCouponListDialog(BaseFragment fragment) {
        super(fragment.getContext(), R.style.UIKit_Dialog_Fixed);
        mFragment = fragment;
    }

    public DishOrderCouponListDialog setCouponList(List<Coupon> list) {
        mCouponList = list;
        mAdapter = new ItemAdapter(getContext(), list);
        return this;
    }

    public DishOrderCouponListDialog setOnItemClick(AdapterView.OnItemClickListener itemClick) {
        mItemClick = itemClick;
        return this;
    }

    @Override
    public View getView(ViewGroup parent) {
        return LayoutInflater.from(getContext()).inflate(R.layout.dialog_order_coupon_list, parent, true);
    }

    @Override
    public void onViewCreated(View view) {
        ListView listView = ((PullToRefreshListView) view.findViewById(R.id.app_dialog_order_coupon_list)).getRefreshableView();
        (((PullToRefreshListView) view.findViewById(R.id.app_dialog_order_coupon_list))).setListViewHeight(ScreenUtils.dip2px(250));

        mLoadMore = new LoadMoreImpl<>();
        mLoadMore.setAutoLoadCount(Integer.MAX_VALUE);
        mLoadMore.setMoreLoader(new UILoader<List<Coupon>>(mFragment) {
            @Override
            public void sync(final Callback<List<Coupon>> callback) {
                double total = DishPolicy.get().getCheckoutTotalPriceWithDelivery();
                WalletApi.getAvailableCouponList(mLoadMore.getCurrentPage(), mLoadMore.getPageSize(),
                        total + "", new ApiCallback<ApiModelList<Coupon>>() {
                            @Override
                            public void onResult(ApiModelList<Coupon> result) {
                                callback.callback(result.getList());
                            }
                        });

            }
        });
        mLoadMore.setOnLoadMoreListener(new LoadMore.OnLoadMoreListener<List<Coupon>>() {
            @Override
            public void onLoaded(boolean success, List<Coupon> o) {
                if (success) {
                    mCouponList.addAll(o);
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
        mLoadMore.setListAdapter(mAdapter, listView);
        listView.setAdapter(mAdapter);
    }

    private class ItemAdapter extends UIAdapter<Coupon> {

        public ItemAdapter(Context context, List<Coupon> data) {
            super(context, R.layout.dialog_order_coupon_item, data);
        }

        @Override
        public void updateView(final int position, int viewType, final Coupon data, Bundle extra) {
            super.updateView(position, viewType, data, extra);
            if (data != null) {
                View label = findViewById(R.id.app_order_coupon_label);
                TextView title = (TextView) findViewById(R.id.app_order_coupon_title);
                final View check = findViewById(R.id.app_order_coupon_check);
                if (StringUtils.parseInt(data.getId()) <= 0) {
                    label.setVisibility(View.GONE);
                } else {
                    label.setVisibility(View.VISIBLE);
                }
                title.setText(data.getDesc());
                check.setSelected(data.isSelected());
                getItemView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!data.isSelected()) {
                            data.setSelected(!data.isSelected());
                            for (Coupon item : mCouponList) {
                                if (item != data && item.isSelected()) {
                                    item.setSelected(false);
                                }
                            }
                            notifyDataSetChanged();
                            if (mItemClick != null) {
                                mItemClick.onItemClick(null, getItemView(), position, 0);
                            }
                        }
                        dismiss();
                    }
                });
            }
        }
    }
}
