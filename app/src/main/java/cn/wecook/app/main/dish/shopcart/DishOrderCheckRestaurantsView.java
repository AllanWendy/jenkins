package cn.wecook.app.main.dish.shopcart;

import android.text.Editable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.model.ShopCartDish;
import com.wecook.sdk.api.model.ShopCartRestaurant;
import com.wecook.uikit.fragment.BaseFragment;
import com.wecook.uikit.view.BaseView;

import java.util.List;

import cn.wecook.app.R;

/**
 * 订单确定列表页面
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/7/24
 */
public class DishOrderCheckRestaurantsView extends BaseView<List<ShopCartRestaurant>> {

    private LinearLayout mLayout;

    public DishOrderCheckRestaurantsView(BaseFragment fragment) {
        super(fragment);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mLayout = (LinearLayout) findViewById(R.id.app_order_check_restaurant_list);
    }

    @Override
    public void updateView(List<ShopCartRestaurant> obj) {
        super.updateView(obj);

        if (obj != null && !obj.isEmpty()) {
            mLayout.removeAllViews();
            for (ShopCartRestaurant restaurant : obj) {
                updateItemView(restaurant);
            }
        }

    }

    private void updateItemView(final ShopCartRestaurant restaurant) {
        if (restaurant != null
                && restaurant.getCheckoutDishes() != null
                && !restaurant.getCheckoutDishes().isEmpty()) {

            View item = LayoutInflater.from(getContext()).inflate(R.layout.view_order_check_restaurant_list_item, null);
            TextView restaurantName = (TextView) item.findViewById(R.id.app_order_check_restaurant_name);
            TextView restaurantDeliveryPrice = (TextView) item.findViewById(R.id.app_order_check_restaurant_delivery_price);
            EditText restaurantInput = (EditText) item.findViewById(R.id.app_order_check_restaurant_input);
            TextView restaurantSubtotal = (TextView) item.findViewById(R.id.app_order_check_restaurant_subtotal);
            ViewGroup restaurantDishGroup = (ViewGroup) item.findViewById(R.id.app_order_check_restaurant_dishes);

            restaurantName.setText(restaurant.getName());
            StringUtils.getPriceText(restaurant.getDeliveryPrice());
            restaurantDeliveryPrice.setText(StringUtils.getPriceText(restaurant.getDeliveryPrice()));
            restaurantInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    restaurant.setRemarkContent(s.toString().trim());
                }
            });
            SpannableStringBuilder sb = new SpannableStringBuilder();
            sb.append(restaurant.getCheckoutDishCount() + "道菜品，共计 ");
            String priceText = StringUtils.getPriceText(restaurant.getCheckoutTotalPrice()
                    + restaurant.getDeliveryPrice());
            SpannableString price = new SpannableString(priceText);
            price.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.uikit_font_orange)),
                    0, priceText.length(), 0);
//            price.setSpan(new RelativeSizeSpan(1.5f), 0, priceText.length(), 0);
            sb.append(price);
            restaurantSubtotal.setText(sb);
            restaurantDishGroup.removeAllViews();
            for (ShopCartDish dish : restaurant.getCheckoutDishes()) {
                updateDishItem(restaurantDishGroup, dish);
            }

            mLayout.addView(item);
        }

    }

    private void updateDishItem(ViewGroup dishGroup, ShopCartDish dish) {
        View item = LayoutInflater.from(getContext()).inflate(R.layout.view_order_check_restaurant_dish_item, null);
        ImageView image = (ImageView) item.findViewById(R.id.app_order_check_dish_image);
        TextView count = (TextView) item.findViewById(R.id.app_order_check_dish_count);
        TextView name = (TextView) item.findViewById(R.id.app_order_check_dish_name);
        TextView price = (TextView) item.findViewById(R.id.app_order_check_dish_price);

        ImageFetcher.asInstance().loadRoundedCorner(dish.getImage(), image);
        count.setText("x" + dish.getQuantity());
        name.setText(dish.getTitle());
        price.setText(StringUtils.getPriceText(dish.getRawPrice()));
        dishGroup.addView(item);
    }
}
