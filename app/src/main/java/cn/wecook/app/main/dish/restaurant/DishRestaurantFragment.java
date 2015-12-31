package cn.wecook.app.main.dish.restaurant;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.modules.location.LocationServer;
import com.wecook.common.utils.ScreenUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.AddressApi;
import com.wecook.sdk.api.legacy.RestaurantApi;
import com.wecook.sdk.api.legacy.SelectCityApi;
import com.wecook.sdk.api.model.Address;
import com.wecook.sdk.api.model.CheckCityByLonLat;
import com.wecook.sdk.api.model.Dish;
import com.wecook.sdk.api.model.Location;
import com.wecook.sdk.api.model.Restaurant;
import com.wecook.sdk.policy.DishPolicy;
import com.wecook.sdk.policy.LogGather;
import com.wecook.uikit.fragment.BaseFragment;
import com.wecook.uikit.view.BaseView;
import com.wecook.uikit.widget.GradientView;
import com.wecook.uikit.widget.pulltorefresh.PullToRefreshBase;

import java.util.List;

import cn.wecook.app.R;
import cn.wecook.app.dialog.LoadingDialog;
import cn.wecook.app.fragment.shopcard.ApiModeListShowCardFragment;
import cn.wecook.app.main.dish.DishActivity;
import cn.wecook.app.main.dish.address.DishAddressSettingFragment;

/**
 * 特色餐厅列表
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/6/16
 */
public class DishRestaurantFragment extends ApiModeListShowCardFragment<Restaurant> {
    /**
     * 更改地址
     */
    public static final String ACTION_UPD_ADDRESS = "cn.wecook.app_adress_upd";


    private RestaurantAddressView mAddressView;
    private LinearLayout titleLayout;
    private TextView title, subTitle;
    private Address mDefaultAddress;
    //    private ViewGroup mAddressHolder;
    private LoadingDialog dialog;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setUseShopCartPolice(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        dialog = new LoadingDialog(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_restaurant_list, null);
        titleLayout = (LinearLayout) view.findViewById(R.id.restaurant_list_title_layout);
        title = (TextView) view.findViewById(R.id.restaurant_list_title);
        subTitle = (TextView) view.findViewById(R.id.restaurant_list_sub_title);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String titleString = getTitle();
        if (null == titleString || titleString.equals("")) {
            title.setText("特色餐厅");
        } else {
            title.setText(titleString);
        }
        setTitle("");
        mAddressView = new RestaurantAddressView(this);
        mAddressView.loadLayout(R.layout.view_supplier_address, null, false);
        getRefreshListLayout().setMode(PullToRefreshBase.Mode.BOTH);
    }

    @Override
    public void onStartUILoad() {
        showLoading();
        if (!dialog.isShowing()) {
            dialog.show();
        }
        //获取默认地址
        mDefaultAddress = null;
        mDefaultAddress = DishPolicy.get().getDishAddress();
        if (mDefaultAddress == null) {
            //请求收菜地址
            AddressApi.getAddressList(new ApiCallback<ApiModelList<Address>>() {
                @Override
                public void onResult(ApiModelList<Address> result) {
                    if (result.available()) {
                        List<Address> addressList = result.getList();
                        //判断是否含有默认地址
                        if (addressList != null) {
                            for (Address address : addressList) {
                                if (address.isDefault()) {
                                    mDefaultAddress = address;
                                    DishPolicy.get().saveDishAddress(mDefaultAddress);
                                    break;
                                }
                            }
                        }
                    }
                    onFinishGetAddress();
                }
            });
        } else {
            onFinishGetAddress();
        }

    }

    /**
     * 完成获取地址
     */
    private void onFinishGetAddress() {
        boolean hasLocation = !(null != mDefaultAddress && null != mDefaultAddress.getCity() && "北京".equals(mDefaultAddress.getCity())
                && (null == mDefaultAddress.getStreet() || "".equals(mDefaultAddress.getStreet())) && null == mDefaultAddress.getId());//是否未定位
        if (mDefaultAddress != null && hasLocation) {
            //判断地址有效性
            String indexCity = LocationServer.asInstance().getIndexCity();
            SelectCityApi.getCheckCityByLonLat(mDefaultAddress.getLon(), mDefaultAddress.getLat(), indexCity, new ApiCallback<CheckCityByLonLat>() {
                @Override
                public void onResult(CheckCityByLonLat result) {
                    if (result.getStatusState() == 1) {//地址验证通过
                        updateSubTitle();
                        if (null != subTitle && "设置地址".equals(subTitle.getText().toString().trim())) {
                            //显示空，引导用户设置地址
                            showEmptyView();
                            hideLoading();
                            UIHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    next(DishAddressSettingFragment.class);
                                }
                            }, 10);
                        }
                        //请求餐厅列表
                        DishRestaurantFragment.super.onStartUILoad();
                    } else {
                        //显示空，引导用户设置地址
                        showEmptyView();
                        hideLoading();
                        UIHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                next(DishAddressSettingFragment.class);
                            }
                        }, 10);
                    }
                }
            });
        } else {
            //显示空，引导用户设置地址
            showEmptyView();
            hideLoading();
            UIHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    next(DishAddressSettingFragment.class);
                }
            }, 100);
        }
    }

    /**
     * 更新副标题
     */
    private void updateSubTitle() {
        if (mDefaultAddress == null) {
            subTitle.setText("设置地址");
        } else {
            String address = mDefaultAddress.getAddress();
            if (!StringUtils.isEmpty(address)) {
                if (address.length() > 18) {
                    subTitle.setText(address.substring(0, 17));
                } else {
                    subTitle.setText(address);
                }
            } else {
                subTitle.setText("设置地址");
            }
        }

        titleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogGather.onEventDishRestaurantListSetAddress();
                //跳转到设置地址
                next(DishAddressSettingFragment.class, "设置收菜地址");
            }
        });
    }

    @Override
    protected void onUpdateEmptyView() {
        super.onUpdateEmptyView();
        if (mDefaultAddress == null) {
            getEmptyView().setImageViewResourceIdOfTop(R.drawable.app_pic_empty_cookshow);
            getEmptyView().setTitle("特色餐厅");
            getEmptyView().getSecondTitle().setSingleLine(false);
            getEmptyView().setSecondTitle(getString(R.string.app_restaurant_list_empty_tip));
        } else {
            getEmptyView().setImageViewResourceIdOfTop(R.drawable.app_pic_empty_cookshow);
            getEmptyView().setTitle("收菜地址附近没有供货餐厅");
            getEmptyView().setSecondTitle("");
        }
    }

    @Override
    protected void requestList(int page, int pageSize, ApiCallback<ApiModelList<Restaurant>> callback) {
        Address address = DishPolicy.get().getDishAddress();
        String lat = address.getLocation().getLatitude();
        String lon = address.getLocation().getLongitude();
        RestaurantApi.getDishRestaurantList(lat, lon, page, pageSize, callback);
    }

    @Override
    protected int getItemLayoutId() {
        return R.layout.listview_item_restaurant_with_food;
    }

    @Override
    protected void onNewView(int viewType, View view) {
        super.onNewView(viewType, view);
        int itemViewWidth = ScreenUtils.getScreenWidthInt();
        ScreenUtils.resizeView(view.findViewById(R.id.app_item_resaurant_food_img), itemViewWidth, 9 / 16f);
        view.findViewById(R.id.list_item_restaurant_with_food_layout_food).setVisibility(View.GONE);
        view.findViewById(R.id.list_item_restaurant_with_food_layout_restaurant).setVisibility(View.VISIBLE);
    }

    @Override
    public void updateItemView(View container, int position, int viewType, final Restaurant data, Bundle extra) {
        super.updateItemView(container, position, viewType, data, extra);
        if (data != null) {

            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), DishActivity.class);
                    intent.putExtra(DishActivity.EXTRA_PAGE, DishActivity.PAGE_RESTAURANT_DETAIL);
                    intent.putExtra(DishRestaurantDetailFragment.EXTRA_RESTAURANT_ID, data.getId());
                    startActivity(intent);
                }
            });
            ImageView cover = (ImageView) container.findViewById(R.id.app_item_resaurant_food_img);
            ImageView restaurantIcon = (ImageView) container.findViewById(R.id.list_item_restaurant_big_2_icon);
            TextView restaurantName = (TextView) container.findViewById(R.id.list_item_restaurant_big_2_name);
            ImageView restaurantTag1 = (ImageView) container.findViewById(R.id.list_item_restaurant_big_2_tag1);
            ImageView restaurantTag2 = (ImageView) container.findViewById(R.id.list_item_restaurant_big_2_tag2);
            ImageView restaurantTag3 = (ImageView) container.findViewById(R.id.list_item_restaurant_big_2_tag3);
            TextView restaurantGrade = (TextView) container.findViewById(R.id.list_item_restaurant_big_2_grade);
            TextView restaurantSale = (TextView) container.findViewById(R.id.list_item_restaurant_big_2_sale);
            TextView restaurantExpressText = (TextView) container.findViewById(R.id.list_item_restaurant_big_2_express_type_txt);
            final GradientView gradientView = (GradientView) container.findViewById(R.id.item_resaurant_food_bgview);//蒙版
            //处理白线
            container.findViewById(R.id.line).setVisibility((position == 0) ? View.GONE : View.VISIBLE);
            if (null != data.getDishes() && data.getDishes().size() > 0) {
                Dish dish = data.getDishes().get(0);
                //设置图片
                gradientView.setVisibility(View.GONE);
                String img_url = dish.getImageBig();
                if (null == img_url || "".equals(img_url)) {
                    img_url = dish.getImage();
                }
                ImageFetcher.asInstance().load(img_url, cover, new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String s, Target<GlideDrawable> target, boolean b) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable glideDrawable, String s, Target<GlideDrawable> target, boolean b, boolean b1) {
                        gradientView.setVisibility(View.VISIBLE);
                        return false;
                    }
                });
            }
            //餐厅图标
            ImageFetcher.asInstance().load(data.getImage(), restaurantIcon);
            //餐厅名称
            restaurantName.setText(data.getName());
            //餐厅Tag1
            if (data.getPerferentials().size() > 0)
                Glide.with(getContext())
                        .load(data.getPerferentials().get(0).getIcon())
                        .crossFade()
                        .into(restaurantTag1);
            //餐厅Tag2
            if (data.getPerferentials().size() > 1)
                Glide.with(getContext())
                        .load(data.getPerferentials().get(1).getIcon())
                        .crossFade()
                        .into(restaurantTag2);
            //餐厅Tag3
            if (data.getPerferentials().size() > 2)
                Glide.with(getContext())
                        .load(data.getPerferentials().get(2).getIcon())
                        .crossFade()
                        .into(restaurantTag3);
            //餐厅评分
            restaurantGrade.setText("评分 " + data.getGrade());
            //餐厅月售
            restaurantSale.setText("月售 " + data.getSale());
            //餐厅送餐类型
            restaurantExpressText.setText(data.getExpress_type_txt());
        }
    }


    @Override
    public void onCardIn(Bundle data) {
        super.onCardIn(data);
        if (data != null) {
            if (data.containsKey("address") && data.containsKey("city") && data.containsKey("street")) {
                if (null != subTitle && subTitle.getText().toString().trim().equals("设置地址") && "".equals(data.get("address")) && "".equals(data.get("city")) && "".equals(data.get("street"))) {
                    UIHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            back();
                        }
                    });
                    return;
                } else {
                    if (mAddressView != null) {
                        String city = data.getString("city");
                        String address = data.getString("address");
                        String street = data.getString("street");
                        String lat = data.getString("lat");
                        String lon = data.getString("lon");

                        if (StringUtils.isEmpty(address)
                                || StringUtils.isEmpty(lat)
                                || StringUtils.isEmpty(lon)) {
                            return;
                        }
                        Address updateAddress = new Address();
                        updateAddress.setStreet(street);
                        updateAddress.setCity(city);
                        Location location = new Location();
                        location.setLatitude(lat);
                        location.setLongitude(lon);
                        updateAddress.setLocation(location);
                        DishPolicy.get().saveDishAddress(updateAddress);


                        hideEmptyView();
                        mDefaultAddress = updateAddress;
                        updateSubTitle();
                        //请求餐厅列表
                        finishAllLoaded(false);
                        DishRestaurantFragment.super.onStartUILoad();

                        //发送广播
                        Intent intent = new Intent();
                        intent.setAction(ACTION_UPD_ADDRESS);
                        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
                    }
                }
            }
        }
    }

    @Override
    public void hideLoading() {
        super.hideLoading();
        dialog.dismiss();
    }

    /**
     * 界面地址的显示视图
     */
    private class RestaurantAddressView extends BaseView<Address> {

        private TextView mAddressLabel;
        private TextView mAddressAction;
        private TextView mAddress;
        private Address mCurrentAddress;

        public RestaurantAddressView(BaseFragment fragment) {
            super(fragment);
        }

        @Override
        protected void onFinishInflate() {
            super.onFinishInflate();
            mAddressLabel = (TextView) findViewById(R.id.app_dish_address_label);
            mAddress = (TextView) findViewById(R.id.app_dish_address);
            mAddressAction = (TextView) findViewById(R.id.app_dish_address_action);

            setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    LogGather.onEventDishRestaurantListSetAddress();
                    //跳转到设置地址
                    next(DishAddressSettingFragment.class, "设置收菜地址");
                }
            });
        }

        @Override
        public void updateView(Address obj) {
            super.updateView(obj);
            mCurrentAddress = obj;
            if (mCurrentAddress == null ||
                    StringUtils.isEmpty(mCurrentAddress.getCity() + mCurrentAddress.getStreet())) {
                //设置地址
                mAddressLabel.setVisibility(GONE);
                mAddressAction.setText("");
                mAddress.setText("设置收菜地址");
            } else {
                mAddressLabel.setVisibility(VISIBLE);
                mAddressAction.setText("更改");
                mAddress.setText(mCurrentAddress.getCity() + mCurrentAddress.getStreet());
            }
        }
    }
}
