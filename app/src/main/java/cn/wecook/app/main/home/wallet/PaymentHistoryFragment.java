package cn.wecook.app.main.home.wallet;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.sdk.api.legacy.WalletApi;
import com.wecook.sdk.api.model.PaymentDetail;
import com.wecook.uikit.fragment.ApiModelListFragment;
import com.wecook.uikit.widget.EmptyView;
import com.wecook.uikit.widget.pulltorefresh.PullToRefreshBase;

import java.text.DecimalFormat;

import cn.wecook.app.R;

/**
 * 收支明细
 * Created by LK on 2015/10/15.
 */
public class PaymentHistoryFragment extends ApiModelListFragment<PaymentDetail> {
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getRefreshListLayout().setMode(PullToRefreshBase.Mode.BOTH);
        getTitleBar().setBackgroundColor(getResources().getColor(R.color.uikit_white));
        setTitle("收支明细");

    }

    @Override
    protected void requestList(int page, int pageSize, ApiCallback<ApiModelList<PaymentDetail>> callback) {
        WalletApi.getWalletIndex(page + "", pageSize + "", callback);
    }

    @Override
    protected void updateItemView(View view, int position, int viewType, PaymentDetail data, Bundle extra) {
        super.updateItemView(view, position, viewType, data, extra);
        TextView money = (TextView) view.findViewById(R.id.app_item_payment_money);
        TextView time = (TextView) view.findViewById(R.id.app_item_payment_time);
        TextView source_type = (TextView) view.findViewById(R.id.app_item_payment_type);
        TextView desc = (TextView) view.findViewById(R.id.app_item_payment_desc);
        //money
        DecimalFormat df = new DecimalFormat("##########0.00");
        float moneyCount = Float.parseFloat(data.getMoney());
        if (moneyCount >= 0) {
            money.setTextColor(getResources().getColor(R.color.uikit_green));
            money.setText("+" + df.format(moneyCount));
        } else {
            money.setTextColor(getResources().getColor(R.color.uikit_red));
            money.setText(df.format(moneyCount) + "");
        }
        //type
        source_type.setText(data.getSourceType());
        //time
        time.setText(data.getTime());
        //desc
        if (null == data.getDesc() || "".equals(data.getDesc())) {
            desc.setVisibility(View.INVISIBLE);
        } else {
            desc.setVisibility(View.VISIBLE);
            desc.setText(data.getDesc());
        }
    }

    @Override
    protected int getItemLayoutId() {
        return R.layout.listview_item_payment_detail;
    }

    @Override
    protected void onUpdateEmptyView() {
        super.onUpdateEmptyView();
        EmptyView emptyView = getEmptyView();
        if (null != emptyView) {
            emptyView.setBackgroundColor(getResources().getColor(R.color.uikit_grey));
            emptyView.setImageViewResourceIdOfTop(R.drawable.app_ic_empty_money_history);
            emptyView.getTitle().setVisibility(View.GONE);
            emptyView.getSecondTitle().setText("暂无记录");
        }
    }
}
