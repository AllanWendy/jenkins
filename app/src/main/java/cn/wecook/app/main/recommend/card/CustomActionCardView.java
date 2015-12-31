package cn.wecook.app.main.recommend.card;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.model.RecommendCustomCard;

import java.util.List;

import cn.wecook.app.R;
import cn.wecook.app.features.deliver.WecookLink;

/**
 * 定制卡片类型4
 * 执行动作
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/5/9
 */
public class CustomActionCardView extends CustomCardView {

    private TextView mTitle;
    private TextView mAction;

    public CustomActionCardView(Context context) {
        super(context);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_custom_card_action;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTitle = (TextView) findViewById(R.id.app_card_style_name);
        mAction = (TextView) findViewById(R.id.app_card_style_button);
    }

    @Override
    public void updateView(RecommendCustomCard obj) {
        super.updateView(obj);
        if (obj != null) {
            List<RecommendCustomCard.Card> cards = obj.getDataList();
            if (cards != null && cards.size() > 0) {
                RecommendCustomCard.Card card = cards.get(0);
                if (card != null) {
                    if (RecommendCustomCard.Card.NUMBER.equals(card.getType())) {
                        updateNumberView(card);
                    } else if (RecommendCustomCard.Card.TIME.equals(card.getType())) {
                        updateTimeView(card);
                    }
                }
            }
        }
    }

    /**
     * 更新倒计时视图
     *
     * @param card
     */
    private void updateTimeView(final RecommendCustomCard.Card card) {
        String title = card.getSubName();
        mTitle.setText(title);
        mAction.setText(card.getName());
        if (!StringUtils.isEmpty(card.getColor())) {
            mAction.setTextColor(Color.parseColor(card.getColor()));
        }

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                WecookLink.sendLink(card.getUrl());
            }
        });
    }

    /**
     * 更新数字视图
     * @param card
     */
    private void updateNumberView(final RecommendCustomCard.Card card) {
        String title = card.getSubName();
        if (title.contains("<color>")) {
            int start = title.indexOf("<color>");
            int end = title.indexOf("</color>");
            SpannableStringBuilder ssb = new SpannableStringBuilder();
            ssb.append(title.substring(0, start));
            ssb.append(title.substring(start + 7, end));
            ssb.append(title.substring(end + 8, title.length()));
            ForegroundColorSpan span = new ForegroundColorSpan(0xffff644e);
            ssb.setSpan(span, start, end - 7, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            mTitle.setText(ssb);
        } else {
            mTitle.setText(title);
        }
        mAction.setText(card.getName());
        if (!StringUtils.isEmpty(card.getColor())) {
            mAction.setTextColor(Color.parseColor(card.getColor()));
        }
        mAction.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG ); //下划线
        mAction.getPaint().setAntiAlias(true);//抗锯齿

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                WecookLink.sendLink(card.getUrl());
            }
        });
    }
}
