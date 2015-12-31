package cn.wecook.app.main.kitchen;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.wecook.common.core.debug.Logger;
import com.wecook.common.core.internet.ApiModelGroup;
import com.wecook.common.modules.asynchandler.AsyncUIHandler;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.utils.ScreenUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.model.FoodResource;
import com.wecook.sdk.policy.KitchenHomePolicy;
import com.wecook.sdk.policy.LogConstant;
import com.wecook.sdk.userinfo.UserProperties;
import com.wecook.uikit.adapter.UIAdapter;
import com.wecook.uikit.fragment.BaseListFragment;
import com.wecook.uikit.widget.shape.HaloCircleImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.wecook.app.R;
import cn.wecook.app.main.home.user.UserLoginActivity;

/**
 * 食材\厨具\等界面
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/14/14
 */
public abstract class KitchenHomeChildFragment extends BaseListFragment {

    private FoodResource mFirstItem = new FoodResource();
    private ListView mListView;
    private HomeChildAdapter mAdapter;
    private List<ApiModelGroup<FoodResource>> mFoodResourceList = new ArrayList<ApiModelGroup<FoodResource>>();
    private boolean mRequestLoading;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        enableTitleBar(false);
        mListView = getListView();
        mAdapter = new HomeChildAdapter(getContext(), mFoodResourceList);
        mListView.setAdapter(mAdapter);
    }

    @Override
    public void onStartUILoad() {
        super.onStartUILoad();
        loadList();
    }

    public void loadList() {
        Logger.d("kitchen-home", "requesting? " + mRequestLoading);
        if (mRequestLoading) {
            return;
        }
        mRequestLoading = true;
        AsyncUIHandler.post(new AsyncUIHandler.AsyncJob() {
            ApiModelGroup<FoodResource> group = new ApiModelGroup<FoodResource>(3);
            List<FoodResource> list;

            @Override
            public void run() {
                list = getListData();
                if (list == null) {
                    list = new ArrayList<FoodResource>();
                }
                list.add(0, mFirstItem);
            }

            @Override
            public void postUi() {
                super.postUi();
                mFoodResourceList.clear();
                mFoodResourceList.addAll(group.loadChildrenFromList(list));
                Logger.d("kitchen-home", "mAdapter null ? " + (mAdapter == null));
                if (mAdapter != null) {
                    mAdapter.notifyDataSetChanged();
                }
                mRequestLoading = false;
                hideLoading();
                Fragment fragment = getParentFragment();
                if (fragment != null && fragment instanceof KitchenHomeFragment) {
                    ((KitchenHomeFragment) getParentFragment()).hideLoading();
                }
            }
        });

    }

    private class HomeChildAdapter extends UIAdapter<ApiModelGroup<FoodResource>> {

        public HomeChildAdapter(Context context, List<ApiModelGroup<FoodResource>> data) {
            super(context, R.layout.listview_item_kitchen_home, data);
        }

        @Override
        protected View newView(int viewType) {
            View view = super.newView(viewType);
            View left = view.findViewById(R.id.app_kitchen_home_item_left);
            View mid = view.findViewById(R.id.app_kitchen_home_item_mid);
            View right = view.findViewById(R.id.app_kitchen_home_item_right);

            float tbSpace = getContext().getResources().getDimension(R.dimen.app_kitchen_item_tb_space);
            float lrSpace = getContext().getResources().getDimension(R.dimen.app_kitchen_item_lr_space);
            ScreenUtils.reMargin(left, (int) lrSpace, (int) tbSpace, (int) (lrSpace), (int) tbSpace);
            ScreenUtils.reMargin(mid, (int) lrSpace, (int) tbSpace, (int) (lrSpace), (int) tbSpace);
            ScreenUtils.reMargin(right, (int) lrSpace, (int) tbSpace, (int) (lrSpace), (int) tbSpace);
            return view;
        }

        @Override
        public void updateView(int position, int viewType, ApiModelGroup<FoodResource> data, Bundle extra) {
            super.updateView(position, viewType, data, extra);


            updateItemView(findViewById(R.id.app_kitchen_home_item_left), data.getItem(0));
            updateItemView(findViewById(R.id.app_kitchen_home_item_mid), data.getItem(1));
            updateItemView(findViewById(R.id.app_kitchen_home_item_right), data.getItem(2));

        }

        private void updateItemView(View view, final FoodResource item) {
            if (item == null) {
                view.setVisibility(View.INVISIBLE);
                return;
            }

            view.setVisibility(View.VISIBLE);
            View clear = view.findViewById(R.id.app_kitchen_item_clear);
            final HaloCircleImageView imageView = (HaloCircleImageView) view.findViewById(R.id.app_kitchen_item_image);
            TextView textView = (TextView) view.findViewById(R.id.app_kitchen_item_name);
            if (mFirstItem == item) {
                imageView.enableHalo(false);

                imageView.setImageResource(getFirstItemDrawableId());
                textView.setText(getFirstItemName());

                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (UserProperties.isLogin() && !KitchenHomePolicy.getInstance().isInEditor()) {
                            //跳转到添加
                            onAddActionClick();
                        } else {
                            startActivity(new Intent(getContext(), UserLoginActivity.class));
                        }
                    }
                });
            } else {
                ColorDrawable colorDrawable = new ColorDrawable(getResources().getColor(R.color.uikit_grey));
                imageView.setImageDrawable(colorDrawable);
                if (!StringUtils.isEmpty(item.getImage())) {
                    ImageFetcher.asInstance().load(item.getImage(), imageView);
                }
                imageView.enableHalo(true);
                textView.setText(item.getName());


                if (KitchenHomePolicy.getInstance().isInEditor()) {
                    if (item.isSelected()) {
                        imageView.setForegroundColor(getResources().getColor(R.color.uikit_orange_transparent_70));
                    } else {
                        imageView.setForegroundColor(getResources().getColor(R.color.uikit_dark_transparent_20));
                    }
                } else {
                    imageView.setForegroundColor(getResources().getColor(R.color.uikit_transparent));
                }

                KitchenHomePolicy.getInstance().updateSelectItem(imageView, item, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (KitchenHomePolicy.getInstance().isInEditor()) {
                            if (item.isSelected()) {
                                imageView.setForegroundColor(getResources().getColor(R.color.uikit_orange_transparent_70));
                            } else {
                                imageView.setForegroundColor(getResources().getColor(R.color.uikit_dark_transparent_20));
                            }
                        } else {
                            Map<String, String> keys = new HashMap<String, String>();
                            keys.put(LogConstant.KEY_TYPE, item.getName());
                            MobclickAgent.onEvent(getContext(), LogConstant.UBS_KITCHEN_ITEM_DETAILS_RECIPE_COUNT, keys);

                            Bundle bundle = new Bundle();
                            bundle.putSerializable(KitchenResourceDetailFragment.EXTRA_DATA, item);
                            next(KitchenResourceDetailFragment.class, bundle);
                        }
                    }

                });

                if (KitchenHomePolicy.getInstance().isInEditor()) {
                    clear.setVisibility(View.VISIBLE);
                } else {
                    clear.setVisibility(View.GONE);
                }
            }

        }
    }

    /**
     * 在编辑模式
     *
     */
    public void onEditorMode() {
        loadList();
    }

    /**
     * 获取列表数据
     *
     * @return
     */
    protected abstract List<FoodResource> getListData();

    /**
     * 点击添加或者扫码按钮动作
     */
    protected abstract void onAddActionClick();

    /**
     * 获得第一个Item的名字
     *
     * @return
     */
    protected abstract String getFirstItemName();

    /**
     * 获得第一个item的图片
     *
     * @return
     */
    protected abstract int getFirstItemDrawableId();
}
