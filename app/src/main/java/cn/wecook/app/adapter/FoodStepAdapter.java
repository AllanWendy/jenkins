package cn.wecook.app.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.utils.FileUtils;
import com.wecook.common.utils.ScreenUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.model.FoodStep;
import com.wecook.uikit.adapter.UIAdapter;

import java.util.List;

import cn.wecook.app.R;

/**
 * 制作步骤
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/15/14
 */
public class FoodStepAdapter extends UIAdapter<FoodStep> {
    public static final String TYPE_DISH = "type_dish";
    public static final String TYPE_RECIPE = "type_recipe";
    private String currentType;

    public FoodStepAdapter(Context context, List<FoodStep> data) {
        super(context, data);
        currentType = TYPE_DISH;
    }

    public FoodStepAdapter(Context context, List<FoodStep> data, String type) {
        super(context, data);
        if (TYPE_DISH.equals(type)) {
            currentType = type;
        } else if (TYPE_RECIPE.equals(type)) {
            currentType = TYPE_RECIPE;
        }
        if (StringUtils.isEmpty(currentType)) {
            currentType = TYPE_DISH;
        }
    }

    @Override
    protected View newView(int viewType) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.listview_item_step, null);
        final ImageView image = (ImageView) view.findViewById(R.id.app_food_detail_step_image);
//        ScreenUtils.resizeView(image, ScreenUtils.dip2px(getContext(), 360), 3 / 4f);
        return view;
    }

    @Override
    public void updateView(int position, int viewType, FoodStep data, Bundle extra) {
        super.updateView(position, viewType, data, extra);

        TextView number = (TextView) findViewById(R.id.app_food_detail_step_number);
        TextView desc = (TextView) findViewById(R.id.app_food_detail_step_desc);
        ImageView image = (ImageView) findViewById(R.id.app_food_detail_step_image);
        number.setText((position + 1) + ".");

        if (data != null) {
            if (!StringUtils.isEmpty(data.getText())) {
                desc.setText(data.getText());
                desc.setVisibility(View.VISIBLE);
            } else {
                desc.setVisibility(View.GONE);
            }
            if (TYPE_DISH.equals(currentType)) {
                if (!StringUtils.isEmpty(data.getImg())) {
                    ImageFetcher.asInstance().load(data.getImg(), image);
                    image.setVisibility(View.VISIBLE);
                } else {
                    image.setVisibility(View.GONE);
                }
            } else if (TYPE_RECIPE.equals(currentType)) {
                if (!StringUtils.isEmpty(data.getOnlineImageUrl())) {
                    ImageFetcher.asInstance().load(data.getOnlineImageUrl(), image);
                    image.setVisibility(View.VISIBLE);
                } else {
                    image.setVisibility(View.GONE);
                }
            }
        }

    }
}
