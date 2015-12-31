package cn.wecook.app.main.dish.address;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.AddressApi;
import com.wecook.sdk.api.model.Address;
import com.wecook.sdk.api.model.Location;
import com.wecook.sdk.api.model.State;
import com.wecook.uikit.alarm.ToastAlarm;
import com.wecook.uikit.fragment.BaseTitleFragment;
import com.wecook.uikit.widget.TitleBar;

import cn.wecook.app.R;
import cn.wecook.app.dialog.ConfirmDialog;
import cn.wecook.app.dialog.LoadingDialog;

/**
 * 地址编辑页面
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/7/27
 */
public class DishAddressEditFragment extends BaseTitleFragment {

    public static String EXTRA_ADDRESS = "extra_address";
    public static String EXTRA_HAS_UPD_ADDRESS = "update_address";

    private Address mEditAddress;
    private EditText mEditName;
    private EditText mEditPhone;
    private TextView mEditCity;
    private TextView mEditStreet;
    private EditText mEditStreetDetail;

    private boolean mIsCreateNew;
    private LoadingDialog mLoadingDialog;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mIsCreateNew = false;
            mEditAddress = (Address) bundle.getSerializable(EXTRA_ADDRESS);
        }

        if (mEditAddress == null) {
            mIsCreateNew = true;
            mEditAddress = new Address();
        }

        mLoadingDialog = new LoadingDialog(getContext());
    }

    @Override
    protected View onCreateInnerView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_address_edit, container);
        mEditName = (EditText) view.findViewById(R.id.app_address_edit_name);
        mEditPhone = (EditText) view.findViewById(R.id.app_address_edit_phone);
        mEditCity = (TextView) view.findViewById(R.id.app_address_edit_city);
        mEditStreet = (TextView) view.findViewById(R.id.app_address_edit_street);
        mEditStreetDetail = (EditText) view.findViewById(R.id.app_address_edit_street_detail);

        mEditStreetDetail.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mEditStreetDetail.getWindowToken(), 0);
                    return true;
                } else {
                    return false;
                }
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final View remove = view.findViewById(R.id.app_address_edit_remove);

        mEditStreet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putBoolean(DishAddressSettingFragment.EXTRA_IS_SHOW_COMMON_ADDRESS, false);
                bundle.putString(DishAddressSettingFragment.EXTRA_TITLE, "搜索收菜地址");
                next(DishAddressSettingFragment.class, bundle);
            }
        });

        if (!mIsCreateNew) {
            remove.setVisibility(View.VISIBLE);
            remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ConfirmDialog confirmDialog = new ConfirmDialog(getContext(), "确定要删除该地址？");
                    confirmDialog.setConfirm(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mLoadingDialog.show();
                            //删除
                            AddressApi.deleteAddress(mEditAddress.getId(), new ApiCallback<State>() {
                                @Override
                                public void onResult(State result) {
                                    if (result.available()) {
                                        Bundle bundle = new Bundle();
                                        mEditAddress = null;
                                        bundle.putBoolean(EXTRA_HAS_UPD_ADDRESS, true);
                                        back(bundle);
                                    } else {
                                        ToastAlarm.show("删除失败");
                                    }

                                    mLoadingDialog.dismiss();
                                }
                            });
                        }
                    });
                    confirmDialog.setTitle("味小库提醒您");
                    confirmDialog.show();
                }
            });
        } else {
            remove.setVisibility(View.GONE);
        }

        TitleBar.ActionCoveredTextView finish = new TitleBar.ActionCoveredTextView(getContext(), "保存");
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAddress();

            }
        });
        getTitleBar().addActionView(finish);
    }

    private void saveAddress() {
        if (checkAddressInfo()) {
            mLoadingDialog.show();
            AddressApi.saveAddress(mEditAddress, new ApiCallback<State>() {
                @Override
                public void onResult(State result) {
                    if (result.available()) {
                        Bundle bundle = new Bundle();
                        bundle.putBoolean(EXTRA_HAS_UPD_ADDRESS, true);
                        back(bundle);
                    } else {
                        ToastAlarm.show("保存失败");
                    }
                    mLoadingDialog.dismiss();
                }
            });
        }
    }

    @Override
    public void onStartUILoad() {
        super.onStartUILoad();
        updateAddressToView();
    }

    protected void updateAddressToView() {
        if (mEditAddress != null) {
            mEditName.setText(mEditAddress.getName());
            mEditPhone.setText(mEditAddress.getTel());
            mEditCity.setText(mEditAddress.getCity());
            mEditStreet.setText(mEditAddress.getStreet());
            mEditStreetDetail.setText(mEditAddress.getAddon());
        }
    }

    public void updateViewToAddress() {
        String newName = mEditName.getText().toString();
        String newPhone = mEditPhone.getText().toString();
        String newCity = mEditCity.getText().toString();
        String newStreet = mEditStreet.getText().toString();
        String newStreetDetail = mEditStreetDetail.getText().toString();

        mEditAddress.setName(newName);
        mEditAddress.setTel(newPhone);
        mEditAddress.setCity(newCity);
        mEditAddress.setStreet(newStreet);
        mEditAddress.setAddon(newStreetDetail);

    }

    @Override
    public void onCardIn(Bundle data) {
        super.onCardIn(data);
        if (data != null && mEditAddress != null) {
            if (data.containsKey("noSelect")) {
                if (data.getBoolean("noSelect")) {
                    return;
                }
            }
            String city = data.getString("city");
            String street = data.getString("street");
            String lat = data.getString("lat");
            String lon = data.getString("lon");
            if (!StringUtils.isEmpty(city)) {
                mEditCity.setText(city);
            }

            mEditStreet.setText(street);

            Location location = new Location();
            location.setLatitude(lat);
            location.setLongitude(lon);
            mEditAddress.setLocation(location);
        }
    }

    private boolean checkAddressInfo() {
        if (mEditAddress == null) {
            return false;
        }

        updateViewToAddress();

        if (StringUtils.isEmpty(mEditAddress.getName())) {
            ToastAlarm.show("姓名不能为空");
            return false;
        }
        int length = mEditAddress.getName().length();
        if (length < 2 || length > 10) {
            ToastAlarm.show("收菜人字数应在2-10字之间");
            return false;
        }
        if (StringUtils.isEmpty(mEditAddress.getTel())) {
            ToastAlarm.show("手机号不能为空");
            return false;
        }

        if (StringUtils.isEmpty(mEditAddress.getTel())
                || !StringUtils.validatePhoneNumber(mEditAddress.getTel())) {
            ToastAlarm.show("手机号码有误，请再检查下");
            return false;
        }

        if (StringUtils.isEmpty(mEditAddress.getStreet())) {
            ToastAlarm.show("地址不能为空");
            return false;
        }
        if (StringUtils.isEmpty(mEditStreetDetail.getText().toString().trim())) {
            ToastAlarm.show("详细地址不能为空");
            return false;
        }

        return true;
    }

    @Override
    public boolean back(Bundle data) {
        if (null == data) {
            data = new Bundle();
        }
        data.putString(DishAddressListFragment.EXTRA_FROM, "DishAddressEdit");
        return super.back(data);
    }
}
