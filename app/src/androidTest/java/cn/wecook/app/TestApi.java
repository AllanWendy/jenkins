package cn.wecook.app;

import android.test.AndroidTestCase;

import com.wecook.common.core.internet.Api;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.utils.SecurityUtils;
import com.wecook.sdk.api.legacy.UserApi;
import com.wecook.sdk.api.model.User;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO
 *
 * @author kevin
 * @version v1.0
 * @since 2014-9/23/14
 */
public class TestApi extends AndroidTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testApiSinger() {
        UserApi userApi1 = (UserApi) Api.get(UserApi.class);
        UserApi userApi2 = (UserApi) Api.get(UserApi.class);

        assertSame(userApi1, userApi2);

        User user = new User();
        userApi1.toModel(user);
        assertSame(userApi1.getApiModel(), user);
    }

    public void testApiSecurity() {
        Map<String, String> params = new HashMap<String, String>();
//        String apkSign = PhoneProperties.getApkSignatures();
        params.put("device", "1234567890");
        String security = SecurityUtils.getSignature(params, "6E820afd87518a475f83e8a279c0d367");
        assertEquals(security, "bc9c723a10bc9d2a34ee3f379345c079");
    }

    public void testApiRun() {
        Api.get(UserApi.class)
                .setCacheTime(10)
                .setApiCallback(new ApiCallback() {
                    @Override
                    public void onResult(ApiModel result) {

                    }
                })
                .executeGet();


    }
}
