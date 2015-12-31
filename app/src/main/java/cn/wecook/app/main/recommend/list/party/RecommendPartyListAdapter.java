package cn.wecook.app.main.recommend.list.party;

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.wecook.common.core.internet.ApiModelGroup;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.utils.ScreenUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.model.Party;
import com.wecook.sdk.policy.LogGather;
import com.wecook.uikit.adapter.UIAdapter;
import com.wecook.uikit.fragment.BaseFragment;

import java.util.List;

import cn.wecook.app.R;
import cn.wecook.app.main.recommend.detail.party.PartyDetailFragment;

/**
 * 推荐活动
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/7/14
 */
public class RecommendPartyListAdapter extends UIAdapter<ApiModelGroup<Party>> {

    private BaseFragment fragment;

    public RecommendPartyListAdapter(BaseFragment fragment, List<ApiModelGroup<Party>> data) {
        super(fragment.getContext(), data);
        this.fragment = fragment;
    }

    @Override
    public int getCount() {
        int count = super.getCount();
        return count == 0 ? 0 : count + 1;
    }

    @Override
    public int getItemViewType(int position) {
        int itemViewType = super.getItemViewType(position);
        if (position == 0) {
            itemViewType = 1;//Label
        } else if (position == getCount() - 1) {
            itemViewType = 2;//底部界面
        }
        return itemViewType;
    }

    @Override
    public int getViewTypeCount() {
        return 5;
    }

    @Override
    protected View newView(int viewType) {
        View view = null;

        if (viewType == 0) {
            view = View.inflate(getContext(), R.layout.listview_item_topic_article, null);
            ScreenUtils.resizeView(view.findViewById(R.id.app_topic_image), ScreenUtils.getScreenWidthInt(), 9f / 16f);
        } else if (viewType == 1) {
            view = View.inflate(getContext(), R.layout.listview_item_label_index, null);
        } else if (viewType == 2) {
            view = View.inflate(getContext(), R.layout.listview_footer_more, null);
        }

        return view;
    }

    @Override
    public void updateView(int position, int viewType, ApiModelGroup<Party> data, Bundle extra) {
        super.updateView(position, viewType, data, extra);
        if (viewType == 2) {
            findViewById(R.id.app_more).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //跳转到更多
                    LogGather.onEventPartyMore();
                    fragment.next(PartyListFragment.class, R.string.app_title_party);
                }
            });
            TextView moreTitle = (TextView) findViewById(R.id.app_more_title);
            moreTitle.setText(R.string.app_party_load_more);
            getItemView().setOnClickListener(null);
        } else if (viewType == 1) {
            TextView name = (TextView) findViewById(R.id.app_list_item_index);
            name.setText(data.getGroupName());
            name.setCompoundDrawablesWithIntrinsicBounds(R.drawable.app_ic_label_party, 0, 0, 0);
            getItemView().setOnClickListener(null);
        } else if (viewType == 0) {
            updateItem(position - 1, getItemView(), data.getItem(0));
        }
    }

    public void updateItem(int pos, View view, final Party item) {
        updateItem(pos, view, item, true);
    }

    /**
     * @param view
     * @param item
     */
    public void updateItem(int pos, View view, final Party item, boolean showDivline) {

        if (item != null) {
            view.setVisibility(View.VISIBLE);
            ImageView image = (ImageView) view.findViewById(R.id.app_topic_image);
            TextView title = (TextView) view.findViewById(R.id.app_topic_title);
            TextView subTitle = (TextView) view.findViewById(R.id.app_topic_sub_title);
            View titleGroup = view.findViewById(R.id.app_topic_title_group);
            View divLine = view.findViewById(R.id.app_topic_div_line);
            divLine.setVisibility(showDivline ? View.VISIBLE : View.INVISIBLE);
            if (pos == 0) {
                image.setVisibility(View.VISIBLE);

                if (!StringUtils.isEmpty(item.getImage())) {
                    ImageFetcher.asInstance().load(item.getImage(), image);
                    titleGroup.setBackgroundResource(R.drawable.uikit_shape_gradient_up_to_dark);
                } else {
                    title.setBackgroundResource(R.drawable.app_empty_grey);
                    subTitle.setBackgroundResource(R.drawable.app_empty_grey);
                    image.setImageResource(R.drawable.app_pic_default_party_icon);
                }
                title.setTextColor(getContext().getResources().getColor(R.color.uikit_font_white));
                subTitle.setTextColor(getContext().getResources().getColor(R.color.uikit_font_white));
            } else {
                image.setVisibility(View.GONE);
                titleGroup.setBackgroundDrawable(null);
            }

            if (!StringUtils.isEmpty(item.getTitle())) {
                title.setText(item.getTitle());
                title.setBackgroundDrawable(null);
            }

            if (!StringUtils.isEmpty(item.getStartDate())) {

                String startTime = StringUtils.formatTime(Long.parseLong(item.getStartDate()) * 1000
                        , "MM.dd HH:mm");
                String endTime = StringUtils.formatTime(Long.parseLong(item.getEndDate()) * 1000
                        , "MM.dd HH:mm");
                boolean isExpireTime = System.currentTimeMillis() > Long.parseLong(item.getEndDate()) * 1000;
                subTitle.setText(Html.fromHtml(item.getCity()
                        + " | " + (isExpireTime ? "<font color='red'>已结束</font>" : (startTime + " - " + endTime))
                        + " | " + getContext().getString(R.string.app_want_join_party, item.getFavouriteCount())));
                subTitle.setBackgroundDrawable(null);
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Bundle bundle = new Bundle();
                    bundle.putString(PartyDetailFragment.EXTRA_TITLE, item.getTitle());
                    bundle.putSerializable(PartyDetailFragment.EXTRA_PARTY, item);
                    fragment.next(PartyDetailFragment.class, bundle);
                }
            });
        } else {
            view.setVisibility(View.GONE);
        }
    }
}
