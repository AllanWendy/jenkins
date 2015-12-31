package cn.wecook.app.main.recommend.card;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.sdk.api.model.RecommendCustomCard;

import cn.wecook.app.R;
import cn.wecook.app.features.deliver.WecookLink;

/**
 * 定制卡片类型1
 * 单图
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/5/9
 */
public class CustomSimpleImageCardView extends CustomCardView {

    private ImageView mImage;


    public CustomSimpleImageCardView(Context context) {
        super(context);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_custom_card_simple_image;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mImage = (ImageView) findViewById(R.id.app_custom_card_one_image);
    }

    @Override
    public void updateView(final RecommendCustomCard obj) {
        super.updateView(obj);
        if (null == obj || null == obj.getDataList() || obj.getDataList().size() != 1) return;
        //设置图片
        ImageFetcher.asInstance().load(obj.getDataList().get(0).getImage(), mImage);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                WecookLink.sendLink(obj.getDataList().get(0).getUrl());
            }
        });
    }
}
