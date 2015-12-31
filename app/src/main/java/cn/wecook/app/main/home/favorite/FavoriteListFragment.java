package cn.wecook.app.main.home.favorite;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wecook.uikit.fragment.BaseFragment;
import com.wecook.uikit.fragment.BaseTitleFragment;
import com.wecook.uikit.widget.TitleBar;
import com.wecook.uikit.widget.indicator.UnderlinePageIndicator;

import cn.wecook.app.R;

/**
 * 我的收藏
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/17/14
 */
public class FavoriteListFragment extends BaseTitleFragment {

    private ViewPager mViewPager;
    private UnderlinePageIndicator mIndicator;
    private FragmentPagerAdapter mAdapter;
    private int mCurrentPos;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return LayoutInflater.from(getContext()).inflate(R.layout.fragment_favorite_list, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TitleBar titleBar = getTitleBar();
        titleBar.setTitle(getString(R.string.app_title_my_favorite));
        titleBar.enableBottomDiv(true);
        titleBar.setBottomDivLineColor(getResources().getColor(R.color.uikit_grey));
        titleBar.setBackDrawable(R.drawable.uikit_bt_back_pressed);

        mViewPager = (ViewPager) view.findViewById(R.id.app_favorite_view_page);
        mIndicator = (UnderlinePageIndicator) view.findViewById(R.id.app_favorite_tag_indicator);
        mIndicator.setFades(false);
        mAdapter = new FragmentPagerAdapter(getChildFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                Fragment fragment = null;
                switch (position) {
                    case 0:
                        fragment = BaseFragment.getInstance(FavoriteFoodListFragment.class);
                        break;
                    case 1:
                        fragment = BaseFragment.getInstance(FavoriteActivityListFragment.class);
                        break;
                    case 2:
                        fragment = BaseFragment.getInstance(FavoriteArticleListFragment.class);
                        break;
                }
                return fragment;
            }

            @Override
            public int getCount() {
                return 3;
            }
        };
        mViewPager.setAdapter(mAdapter);
        mIndicator.setViewPager(mViewPager, 0);
        mIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                updateCurrent(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        view.findViewById(R.id.app_favorite_tag_food).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCurrent(0);
            }
        });

        view.findViewById(R.id.app_favorite_tag_activity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCurrent(1);
            }
        });

        view.findViewById(R.id.app_favorite_tag_article).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCurrent(2);
            }
        });
    }

    public void setCurrent(int position) {
        if (mCurrentPos != position) {
            if (mViewPager != null) {
                mViewPager.setCurrentItem(position, false);
            }
        }
    }

    public void updateCurrent(int position) {
        mCurrentPos = position;
    }
}
