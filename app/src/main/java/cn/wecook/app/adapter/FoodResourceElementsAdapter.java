package cn.wecook.app.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.wecook.sdk.api.model.FoodResourceElements;
import com.wecook.uikit.adapter.UIAdapter;

import java.util.List;

import cn.wecook.app.R;

/**
 * 营养元素列表
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/15/14
 */
public class FoodResourceElementsAdapter extends UIAdapter<FoodResourceElements> {

    public FoodResourceElementsAdapter(Context context, List<FoodResourceElements> data) {
        super(context, data);
    }

    @Override
    protected View newView(int viewType) {
        return LayoutInflater.from(getContext()).inflate(R.layout.listview_item_resource, null);
    }

    @Override
    public void updateView(int position, int viewType, FoodResourceElements data, Bundle extra) {
        super.updateView(position, viewType, data, extra);

        TextView name = (TextView) findViewById(R.id.app_food_detail_resource_name);
        TextView quantity = (TextView) findViewById(R.id.app_food_detail_resource_quantity);

        name.setText(data.getName());
        quantity.setText(data.getValue());
    }
}
