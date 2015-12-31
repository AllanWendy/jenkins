package cn.wecook.app.main.dish.shopcart;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.utils.ListUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.model.DeliveryDate;
import com.wecook.sdk.api.model.DeliveryRestaurant;
import com.wecook.sdk.api.model.DeliveryRestaurantList;
import com.wecook.sdk.api.model.DeliveryTime;
import com.wecook.sdk.api.model.ShopCartDish;
import com.wecook.sdk.api.model.ShopCartRestaurant;
import com.wecook.sdk.policy.DishPolicy;
import com.wecook.uikit.alarm.ToastAlarm;
import com.wecook.uikit.fragment.ApiModelListFragment;
import com.wecook.uikit.widget.pulltorefresh.PullToRefreshBase;

import java.util.List;

import cn.wecook.app.R;

/**
 * 多餐厅配送时间设置
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/7/28
 */
public class DishOrderMultiDeliveryDateFragment extends ApiModelListFragment<ShopCartRestaurant> {

    public static final String EXTRA_DELIVERY_DATE = "extra_delivery_date";

    private DeliveryRestaurantList deliveryRestaurantList;

    private TextView mNotice;
    private TextView mCommit;
    private String defaultFormatTime;
    private boolean showRed = false;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setTitle("配送方式");
        showRed = false;
        Bundle bundle = getArguments();
        if (bundle != null) {
            deliveryRestaurantList = (DeliveryRestaurantList) bundle.getSerializable(EXTRA_DELIVERY_DATE);
        }
    }

    @Override
    public void onCardIn(Bundle data) {
        super.onCardIn(data);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getRefreshListLayout().setMode(PullToRefreshBase.Mode.DISABLED);
        getTitleBar().setBackgroundColor(getResources().getColor(R.color.uikit_white));
    }

    @Override
    protected int getItemLayoutId() {
        return R.layout.listview_item_restaurant_delivery;
    }

    @Override
    protected void updateItemView(View view, int position, int viewType, final ShopCartRestaurant data, Bundle extra) {
        super.updateItemView(view, position, viewType, data, extra);
        TextView restaurantName = (TextView) view.findViewById(R.id.app_restaurant_name);
        TextView restaurantInfo = (TextView) view.findViewById(R.id.app_restaurant_info);
        TextView restaurantType = (TextView) view.findViewById(R.id.app_delivery_type);
        ImageView dish1 = (ImageView) view.findViewById(R.id.app_restaurant_dish_1);
        ImageView dish2 = (ImageView) view.findViewById(R.id.app_restaurant_dish_2);
        ImageView dish3 = (ImageView) view.findViewById(R.id.app_restaurant_dish_3);
        ImageView dish4 = (ImageView) view.findViewById(R.id.app_restaurant_dish_4);
        View dish4Layout = view.findViewById(R.id.app_restaurant_dish_4_layout);
        TextView deliveryTime = (TextView) view.findViewById(R.id.app_delivery_time);
        View bottomDiv = view.findViewById(R.id.app_delivery_bottom_div);

        bottomDiv.setVisibility(position == (getListData().size() - 1) ? View.VISIBLE : View.GONE);
        String restaurantId = data.getId();
        String expressType = null;//配送类型
        if (null != deliveryRestaurantList) {
            ApiModelList<DeliveryRestaurant> restaurants = deliveryRestaurantList.getDeliveryRestaurantList();
            if (null != restaurants) {
                List<DeliveryRestaurant> deliveryRestaurants = restaurants.getList();
                for (DeliveryRestaurant restaurant : deliveryRestaurants) {
                    if (null != restaurant) {
                        if (restaurant.getId().equals(restaurantId)) {
                            expressType = restaurant.getExpress_by();
                            if (expressType.equals(DeliveryRestaurant.EXPRESS_BY_RESTAURANT)) {
                                restaurantType.setText("商家配送");
                            } else if (expressType.equals(DeliveryRestaurant.EXPRESS_BY_WECOOK)) {
                                restaurantType.setText("味库配送");
                            }
                            break;
                        }
                    }
                }
            }
        }
        final String deliveryType = expressType;
        restaurantName.setText(data.getName());
        String priceText = StringUtils.getPriceText(data.getCheckoutTotalPrice()
                + data.getDeliveryPrice());
        restaurantInfo.setText(data.getCheckoutDishCount() + "道菜品，共计" + priceText);

        List<ShopCartDish> dishes = data.getCheckoutDishes();
        ShopCartDish dish1Data = ListUtils.getItem(dishes, 0);
        ShopCartDish dish2Data = ListUtils.getItem(dishes, 1);
        ShopCartDish dish3Data = ListUtils.getItem(dishes, 2);
        ShopCartDish dish4Data = ListUtils.getItem(dishes, 3);

        if (dish1Data != null) {
            dish1.setVisibility(View.VISIBLE);
            ImageFetcher.asInstance().load(dish1Data.getImage(), dish1);
        } else {
            dish1.setVisibility(View.INVISIBLE);
        }
        if (dish2Data != null) {
            dish2.setVisibility(View.VISIBLE);
            ImageFetcher.asInstance().load(dish2Data.getImage(), dish2);
        } else {
            dish2.setVisibility(View.INVISIBLE);
        }
        if (dish3Data != null) {
            dish3.setVisibility(View.VISIBLE);
            ImageFetcher.asInstance().load(dish3Data.getImage(), dish3);
        } else {
            dish3.setVisibility(View.INVISIBLE);
        }
        if (dish4Data != null) {
            dish4.setVisibility(View.VISIBLE);
            dish4Layout.setVisibility(View.VISIBLE);
            ImageFetcher.asInstance().load(dish4Data.getImage(), dish4);
        } else {
            dish4Layout.setVisibility(View.INVISIBLE);
            dish4.setVisibility(View.INVISIBLE);
        }
        //设置时间状态
        if (deliveryRestaurantList != null && deliveryRestaurantList.getDeliveryRestaurantList() != null) {
            List<DeliveryRestaurant> list = deliveryRestaurantList.getDeliveryRestaurantList().getList();
            for (DeliveryRestaurant deliveryRestaurant : list) {
                if (data.getId().equals(deliveryRestaurant.getId())) {
                    updTimeLayout(position, deliveryTime, deliveryRestaurant, data);
                    break;
                }
            }
        }

    }

    /**
     * 设置时间布局
     *
     * @param tv                 控件
     * @param deliveryRestaurant 餐厅对象
     * @param data               本页数据
     */
    private void updTimeLayout(int position, final TextView tv, final DeliveryRestaurant deliveryRestaurant, final ShopCartRestaurant data) {
        if (null != deliveryRestaurant.getDeliveryDateList() && null != deliveryRestaurant.getDeliveryDateList().getList()) {
            List<DeliveryDate> dates = deliveryRestaurant.getDeliveryDateList().getList();
            //处理可选时间为1的情况
            if (dates.size() > 1) {
                //处理默认显示
                if (null == data.getDeliveryTime()) {
                    for (int i = 0; i < dates.size(); i++) {
                        DeliveryTime time = dates.get(i).getDeliveryTimes().get(0);
                        if (time.getFormatTime().equals(defaultFormatTime)) {
                            data.setExpressType(deliveryRestaurant.getExpress_by());
                            data.setDeliveryTime(time);
                            tv.setText(defaultFormatTime);
                            break;
                        }
                    }
                } else {
                    tv.setText(data.getDeliveryTime().getFormatTime());
                }
                if (null == data.getDeliveryTime()) {
                    if (showRed) {
                        tv.setTextColor(getResources().getColor(R.color.uikit_red));
                        tv.setText("选择配送时间");
                    } else {
                        tv.setTextColor(getResources().getColor(R.color.uikit_333));
                    }
                } else {
                    tv.setTextColor(getResources().getColor(R.color.uikit_333));
                }
                //处理点击事件
                showRightIcon(tv, R.drawable.app_ic_goto, true);
                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DishOrderDeliveryTimeDialog dialog;
                        if (Build.BRAND.equals("vivo")) {
                            dialog = new DishOrderDeliveryTimeDialog(getContext(), true);
                        } else {
                            dialog = new DishOrderDeliveryTimeDialog(getContext());
                        }
                        dialog
                                .setDeliveryType(deliveryRestaurant.getExpress_by())
                                .setDeliveryTimes(deliveryRestaurant.getDeliveryDateList().getList())
                                .setOnDoneClick(new DishOrderDeliveryTimeDialog.OnDoneClickListener() {
                                    @Override
                                    public void onClick(DeliveryTime time, String deliveryType) {
                                        data.setExpressType(deliveryRestaurant.getExpress_by());
                                        data.setDeliveryTime(time);
                                        tv.setTextColor(getResources().getColor(R.color.uikit_333));
                                        tv.setText(time.getFormatTime());
                                        defaultFormatTime = time.getFormatTime();
                                        notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        ToastAlarm.show(e.getMessage());
                                        e.printStackTrace();
                                    }
                                })
                                .show();
                    }
                });
            } else if (dates.size() == 1) {
                data.setExpressType(deliveryRestaurant.getExpress_by());
                data.setDeliveryTime(deliveryRestaurant.getDeliveryDateList().getList().get(0).getDeliveryTimes().get(0));
                tv.setText(deliveryRestaurant.getDeliveryDateList().getList().get(0).getDeliveryTimes().get(0).getFormatTime());
                showRightIcon(tv, R.drawable.app_ic_goto, false);
            }
        }
    }

    private void showRightIcon(TextView tv, int Rid, boolean show) {
        Drawable img_off;
        Resources res = getResources();
        img_off = res.getDrawable(Rid);
        //调用setCompoundDrawables时，必须调用Drawable.setBounds()方法,否则图片不显示
        img_off.setBounds(0, 0, img_off.getMinimumWidth(), img_off.getMinimumHeight());
        if (show) {
            tv.setCompoundDrawables(null, null, img_off, null); //设置右图标
        } else {
            tv.setCompoundDrawables(null, null, null, null); //设置右图标
        }
    }


    @Override
    protected void requestList(int page, int pageSize, ApiCallback<ApiModelList<ShopCartRestaurant>> callback) {
        ApiModelList<ShopCartRestaurant> list = new ApiModelList<>(new ShopCartRestaurant());
        list.addAll(DishPolicy.get().getCheckoutRestaurantList());
        list.setAvailable();
        callback.onResult(list);
    }

    @Override
    public void onCardOut() {
        super.onCardOut();
//        DishPolicy.get().updateDeliveryTimes();
    }

    @Override
    public boolean back(Bundle data) {
        if (data == null) {
            data = new Bundle();
            data.putBoolean("update_delivery_data", false);
        }
        return super.back(data);
    }

    @Override
    protected View updRootView(View view) {
        view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_check_order_date, null);
        mNotice = (TextView) view.findViewById(R.id.app_check_order_date_notice);
        mCommit = (TextView) view.findViewById(R.id.app_check_order_date_btn);
        mNotice.setText(deliveryRestaurantList.getExpress_notice());
        mCommit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commit();
            }
        });
        return view;
    }

    /**
     * 提交
     */
    private void commit() {
        //检查配送时间
        for (int i = 0; i < getListData().size(); i++) {
            ShopCartRestaurant shopCartRestaurant = getListData().get(i);
            if (null == shopCartRestaurant.getDeliveryTime()) {
                ToastAlarm.show("请将所有商家的配送时间填写完整");
                showRed = true;
                notifyDataSetChanged();
                return;
            }
        }
        DishPolicy.get().updateDeliveryTimes();

        Bundle data = new Bundle();
        data.putBoolean("update_delivery_data", true);
        back(data);
    }

}
