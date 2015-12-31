package cn.wecook.app.main.dish.address;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.core.internet.ApiResult;
import com.wecook.common.modules.asynchandler.AsyncUIHandler;
import com.wecook.common.modules.location.LocationServer;
import com.wecook.common.utils.KeyboardUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.AddressApi;
import com.wecook.sdk.api.legacy.LocationApi;
import com.wecook.sdk.api.legacy.SelectCityApi;
import com.wecook.sdk.api.model.Address;
import com.wecook.sdk.api.model.AddressSuggestion;
import com.wecook.sdk.api.model.CheckCityByLonLat;
import com.wecook.sdk.policy.LogGather;
import com.wecook.uikit.adapter.UIAdapter;
import com.wecook.uikit.alarm.ToastAlarm;
import com.wecook.uikit.fragment.BaseListFragment;

import java.util.ArrayList;
import java.util.List;

import cn.wecook.app.R;
import cn.wecook.app.dialog.ConfirmDialog;
import cn.wecook.app.dialog.LoadingDialog;

/**
 * 设置地址界面
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/6/24
 */
public class DishAddressSettingFragment extends BaseListFragment {

    public static final String EXTRA_IS_SHOW_COMMON_ADDRESS = "extra_is_show_common_address";

    private ListView mListView;
    private View mListSuggestionView;
    private EditText mInput;
    private View mInputClear;
    private View mAutoLocationView;
    private ViewGroup mAddressGroup;
    private ViewGroup mAddressGroupInner;

    private List<AddressSuggestion> mListData;
    private SuggestionAdapter mAdapter;
    //显示常用地址
    private boolean mIsShowCommonAddress = true;

    private String mCurrentKey;
    private ApiResult mSuggestApiResult;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mIsShowCommonAddress = bundle.getBoolean(EXTRA_IS_SHOW_COMMON_ADDRESS, true);
        }
    }

    @Override
    public void onCardIn(Bundle data) {
        super.onCardIn(data);
        setFixed(true);
    }

    @Override
    public boolean back(Bundle data) {
        setFixed(false);
        if (null == data) {
            data = new Bundle();
            data.putString("city", "");
            data.putString("address", "");
            data.putString("street", "");
            //标识未选择地理位置
            data.putBoolean("noSelect", true);
        }
        return super.back(data);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return LayoutInflater.from(getContext()).inflate(R.layout.fragment_address_setting, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setFixed(true);
        getTitleBar().setBackgroundColor(getResources().getColor(R.color.uikit_white));
        setTitle("设置收菜地址");
        mListView = getListView();
        mListSuggestionView = view.findViewById(R.id.app_address_suggestion_list);
        mListData = new ArrayList<>();
        mAdapter = new SuggestionAdapter(getContext(), mListData);
        mInput = (EditText) view.findViewById(R.id.app_address_input);
        mInputClear = view.findViewById(R.id.app_address_input_clear);
        mAutoLocationView = view.findViewById(R.id.app_address_auto_location);
        mAddressGroup = (ViewGroup) view.findViewById(R.id.app_address_group);
        mAddressGroupInner = (ViewGroup) view.findViewById(R.id.app_address_group_inner);

        mInput.setText("");
        mInput.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        mInput.setHint("例如：苹果社区");
        mInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    //关闭搜索框
                    KeyboardUtils.closeKeyboard(getContext(), mInput);
                    return true;
                }
                return false;
            }
        });
        mInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    KeyboardUtils.openKeyboard(getContext(), mInput);
                } else {
                    KeyboardUtils.closeKeyboard(getContext(), mInput);
                }
            }
        });
        mInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    checkAndRequestSuggestion();
                    mInputClear.setVisibility(View.VISIBLE);
                    mListSuggestionView.setVisibility(View.VISIBLE);
                } else {
                    mInputClear.setVisibility(View.GONE);
                    mListSuggestionView.setVisibility(View.GONE);
                }
            }
        });

        mInputClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInput.setText("");
                mInputClear.setVisibility(View.GONE);
            }
        });

        mAutoLocationView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogGather.onEventDishAddressSettingAutoLocation();
                final LoadingDialog loadingDialog = new LoadingDialog(getContext());
                loadingDialog.show();
                LocationServer.asInstance().location(new LocationServer.OnLocationResultListener() {
                    @Override
                    public void onLocationResult(boolean success, final BDLocation location) {
                        loadingDialog.dismiss();
                        if (success) {
                            //验证地址有效性
                            String indexCity = LocationServer.asInstance().getIndexCity();
                            SelectCityApi.getCheckCityByLonLat(LocationServer.asInstance().getLon() + "", LocationServer.asInstance().getLat() + "", indexCity, new ApiCallback<CheckCityByLonLat>() {
                                @Override
                                public void onResult(CheckCityByLonLat result) {
                                    if (result.getStatusState() == 1) {//地址验证通过
                                        Bundle bundle = new Bundle();
                                        bundle.putString("city", location.getCity());
                                        bundle.putString("address", LocationServer.asInstance().getLocationAddress());
                                        if (!StringUtils.isEmpty(LocationServer.asInstance().getLocationAddressDesc())) {
                                            bundle.putString("street", LocationServer.asInstance().getLocationStreet()
                                                    + "(" + LocationServer.asInstance().getLocationAddressDesc() + ")");
                                        } else {
                                            bundle.putString("street", LocationServer.asInstance().getLocationStreet());
                                        }
                                        bundle.putString("lat", LocationServer.asInstance().getLat() + "");
                                        bundle.putString("lon", LocationServer.asInstance().getLon() + "");
                                        back(bundle);
                                    } else {
                                        ToastAlarm.show("当前城市不在服务范围内");
                                    }
                                }
                            });
                        } else {
                            if (getContext() != null) {
                                ConfirmDialog dialog = new ConfirmDialog(getContext(), "");
                                dialog.setTitle("定位服务未授权");
                                dialog.setMessage("请在设置>定位权限中设置,\r\n允许 味库 访问你的地理位置");
                                dialog.show();
                            }
                        }
                    }
                });
            }
        });
    }

    /**
     * 地址建议
     */
    private void checkAndRequestSuggestion() {
        if (mInput.getText().toString().equals(mCurrentKey)) {
            return;
        }

        if (mSuggestApiResult != null) {
            mSuggestApiResult.cancel();
        }

        mCurrentKey = mInput.getText().toString();
        showLoading();
        mSuggestApiResult = LocationApi.getLocationSuggestion(mCurrentKey, null,
                new ApiCallback<ApiModelList<AddressSuggestion>>() {
                    @Override
                    public void onResult(ApiModelList<AddressSuggestion> result) {
                        mListData.clear();
                        if (result.available()) {
                            mListData.addAll(result.getList());
                        }
                        if (mListView.getAdapter() == null) {
                            mListView.setAdapter(mAdapter);
                            mAdapter.notifyDataSetChanged();
                        } else {
                            mAdapter.notifyDataSetChanged();
                        }

                        hideLoading();
                    }
                });
    }

    @Override
    public void onStartUILoad() {
        super.onStartUILoad();

        if (mIsShowCommonAddress) {
            showLoading();
            //获得用户地址列表
            AddressApi.getAddressList(new ApiCallback<ApiModelList<Address>>() {
                @Override
                public void onResult(ApiModelList<Address> result) {
                    if (result.available() && !result.isEmpty()) {
                        mAddressGroup.setVisibility(View.VISIBLE);
                        updateAddressGroup(result.getList());
                    } else {
                        mAddressGroup.setVisibility(View.GONE);
                    }
                    hideLoading();
                }
            });
        }
    }

    /**
     * 创建并更新常用地址
     *
     * @param list
     */
    private void updateAddressGroup(final List<Address> list) {
        if (list != null) {
            AsyncUIHandler.post(new AsyncUIHandler.AsyncJob() {
                View[] items;

                @Override
                public void run() {
                    items = new View[list.size()];
                    for (int i = 0; i < list.size(); i++) {
                        final Address address = list.get(i);
                        items[i] = LayoutInflater.from(getContext()).inflate(R.layout.view_address_item, null);
                        TextView name = (TextView) items[i].findViewById(R.id.app_address_name);
                        TextView subName = (TextView) items[i].findViewById(R.id.app_address_sub_name);
                        TextView phone = (TextView) items[i].findViewById(R.id.app_address_phone);
                        name.setText(address.getName());
                        subName.setText(address.getCity() + address.getStreet());
                        phone.setText(address.getTel());

                        items[i].setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Bundle bundle = new Bundle();
                                bundle.putString("city", address.getCity());
                                bundle.putString("address", address.getStreet());
                                bundle.putString("street", address.getStreet());
                                if (address.getLocation() != null) {
                                    bundle.putString("lat", address.getLocation().getLatitude());
                                    bundle.putString("lon", address.getLocation().getLongitude());
                                }
                                back(bundle);
                            }
                        });
                    }
                }

                @Override
                public void postUi() {
                    super.postUi();
                    for (int i = 0; i < list.size(); i++) {
                        mAddressGroupInner.addView(items[i]);
                    }
                }
            });
        }
    }

    /**
     * 搜索建议
     */
    private class SuggestionAdapter extends UIAdapter<AddressSuggestion> {

        public SuggestionAdapter(Context context, List<AddressSuggestion> data) {
            super(context, R.layout.listview_item_dish_address_suggestion, data);
        }

        @Override
        public void updateView(final int position, int viewType, final AddressSuggestion data, Bundle extra) {
            super.updateView(position, viewType, data, extra);
            TextView name = (TextView) findViewById(R.id.app_search_suggestion_name);
            TextView subName = (TextView) findViewById(R.id.app_search_suggestion_sub_name);
            name.setText(data.getName());
            subName.setText(data.getAddress());
            getItemView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LogGather.onEventDishAddressSettingSearch();
                    Bundle bundle = new Bundle();
                    bundle.putString("city", "北京");
                    bundle.putString("address", data.getName());
                    bundle.putString("street", data.getName());
                    if (data.getLocation() != null) {
                        bundle.putString("lat", data.getLocation().getLatitude());
                        bundle.putString("lon", data.getLocation().getLongitude());
                    }
                    back(bundle);
                }
            });
        }
    }


}
