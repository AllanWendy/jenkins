package cn.wecook.app.main.recommend.detail.food;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.model.FoodRecipe;
import com.wecook.uikit.view.BaseView;

import cn.wecook.app.R;

/**
 * 菜谱等级界面
 *
 * @author kevin
 * @version v1.0
 * @since 2015-2/2/15
 */
public class FoodLevelView extends BaseView {

    private TextView mDesc;
    private TextView mDifficulty;
    private TextView mCookTime;

    public FoodLevelView(Context context) {
        super(context);
    }

    public FoodLevelView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FoodLevelView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mDesc = (TextView) findViewById(R.id.app_food_detail_desc);
        mDifficulty = (TextView) findViewById(R.id.app_food_detail_difficulty);
        mCookTime = (TextView) findViewById(R.id.app_food_detail_cook_time);
    }

    @Override
    public void updateView(Object obj) {
        super.updateView(obj);

        if (obj != null && obj instanceof FoodRecipe) {
            mDifficulty.setText(getContext().getString(R.string.app_food_publish_add_difficulty_,
                    getDifficulty((FoodRecipe) obj)));
            mCookTime.setText(getContext().getString(R.string.app_food_publish_add_time_,
                    getCookTime((FoodRecipe) obj)));
            mDesc.setText(((FoodRecipe) obj).getDescription());
        }
    }

    public String getDifficulty(FoodRecipe recipe) {
        int difficultyLevel = 0;
        try {
            difficultyLevel = StringUtils.parseInt(recipe.getDifficulty());
        } catch (Exception e) {
        }
        if (difficultyLevel > 0 && difficultyLevel <= FoodRecipe.DIFFICULTY_LEVEL.length) {
            return FoodRecipe.DIFFICULTY_LEVEL[difficultyLevel - 1];
        }

        return "";
    }

    public String getCookTime(FoodRecipe recipe) {
        int timeLevel = 0;
        try {
            timeLevel = StringUtils.parseInt(recipe.getTime());
        } catch (Exception e) {
        }
        if (timeLevel > 0 && timeLevel <= FoodRecipe.TIME_LEVEL.length) {
            return FoodRecipe.TIME_LEVEL[timeLevel - 1];
        }

        return "";
    }

}
