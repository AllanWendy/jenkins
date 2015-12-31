package cn.wecook.app.main.search;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wecook.common.modules.property.SharePreferenceProperties;
import com.wecook.common.utils.ListUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.model.Tags;
import com.wecook.sdk.policy.LogGather;
import com.wecook.uikit.view.BaseView;

import java.util.List;

import cn.wecook.app.R;

/**
 * 搜索历史和推荐界面
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/31/14
 */
public class SearchHistoryView extends BaseView {

    public static final String KEY_SEARCH_HISTORY = "search_history";
    private TextView[] mHotTagViews;
    private View mClearHistory;
    private View mHistoryGroupTitle;
    private ViewGroup mHistoryGroup;
    private ViewGroup mHotGroup;
    private SearchFragment mSearchFragment;
    private String mSearchType = "";

    public SearchHistoryView(Context context) {
        this(context, null);
    }

    public SearchHistoryView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SearchHistoryView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        loadLayout(R.layout.view_search_history, null);
    }

    public void setSearchFragment(SearchFragment fragment) {
        mSearchFragment = fragment;
        mSearchType = mSearchFragment.getSearchType();
        updateView();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mClearHistory = findViewById(R.id.app_search_history_clear);
        mClearHistory.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                clearHistory();
            }
        });
        mHotTagViews = new TextView[8];
        mHotTagViews[0] = (TextView) findViewById(R.id.app_search_hot_1);
        mHotTagViews[1] = (TextView) findViewById(R.id.app_search_hot_2);
        mHotTagViews[2] = (TextView) findViewById(R.id.app_search_hot_3);
        mHotTagViews[3] = (TextView) findViewById(R.id.app_search_hot_4);
        mHotTagViews[4] = (TextView) findViewById(R.id.app_search_hot_5);
        mHotTagViews[5] = (TextView) findViewById(R.id.app_search_hot_6);
        mHotTagViews[6] = (TextView) findViewById(R.id.app_search_hot_7);
        mHotTagViews[7] = (TextView) findViewById(R.id.app_search_hot_8);
        mHotGroup = (ViewGroup) findViewById(R.id.app_search_hot_group);
        mHistoryGroup = (ViewGroup) findViewById(R.id.app_search_history_item_group);
        mHistoryGroupTitle = findViewById(R.id.app_search_history_item_group_title);
    }

    @Override
    public void updateView(Object obj) {
        super.updateView(obj);
        if (mSearchFragment != null) {
            String history = (String) SharePreferenceProperties.get(KEY_SEARCH_HISTORY + mSearchType, "");
            if (StringUtils.isEmpty(history)) {
                mHistoryGroupTitle.setVisibility(GONE);
            } else {
                mHistoryGroupTitle.setVisibility(VISIBLE);
            }
            String[] historyItems = history.split(";");

            for (int i = historyItems.length - 1; i >= 0; i--) {
                View item = newHistoryItemView(historyItems[i]);
                if (item != null) {
                    mHistoryGroup.addView(item);
                }
            }
        }

    }

    /**
     * 更新热门搜索
     *
     * @param searchTags
     */
    public void updateHotSearchTag(List<Tags> searchTags) {
        if (searchTags == null || searchTags.isEmpty()) {
            mHotGroup.setVisibility(GONE);
        } else {
            mHotGroup.setVisibility(VISIBLE);
            for (int index = 0; index < 8; index++) {
                final Tags tag = ListUtils.getItem(searchTags, index);
                if (tag == null || StringUtils.isEmpty(tag.getName())) {
                    mHotTagViews[index].setVisibility(INVISIBLE);
                } else {
                    mHotTagViews[index].setVisibility(VISIBLE);
                    mHotTagViews[index].setText(tag.getName());
                    mHotTagViews[index].setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mSearchFragment != null) {
                                LogGather.onEventSearchTag(tag.getName());
                                mSearchFragment.search(tag.getName());
                            }
                        }
                    });
                }

            }
        }
    }

    /**
     * 添加到历史纪录中
     *
     * @param searchKey
     */
    public void addHistory(String searchKey) {

        String history = (String) SharePreferenceProperties.get(KEY_SEARCH_HISTORY + mSearchType, "");
        String[] historyItems = history.split(";");
        boolean hasExist = false;
        for (String item : historyItems) {
            if (searchKey.equals(item)) {
                hasExist = true;
                break;
            }
        }

        if (!hasExist) {
            SharePreferenceProperties.set(KEY_SEARCH_HISTORY + mSearchType, history + ";" + searchKey);
            View item = newHistoryItemView(searchKey);
            mHistoryGroup.addView(item, 0);
            mHistoryGroupTitle.setVisibility(VISIBLE);
        }
    }

    public void clearHistory() {
        SharePreferenceProperties.set(KEY_SEARCH_HISTORY + mSearchType, "");
        mHistoryGroup.removeAllViews();
        mHistoryGroupTitle.setVisibility(GONE);
    }

    private View newHistoryItemView(final String historyName) {
        if (StringUtils.isEmpty(historyName)) {
            return null;
        }
        View item = LayoutInflater.from(getContext()).inflate(R.layout.view_search_history_item, null);
        TextView name = (TextView) item.findViewById(R.id.app_search_history_item_name);
        name.setText(historyName);
        item.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSearchFragment != null) {
                    LogGather.onEventSearchInput(historyName);
                    mSearchFragment.search(historyName);
                }
            }
        });
        return item;
    }

}
