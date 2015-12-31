package cn.wecook.app;

import android.test.AndroidTestCase;

import com.wecook.common.modules.ModuleManager;
import com.wecook.common.modules.network.NetworkState;

/**
 * 网络模块的测试
 *
 * @author kevin
 * @version v1.0
 * @since 2014-9/23/14
 */
public class TestNetworkState extends AndroidTestCase{

    public void testNetworkSingle() {
        assertEquals(NetworkState.asInstance(), ModuleManager.asInstance().getModule(NetworkState.class));
    }

}
