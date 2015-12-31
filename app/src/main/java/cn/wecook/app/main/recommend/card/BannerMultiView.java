package cn.wecook.app.main.recommend.card;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import com.wecook.common.utils.ListUtils;
import com.wecook.common.utils.ScreenUtils;
import com.wecook.sdk.api.model.Banner;
import com.wecook.uikit.fragment.BaseFragment;
import com.wecook.uikit.view.BaseView;
import com.wecook.uikit.widget.indicator.CirclePageIndicator;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import cn.wecook.app.R;

/**
 * 多广告栏
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/6/17
 */
public class BannerMultiView extends BaseView<List<Banner>> {
    private static final long SCROLL_TIME = 5 * 1000;
    private static final int MAX_BANNER_SIZE = 8;
    private static final int SCROLL_SPEED = (int) (0.4 * 1000);
    /**
     * 能否自动滚动
     */
    private boolean stat_AutoRun = false;
    private int currentItem = 0;

    private ViewPager mViewPager;
    private FixedSpeedScroller mFixedSpeedScroller;
    private BannerAdapter mBannerAdapter;
    private CirclePageIndicator mIndicator;
    private ScheduledExecutorService scheduledExecutorService;

    public BannerMultiView(BaseFragment fragment) {
        super(fragment);
    }

    public BannerMultiView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mViewPager.setOffscreenPageLimit(2);
        controlViewPagerSpeed();
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                currentItem = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        mViewPager.requestDisallowInterceptTouchEvent(true);
        mViewPager.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (null != mBannerAdapter && stat_AutoRun) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_MOVE:
                            mBannerAdapter.stopAutoRun();
                            break;
                        case MotionEvent.ACTION_UP:
                            mBannerAdapter.startAutoRun();
                            break;
                    }
                }
                return false;
            }
        });
        mIndicator = (CirclePageIndicator) findViewById(R.id.view_pager_indicator);
        ScreenUtils.resizeViewOnScreen(mViewPager, 0.57f);
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    }


    /**
     * 设置能否自动滚动
     *
     * @param stat_AutoRun
     */
    public void setStatAutoRun(boolean stat_AutoRun) {
        this.stat_AutoRun = stat_AutoRun;
    }

    /**
     * 自动滚动
     */
    public void startAutoRun() {
        if (stat_AutoRun && null != mBannerAdapter) {
            mBannerAdapter.startAutoRun();
        }
    }

    /**
     * 停止自动滚动
     */
    public void stopAutoRun() {
        if (stat_AutoRun && null != mBannerAdapter) {
            mBannerAdapter.stopAutoRun();
        }
    }

    @Override
    public void updateView(List<Banner> obj) {
        super.updateView(obj);
        if (obj != null && !obj.isEmpty()) {
            if (mBannerAdapter == null) {
                mBannerAdapter = new BannerAdapter(obj);
                mViewPager.setAdapter(mBannerAdapter);
            }
            mBannerAdapter.notifyDataSetChanged();
            mIndicator.setViewPager(mViewPager);
            if (obj.size() > 1) {
                mIndicator.setVisibility(VISIBLE);
                mIndicator.setPageCount(mBannerAdapter.getCount());
            } else {
                mIndicator.setVisibility(GONE);
            }
        }
    }

    /**
     * 多页面视图
     */
    private class BannerAdapter extends PagerAdapter {

        private List<Banner> banners;
        private ViewPagerTask mPagerTask = new ViewPagerTask();

        public BannerAdapter(List<Banner> banners) {
            this.banners = banners;
            if (stat_AutoRun) {
                startAutoRun();
            }
        }


        /**
         * 开始自动滚动
         */
        public void startAutoRun() {
            stopAutoRun();
            scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            // 当Activity显示出来后，每两秒钟切换一次图片显示
            scheduledExecutorService.scheduleAtFixedRate(mPagerTask, (int) (SCROLL_TIME / 1000), (int) (SCROLL_TIME / 1000), TimeUnit.SECONDS);
        }

        /**
         * 停止自动滚动
         */
        public void stopAutoRun() {
            scheduledExecutorService.shutdown();
        }


        @Override
        public int getCount() {
            if (banners != null) {
                return banners.size() > MAX_BANNER_SIZE ? MAX_BANNER_SIZE : banners.size();
            }
            return 0;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            BannerView bannerView = new BannerView(getFragment());
            bannerView.loadLayout(R.layout.view_banner, ListUtils.getItem(banners, position));
            container.addView(bannerView);
            return bannerView;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        private class ViewPagerTask implements Runnable {

            public void run() {
                if (null == banners || getCount() == 0) return;
                currentItem = (currentItem + 1) % getCount();
                //Handler来实现图片切换
                handler.obtainMessage().sendToTarget();
            }
        }

        private Handler handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                //设定viewPager当前页面
                mViewPager.setCurrentItem(currentItem);
            }
        };
    }

    /**
     * 修改滑动时间
     */
    private void controlViewPagerSpeed() {
        if (null == mViewPager) return;
        try {
            Field mField;

            mField = ViewPager.class.getDeclaredField("mScroller");
            mField.setAccessible(true);

            mFixedSpeedScroller = new FixedSpeedScroller(getContext(),
                    new AccelerateInterpolator());
            mFixedSpeedScroller.setmDuration(SCROLL_SPEED);
            mField.set(mViewPager, mFixedSpeedScroller);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class FixedSpeedScroller extends Scroller {

        private int mDuration = 1500; // default time is 1500ms

        public FixedSpeedScroller(Context context) {
            super(context);
        }

        public FixedSpeedScroller(Context context, Interpolator interpolator) {
            super(context, interpolator);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            // Ignore received duration, use fixed one instead
            super.startScroll(startX, startY, dx, dy, mDuration);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy) {
            // Ignore received duration, use fixed one instead
            super.startScroll(startX, startY, dx, dy, mDuration);
        }

        /**
         * set animation time
         *
         * @param time
         */
        public void setmDuration(int time) {
            mDuration = time;
        }

        /**
         * get current animation time
         *
         * @return
         */
        public int getmDuration() {
            return mDuration;
        }
    }
}
