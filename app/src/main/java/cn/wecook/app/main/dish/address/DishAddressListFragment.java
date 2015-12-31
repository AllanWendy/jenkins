package cn.wecook.app.main.dish.address;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.utils.ListUtils;
import com.wecook.sdk.api.legacy.AddressApi;
import com.wecook.sdk.api.model.Address;
import com.wecook.sdk.api.model.State;
import com.wecook.sdk.policy.DishPolicy;
import com.wecook.uikit.fragment.ApiModelListFragment;

import cn.wecook.app.R;
import cn.wecook.app.dialog.ConfirmDialog;
import cn.wecook.app.dialog.LoadingDialog;

/**
 */
public class DishAddressListFragment extends ApiModelListFragment<Address> {

    public static final String EXTRA_ADDRESS = "extra_address";
    public static final String EXTRA_FOR_SHOW = "extra_for_show";
    public static final String EXTRA_AUTO_TO_EDIT = "extra_auto_to_edit";
    public static final String EXTRA_FROM = "from";

    private Address mSelectAddress;
    private LoadingDialog mLoadingDialog;
    private boolean mIsForShow;
    private boolean mAutoToEdit;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setTitle("收菜地址");
        Bundle bundle = getArguments();
        if (bundle != null) {
            mSelectAddress = (Address) bundle.getSerializable(EXTRA_ADDRESS);
            mIsForShow = bundle.getBoolean(EXTRA_FOR_SHOW);
            if (bundle.containsKey(EXTRA_AUTO_TO_EDIT)) {
                mAutoToEdit = bundle.getBoolean(EXTRA_AUTO_TO_EDIT);
            }
        }
    }

    @Override
    public void onCardIn(Bundle data) {
        super.onCardIn(data);
        if (data != null) {
            //有地址变动
            if (data.get(DishAddressEditFragment.EXTRA_HAS_UPD_ADDRESS) != null) {
                finishAllLoaded(false);
                onStartUILoad();
                //自动加载&包含来源参数
            } else if (mAutoToEdit && data.get(EXTRA_FROM) != null) {
                String from = data.getString(EXTRA_FROM);
                if (from.equals("DishAddressEdit")) {
                    UIHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            boolean isEmpty = ListUtils.isEmpty(getListData());
                            if (isEmpty) {
                                back();
                            } else {
                                boolean noDate = true;
                                for (Address address : getListData()) {
                                    if (null != address) {
                                        noDate = false;
                                        break;
                                    }
                                }
                                if (noDate) {
                                    back();
                                }
                            }
                        }
                    });
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup
            container, @Nullable Bundle savedInstanceState) {
        mLoadingDialog = new LoadingDialog(getContext());
        return inflater.inflate(R.layout.fragment_address_list, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.app_address_list_new).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //新增收菜地址
                next(DishAddressEditFragment.class, "新增收菜地址");
            }
        });
    }

    /**
     * 检查地址有效性
     *
     * @param checkAddress 待验证地址
     */

    private void checkAddress(final Address checkAddress) {
        if (checkAddress != null && !mIsForShow) {
            if (checkAddress.getLocation() == null
                    || !checkAddress.getLocation().effective()) {
                ConfirmDialog confirmDialog = new ConfirmDialog(getContext(), "");
                confirmDialog.setConfirm(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Address address = checkAddress.clone();
                        address.setStreet("");
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(DishAddressEditFragment.EXTRA_ADDRESS, address);
                        bundle.putString(DishAddressEditFragment.EXTRA_TITLE, "编辑收菜地址");
                        next(DishAddressEditFragment.class, bundle);
                    }
                });
                confirmDialog.show();
                return;
            }
            mLoadingDialog.show();
            //检查地址是否再有效配送范围内
            AddressApi.checkAddressRange(checkAddress.getId(),
                    DishPolicy.get().getCheckoutRestaurantListIds(),
                    new ApiCallback<State>() {
                        @Override
                        public void onResult(State result) {
                            if (result.available()) {
                                checkAddress.setAvailable(true);
                                //设置为默认收菜地址
                                AddressApi.setDefault(checkAddress.getId(), new ApiCallback<State>() {
                                    @Override
                                    public void onResult(State result) {
                                        if (result.available()) {
                                            //处理地址状态
                                            checkAddress.setSelected(true);
                                            for (Address address : getListData()) {
                                                if (!checkAddress.getId().equals(address.getId())) {
                                                    address.setSelected(false);
                                                }
                                            }
                                            mSelectAddress = checkAddress;
                                            //返回
                                            Bundle bundle = new Bundle();
                                            bundle.putSerializable(EXTRA_ADDRESS, mSelectAddress);
                                            finishFragment(bundle);
                                        }
                                    }
                                });
                            } else {
                                checkAddress.setAvailable(false);
                                ConfirmDialog confirmDialog = new ConfirmDialog(getContext(), result.getErrorMsg());
                                confirmDialog.setConfirm("去修改", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Address address = checkAddress.clone();
                                        address.setStreet("");
                                        Bundle bundle = new Bundle();
                                        bundle.putSerializable(DishAddressEditFragment.EXTRA_ADDRESS, address);
                                        bundle.putString(DishAddressEditFragment.EXTRA_TITLE, "编辑收菜地址");
                                        next(DishAddressEditFragment.class, bundle);
                                    }
                                });
                                confirmDialog.show();
                            }
                            mLoadingDialog.dismiss();
                        }
                    });

        }
    }

    @Override
    protected void requestList(int page, int pageSize, ApiCallback<ApiModelList<Address>> callback) {
        AddressApi.getAddressList(page, pageSize, callback);
    }

    @Override
    protected void onListDataResult(ApiModelList<Address> result) {
        super.onListDataResult(result);
        //地址列表为空，默认跳转到编辑地址
        if (null != result && null != result.getList() && result.getList().size() == 0 && mAutoToEdit) {
            UIHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    getListData().clear();
                    next(DishAddressEditFragment.class, "新增收菜地址");
                }
            }, 250);
        }
        if (mSelectAddress != null) {
            //判断列表是否含有地址
            boolean hasSelectAddress = false;
            for (int i = 0; i < result.getCountOfList(); i++) {
                Address address = result.getItem(i);
                if (address.getId().equals(mSelectAddress.getId())) {
                    address.setSelected(mSelectAddress.isSelected());
                    hasSelectAddress = true;
                    break;
                }
            }
            if (!hasSelectAddress) {
                mSelectAddress = null;
            }
        }
    }

    @Override
    protected void updateItemView(View view, int position, int viewType, final Address data, Bundle extra) {
        super.updateItemView(view, position, viewType, data, extra);

        View containerView = view.findViewById(R.id.app_address_selector_item);
        View selector = view.findViewById(R.id.app_address_selector);
        View selectorDiv = view.findViewById(R.id.app_address_selector_div);
        View div = view.findViewById(R.id.app_address_div);
        TextView userName = (TextView) view.findViewById(R.id.app_address_user_name);
        TextView userPhone = (TextView) view.findViewById(R.id.app_address_user_phone);
        TextView content = (TextView) view.findViewById(R.id.app_address_content);
        View edit = view.findViewById(R.id.app_address_edit);

        selectorDiv.setVisibility(mIsForShow ? View.GONE : View.VISIBLE);
        selector.setVisibility(mIsForShow ? View.GONE : View.VISIBLE);

        if (position == 0) {
            div.setVisibility(View.VISIBLE);
        } else {
            div.setVisibility(View.GONE);
        }

        selector.setSelected(data.isSelected());
        if (data.isSelected()) {
            mSelectAddress = data;
        }
        containerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (data.isSelected()) {
                    mSelectAddress = data;
                    back();
                    return;
                } else {
                    checkAddress(data);
                }
            }
        });
        userName.setText(data.getName());
        userPhone.setText(data.getTel());
        content.setText(data.getFullAddress());
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转到地址编辑
                Bundle bundle = new Bundle();
                bundle.putSerializable(DishAddressEditFragment.EXTRA_ADDRESS, data);
                bundle.putString(DishAddressEditFragment.EXTRA_TITLE, "编辑收菜地址");
                next(DishAddressEditFragment.class, bundle);
            }
        });
    }

    @Override
    protected int getItemLayoutId() {
        return R.layout.listview_item_address;
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (KeyEvent.KEYCODE_BACK == keyCode) {
//            if (mSelectAddress != null && getListData() != null && getListData().size() > 0 && !mIsForShow) {
//                checkAddress();
//                return true;
//            }
//        }
//        return super.onKeyDown(keyCode, event);
//    }

    @Override
    protected void onUpdateEmptyView() {
        super.onUpdateEmptyView();
        if (null != getEmptyView()) {
            getEmptyView().setTitle("");
        }
    }

    @Override
    public boolean back(Bundle data) {
        if (null == data) {
            data = new Bundle();
        }
        if (!data.containsKey(EXTRA_ADDRESS)) {
            data.putSerializable(EXTRA_ADDRESS, mSelectAddress);
        }
        return super.back(data);
    }
}
