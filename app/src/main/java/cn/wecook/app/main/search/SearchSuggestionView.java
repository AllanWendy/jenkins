package cn.wecook.app.main.search;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.wecook.sdk.api.model.SearchSuggestion;
import com.wecook.sdk.policy.LogGather;
import com.wecook.uikit.adapter.UIAdapter;
import com.wecook.uikit.view.BaseView;

import java.util.ArrayList;
import java.util.List;

import cn.wecook.app.R;

/**
 * 搜索建议界面
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/31/14
 */
public class SearchSuggestionView extends BaseView {

    private List<SearchSuggestion> mSugData;
    private ListView mListView;
    private SuggestionAdapter mAdapter;
    private SearchFragment mSearchFragment;

    public SearchSuggestionView(Context context) {
        this(context, null);
    }

    public SearchSuggestionView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SearchSuggestionView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        loadLayout(R.layout.view_search_suggestion, null);
    }

    public void setSearchFragment(SearchFragment fragment) {
        mSearchFragment = fragment;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mSugData = new ArrayList<SearchSuggestion>();
        mAdapter = new SuggestionAdapter(getContext(), mSugData);
        mListView = (ListView) findViewById(R.id.app_search_suggestion_list);
        mListView.setAdapter(mAdapter);
    }

    @Override
    public void updateView(Object obj) {
        super.updateView(obj);
        if (obj != null && obj instanceof List) {
            List list = (List) obj;
            mSugData.clear();
            mSugData.addAll(list);
            mAdapter.notifyDataSetChanged();
        }
    }

    public void clearSuggestion() {
        mSugData.clear();
        mAdapter.notifyDataSetChanged();
    }

    /**
     * 搜索建议
     */
    public class SuggestionAdapter extends UIAdapter<SearchSuggestion> {

        public SuggestionAdapter(Context context, List<SearchSuggestion> data) {
            super(context, R.layout.listview_item_search_suggestion, data);
        }

        @Override
        public void updateView(final int position, int viewType, final SearchSuggestion data, Bundle extra) {
            super.updateView(position, viewType, data, extra);
            TextView textView = (TextView) findViewById(R.id.app_search_suggestion_name);
            textView.setText(data.getName());
            getItemView().setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mSearchFragment != null) {
                        LogGather.onEventSearchSuggestion(data.getName(), position);
                        mSearchFragment.search(data.getName());
                    }
                }
            });
        }
    }
}
