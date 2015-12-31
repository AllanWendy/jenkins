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
 * 定制卡片类型2
 * 双行
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/5/9
 */
public class CustomDoubleLineCardView extends CustomCardView {

    private View mFirstView;
    private View mSecondView;


    public CustomDoubleLineCardView(Context context) {
        super(context);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_custom_card_double_line;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mFirstView = findViewById(R.id.app_custom_card_dl_1);
        mSecondView = findViewById(R.id.app_custom_card_dl_2);
    }

    @Override
    public void updateView(RecommendCustomCard obj) {
        super.updateView(obj);
        if (obj != null) {
            List<RecommendCustomCard.Card> list = obj.getDataList();
            if (list != null) {
                updateItem(mFirstView, ListUtils.getItem(list, 0));
                updateItem(mSecondView, ListUtils.getItem(list, 1));
            }
        }
    }

    private void updateItem(View view, final RecommendCustomCard.Card item) {
        if (item != null && view != null) {
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
                    String url = item.getUrl();
                    WecookLink.sendLink(url);
                }
            });
        }
    }
}
