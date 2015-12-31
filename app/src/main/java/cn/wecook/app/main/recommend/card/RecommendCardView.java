package cn.wecook.app.main.recommend.card;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wecook.common.core.internet.ApiModelGroup;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.utils.ScreenUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.model.CookShow;
import com.wecook.sdk.api.model.Dish;
import com.wecook.sdk.api.model.Food;
import com.wecook.sdk.api.model.Party;
import com.wecook.sdk.api.model.RecommendCard;
import com.wecook.sdk.api.model.RecommendContentCard;
import com.wecook.sdk.api.model.RecommendCustomCard;
import com.wecook.sdk.api.model.Topic;
import com.wecook.sdk.policy.LogGather;
import com.wecook.uikit.fragment.BaseFragment;
import com.wecook.uikit.view.BaseView;

import java.util.ArrayList;

import cn.wecook.app.R;
import cn.wecook.app.adapter.DishAdapter;
import cn.wecook.app.features.deliver.WecookLink;
import cn.wecook.app.main.recommend.list.cookshow.RecommendCookShowAdapter;
import cn.wecook.app.main.recommend.list.food.RecommendFoodListAdapter;
import cn.wecook.app.main.recommend.list.party.RecommendPartyListAdapter;
import cn.wecook.app.main.recommend.list.topic.RecommendTopicListAdapter;

/**
 * 推荐卡片
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/5/9
 */
public class RecommendCardView extends BaseView<RecommendCard> {

    private ViewGroup mCardInfoLayout;
    private ViewGroup mCardCustomLayout;
    private ViewGroup mCardTextContentLayout;
//    private ViewGroup mCardHorizontalContentLayout;

    private ImageView mCardIcon;
    private TextView mCardDesc;
    private TextView mCardMore;

    private int mCardWidth;
    private int mCustomCardWidth;

    public RecommendCardView(BaseFragment fragment) {
        super(fragment);
    }

    public void loadLayout(RecommendCard data) {
        this.loadLayout(data, true);
    }

    public void loadLayout(RecommendCard data, boolean update) {
        super.loadLayout(R.layout.view_recommed_card, data, update);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mCardInfoLayout = (ViewGroup) findViewById(R.id.app_recommend_card_info_layout);
//        mCardHorizontalContentLayout = (ViewGroup) findViewById(R.id.app_recommend_card_content_layout1);
        mCardTextContentLayout = (ViewGroup) findViewById(R.id.app_recommend_card_content_layout2);
        mCardCustomLayout = (ViewGroup) findViewById(R.id.app_recommend_card_custom_layout);

        mCardIcon = (ImageView) findViewById(R.id.app_recommend_card_icon);
        mCardDesc = (TextView) findViewById(R.id.app_recommend_card_desc);
        mCardMore = (TextView) findViewById(R.id.app_recommend_card_more);

        float space = getContext().getResources().getDimension(R.dimen.uikit_default_space_margin);
        mCardWidth = Math.round((ScreenUtils.getScreenWidthInt() - 2 * space) * 0.382f);
        mCustomCardWidth = Math.round((ScreenUtils.getScreenWidthInt() - 2 * space) * 0.618f);
//        ScreenUtils.resizeViewWithSpecial(mCardInfoLayout, mCardWidth, mCardWidth);
//        ScreenUtils.resizeViewOfHeight(mCardCustomLayout, mCardWidth);
        ScreenUtils.rePadding(this, (int) space, (int) space / 2, (int) space, (int) space / 2, true);
    }

    @Override
    public void updateView(final RecommendCard obj) {
        super.updateView(obj);

        if (obj != null) {
            final String type = obj.getType();

            if (RecommendCard.CARD_DISH.equals(type)) {
                mCardIcon.setImageResource(R.drawable.app_ic_card_dish);
            } else if (RecommendCard.CARD_FOOD.equals(type)) {
                mCardIcon.setImageResource(R.drawable.app_ic_card_food);
            } else if (RecommendCard.CARD_COOK.equals(type)) {
                mCardIcon.setImageResource(R.drawable.app_ic_card_cook);
            } else if (RecommendCard.CARD_PARTY.equals(type)) {
                mCardIcon.setImageResource(R.drawable.app_ic_card_party);
            } else if (RecommendCard.CARD_TOPIC.equals(type)) {
                mCardIcon.setImageResource(R.drawable.app_ic_card_topic);
            }

            if (!StringUtils.isEmpty(obj.getIcon())) {
                ImageFetcher.asInstance().loadSimple(obj.getIcon(), mCardIcon);
            }

            mCardDesc.setText(obj.getDescription());
            GradientDrawable drawable = new GradientDrawable();
            if (!StringUtils.isEmpty(obj.getColor())) {
                drawable.setStroke(ScreenUtils.dip2px(getContext(), 1f),
                        Color.parseColor(obj.getColor()));
                mCardMore.setTextColor(Color.parseColor(obj.getColor()));
            }
            drawable.setCornerRadius(ScreenUtils.dip2px(getContext(), 30f));
            drawable.setColor(Color.WHITE);
            mCardMore.setBackgroundDrawable(drawable);

            mCardInfoLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (RecommendCard.CARD_DISH.equals(type)) {
                        LogGather.onEventDishCardClick();
                    } else if (RecommendCard.CARD_FOOD.equals(type)) {
                        LogGather.onEventRecipeMore();
                    } else if (RecommendCard.CARD_COOK.equals(type)) {
                        LogGather.onEventCookShowMore();
                    } else if (RecommendCard.CARD_PARTY.equals(type)) {
                        LogGather.onEventPartyMore();
                    } else if (RecommendCard.CARD_TOPIC.equals(type)) {
                        LogGather.onEventTopicMore();
                    }

                    gotoDetail();
                }
            });

//            //TODO 2.4.0 test
//            if (RecommendCard.CARD_DISH.equals(type)) {
//                obj.setUrl("wecook://dishes/recommend");
//                if (obj.getCustomCard() != null) {
//                    List<RecommendCustomCard.Card> cards = obj.getCustomCard().getDataList();
//                    if (cards != null && cards.size() >= 2) {
//                        cards.get(0).setUrl("wecook://dishes/dish/list?type=2");
//                        cards.get(1).setUrl("wecook://dishes/restaurant/list");
//                    }
//                }
//            }

            RecommendCustomCard customCard = obj.getCustomCard();
            updateCustomCard(customCard);

//            RecommendContentCard contentCard = obj.getContentCard();
//            updateContentCard(type, contentCard);
        }
    }

    /**
     * 更新定制卡片
     *
     * @param customCard
     */
    private void updateCustomCard(RecommendCustomCard customCard) {
        if (customCard != null) {
            String type = customCard.getType();
            CustomCardView custom = null;
            if (RecommendCustomCard.TYPE_SIMPLE_IMAGE.equals(type)) {
                custom = new CustomSimpleImageCardView(getContext());
            } else if (RecommendCustomCard.TYPE_DOUBLE_LINE.equals(type)) {
                custom = new CustomDoubleLineCardView(getContext());
            } else if (RecommendCustomCard.TYPE_THREE_CARD.equals(type)) {
                custom = new CustomThreeCardView(getContext());
            } else if (RecommendCustomCard.TYPE_FOUR_GRID.equals(type)) {
                custom = new CustomFourGridCardView(getContext());
            }

            if (custom != null) {
                custom.loadLayout(customCard);
                mCardCustomLayout.removeAllViews();
                mCardCustomLayout.addView(custom);
            }
        } else {
            //null的类型使用数据
            RecommendCard card = getData();
            if (card != null) {
                RecommendContentCard contentCard = card.getContentCard();
                if (contentCard != null) {
                    Object item = contentCard.getTopCard();
                    String cardType = card.getType();

//                    View itemView = createItemView(cardType, item, 0, false);
//                    if (itemView != null) {
//                        mCardCustomLayout.removeAllViews();
//                        mCardCustomLayout.addView(itemView);
//                    }
                }
            }
        }
    }

//    /**
//     * 更新内容卡片
//     *
//     * @param type
//     * @param contentCard
//     */
//    private void updateContentCard(final String type, RecommendContentCard contentCard) {
//        if (contentCard != null) {
//            if (RecommendContentCard.TYPE_HORIZONTAL.equals(contentCard.getType())) {
//                mCardTextContentLayout.setVisibility(GONE);
//                mCardHorizontalContentLayout.setVisibility(VISIBLE);
//                mCardHorizontalContentLayout.removeAllViews();
//                List dataList = contentCard.getDataList();
//                if (dataList != null) {
//                    for (int i = 0; i < dataList.size(); i++) {
//                        Object data = dataList.get(i);
//                        boolean showLine = true;
//                        if (i == 0) {
//                            showLine = false;
//                        }
//                        View child = createItemView(type, data, 1, showLine);
//                        if (child != null) {
//                            mCardHorizontalContentLayout.addView(child);
//                        }
//                    }
//
//                    //添加更多
//                    View view = LayoutInflater.from(getContext()).inflate(R.layout.view_card_more, null);
//                    view.setOnClickListener(new OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            if (RecommendCard.CARD_DISH.equals(type)) {
//                                LogGather.onEventDishCardMoreClick();
//                            }
//                            gotoDetail();
//                        }
//                    });
//                    mCardHorizontalContentLayout.addView(view);
//                    ScreenUtils.resizeView(view.findViewById(R.id.app_card_more_item), mCardWidth, 1);
//                }
//            } else if (RecommendContentCard.TYPE_TEXT.equals(contentCard.getType())) {
//                mCardHorizontalContentLayout.setVisibility(GONE);
//                mCardTextContentLayout.setVisibility(VISIBLE);
//                mCardTextContentLayout.removeAllViews();
//                List dataList = contentCard.getDataList();
//                if (dataList != null) {
//                    for (int i = 0; i < dataList.size(); i++) {
//                        Object data = dataList.get(i);
//                        boolean showLine = true;
//                        if (i == 0) {
//                            showLine = false;
//                        }
//                        View child = createItemView(type, data, 1, showLine);
//                        if (child != null) {
//                            mCardTextContentLayout.addView(child);
//                        }
//                    }
//                }
//            }
//        }
//    }

    /**
     * 创建content的视图
     *
     * @param type
     * @param data
     * @param pos
     * @return
     */
    private View createItemView(String type, Object data, int pos, boolean showLine) {
        if (data == null) {
            return null;
        }
        View itemView = null;
        if (RecommendCard.CARD_DISH.equals(type)) {
            if (data instanceof Dish) {
                itemView = LayoutInflater.from(getContext()).inflate(R.layout.view_dish_small, null);
                ScreenUtils.resizeView(itemView.findViewById(R.id.app_dish_cover_group), mCardWidth, 1);
                ScreenUtils.resizeViewOfWidth(itemView.findViewById(R.id.app_dish_title_group), mCardWidth);
                ScreenUtils.rePadding(itemView, 2);
                DishAdapter.updateItem(fragment, itemView, false, 0, 0, (Dish) data, null);
            }
        } else if (RecommendCard.CARD_FOOD.equals(type)) {
            if (data instanceof Food) {
                itemView = LayoutInflater.from(getContext()).inflate(R.layout.view_food, null);
                RecommendFoodListAdapter adapter = new RecommendFoodListAdapter(fragment, new ArrayList<ApiModelGroup<Food>>());

                ScreenUtils.resizeView(itemView.findViewById(R.id.app_food_cover_group), mCardWidth, 1);
                ScreenUtils.resizeViewOfWidth(itemView.findViewById(R.id.app_food_operator_group), mCardWidth);
                ScreenUtils.rePadding(itemView, 2);
                adapter.updateItem(itemView, (Food) data, 0);
            }
        } else if (RecommendCard.CARD_COOK.equals(type)) {
            if (data instanceof CookShow) {
                itemView = LayoutInflater.from(getContext()).inflate(R.layout.view_cook_show_recommend, null);
                RecommendCookShowAdapter adapter = new RecommendCookShowAdapter(fragment, new ArrayList<ApiModelGroup<CookShow>>());
                ScreenUtils.resizeView(itemView.findViewById(R.id.app_cook_show_cover_layout), mCardWidth, 1);
                ScreenUtils.resizeViewOfWidth(itemView.findViewById(R.id.app_cook_show_author_layout), mCardWidth);
                ScreenUtils.rePadding(itemView, 2);
                adapter.updateItem(itemView, (CookShow) data);
            }
        } else if (RecommendCard.CARD_PARTY.equals(type)) {
            if (data instanceof Party) {
                itemView = LayoutInflater.from(getContext()).inflate(R.layout.listview_item_topic_article, null);
                RecommendPartyListAdapter adapter = new RecommendPartyListAdapter(fragment, new ArrayList<ApiModelGroup<Party>>());
                adapter.updateItem(pos, itemView, (Party) data, showLine);
                if (pos == 0) {
                    ScreenUtils.resizeViewOfWidth(itemView.findViewById(R.id.app_topic_image), mCustomCardWidth);
                    ScreenUtils.resizeViewOfHeight(itemView.findViewById(R.id.app_topic_image), mCardWidth);
                    ScreenUtils.rePadding(itemView, 0);
                }
            }
        } else if (RecommendCard.CARD_TOPIC.equals(type)) {
            if (data instanceof Topic) {
                itemView = LayoutInflater.from(getContext()).inflate(R.layout.listview_item_topic_article, null);
                RecommendTopicListAdapter adapter = new RecommendTopicListAdapter(fragment, new ArrayList<ApiModelGroup<Topic>>());
                adapter.updateItem(pos, itemView, (Topic) data, showLine);
                if (pos == 0) {
                    ScreenUtils.resizeViewOfWidth(itemView.findViewById(R.id.app_topic_image), mCustomCardWidth);
                    ScreenUtils.resizeViewOfHeight(itemView.findViewById(R.id.app_topic_image), mCardWidth);
                    ScreenUtils.rePadding(itemView, 0);
                }
            }
        }
        return itemView;
    }

    private void gotoDetail() {
        if (getData() != null) {
            WecookLink.sendLink(getData().getUrl());
        }
    }

}
