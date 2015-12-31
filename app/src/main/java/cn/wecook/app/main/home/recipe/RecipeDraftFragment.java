package cn.wecook.app.main.home.recipe;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.wecook.common.modules.asynchandler.AsyncUIHandler;
import com.wecook.common.modules.database.DataLibraryManager;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.utils.FileUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.model.FoodRecipe;
import com.wecook.sdk.dbprovider.AppDataLibrary;
import com.wecook.sdk.dbprovider.tables.RecipeTable;
import com.wecook.sdk.policy.FoodDetailEditorPolicy;
import com.wecook.sdk.policy.LogConstant;
import com.wecook.uikit.adapter.UIAdapter;
import com.wecook.uikit.fragment.BaseListFragment;
import com.wecook.uikit.widget.EmptyView;
import com.wecook.uikit.widget.TitleBar;
import com.wecook.uikit.widget.pulltorefresh.PullToRefreshBase;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import cn.wecook.app.R;
import cn.wecook.app.dialog.ConfirmDialog;
import cn.wecook.app.features.publish.PublishFoodActivity;

/**
 * 菜谱草稿箱
 *
 * @author kevin
 * @version v1.0
 * @since 2015-1/31/15
 */
public class RecipeDraftFragment extends BaseListFragment {

    private List<FoodRecipe> mDraftFoodRecipe = new ArrayList<FoodRecipe>();

    private ItemAdapter mAdapter;
    private TitleBar.ActionTextView mTitleClear;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TitleBar titleBar = getTitleBar();
        titleBar.setTitle(getString(R.string.app_home_item_recipe_draft));

        mTitleClear = new TitleBar.ActionTextView(getContext(),
                getString(R.string.app_button_title_clear));
        mTitleClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ConfirmDialog(getContext(), R.string.app_msg_clear_all)
                        .setConfirm(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                MobclickAgent.onEvent(getContext(), LogConstant.NEW_RECIPE_DRAFT_CLEAR);
                                AppDataLibrary library = DataLibraryManager.getDataLibrary(AppDataLibrary.class);
                                final RecipeTable recipeTable = library.getTable(RecipeTable.class);
                                recipeTable.clear();
                                mDraftFoodRecipe.clear();
                                showEmptyView();
                                if (mAdapter != null) {
                                    mAdapter.notifyDataSetChanged();
                                }
                            }
                        }).show();
            }
        });
        mTitleClear.setTextColor(getResources().getColor(R.color.uikit_font_orange));
        titleBar.addActionView(mTitleClear);
        mAdapter = new ItemAdapter(getContext(), mDraftFoodRecipe);
        setListAdapter(getListView(), mAdapter);
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MobclickAgent.onEvent(getContext(), LogConstant.NEW_RECIPE_DRAFT_ITEM_CLICK);
                final FoodRecipe foodRecipe = mAdapter.getItem(position - getListView().getHeaderViewsCount());
                Intent intent = new Intent(getContext(), PublishFoodActivity.class);
                intent.putExtra(PublishFoodActivity.EXTRA_FOOD_RECIPE, foodRecipe);
                intent.putExtra(PublishFoodActivity.EXTRA_REEDIT_RECIPE, true);
                startActivity(intent);
            }
        });
        getListView().setOnCreateContextMenuListener(this);
        getRefreshListLayout().setMode(PullToRefreshBase.Mode.PULL_FROM_END);
    }

    @Override
    protected void onUpdateEmptyView() {
        super.onUpdateEmptyView();
        EmptyView view = super.getEmptyView();
        if (getActivity() != null) {
            view.checkNetwork(false);
            view.setTitle(getString(R.string.app_empty_title_draft));
            view.setSecondTitle("");
            view.setImageViewResourceIdOfBottom(R.drawable.app_pic_empty_draft);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, Menu.FIRST + 1, 1, R.string.app_context_menu_edit);
        menu.add(0, Menu.FIRST + 2, 2, R.string.app_context_menu_remove);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = menuInfo.position - getListView().getHeaderViewsCount();
        final FoodRecipe foodRecipe = mAdapter.getItem(position);

        if(item.getItemId() == Menu.FIRST + 1) {
            //编辑
            Intent intent = new Intent(getContext(), PublishFoodActivity.class);
            intent.putExtra(PublishFoodActivity.EXTRA_FOOD_RECIPE, foodRecipe);
            startActivity(intent);
        } else if(item.getItemId() == Menu.FIRST + 2) {
            //删除
            AppDataLibrary library = DataLibraryManager.getDataLibrary(AppDataLibrary.class);
            final RecipeTable recipeTable = library.getTable(RecipeTable.class);
            int ret = recipeTable.delete(RecipeTable.COLUMN_CREATE_TIME + " = '" + foodRecipe.getCreateTime() + "'", null);
            if (ret > 0) {
                mDraftFoodRecipe.remove(position);
                mAdapter.notifyDataSetChanged();
            }
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onStartUILoad() {
        super.onStartUILoad();

        AsyncUIHandler.post(new AsyncUIHandler.AsyncJob() {
            List<FoodRecipe> local;
            @Override
            public void run() {
                try {
                    local = FoodDetailEditorPolicy.get().restoreFromLocal();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void postUi() {
                super.postUi();
                if (local != null && !local.isEmpty()) {
                    mDraftFoodRecipe.clear();
                    mDraftFoodRecipe.addAll(local);
                    mAdapter.notifyDataSetChanged();
                } else {
                    showEmptyView();
                }
            }
        });

    }

    @Override
    protected void showEmptyView() {
        super.showEmptyView();
        if (mTitleClear != null) {
            mTitleClear.setVisibility(View.GONE);
        }
    }

    @Override
    protected void hideEmptyView() {
        super.hideEmptyView();
        if (mTitleClear != null) {
            mTitleClear.setVisibility(View.VISIBLE);
        }
    }

    private class ItemAdapter extends UIAdapter<FoodRecipe> {

        public ItemAdapter(Context context, List<FoodRecipe> data) {
            super(context, R.layout.listview_item_recipe_draft, data);
        }

        @Override
        public void updateView(int position, int viewType, FoodRecipe data, Bundle extra) {
            super.updateView(position, viewType, data, extra);

            TextView name = (TextView) findViewById(R.id.app_recipe_draft_name);
            TextView tags = (TextView) findViewById(R.id.app_recipe_draft_tags);
            TextView time = (TextView) findViewById(R.id.app_recipe_draft_time);
            ImageView image = (ImageView) findViewById(R.id.app_recipe_draft_image);

            name.setText(data.getTitle());
            tags.setText(getTags(data));
            if (FileUtils.isExist(data.getLocalImage())) {
                ImageFetcher.asInstance().load(data.getLocalImage(), image);
            } else if (!StringUtils.isEmpty(data.getImage())) {
                ImageFetcher.asInstance().load(data.getImage(), image);
            } else {
                image.setImageBitmap(null);
            }
            time.setText(getModifyTime(data));
        }

        private String getModifyTime(FoodRecipe data) {
            String modifyTime = data.getModifyTime();
            try {
                long modifyTimeLong = Long.parseLong(modifyTime);
                modifyTime = StringUtils.formatTime(modifyTimeLong, "编辑于 yy-MM HH:mm");
            } catch (Exception e) {
            }
            return modifyTime;
        }

        private String getTags(FoodRecipe data) {
            String resultFormatTags = "";
            String tags = data.getTags();
            if (!StringUtils.isEmpty(tags)) {
                String[] tagArray = tags.split(",");
                for (String tag : tagArray) {
                    resultFormatTags += "#" + tag + " ";
                }
            }
            return resultFormatTags;
        }
    }
}
