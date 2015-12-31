package cn.wecook.app.adapter;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.sdk.api.model.City;
import com.wecook.uikit.adapter.UIAdapter;

import java.util.List;

import cn.wecook.app.R;

/**
 * 配送地址列表
 * Created by shan on 2015/8/29.
 */
public class SelectorAddressAdapter extends UIAdapter<City> {
    private final Context context;
    /**
     * 数据源
     */
    private List<City> list;
    /**
     * 显示或关闭详情动画时间
     */
    private long showDetailAnimationTime = 200;
    /**
     * 正在显示或关闭详情动画
     */
    private boolean isShowDetailAnimating = false;
    /**
     * 显示或关闭详情的动画对象
     */
    private ValueAnimator showDetailAnimation;

    public SelectorAddressAdapter(Context context, List<City> list) {
        super(context, list);
        this.context = context;
        this.list = list;
    }

    @Override
    protected View newView(int viewType) {
        return LayoutInflater.from(getContext()).inflate(R.layout.listview_item_address_select, null);
    }

    @Override
    public void updateView(int position, int viewType, final City data, Bundle extra) {
        super.updateView(position, viewType, data, extra);
        Log.d("Simon", data.toString() + "---selected:" + data.isSelected());
        ImageView detailButton = (ImageView) findViewById(R.id.app_address_selector_detail_botton);
        View line = (View) findViewById(R.id.app_address_selector_line);
        ImageView addressIcon = (ImageView) findViewById(R.id.app_address_selector_icon);
        final TextView detailContent = (TextView) findViewById(R.id.app_address_selector_detail_content);
        TextView addressTitle = (TextView) findViewById(R.id.app_address_selector_title);
        final TextView addressSubTitle = (TextView) findViewById(R.id.app_address_selector_subtitle);
        View bottomEmptyView = findViewById(R.id.app_address_selector_bottom_empty_view);
        //设置图标
        ImageFetcher.asInstance().load(data.getIconUrl(), addressIcon);
        //设置内容详情
        detailContent.setText(data.getDetailContent());

        if (data.getStatus() == 0) {
            //暂未开通地域
            //设置主标题
            addressTitle.setText(data.getName());
            addressTitle.setTextColor(getContext().getResources().getColor(R.color.app_address_selecte_available_false));
            //设置副标题
            addressSubTitle.setText("尚未开通");
            addressSubTitle.setVisibility(View.VISIBLE);
            addressSubTitle.setTextColor(getContext().getResources().getColor(R.color.app_address_selecte_available_false));
            //设置详情按钮
            setDetailButtonStatus(true, data.isSelected(), detailButton);
            detailButton.setVisibility(View.GONE);
            //分割线
            line.setVisibility(View.GONE);
        } else {
            //已开通地域
            //设置主标题
            addressTitle.setText(data.getName());
            addressTitle.setTextColor(getContext().getResources().getColor(R.color.app_address_selecte_available_true));
            //设置副标题
            addressSubTitle.setText("配送范围");
            addressSubTitle.setTextColor(getContext().getResources().getColor(R.color.app_address_selecte_available_true));
            //设置详情按钮
            setDetailButtonStatus(false, data.isSelected(), detailButton);
            detailButton.setVisibility(View.VISIBLE);
            //分割线
            line.setVisibility(View.VISIBLE);
        }
        //设置显示详情或隐藏详情
        detailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (data.getStatus() != 0) {
                    //可选择
                    if (data.isSelected()) {
                        data.setSelected(false);
                    } else {
                        data.setSelected(true);
                    }
                    //设置详情button状态
                    setDetailButtonStatus(false, data.isSelected(), (ImageView) v);

                    //已经开通的地域配送详情的显示或关闭
                    if (isShowDetailAnimating && showDetailAnimation != null) {
                        showDetailAnimation.cancel();
                    }
                    showDetailAnimation(data.isSelected(), detailContent, addressSubTitle);

                }
            }
        });
        //设置是否添加底部空view
        if (position == (list.size() - 1)) {
            bottomEmptyView.setVisibility(View.VISIBLE);
        } else {
            bottomEmptyView.setVisibility(View.GONE);
        }
    }

    /***
     * 设置详情button的状态
     *
     * @param isStatus
     * @param isSelected
     * @param detailButton
     */
    private void setDetailButtonStatus(boolean isStatus, boolean isSelected, ImageView detailButton) {
        if (isStatus) {
            if (!isSelected) {
                detailButton.setImageResource(R.drawable.app_bt_address_detail_button_down_not_selected);
            } else {
                detailButton.setImageResource(R.drawable.app_bt_address_detail_button_up_not_selected);
            }
        } else {
            if (!isSelected) {
                detailButton.setImageResource(R.drawable.app_bt_address_detail_button_down_selected);
            } else {
                detailButton.setImageResource(R.drawable.app_bt_address_detail_button_up_selected);
            }
        }
    }

    /**
     * 显示或关闭详情动画
     */
    private void showDetailAnimation(final boolean isSelected, final TextView detailContent, final TextView addressSubTitle) {
        showDetailAnimation = ValueAnimator.ofFloat(0f, 1f);
        showDetailAnimation.setDuration(showDetailAnimationTime);
        if (isSelected) {
            //显示
            showDetailAnimation.setInterpolator(new OvershootInterpolator(2f));
        }
        showDetailAnimation.addListener(new Animator.AnimatorListener() {


            @Override
            public void onAnimationStart(Animator animation) {
                isShowDetailAnimating = true;
                if (isSelected) {
                    detailContent.setVisibility(View.VISIBLE);
                    addressSubTitle.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isShowDetailAnimating = false;
                if (!isSelected) {
                    detailContent.setVisibility(View.GONE);
                    addressSubTitle.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                isShowDetailAnimating = false;
                if (!isSelected) {
                    detailContent.setVisibility(View.GONE);
                    addressSubTitle.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        showDetailAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float valueAnimation = 0;
                float value = (float) animation.getAnimatedValue();
                if (isSelected) {
                    //显示详情value
                    valueAnimation = value;
                } else {
                    //关闭详情value
                    valueAnimation = (1f - value);
                }
                //详情内容的动画
                detailContent.setPivotX(detailContent.getWidth() / 2f);
                detailContent.setPivotY(detailContent.getHeight() / 2f);
                detailContent.setScaleX(valueAnimation);
                detailContent.setScaleY(valueAnimation);
                //副标题的动画
                addressSubTitle.setAlpha(valueAnimation);

            }
        });
        showDetailAnimation.start();
    }
}
