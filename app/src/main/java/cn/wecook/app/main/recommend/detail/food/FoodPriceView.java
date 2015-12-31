package cn.wecook.app.main.recommend.detail.food;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.model.FoodDetail;
import com.wecook.uikit.view.BaseView;

import cn.wecook.app.R;

/**
 * 菜谱价格
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/15/14
 */
public class FoodPriceView extends BaseView{

    private TextView mPriceRangeView;
    private TextView mPriceBuy;

    public FoodPriceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public FoodPriceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FoodPriceView(Context context) {
        super(context);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mPriceRangeView = (TextView) findViewById(R.id.app_food_detail_price_range);
        mPriceBuy = (TextView) findViewById(R.id.app_food_detail_buy);

    }

    @Override
    public void updateView(Object obj) {
        super.updateView(obj);

        if (obj != null && obj instanceof FoodDetail) {
            FoodDetail detail = (FoodDetail) obj;
            String priceRange = "";
            if (StringUtils.isEmpty(detail.getMinPrice()) && !StringUtils.isEmpty(detail.getMaxPrice())) {
                priceRange = detail.getMaxPrice();
            } else if(!StringUtils.isEmpty(detail.getMinPrice()) && StringUtils.isEmpty(detail.getMaxPrice())){
                priceRange = detail.getMinPrice();
            } else if(!StringUtils.isEmpty(detail.getMinPrice()) && !StringUtils.isEmpty(detail.getMaxPrice())){
                priceRange = "¥" + detail.getMinPrice() + " - ¥" + detail.getMaxPrice();
            }

            if (!StringUtils.isEmpty(priceRange)) {
                mPriceRangeView.setText(priceRange);
            }

            mPriceBuy.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO goto pay
                }
            });
        }
    }
}
