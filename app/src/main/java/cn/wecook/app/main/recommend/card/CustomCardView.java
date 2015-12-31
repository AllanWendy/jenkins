package cn.wecook.app.main.recommend.card;

import android.content.Context;

import com.wecook.sdk.api.model.RecommendCustomCard;
import com.wecook.uikit.view.BaseView;

/**
 * 定制卡片
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/5/11
 */
public abstract class CustomCardView extends BaseView<RecommendCustomCard> {

    public CustomCardView(Context context) {
        super(context);
    }

    public void loadLayout(RecommendCustomCard data) {
        super.loadLayout(getLayoutId(), data, true);
    }

    protected abstract int getLayoutId();
}
