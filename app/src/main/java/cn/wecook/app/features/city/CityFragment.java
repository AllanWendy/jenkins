package cn.wecook.app.features.city;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ValueAnimator;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.location.LocationServer;
import com.wecook.sdk.api.legacy.SelectCityApi;
import com.wecook.sdk.api.model.City;
import com.wecook.uikit.fragment.BaseTitleFragment;
import com.wecook.uikit.widget.EmptyView;

import java.util.ArrayList;
import java.util.List;

import cn.wecook.app.R;
import cn.wecook.app.adapter.SelectorAddressAdapter;

/**
 * Created by shan on 2015/8/29.
 */
public class CityFragment extends BaseTitleFragment {
    /**
     * 数据
     */
    List<City> data = new ArrayList<City>();
    /**
     * 底部布局
     */
    private LinearLayout bottom;
    /**
     * 进出的动画
     */
    private ValueAnimator valueAnimator;
    private ListView listView;

    private SelectorAddressAdapter selectorAddressAdapter;
    /**
     * 当前位置
     */
    private TextView selectorCurrentAddress;
    /**
     * 没有数据时的空VIew（暂未用）
     */
    private EmptyView mEmptyView;
    /**
     *
     */
    private String currentAddress;
    /**
     * 是否是进入界面
     */
    private boolean isInFragment;
    /**
     * 整个title布局
     */
    private RelativeLayout titleLayout;
    /**
     * 加载后的runnable任务
     */
    private Runnable mUpdateList = new Runnable() {
        @Override
        public void run() {
            if (data == null || data.size() == 0) {
                //数据为空，显示空view
                if (mEmptyView.getVisibility() != View.VISIBLE)
                    mEmptyView.setVisibility(View.VISIBLE);
            } else {
                //数据不为空，更新adapter
                if (mEmptyView.getVisibility() != View.GONE) mEmptyView.setVisibility(View.GONE);
                selectorAddressAdapter.notifyDataSetChanged();

            }
            startAnimation();
            hideLoading();
        }
    };
    private TextView tv_title;
    private boolean isShowDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_address_selector, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        initView(view);
        initAnimation();

    }

    /**
     * 初始化数据
     */
    private void initData() {
        String selecteCity = LocationServer.asInstance().getSelectedCity();
        if (selecteCity != null && !"".equals(selecteCity)) {
            currentAddress = selecteCity;
        } else {
            currentAddress = "北京";
        }
    }

    /**
     * 初始化titleBar
     */
    private void initTitleBar(View view) {
        getTitleBar().enableBack(false);
        titleLayout = (RelativeLayout) view.findViewById(R.id.app_address_selector_titlelayout);
        selectorCurrentAddress = (TextView) view.findViewById(R.id.app_address_selector_current_address);
        tv_title = (TextView) view.findViewById(R.id.app_address_selector_title);
        selectorCurrentAddress.setText(currentAddress);
        tv_title.setText("选择城市");
        selectorCurrentAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAnimation();
            }
        });
    }

    /**
     * 初始化view
     *
     * @param view
     */
    private void initView(View view) {
        initTitleBar(view);
        listView = (ListView) view.findViewById(R.id.app_address_selector_listview);
        mEmptyView = (EmptyView) view.findViewById(R.id.app_address_selector_emptyview);
        mEmptyView.setRefreshListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStartUILoad();
            }
        });
        bottom = (LinearLayout) view.findViewById(R.id.app_address_selector_bottom);
        selectorAddressAdapter = new SelectorAddressAdapter(getContext(), data);
        listView.setAdapter(selectorAddressAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                City selectAddress = (City) parent.getItemAtPosition(position);
                if (selectAddress != null) {
                    if (selectAddress.getStatus() != 0) {
                        //已开通地域
                        currentAddress = selectAddress.getName();
                        selectorCurrentAddress.setText(currentAddress);
                        LocationServer.asInstance().setIndexCity(selectAddress.getIndex(), getActivity());
                        LocationServer.asInstance().setSelectedCity(currentAddress, getActivity());
                        finishAnimation();
                    } else {
//                        ToastAlarm.makeToastAlarm(getContext(), "该城市即将开通,敬请期待", 300).show();
                    }
                }
            }
        });
    }

    @Override
    public void onStartUILoad() {
        super.onStartUILoad();
        showLoading();
        SelectCityApi.getSelecteCityList(currentAddress, new ApiCallback<ApiModelList<City>>() {
            @Override
            public void onResult(ApiModelList<City> result) {
                data.clear();
                data.addAll(result.getList());
                UIHandler.post(mUpdateList);
            }
        });
    }

    /**
     * 初始化页面进入退出动画
     */
    private void initAnimation() {
        valueAnimator = ValueAnimator.ofFloat(1f, 0f);
        valueAnimator.setDuration(300);
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                exit();

            }

            @Override
            public void onAnimationCancel(Animator animation) {
                exit();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Float animatedValue = (Float) animation.getAnimatedValue();
                if (!isInFragment) {
                    animatedValue = 1 - animatedValue;
                }
                listView.setTranslationY(-(animatedValue * (listView.getMeasuredHeight())));
                bottom.setTranslationY(animatedValue * (bottom.getMeasuredHeight()));
                if (mEmptyView.getVisibility() != View.GONE)
                    mEmptyView.setTranslationY(-(animatedValue * (listView.getMeasuredHeight())));
            }
        });
    }

    private void exit() {
        if (!isInFragment) {
            titleLayout.setAlpha(0f);
            if (mEmptyView.getVisibility() != View.GONE) mEmptyView.setAlpha(0f);
            if (getActivity() != null) {
                getActivity().finish();
            }
        }
    }

    /**
     * 进入动画
     */
    private void startAnimation() {
        isInFragment = true;
        if (valueAnimator.isRunning()) {
            valueAnimator.cancel();
        }
        valueAnimator.start();
        if (listView.getVisibility() != View.VISIBLE) listView.setVisibility(View.VISIBLE);
        if (bottom.getVisibility() != View.VISIBLE) bottom.setVisibility(View.VISIBLE);

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finishAnimation();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    /**
     * 推出动画
     */
    private void finishAnimation() {
        isInFragment = false;
        if (valueAnimator.isRunning()) {
            valueAnimator.cancel();
        }
        valueAnimator.start();
    }

}
