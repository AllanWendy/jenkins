package com.wecook.uikit.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wecook.common.utils.StringUtils;
import com.wecook.uikit.R;
import com.wecook.uikit.widget.TitleBar;

/**
 * 带有TitleBar的Fragment
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/14/14
 */
public class BaseTitleFragment extends BaseFragment {

    public static final String EXTRA_TITLE = "data_title";
    public static final String EXTRA_HIDE_TITLE_BAR = "extra_hide_title_bar";

    private String mTitle;

    private TitleBar mTitleBar;
    private boolean mIsHideTitleBar;


    public void setTitle(int titleId) {
        setTitle(getContext().getString(titleId));
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
        if (mTitleBar != null) {
            mTitleBar.setTitle(mTitle);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View parent = inflater.inflate(R.layout.uikit_fragment_titlebar, null);
        View innerView = onCreateInnerView(inflater, (parent instanceof ViewGroup ? (ViewGroup) parent : null), savedInstanceState);
        return innerView != null ? innerView : parent;
    }

    protected View onCreateInnerView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return null;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String title = null;
        Bundle bundle = getArguments();
        if (bundle != null) {
            title = bundle.getString(EXTRA_TITLE);
            mIsHideTitleBar = bundle.getBoolean(EXTRA_HIDE_TITLE_BAR);
        }

        if (StringUtils.isEmpty(title)) {
            title = mTitle;
        }

        mTitleBar = (TitleBar) view.findViewById(R.id.uikit_title_bar);
        if (mTitleBar != null) {
            if (mIsHideTitleBar) {
                mTitleBar.setVisibility(View.GONE);
            }

            mTitleBar.setBackListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    back();
                }
            });

            if (!StringUtils.isEmpty(title)) {
                setTitle(title);
            }
        }
    }

    public boolean isHideTitleBar() {
        return mIsHideTitleBar;
    }


    public void setIsShowShoppingCard(boolean flag) {
        if (null != mTitleBar) {
            mTitleBar.enableShoppingCard(flag);
            if (flag) {

            }
        }
    }

    public void showLoading() {
        super.showLoading();
        if (mTitleBar != null) {
            mTitleBar.showLoading();
        }
    }

    public void hideLoading() {
        super.hideLoading();
        if (mTitleBar != null) {
            mTitleBar.hideLoading();
        }
    }

    public TitleBar getTitleBar() {
        return mTitleBar;
    }

}
