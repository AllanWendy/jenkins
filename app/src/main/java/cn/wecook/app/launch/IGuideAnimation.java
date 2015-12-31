package cn.wecook.app.launch;

/**
 * 引导页面的动画接口
 * <p/>
 * Created by simon on 15/9/12.
 */
public interface IGuideAnimation {
    /**
     * 进入动画(页面向左滑)
     */
    void inAnimationLeftToRight();

    /**
     * 进入动画(页面向右滑)
     */
    void inAnimationRightToLeft();

    /**
     * 退出动画(页面向左滑)
     */
    void outAnimationLeftToRight();

    /**
     * 退出动画(页面向右滑)
     */
    void outAnimationRightToLeft();


    /**
     * 退出动画执行时间
     */
    long getOutAnimationTime();

}
