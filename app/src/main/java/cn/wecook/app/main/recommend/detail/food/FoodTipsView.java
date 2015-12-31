package cn.wecook.app.main.recommend.detail.food;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.model.FoodRecipe;
import com.wecook.uikit.view.BaseView;

import cn.wecook.app.R;

/**
 * 制作小贴士
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/15/14
 */
public class FoodTipsView extends BaseView {

    private TextView mTipsTextView;

    public FoodTipsView(Context context) {
        super(context);
    }

    public FoodTipsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FoodTipsView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTipsTextView = (TextView) findViewById(R.id.app_food_detail_tips);
    }

    @Override
    public void updateView(Object obj) {
        super.updateView(obj);

//        StringBuffer stringBuffer = new StringBuffer();
//        if (obj != null && obj instanceof FoodRecipe) {
//            List<FoodStep> stepList = ((FoodRecipe) obj).getStepList().getList();
//            for (FoodStep step : stepList) {
//                stringBuffer.append(step.getTip());
//            }
//        }
//
//        String tips = stringBuffer.toString();
//        if (StringUtils.isEmpty(tips)) {
//            setVisibility(GONE);
//        } else {
//            setVisibility(VISIBLE);
//            mTipsTextView.setText(tips);
//        }

        if (obj != null && obj instanceof FoodRecipe) {
            String tips = ((FoodRecipe) obj).getTips();
            if (StringUtils.isEmpty(tips)) {
                setVisibility(GONE);
            } else {
                setVisibility(VISIBLE);
                mTipsTextView.setText(tips);
            }
        }
    }
}
