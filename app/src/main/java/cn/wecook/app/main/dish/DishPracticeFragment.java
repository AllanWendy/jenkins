package cn.wecook.app.main.dish;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.wecook.common.core.debug.Logger;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.utils.FileUtils;
import com.wecook.common.utils.ScreenUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.DishApi;
import com.wecook.sdk.api.model.Dish;
import com.wecook.sdk.api.model.FoodStep;
import com.wecook.sdk.api.model.RecentlyDish;
import com.wecook.uikit.adapter.MergeAdapter;
import com.wecook.uikit.adapter.UIAdapter;
import com.wecook.uikit.fragment.BaseListFragment;
import com.wecook.uikit.widget.pulltorefresh.PullToRefreshListView;

import java.util.ArrayList;
import java.util.List;

import cn.wecook.app.R;

/**
 * 菜品做法页面
 * Created by simon on 15/9/21.
 */
public class DishPracticeFragment extends BaseListFragment {

    public static final String EXTRA_DISH_ID = "extra_dish_id";
    private PullToRefreshListView mPullToRefreshListView;
    private ImageView mBackView;
    private ListView mListView;
    private String dishId;//菜品id
    private Dish dish;
    private MergeAdapter mergeAdapter;
    private View mPracticHeadView;//顶部试图
    private List<FoodStep> foodStepList = new ArrayList<FoodStep>();//做法步骤数据
    private PracticeAdapter practiceAdapter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle bundle = getArguments();
        if (bundle != null) {
            String tempId = bundle.getString(EXTRA_DISH_ID);
            if (tempId != null) {
                dishId = tempId;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dish_practice, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        initView(view);
    }

    /**
     * 初始化view
     *
     * @param view
     */
    private void initView(View view) {
        mPullToRefreshListView = (PullToRefreshListView) view.findViewById(R.id.app_dish_practice_list);
        mListView = mPullToRefreshListView.getRefreshableView();
        mBackView = (ImageView) view.findViewById(R.id.app_dish_practice_back);
        mBackView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //返回
                back();
            }
        });
    }

    /**
     * 初始化数据
     */
    private void initData() {

    }

    @Override
    public void onStartUILoad() {
        super.onStartUILoad();
        showLoading();
        DishApi.getDishDetail(dishId, new ApiCallback<Dish>() {
            @Override
            public void onResult(Dish result) {
                if (result.available()) {
                    dish = result;
                    updateMergeAdapter();
                }
            }
        });
    }

    /**
     * 更新MergerAdapter
     */
    private void updateMergeAdapter() {

        mergeAdapter = new MergeAdapter();
        mergePracticeHead();
        mergePracticeList();
        mListView.setAdapter(mergeAdapter);
        mergeAdapter.notifyDataSetChanged();
    }

    /**
     * 组合_做法步骤
     */
    private void mergePracticeList() {
        if (practiceAdapter == null) {
            practiceAdapter = new PracticeAdapter(getContext(), foodStepList);
            mergeAdapter.addAdapter(practiceAdapter);
        }
        if (dish != null && dish.getDishSteps() != null && dish.getDishSteps().getList() != null) {
            foodStepList.clear();
            foodStepList.addAll(dish.getDishSteps().getList());
            practiceAdapter.notifyDataSetChanged();
        }

    }

    /**
     * 组合_头部
     */
    private void mergePracticeHead() {
        if (mPracticHeadView == null) {
            mPracticHeadView = LayoutInflater.from(getContext()).inflate(R.layout.view_practice_head, null);
            mergeAdapter.addView(mPracticHeadView);
        }
        if (dish != null) {
            ImageView practicHeadImage = (ImageView) mPracticHeadView.findViewById(R.id.app_dish_practice_title_img);
            TextView practicHeadTitle = (TextView) mPracticHeadView.findViewById(R.id.app_dish_practice_title);
            TextView practicHeadTitleDesc = (TextView) mPracticHeadView.findViewById(R.id.app_dish_practice_title_desc);
            ScreenUtils.resizeViewOnScreen(practicHeadImage, 1);

            if (dish.getDishCovers() != null && dish.getDishCovers().getList().get(0) != null && !StringUtils.isEmpty(dish.getDishCovers().getList().get(0).getUrl())) {
                ImageFetcher.asInstance().load(dish.getDishCovers().getList().get(0).getUrl(), practicHeadImage);
            }
            if (dish.getRestaurantName() != null) {
                practicHeadTitleDesc.setText(dish.getRestaurantName());
            }
            if (dish.getTitle() != null) {
                practicHeadTitle.setText(dish.getTitle());
            }
        }


    }

    /**
     * 最近购买菜品dapater
     */
    class PracticeAdapter extends UIAdapter<FoodStep> {

        public PracticeAdapter(Context context, List<FoodStep> data) {
            super(context, R.layout.listview_item_practice, data);
        }

        @Override
        protected View newView(int viewType, int position) {
            return super.newView(viewType, position);
        }

        @Override
        public void updateView(int position, int viewType, final FoodStep data, Bundle extra) {
            super.updateView(position, viewType, data, extra);
            View view = getItemView();
            TextView practicePosition = (TextView) view.findViewById(R.id.app_dish_practice_position);
            TextView practiceContent = (TextView) view.findViewById(R.id.app_dish_practice_content);
            ImageView practiceImage = (ImageView) view.findViewById(R.id.app_dish_practice_img);

            if (data != null) {
                if (!StringUtils.isEmpty(data.getText())) {
                    practiceContent.setText(data.getText());
                }
                if (!StringUtils.isEmpty(data.getImg())) {
                    ImageFetcher.asInstance().load(data.getImg(), practiceImage);
                    practiceImage.setVisibility(View.VISIBLE);
                } else {
                    practiceImage.setVisibility(View.GONE);
                }

                practicePosition.setText(position + 1 + "");
            }
        }
    }
}

