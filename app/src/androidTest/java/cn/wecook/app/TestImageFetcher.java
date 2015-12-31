package cn.wecook.app;

import android.test.ActivityTestCase;

import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.wecook.common.core.debug.Logger;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.downer.image.ImageFetcher;

/**
 * @author kevin
 * @version v1.0
 * @since 2014-9/24/14
 */
public class TestImageFetcher extends ActivityTestCase {

    public void testObjectSingle() {
        assertEquals(ImageFetcher.asInstance(), ImageFetcher.asInstance());

        UIHandler.post(new Runnable() {
            @Override
            public void run() {
                ImageFetcher.asInstance().load("http://b.hiphotos.baidu.com/image/pic/item/9213b07eca80653815e0dc1a95dda144ad3482b4.jpg", new RequestListener() {
                    @Override
                    public boolean onException(Exception e, Object model, Target target, boolean isFirstResource) {
                        Logger.d("model:" + model + "|target:" + target + "|isFirstResource:"
                                + isFirstResource, e);
                        assertTrue("onException", false);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Object resource, Object model, Target target, boolean isFromMemoryCache, boolean isFirstResource) {
                        Logger.d("resource:" + resource + "|model:" + model + "|target:" + target
                                + "|isFromMemoryCache:" + isFromMemoryCache + "|isFirstResource:"
                                + isFirstResource);
                        assertTrue("onResourceReady", true);
                        return true;
                    }
                });
            }
        });

    }
}
