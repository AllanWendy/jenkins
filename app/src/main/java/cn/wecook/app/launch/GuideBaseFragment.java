package cn.wecook.app.launch;


import com.wecook.uikit.fragment.BaseFragment;

/**
 * Created by simon on 15/9/12.
 */
public abstract class GuideBaseFragment extends BaseFragment implements IGuideAnimation {


    @Override
    public abstract long getOutAnimationTime();

    @Override
    public abstract void inAnimationLeftToRight();

    @Override
    public abstract void inAnimationRightToLeft();

    @Override
    public abstract void outAnimationLeftToRight();

    @Override
    public abstract void outAnimationRightToLeft();
}
