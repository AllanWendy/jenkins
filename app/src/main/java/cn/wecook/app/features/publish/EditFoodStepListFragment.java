package cn.wecook.app.features.publish;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.utils.ListUtils;
import com.wecook.sdk.api.model.FoodStep;
import com.wecook.sdk.policy.FoodDetailEditorPolicy;
import com.wecook.uikit.adapter.BaseFragmentStatePagerAdapter;
import com.wecook.uikit.fragment.BaseFragment;
import com.wecook.uikit.widget.viewpage.MultiPageContainer;

import java.util.ArrayList;
import java.util.List;

import cn.wecook.app.R;
import cn.wecook.app.dialog.ConfirmDialog;
import cn.wecook.app.dialog.ListActionDialog;

/**
 * 编辑菜谱的步骤列表
 *
 * @author kevin
 * @version v1.0
 * @since 2015-1/19/15
 */
public class EditFoodStepListFragment extends BaseFragment {

    public static final String EXTRA_STEP_TYPE = "extra_step";
    public static final String EXTRA_STEP_NO = "extra_step_no";

    public static final int STEP_TITLE = 0;//标题封面
    public static final int STEP_INGREDIENT = 1;//食材
    public static final int STEP_ONE_OPTION = 2;//一步操作
    public static final int STEP_ONE_OPTION_TEXT = 3;//一步文本操作

    private int mStepType;
    private int mStepNo;

    private ViewPager mViewPage;
    private MultiPageContainer mViewPageContainer;
    private StepFragmentPagerAdapter mAdapter;

    private List<StepInfo> mStepInfos = new ArrayList<StepInfo>();

    private TextView mTitleView;
    private TextView mSubTitleView;
    private View mStepDeleteView;
    private View mStepInsertView;
    private View mStepCloseView;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mStepType = bundle.getInt(EXTRA_STEP_TYPE);
            mStepNo = bundle.getInt(EXTRA_STEP_NO, 0);
        }

        initSteps();
    }

    private void initSteps() {
        //Title
        StepInfo info = new StepInfo();
        info.type = STEP_TITLE;
        info.title = getString(R.string.app_title_step_cover);
        mStepInfos.add(info);

        //Ingredient
        info = new StepInfo();
        info.type = STEP_INGREDIENT;
        info.title = getString(R.string.app_title_step_ingredient);
        mStepInfos.add(info);

        //Steps
        int index = 0;
        int count = FoodDetailEditorPolicy.get().getStepCount();
        for (FoodStep step : FoodDetailEditorPolicy.get().getStepList()) {
            index++;
            info = new StepInfo();
            info.type = step.isOnlyText()? STEP_ONE_OPTION_TEXT : STEP_ONE_OPTION;
            info.title = index + "/" + count;
            info.object = step;
            mStepInfos.add(info);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_food_step, null);
    }

    @Override
    public boolean back(Bundle data) {
        mStepInfos.clear();
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
        return super.back(data);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTitleView = (TextView) view.findViewById(R.id.app_food_publish_step_title);
        mSubTitleView = (TextView) view.findViewById(R.id.app_food_publish_step_sub_title);
        mStepDeleteView = view.findViewById(R.id.app_food_publish_step_delete);
        mStepCloseView = view.findViewById(R.id.app_food_publish_step_close);
        mStepInsertView = view.findViewById(R.id.app_food_publish_step_insert);

        mViewPageContainer = (MultiPageContainer) view.findViewById(R.id.app_food_publish_step_pages);
        mViewPage = mViewPageContainer.getViewPager();
        mAdapter = new StepFragmentPagerAdapter(getChildFragmentManager());
        mViewPage.setAdapter(mAdapter);
        mViewPage.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                updateStep(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        mStepDeleteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ConfirmDialog(getContext(), R.string.app_alarm_delete_step)
                        .setConfirm(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // 删除当前步骤
                                deleteCurrentStep();
                                updateToCurrent();
                            }
                        }).show();

            }
        });

        mStepCloseView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 关闭当前界面
                back();
            }
        });

        mStepInsertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 插入步骤
                new ListActionDialog(getContext(), getString(R.string.app_alarm_title_add_step), new String[]{
                        "图片",
                        "文本"
                }, new AdapterView.OnItemClickListener(){

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        switch (position) {
                            case 0:
                                addNewStep(STEP_ONE_OPTION);
                                updateToCurrent();
                                break;
                            case 1:
                                addNewStep(STEP_ONE_OPTION_TEXT);
                                updateToCurrent();
                                break;
                        }
                    }
                }).show();
            }
        });

        if (mStepType == STEP_ONE_OPTION_TEXT) {//插入新步骤
            mStepNo = mStepInfos.size() - 1;
            addNewStep(STEP_ONE_OPTION_TEXT);
        } else if (mStepType == STEP_ONE_OPTION) {//跳转到步骤
            mStepNo = mStepNo + 2;
        } else if (mStepType == STEP_TITLE) {//跳转到标题
            mStepNo = 0;
        } else if (mStepType == STEP_INGREDIENT) {//跳转到食材
            mStepNo = 1;
        }

        updateToCurrent();
    }

    /**
     * 添加新的步骤
     */
    private void addNewStep(int type) {
        mStepNo ++;
        if (mAdapter != null) {
            mAdapter.addPrimaryItem();
        }
        FoodStep step = new FoodStep();
        step.setOnlyText(type == STEP_ONE_OPTION_TEXT);
        FoodDetailEditorPolicy.get().addStep(mStepNo - 2, step);
        int count = FoodDetailEditorPolicy.get().getStepCount();

        StepInfo info = new StepInfo();
        info.type = type;
        info.object = step;

        if (mStepNo >= mStepInfos.size()) {
            mStepInfos.add(info);
        } else {
            mStepInfos.add(mStepNo, info);
        }

        int index = 0;
        for (StepInfo stepInfo : mStepInfos) {
            if (stepInfo.type == STEP_ONE_OPTION
                    || stepInfo.type == STEP_ONE_OPTION_TEXT) {
                index++;
                stepInfo.title = index + "/" + count;
            }
        }

        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    private void deleteCurrentStep() {
        if (mAdapter != null) {
            mAdapter.deletePrimaryItem();
            mAdapter.notifyDataSetChanged();
        }
    }

    private void updateToCurrent() {
        int current = mViewPage.getCurrentItem();
        if (mStepNo == current) {
            updateStep(current);
        } else {
            UIHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mViewPage != null) {
                        mViewPage.setCurrentItem(mStepNo);
                    }
                }
            }, 100);
        }
    }

    private void updateStep(int position) {
        StepInfo info = ListUtils.getItem(mStepInfos, position);
        if (info != null) {
            mTitleView.setText(info.title);
            switch (info.type) {
                case STEP_TITLE:
                    mStepDeleteView.setVisibility(View.GONE);
                    mStepInsertView.setVisibility(View.GONE);
                    break;
                case STEP_INGREDIENT:
                    mStepDeleteView.setVisibility(View.GONE);
                    mStepInsertView.setVisibility(View.GONE);
                    break;
                case STEP_ONE_OPTION:
                case STEP_ONE_OPTION_TEXT:
                    mStepDeleteView.setVisibility(View.VISIBLE);
                    mStepInsertView.setVisibility(View.VISIBLE);
                    break;
            }
        }

        mStepNo = position;

    }

    private class StepInfo {
        int type;
        String title;
        Object object;
    }

    private class StepFragmentPagerAdapter extends BaseFragmentStatePagerAdapter {

        private boolean mIsPendingAdding;
        private boolean mIsPendingDeleting;

        private int mPrimaryItemPosition;
        private StepInfo mPendingStepInfo;

        public StepFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            StepInfo info = ListUtils.getItem(mStepInfos, position);
            Fragment fragment = null;
            if (info != null) {
                switch (info.type) {
                    case STEP_TITLE:
                        fragment = BaseFragment.getInstance(EditFoodStepTitleFragment.class);
                        break;
                    case STEP_INGREDIENT:
                        fragment = BaseFragment.getInstance(EditFoodStepIngredientFragment.class);
                        break;
                    case STEP_ONE_OPTION: {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(EditFoodStepOneFragment.EXTRA_FOOD_STEP, (FoodStep) info.object);
                        fragment = BaseFragment.getInstance(EditFoodStepOneFragment.class, bundle);
                        break;
                    }
                    case STEP_ONE_OPTION_TEXT: {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(EditFoodStepOneFragment.EXTRA_FOOD_STEP, (FoodStep) info.object);
                        fragment = BaseFragment.getInstance(EditFoodStepOneTextFragment.class, bundle);
                        break;
                    }
                }
            }
            return fragment;
        }

        @Override
        public int getItemPosition(Object object) {
            int currentPos = getPosition(object);

            if ((mIsPendingDeleting || mIsPendingAdding) && currentPos >= mPrimaryItemPosition) {
                if (mIsPendingDeleting) {
                    if (currentPos == mPrimaryItemPosition) {
                        mPendingStepInfo = mStepInfos.remove(mStepNo);
                        mStepInfos.add(mPendingStepInfo);
                    }
                }
                return POSITION_NONE;
            }
            return super.getItemPosition(object);
        }

        @Override
        public int getCount() {
            return mStepInfos.size();
        }

        @Override
        public void finishUpdate(ViewGroup container) {
            super.finishUpdate(container);
            if (mIsPendingDeleting) {
                mIsPendingDeleting = false;
                FoodDetailEditorPolicy.get().deleteStep(mStepNo - 2);
                mStepInfos.remove(mPendingStepInfo);
                if (mStepNo == mStepInfos.size()) {
                    mStepNo--;
                }
                int index = 0;
                int count = FoodDetailEditorPolicy.get().getStepCount();
                for (StepInfo info : mStepInfos) {
                    if (info.type == STEP_ONE_OPTION
                            || info.type == STEP_ONE_OPTION_TEXT) {
                        index++;
                        info.title = index + "/" + count;
                    }
                }
                notifyDataSetChanged();
            }
        }

        public void deletePrimaryItem() {
            mIsPendingDeleting = true;
            mPrimaryItemPosition = getPosition(getPrimaryItem());
        }

        public void addPrimaryItem() {
            mIsPendingAdding = true;
            mPrimaryItemPosition = mStepNo;
        }
    }
}
