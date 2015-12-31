package cn.wecook.app.utils;

import android.content.Context;
import android.content.Intent;

import cn.wecook.app.main.dish.DishActivity;

/**
 * 跳转工具类
 * Created by LK on 2015/9/17.
 */
public class IntentUtils {
    public static Intent toShopCard(Context context) {
        //跳转到购物车
        Intent intent = new Intent(context, DishActivity.class);
        intent.putExtra(DishActivity.EXTRA_PAGE, DishActivity.PAGE_SHOP_CART);
        return intent;
    }
}
