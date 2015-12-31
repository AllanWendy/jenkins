package cn.wecook.app.features.debug;

import com.wecook.uikit.fragment.BaseFragment;
import com.wecook.uikit.fragment.BaseTitleFragment;

/**
 * 测试调试类
 *
 * @author kevin
 * @version v1.0
 * @since 2015-1/12/15
 */
public class BaseDebugFragment extends BaseTitleFragment implements IDebug {

    @Override
    public Class<? extends BaseFragment> getFragmentCls() {
        return this.getClass();
    }
}
