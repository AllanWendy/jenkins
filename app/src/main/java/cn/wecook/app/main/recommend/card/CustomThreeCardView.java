package cn.wecook.app.main.recommend.card;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.utils.ListUtils;
import com.wecook.sdk.api.model.RecommendCustomCard;

import java.util.List;

import cn.wecook.app.R;
import cn.wecook.app.features.deliver.WecookLink;

/**
 * 定制卡片类型3
 * 四宫格
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/5/9
 */
public class CustomThreeCardView extends CustomCardView {

    private View mView1;
    private View mView2;
    private View mView3;


    public CustomThreeCardView(Context context) {
        super(context);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_custom_card_three_grid;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mView1 = findViewById(R.id.app_custom_card_fg_1);
        mView2 = findViewById(R.id.app_custom_card_fg_2);
        mView3 = findViewById(R.id.app_custom_card_fg_3);
    }

    @Override
    public void updateView(RecommendCustomCard obj) {
        super.updateView(obj);
        if (obj != null) {
            List<RecommendCustomCard.Card> cards = obj.getDataList();
            if (cards != null) {
                updateItemV(mView1, ListUtils.getItem(cards, 0));
                updateItemH(mView2, ListUtils.getItem(cards, 1));
                updateItemH(mView3, ListUtils.getItem(cards, 2));
            }
        }
    }

    /**
     * 更新横向布局
     *
     * @param view
     * @param item
     */
    private void updateItemH(View view, final RecommendCustomCard.Card item) {
        if (view != null && item != null) {
            TextView title = (TextView) view.findViewById(R.id.app_card_style_name);
            TextView subTitle = (TextView) view.findViewById(R.id.app_card_style_sub_name);
            ImageView imageView = (ImageView) view.findViewById(R.id.app_card_style_image);
            //设置Title
            try {
                if (item.getColor() != null)
                    title.setTextColor(Color.parseColor(item.getColor()));
            } catch (Exception e) {
            }
            title.setText(item.getTitle());
            //设置SubTitle
            subTitle.setText(item.getSub_title());
            //设置图片
            ImageFetcher.asInstance().load(item.getImage(), imageView);
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    WecookLink.sendLink(item.getUrl());
                }
            });
        }
    }

    /**
     * 更新纵向布局
     *
     * @param view
     * @param item
     */
    private void updateItemV(View view, final RecommendCustomCard.Card item) {
        if (view != null && item != null) {
            TextView title = (TextView) view.findViewById(R.id.app_recommend_card_title);
            TextView subTitle = (TextView) view.findViewById(R.id.app_recommend_card_subtitle);
            ImageView imageView = (ImageView) view.findViewById(R.id.app_card_style_image);
            //设置Title
            title.setText(item.getTitle());
            try {
                if (item.getColor() != null)
                    title.setTextColor(Color.parseColor(item.getColor()));
            } catch (Exception e) {
            }
            //设置SubTitle
            subTitle.setText(item.getSub_title());
            //设置图片
            ImageFetcher.asInstance().load(item.getImage(), imageView);
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    WecookLink.sendLink(item.getUrl());
                }
            });
        }
    }
}
