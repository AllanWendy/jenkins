package cn.wecook.app;

import android.test.AndroidTestCase;

import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.sdk.api.legacy.UserApi;
import com.wecook.sdk.api.model.State;
import com.wecook.sdk.api.model.User;
import com.wecook.sdk.api.model.UserBindState;
import com.wecook.sdk.userinfo.UserProperties;

/**
 * 测试用户API接口
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/6/4
 */
public class TestUserApi extends AndroidTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();

        if (!UserProperties.isLogin()) {
            fail();
        }

        if (!WecookConfig.getInstance().isTest()) {
            WecookConfig.getInstance().toggleTest(true);
        }
    }

    public void testGetSocialAccountList() throws Exception {
        UserApi.getSocialAccountList(new ApiCallback<ApiModelList<UserBindState>>() {
            @Override
            public void onResult(ApiModelList<UserBindState> result) {

            }
        });
    }

    public void testPlatformLogin() throws Exception {
        String platform = "";
        String userId = "";
        String nickName = "";
        String avatar = "";
        String gender = "";
        String token = "";
        UserApi.platformLogin(platform, userId, nickName, avatar, gender, token, new ApiCallback<User>() {
            @Override
            public void onResult(User result) {

            }
        });

    }

    public void testBindMobile() throws Exception {
        String phone = "";
        String verity = "";
        boolean force = false;
        UserApi.bindMobile(phone, force, verity, new ApiCallback<State>() {
            @Override
            public void onResult(State result) {

            }
        });

    }

    public void testBindSocial() throws Exception {
        boolean force = false;
        String platform = "";
        String socialId = "";
        UserApi.bindSocial(force, platform, socialId, new ApiCallback<State>() {
            @Override
            public void onResult(State result) {

            }
        });

    }

    public void testUnbindSocial() throws Exception {
        String platformName = "";
        UserApi.unbindSocial(platformName, new ApiCallback<State>() {
            @Override
            public void onResult(State result) {

            }
        });

    }

    public void testVerityMobile() throws Exception {
        String phone = "";
        String verity = "";
        UserApi.unbindMobile(phone, verity, new ApiCallback<State>() {
            @Override
            public void onResult(State result) {

            }
        });

    }
}
