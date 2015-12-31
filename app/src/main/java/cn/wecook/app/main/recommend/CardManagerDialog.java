package cn.wecook.app.main.recommend;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wecook.sdk.api.model.RecommendCard;
import com.wecook.uikit.alarm.DialogAlarm;

import java.util.List;

import cn.wecook.app.R;

/**
 * 卡片管理对话框
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/5/14
 */
public class CardManagerDialog extends DialogAlarm {

    private ViewGroup mCardLayout;
    private RecommendFragment mFragment;
    private List<String> mCardTypes;

    public CardManagerDialog(RecommendFragment fragment) {
        super(fragment.getContext(), R.style.UIKit_Dialog_Fixed);
        mFragment = fragment;
    }

    @Override
    public View getView(ViewGroup parent) {
        return LayoutInflater.from(getContext()).inflate(R.layout.dialog_card_manager, parent, true);
    }

    @Override
    public void onViewCreated(View view) {
        mCardLayout = (ViewGroup) view.findViewById(R.id.app_card_list_layout);
        mCardTypes = mFragment.getCardTypes();

        List<RecommendCard> cards = mFragment.getRecommendCards();
        if (cards != null) {
            for (RecommendCard card : cards) {
                addCard(card);
            }
        }
    }

    private void addCard(final RecommendCard card) {
        if (card != null) {

            View item = LayoutInflater.from(getContext()).inflate(R.layout.view_card_item, null);
            ImageView image = (ImageView) item.findViewById(R.id.app_card_item_image);
            TextView name = (TextView) item.findViewById(R.id.app_card_item_name);
            View tip = item.findViewById(R.id.app_card_item_tip);
            mCardLayout.addView(item);

            if (RecommendCard.CARD_DISH.equals(card.getType())) {
                image.setImageResource(R.drawable.app_ic_card_item_dish);
            } else if (RecommendCard.CARD_FOOD.equals(card.getType())) {
                image.setImageResource(R.drawable.app_ic_card_item_food);
            } else if (RecommendCard.CARD_COOK.equals(card.getType())) {
                image.setImageResource(R.drawable.app_ic_card_item_cook);
            } else if (RecommendCard.CARD_PARTY.equals(card.getType())) {
                image.setImageResource(R.drawable.app_ic_card_item_party);
            } else if (RecommendCard.CARD_TOPIC.equals(card.getType())) {
                image.setImageResource(R.drawable.app_ic_card_item_topic);
            }

            name.setText(card.getName());
            tip.setVisibility(card.getIndex() == 0 ? View.VISIBLE : View.INVISIBLE);

            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //置顶操作
                    int index = card.getIndex();
                    if (index > 0) {
                        mCardTypes.remove(index);
                        mCardTypes.add(0, card.getType());
                        mFragment.updateSortCard();
                        dismiss();
                    }
                }
            });
        }
    }
}
