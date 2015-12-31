package cn.wecook.app.features.debug;

import com.wecook.uikit.fragment.BaseFragment;

/**
 * TODO
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/3/25
 */
public interface IDebug {
    public String getTitle();

    public Class<? extends BaseFragment> getFragmentCls();
}
