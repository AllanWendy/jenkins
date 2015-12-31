package cn.wecook.app.launch;

import java.util.HashMap;

/**
 * 引导页面manager
 * Created by simon on 15/9/12.
 */
public class GuideFragmentManager {
    /**
     * 引导页one的key
     */
    private static final int GUIDE_ONE = 0;
    /**
     * 引导页two的key
     */
    private static final int GUIDE_TWO = 1;
    /**
     * 引导页3的key
     */
    private static final int GUIDE_THREE = 2;
    /**
     * 引导页4的key
     */
    private static final int GUIDE_FOUR = 3;
    private static GuideFragmentManager guideFragmentManager;
    ;
    /**
     * 当前页面是否执行退出动画
     */
    private boolean isCanOutAnimation;
    /**
     * 当前的fragment
     */
    private GuideBaseFragment currentFragment;
    private HashMap<Integer, GuideBaseFragment> map = new HashMap<Integer, GuideBaseFragment>();

    private GuideFragmentManager() {

    }

    public static GuideFragmentManager asinstall() {
        if (guideFragmentManager == null) {
            guideFragmentManager = new GuideFragmentManager();
        }
        return guideFragmentManager;
    }

    /**
     * 获取数据
     *
     * @param position
     */
    public GuideBaseFragment getFragmentByPosition(int position) {
        if (!map.containsKey(position)) {
            GuideBaseFragment baseFragment = null;
            switch (position) {
                case GUIDE_ONE:
                    baseFragment = new Guide_one();
                    break;
                case GUIDE_TWO:
                    baseFragment = new Guide_two();
                    break;
                case GUIDE_THREE:
                    baseFragment = new GuideThreeFragment();
                    break;
                case GUIDE_FOUR:
                    baseFragment = new GuideFourFragment();
                    break;
            }
            map.put(position, baseFragment);
        }
        return map.get(position);
    }

    /**
     * 存储当前item
     *
     * @param position
     */
    public void setCurrentFragment(int position) {
        if (map.containsKey(position)) {
            currentFragment = map.get(position);
        } else {
            GuideBaseFragment fragmentByPosition = getFragmentByPosition(position);
            if (fragmentByPosition != null) {
                currentFragment = fragmentByPosition;
            }
        }
    }

    /**
     * 设置当前手指抬起
     */
    public void setOnTouchUP() {
        isCanOutAnimation = true;
    }

    /**
     * 设置当前手指按下
     */
    public void setOnTouchDown() {
        isCanOutAnimation = false;
    }

    /**
     * 获得当前item；
     */
    public GuideBaseFragment getCurrentFragment() {
        return currentFragment;
    }

    /**
     * 存储当前item
     *
     * @param guideBaseFragment
     */
    public void setCurrentFragment(GuideBaseFragment guideBaseFragment) {
        currentFragment = guideBaseFragment;
    }

}
