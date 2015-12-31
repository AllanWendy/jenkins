package cn.wecook.app.main.recommend.list.topic;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.wecook.common.core.internet.ApiModelGroup;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.utils.ScreenUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.model.Topic;
import com.wecook.sdk.policy.LogGather;
import com.wecook.uikit.adapter.UIAdapter;
import com.wecook.uikit.fragment.BaseFragment;

import java.util.List;

import cn.wecook.app.R;
import cn.wecook.app.features.webview.WebViewActivity;
import cn.wecook.app.main.recommend.detail.topic.TopicDetailFragment;

/**
 * 推荐专题
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/7/14
 */
public class RecommendTopicListAdapter extends UIAdapter<ApiModelGroup<Topic>> {

    private BaseFragment fragment;

    public RecommendTopicListAdapter(BaseFragment fragment, List<ApiModelGroup<Topic>> data) {
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
        return 4;
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
    public void updateView(int position, int viewType, ApiModelGroup<Topic> data, Bundle extra) {
        super.updateView(position, viewType, data, extra);
        if (viewType == 2) {
            findViewById(R.id.app_more).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 跳转到更多
                    LogGather.onEventTopicMore();
                    fragment.next(TopicListFragment.class, R.string.app_title_topic);
                }
            });
            TextView moreTitle = (TextView) findViewById(R.id.app_more_title);
            moreTitle.setText(R.string.app_topic_load_more);
            getItemView().setOnClickListener(null);
        } else if (viewType == 1) {
            TextView name = (TextView) findViewById(R.id.app_list_item_index);
            name.setText(data.getGroupName());
            name.setCompoundDrawablesWithIntrinsicBounds(R.drawable.app_ic_label_topic, 0, 0, 0);
            getItemView().setOnClickListener(null);
        } else if (viewType == 0) {
            updateItem(position - 1, getItemView(), data.getItem(0));
        }
    }

    public void updateItem(int pos, View view, final Topic item) {
        updateItem(pos, view, item, true);
    }

    /**
     * @param view
     * @param item
     */
    public void updateItem(int pos, View view, final Topic item, boolean showDivLine) {
        if (item != null) {
            ImageView image = (ImageView) view.findViewById(R.id.app_topic_image);
            TextView title = (TextView) view.findViewById(R.id.app_topic_title);
            TextView subTitle = (TextView) view.findViewById(R.id.app_topic_sub_title);
            View titleGroup = view.findViewById(R.id.app_topic_title_group);
            View divLine = view.findViewById(R.id.app_topic_div_line);
            divLine.setVisibility(showDivLine? View.VISIBLE : View.INVISIBLE);
            if (pos == 0) {
                image.setVisibility(View.VISIBLE);
                if (!StringUtils.isEmpty(item.getImage())) {
                    ImageFetcher.asInstance().load(item.getImage(), image);
                    titleGroup.setBackgroundResource(R.drawable.uikit_shape_gradient_up_to_dark);
                } else {
                    title.setBackgroundResource(R.drawable.app_empty_grey);
                    image.setImageResource(R.drawable.app_pic_default_topic_icon);
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
            subTitle.setVisibility(View.GONE);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Bundle bundle = new Bundle();
                    if (!StringUtils.isEmpty(item.getId())) {
                        bundle.putString(TopicDetailFragment.EXTRA_URL,
                                "http://m.wecook.cn/topic/detail?inwecook=true&id=" + item.getId());
                        bundle.putString(TopicDetailFragment.EXTRA_TITLE, item.getTitle());
                        item.setUrl("http://m.wecook.cn/topic/detail?inwecook=true&id=" + item.getId());
                        bundle.putSerializable(TopicDetailFragment.EXTRA_DATA, item);
                    }
                    bundle.putInt(WebViewActivity.EXTRA_PAGE, WebViewActivity.PAGE_TOPIC_DETAIL);
                    fragment.startActivity(new Intent(getContext(), WebViewActivity.class), bundle);

                }
            });
        }
    }
}
