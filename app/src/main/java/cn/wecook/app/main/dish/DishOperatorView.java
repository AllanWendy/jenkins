package cn.wecook.app.main.dish;

import android.graphics.Color;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.utils.ListUtils;
import com.wecook.sdk.api.model.Operator;
import com.wecook.sdk.api.model.OperatorItem;
import com.wecook.sdk.policy.LogGather;
import com.wecook.uikit.fragment.BaseFragment;
import com.wecook.uikit.view.BaseView;

import java.util.List;

import cn.wecook.app.R;
import cn.wecook.app.features.deliver.WecookLink;

/**
 * 买菜帮手运营栏界面
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/6/17
 */
public class DishOperatorView extends BaseView<Operator> {

    private ViewGroup[] viewGroups;
    private Operator mOperator;

    public DishOperatorView(BaseFragment fragment) {
        super(fragment);
    }

    public void initOperator(Operator operator) {
        mOperator = operator;
        if (operator != null) {
            switch (operator.getType()) {
                case Operator.TYPE_DOUBLE:
                    loadLayout(R.layout.view_dish_operator_double, operator);
                    break;
                case Operator.TYPE_THREE:
                    loadLayout(R.layout.view_dish_operator_three, operator);
                    break;
                case Operator.TYPE_GRID:
                    loadLayout(R.layout.view_dish_operator_grid, operator);
                    break;
            }
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        switch (mOperator.getType()) {
            case Operator.TYPE_DOUBLE:
                viewGroups = new ViewGroup[2];
                break;
            case Operator.TYPE_THREE:
                viewGroups = new ViewGroup[3];
                break;
            case Operator.TYPE_GRID:
                viewGroups = new ViewGroup[4];
                break;
        }

        int[] viewGroupIds = {
                R.id.app_dish_operator_1,
                R.id.app_dish_operator_2,
                R.id.app_dish_operator_3,
                R.id.app_dish_operator_4
        };

        for (int i = 0; i < viewGroups.length; i++) {
            viewGroups[i] = (ViewGroup) findViewById(viewGroupIds[i]);
        }
        setBackgroundColor(Color.WHITE);
    }

    @Override
    public void updateView(Operator obj) {
        super.updateView(obj);

        if (obj != null) {
            List<OperatorItem> items = obj.getItemList();
            for (int i = 0; i < viewGroups.length; i++) {
                TextView title = (TextView) viewGroups[i].findViewById(R.id.app_card_style_name);
                TextView subTitle = (TextView) viewGroups[i].findViewById(R.id.app_card_style_sub_name);
                ImageView image = (ImageView) viewGroups[i].findViewById(R.id.app_card_style_image);
                image.setBackgroundDrawable(null);
                final OperatorItem item = ListUtils.getItem(items, i);
                if (item != null) {
                    viewGroups[i].setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            LogGather.onEventDishRecommendColumn(item.getTitle());
                            if (fragment.getActivity() instanceof DishActivity) {
                                Uri uri = null;
                                try {
                                    uri = Uri.parse(item.getUrl());
                                    uri = uri.buildUpon().appendQueryParameter("jump_to_fragment", "true").build();
                                } catch (Throwable e) {
                                    e.printStackTrace();
                                }
                                WecookLink.sendLink(uri);
                            } else {
                                WecookLink.sendLink(item.getUrl());
                            }
                        }
                    });
                    title.setText(item.getTitle());
                    title.setTextColor(item.getColor());
                    subTitle.setText(item.getSubTitle());
                    ImageFetcher.asInstance().load(item.getImage(), image);
                }
            }
        }
    }
}
