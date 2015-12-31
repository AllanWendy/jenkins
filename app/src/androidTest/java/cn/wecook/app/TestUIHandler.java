package cn.wecook.app;

import android.test.AndroidTestCase;

import com.wecook.common.modules.asynchandler.UIHandler;

/**
 * TODO
 *
 * @author kevin
 * @version v1.0
 * @since 2014-12/13/14
 */
public class TestUIHandler extends AndroidTestCase{
    int count = 10;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        UIHandler.initInApp(getContext());
    }

    public void testLoop(){
//
//        UIHandler.loop("testLoop", new Runnable() {
//            @Override
//            public void run() {
//                Logger.d("run loop");
//                count--;
//                if (count <= 0) {
//                    UIHandler.stopLoop("testLoop");
//                }
//            }
//        }, 1000);
    }
}
