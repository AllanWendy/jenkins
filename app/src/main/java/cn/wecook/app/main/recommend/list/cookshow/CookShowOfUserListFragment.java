package cn.wecook.app.main.recommend.list.cookshow;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.CookShowApi;
import com.wecook.sdk.api.model.CookShow;
import com.wecook.sdk.api.model.State;
import com.wecook.sdk.api.model.Tags;
import com.wecook.sdk.userinfo.UserProperties;
import com.wecook.uikit.adapter.UIAdapter;
import com.wecook.uikit.fragment.BaseFragment;
import com.wecook.uikit.widget.TitleBar;

import java.util.List;

import cn.wecook.app.R;
import cn.wecook.app.adapter.CookShowListAdapter;
import cn.wecook.app.main.recommend.CookShowPublishFragment;
import cn.wecook.app.main.recommend.detail.cookshow.CookShowDetailFragment;

/**
 * 自己已晒厨艺列表
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/17/14
 */
public class CookShowOfUserListFragment extends CookShowListFragment {

    public static final String EXTRA_LIST_TYPE = "extra_list_type";

    /** 详情列表 */
    public static final int LIST_TYPE_DETAIL = 0;

    /** 简单列表 */
    public static final int LIST_TYPE_SIMPLE = 1;

    private CookShowSimpleListAdapter mSimpleAdapter;
    private CookShowListAdapter mDetailAdapter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mType = TYPE_ONE_TAB_COOK_SHOW_LIST;
        mListType = LIST_TYPE_SIMPLE;
        Bundle bundle = getArguments();
        if (bundle != null) {
            mListType = bundle.getInt(EXTRA_LIST_TYPE, LIST_TYPE_SIMPLE);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TitleBar titleBar = getTitleBar();
        titleBar.setTitle(getString(R.string.app_title_cook_show));
        titleBar.clearActions();
        final TitleBar.ActionCoveredImageView actionListType =
                new TitleBar.ActionCoveredImageView(getContext(), getListTypeResId());
        actionListType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isInSimpleType = false;
                if (mListType == LIST_TYPE_SIMPLE) {
                    mListType = LIST_TYPE_DETAIL;
                    isInSimpleType = false;
                } else {
                    mListType = LIST_TYPE_SIMPLE;
                    isInSimpleType = true;
                }

                actionListType.setImageResource(getListTypeResId());

                if (isInSimpleType) {
                    setListAdapter(getListView(), mSimpleAdapter);
                    mSimpleAdapter.notifyDataSetChanged();
                } else {
                    setListAdapter(getListView(), mDetailAdapter);
                    mDetailAdapter.notifyDataSetChanged();
                }
                UIHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getListView().setSelection(0);
                    }
                }, 200);
            }
        });
        titleBar.addActionView(actionListType);

        getListView().setOnCreateContextMenuListener(this);
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final CookShow cookShow = mAdapter.getItem(position - getListView().getHeaderViewsCount());
                Bundle bundle = new Bundle();
                bundle.putSerializable(CookShowDetailFragment.EXTRA_COOK_SHOW, cookShow);
                next(CookShowDetailFragment.class, bundle);
            }
        });
    }

    private int getListTypeResId() {
        return mListType == LIST_TYPE_SIMPLE ? R.drawable.app_ic_action_simple : R.drawable.app_ic_action_detail;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, Menu.FIRST + 1, 1, R.string.app_context_menu_edit);
        menu.add(0, Menu.FIRST + 2, 2, R.string.app_context_menu_remove);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == Menu.FIRST + 1) {
            AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            final CookShow cookShow = mAdapter.getItem(menuInfo.position - getListView().getHeaderViewsCount());
            if (cookShow != null) {
                Bundle bundle = new Bundle();
                bundle.putSerializable(CookShowPublishFragment.EXTRA_PUBLISH_COOK_SHOW, cookShow);
                next(CookShowPublishFragment.class, bundle);
            }
        } else if(item.getItemId() == Menu.FIRST + 2) {
            AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            requestCookShowRemove(menuInfo.position);
        }
        return super.onContextItemSelected(item);
    }

    private void requestCookShowRemove(int position) {
        final CookShow cookShow = mAdapter.getItem(position - getListView().getHeaderViewsCount());
        CookShowApi.deleteCookShow(cookShow.getId(), new ApiCallback<State>() {
            @Override
            public void onResult(State result) {
                if (result != null && result.available()) {
                    mAdapter.removeEntry(cookShow);
                    if (getListData().isEmpty()) {
                        getEmptyView().setVisibility(View.VISIBLE);
                        getEmptyView().setTitle(getString(R.string.app_empty_title_cookshow));
                        getEmptyView().setSecondTitle(getString(R.string.app_empty_second_title_cookshow));
                        getEmptyView().setImageViewResourceIdOfBottom(R.drawable.app_pic_empty_cookshow);
                    }
                }
            }
        });
    }

    @Override
    protected boolean preCheckLoadData() {
        return super.preCheckLoadData() && UserProperties.isLogin();
    }

    @Override
    protected UIAdapter<CookShow> getAdapter(BaseFragment fragment, List<CookShow> data) {
        mSimpleAdapter = new CookShowSimpleListAdapter(fragment.getContext(), data);
        mDetailAdapter = new CookShowListAdapter(fragment, data);
        if (mListType == LIST_TYPE_DETAIL) {
            return mDetailAdapter;
        }
        return mSimpleAdapter;
    }

    @Override
    protected void requestListDataByType(int type, int currentPage, int pageSize, ApiCallback<ApiModelList<CookShow>> callback) {
        CookShowApi.getUserCookShowList(currentPage, pageSize, callback);
    }

    public class CookShowSimpleListAdapter extends UIAdapter<CookShow> {

        public CookShowSimpleListAdapter(Context context, List<CookShow> data) {
            super(context, R.layout.listview_item_cookshow_style_simple, data);
        }

        @Override
        public void updateView(int position, int viewType, final CookShow data, Bundle extra) {
            super.updateView(position, viewType, data, extra);
            data.setUser(UserProperties.getUser());

            ImageView cookshowImage = (ImageView) findViewById(R.id.app_cookshow_image);
            TextView title = (TextView) findViewById(R.id.app_cookshow_name);
            TextView tags = (TextView) findViewById(R.id.app_cookshow_tags);
            TextView createTime = (TextView) findViewById(R.id.app_cookshow_time);
            TextView score = (TextView) findViewById(R.id.app_cookshow_score);

            ImageFetcher.asInstance().load(data.getImage(), cookshowImage);
            title.setText(data.getTitle());
            tags.setText(getTags(data));
            createTime.setText(StringUtils.formatTimeWithNearBy(Long.parseLong(data.getCreateTime()), "MM-dd"));
            score.setText(getPraiseScore(data));
        }

        /**
         * 分数
         *
         * @param cookShow
         * @return
         */
        public String getPraiseScore(CookShow cookShow) {
            if (StringUtils.isEmpty(cookShow.getPraiseScore())
                    || "0".equals(cookShow.getPraiseScore())) {
                return "暂无评分";
            }
            return cookShow.getPraiseScore() + "分";
        }

        /**
         * 获得标签字符串
         *
         * @param cookShow
         * @return
         */
        private String getTags(CookShow cookShow) {
            ApiModelList<Tags> tags = cookShow.getTags();

            String tagsString = "";
            for (Tags tag : tags.getList()) {
                if (!StringUtils.isEmpty(tag.getName())) {
                    tagsString += "#" + tag.getName() + " ";
                }
            }
            return tagsString;
        }
    }
}
