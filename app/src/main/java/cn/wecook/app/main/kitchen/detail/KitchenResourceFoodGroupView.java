package cn.wecook.app.main.kitchen.detail;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.sdk.api.model.Food;
import com.wecook.uikit.adapter.UIAdapter;
import com.wecook.uikit.view.BaseView;

import java.util.List;

import cn.wecook.app.R;

/**
 * 厨房食材专属菜谱组
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/17/14
 */
public class KitchenResourceFoodGroupView extends BaseView {

    private ViewGroup mGroupView;

    public KitchenResourceFoodGroupView(Context context) {
        super(context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mGroupView = (ViewGroup) findViewById(R.id.app_kitchen_detail_food_group);
    }

    @Override
    public void updateView(Object obj) {
        super.updateView(obj);
        if (obj != null && obj instanceof List) {
            FoodListAdapter adapter = new FoodListAdapter(getContext(), (List<Food>) obj);
            for (int i = 0; i < ((List) obj).size(); i++) {
                View child = adapter.getView(i, null, null);
                mGroupView.addView(child);
            }
        }
    }

    private class FoodListAdapter extends UIAdapter<Food> {

        public FoodListAdapter(Context context, List<Food> data) {
            super(context, R.layout.listview_item_food_horizontal, data);
        }

        @Override
        public void updateView(int position, int viewType, Food data, Bundle extra) {
            super.updateView(position, viewType, data, extra);
            ImageView foodImage = (ImageView) findViewById(R.id.app_kitchen_detail_food_image);
            TextView foodName = (TextView) findViewById(R.id.app_kitchen_detail_food_name);
            ImageFetcher.asInstance().load(data.image, foodImage);
            foodName.setText(data.title);
        }
    }
}
