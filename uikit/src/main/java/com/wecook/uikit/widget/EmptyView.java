package com.wecook.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.wecook.common.modules.network.NetworkState;
import com.wecook.uikit.R;

/**
 * 空界面
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/18/14
 */
public class EmptyView extends FrameLayout {

    private TextView mTitle;
    private TextView mSecondTitle;
    private TextView mButton;
    private ImageView mEmptyViewTop;
    private ImageView mEmptyViewBottom;


    private View mNetworkGroup;
    private TextView mNetworkTitle;
    private TextView mNetworkSecondTitle;
    private ImageView mNetworkEmptyImage;
    private TextView mNetworkButton;

    private OnClickListener mRefreshListener;

    private boolean mIsCanRefresh = true;
    private boolean mIsCheckNetwork = true;

    public EmptyView(Context context) {
        super(context);
    }

    public EmptyView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        LayoutInflater.from(getContext()).inflate(R.layout.uikit_view_default_empty, this, true);
        mTitle = (TextView) findViewById(R.id.uikit_empty_title);
        mSecondTitle = (TextView) findViewById(R.id.uikit_empty_second_title);
        mEmptyViewTop = (ImageView) findViewById(R.id.uikit_empty_image_top);
        mEmptyViewBottom = (ImageView) findViewById(R.id.uikit_empty_image_bottom);
        mButton = (TextView) findViewById(R.id.uikit_empty_button);

        mNetworkGroup = findViewById(R.id.uikit_empty_network_group);
        mNetworkTitle = (TextView) findViewById(R.id.uikit_empty_network_title);
        mNetworkSecondTitle = (TextView) findViewById(R.id.uikit_empty_network_second_title);
        mNetworkEmptyImage = (ImageView) findViewById(R.id.uikit_empty_network_image);
        mNetworkButton = (TextView) findViewById(R.id.uikit_empty_network_refresh);

        setBackgroundColor(getResources().getColor(R.color.uikit_grey_light));
    }

    public TextView getSecondTitle() {
        return mSecondTitle;
    }

    public TextView getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle.setText(title);
    }

    public void setSecondTitle(String title) {
        mSecondTitle.setText(title);
    }

    public void setActionButton(String name, OnClickListener clickListener) {
        mButton.setText(name);
        mButton.setOnClickListener(clickListener);
        mButton.setVisibility(VISIBLE);
    }

    public TextView getActionButton() {
        return mButton;
    }

    public void setImageViewResourceIdOfBottom(int resId) {
        mEmptyViewBottom.setImageResource(resId);
    }

    public void setImageViewResourceIdOfTop(int resId) {
        mEmptyViewTop.setImageResource(resId);
    }

    public void setRefreshListener(OnClickListener refreshListener) {
        mRefreshListener = refreshListener;
        if (mIsCanRefresh) {
            setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mRefreshListener != null) {
//                        setVisibility(GONE);
                        mRefreshListener.onClick(v);
                    }
                }
            });
        } else {
            setOnClickListener(null);
        }
    }

    public void setCanRefresh(boolean canRefresh) {
        mIsCanRefresh = canRefresh;
    }

    @Override
    public void setVisibility(int visibility) {

        if (mIsCheckNetwork) {
            if (!NetworkState.available()) {
                showNetworkFail();
                visibility = VISIBLE;
            }
        }

        super.setVisibility(visibility);
    }

    private void showNetworkFail() {
        mNetworkGroup.setVisibility(VISIBLE);
        mNetworkTitle.setText(R.string.uikit_empty_title);
        if (mIsCanRefresh) {
            mNetworkSecondTitle.setText(R.string.uikit_empty_second_title);
            mNetworkButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mRefreshListener != null) {
                        mNetworkGroup.setVisibility(GONE);
                        setVisibility(GONE);
                        mRefreshListener.onClick(v);
                    }
                }
            });
        } else {
            mNetworkButton.setOnClickListener(null);
        }
        mNetworkEmptyImage.setImageResource(R.drawable.uikit_pic_empty_network);
    }

    public void checkNetwork(boolean check) {
        mIsCheckNetwork = check;
    }
}
