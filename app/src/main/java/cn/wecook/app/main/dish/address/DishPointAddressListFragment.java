package cn.wecook.app.main.dish.address;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.property.SharePreferenceProperties;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.AddressApi;
import com.wecook.sdk.api.legacy.UserApi;
import com.wecook.sdk.api.model.PointAddress;
import com.wecook.uikit.adapter.BaseUIAdapter;
import com.wecook.uikit.fragment.ApiModelListFragment;
import com.wecook.uikit.widget.pulltorefresh.PullToRefreshBase;

import java.util.List;

import cn.wecook.app.R;
import cn.wecook.app.features.webview.WebViewActivity;
import cn.wecook.app.features.webview.WebViewFragment;

/**
 * 订单自提点地址列表
 *
 * @author Likuo
 * @version v1.0
 * @since 2015-15/7/27
 */
public class DishPointAddressListFragment extends ApiModelListFragment<PointAddress> {
    public static final String PARAMTS_ADDRESS_ID = "address_id";
    public static final String PARAM_DEFAULT_POINT_ADDRESS = "default_point_address";
    public static final String PARAM_DEFAULT_POINT_ADDRESS_ID = "default_point_address_id";

    private String addressId;
    private PointAddress mDefaultPointAddress;

    @Override
    protected View updRootView(View view) {
        return LayoutInflater.from(getContext()).inflate(R.layout.fragment_address_points_list, null);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle bundle = getArguments();
        if (null != bundle) {
            addressId = bundle.getString(PARAMTS_ADDRESS_ID);
            mDefaultPointAddress = (PointAddress) bundle.getSerializable(PARAM_DEFAULT_POINT_ADDRESS);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getRefreshListLayout().setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        if (null != getTitleBar()) {
            getTitleBar().setBackgroundColor(getResources().getColor(R.color.uikit_white));
            setTitle("选择自提点");
        }
        //不使用自提点
        view.findViewById(R.id.point_address_list_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDefaultPointAddress = null;
                Bundle bundle = new Bundle();
                bundle.putSerializable(PARAM_DEFAULT_POINT_ADDRESS, mDefaultPointAddress);
                SharePreferenceProperties.set(PARAM_DEFAULT_POINT_ADDRESS_ID, "null");
                back(bundle);
            }
        });
    }


    @Override
    protected void requestList(int page, int pageSize, ApiCallback<ApiModelList<PointAddress>> callback) {
        AddressApi.getNearbyStroresList(addressId, callback);
    }

    @Override
    protected int getItemLayoutId() {
        return R.layout.listview_item_address_point;
    }

    @Override
    protected void updateItemView(View view, int position, int viewType, final PointAddress data, Bundle extra) {
        super.updateItemView(view, position, viewType, data, extra);
        View selector = view.findViewById(R.id.app_point_address_selector);
        final TextView textAgreement = (TextView) view.findViewById(R.id.app_point_address_agreement);
        final TextView name = (TextView) view.findViewById(R.id.app_item_point_address_title);
        final TextView subName = (TextView) view.findViewById(R.id.app_item_point_address_subtitle);
        final TextView time = (TextView) view.findViewById(R.id.app_item_point_address_time);
        final TextView phone = (TextView) view.findViewById(R.id.app_item_point_address_phone);
        final TextView distance = (TextView) view.findViewById(R.id.app_item_point_address_distance);
        final TextView distanceUnit = (TextView) view.findViewById(R.id.app_item_point_address_distance_unit);
        if (getListData().size() == (position + 1)) {
            //协议
            SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
            Spannable serverSpan1 = StringUtils.getSpanClickable(getContext(),
                    R.string.app_collecting_server, new ClickableSpan() {
                        @Override
                        public void onClick(View widget) {
                            Bundle bundle = new Bundle();
                            bundle.putString(WebViewFragment.EXTRA_URL, UserApi.ZTDDSXY_PATH);
                            bundle.putString(WebViewFragment.EXTRA_TITLE, "代收协议");
                            startActivity(new Intent(getContext(), WebViewActivity.class), bundle);
                        }
                    }, R.color.uikit_999
            );
            stringBuilder.append("*使用代收服务视为已同意 ");
            stringBuilder.append(serverSpan1);
            textAgreement.setText(stringBuilder);
            UIHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    textAgreement.setMovementMethod(LinkMovementMethod.getInstance());
                }
            }, 400);
            textAgreement.setVisibility(View.VISIBLE);
        } else {
            textAgreement.setVisibility(View.GONE);
        }
        //按钮状态
        data.setSelected(null != mDefaultPointAddress && data.getId().equals(mDefaultPointAddress.getId()));
        selector.setSelected(data.isSelected());
        //全名
        name.setText(data.getFullName());
        //营业时间
        time.setText(data.getBusinessHour());
        //联系电话
        phone.setText(data.getTel());
        //位置
        subName.setText(data.getStreet());
        //距离
        distance.setText(data.getDistance());
        //单位
        distanceUnit.setText(data.getUnit());
        view.findViewById(R.id.app_point_address_selector_item).
                setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDefaultPointAddress = data;
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(PARAM_DEFAULT_POINT_ADDRESS, mDefaultPointAddress);
                        back(bundle);
                    }
                });
    }

    @Override
    protected List onLoadMoreList(BaseUIAdapter adapter, int currentPage, int pageSize) {
        return null;
    }
}
