package cn.wecook.app.main.kitchen;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.sdk.api.legacy.KitchenApi;
import com.wecook.sdk.api.model.FoodResource;
import com.wecook.sdk.policy.KitchenHomePolicy;
import com.wecook.uikit.adapter.UIAdapter;
import com.wecook.uikit.fragment.BaseListFragment;
import com.wecook.uikit.widget.TitleBar;
import com.wecook.uikit.widget.shape.CircleImageView;

import java.util.ArrayList;
import java.util.List;

import cn.wecook.app.R;

/**
 * 添加界面
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/15/14
 */
public class KitchenAddSearchFragment extends BaseListFragment {

    private ListView mContentListView;
    private List<FoodResource> mResourceList;
    private ItemAdapter mItemAdapter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mResourceList = new ArrayList<FoodResource>();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final TitleBar titleBar = getTitleBar();
        titleBar.setSearchHint(getString(R.string.app_search_resource_hint));
        titleBar.setSearchLayer(true);
        titleBar.setSearchListener(new TitleBar.SearchListener() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                KitchenApi.getSuggestionList(s.toString(), 20, new ApiCallback<ApiModelList<FoodResource>>() {
                    @Override
                    public void onResult(ApiModelList<FoodResource> result) {
                        mResourceList.clear();
                        if (result != null && result.available()) {
                            mResourceList.addAll(result.getList());
                        }
                        UIHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mItemAdapter.notifyDataSetChanged();
                            }
                        });

                    }
                });
            }

            @Override
            protected void onSearchFinishClick() {
                super.onSearchFinishClick();
                titleBar.setSearchText("");
                KitchenHomePolicy.getInstance().updateChangeList();
                back();
            }
        });


        mContentListView = getListView();
        mItemAdapter = new ItemAdapter(getContext(), mResourceList);
        mContentListView.setAdapter(mItemAdapter);
    }

    private class ItemAdapter extends UIAdapter<FoodResource> {

        public ItemAdapter(Context context, List<FoodResource> data) {
            super(context, R.layout.listview_item_kitchen_add, data);
        }

        @Override
        public void updateView(int position, int viewType, FoodResource data, Bundle extra) {
            super.updateView(position, viewType, data, extra);
            CircleImageView image = (CircleImageView) findViewById(R.id.app_kitchen_add_image);
            TextView name = (TextView) findViewById(R.id.app_kitchen_add_name);
            View check = findViewById(R.id.app_kitchen_add_check);

            name.setText(data.getName());
            ImageFetcher.asInstance().load(data.getImage(), image);
            KitchenHomePolicy.getInstance().checkSelectState(getItemView(), check, data);
        }
    }

}
