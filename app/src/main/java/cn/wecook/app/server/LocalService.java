package cn.wecook.app.server;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.baidu.location.BDLocation;
import com.wecook.common.core.debug.Logger;
import com.wecook.common.core.internet.server.LocalHttpServer;
import com.wecook.common.core.internet.server.NanoHTTPD;
import com.wecook.common.modules.location.LocationServer;
import com.wecook.common.modules.property.PhoneProperties;

import java.io.IOException;

/**
 * 本地服务
 *
 * @author kevin
 * @version v1.0
 * @since 2014-12/18/14
 */
public class LocalService extends Service {

    public static final String KEY_IS_IN_BEIJING = "user_is_in_beijing";
    private static final String ACTION_REQ_LOCATION = "cn.wecook.app.REQ_LOCATION";

    private LocalHttpServer mLocalHttpServer;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            if (mLocalHttpServer == null || !mLocalHttpServer.isAlive()) {
                mLocalHttpServer = new LocalHttpServer(7956);
                mLocalHttpServer.interceptRequest(new LocalHttpServer.RequestIntercept() {

                    private String callbackName;

                    @Override
                    public boolean intercepted(NanoHTTPD.IHTTPSession session) {
                        String path = session.getUri();
                        if ("/getpackageinfo".equals(path)) {
                            callbackName = session.getParms().get("callback");
                            Logger.d("local server response /getpackageinfo : params callback=" + callbackName);
                            return true;
                        }
                        return false;
                    }

                    @Override
                    public String response() {
                        String response = callbackName + "({\"status\": 1,\"name\": \"wecook\",\"platform\": \"android\",\"version\": \"" + PhoneProperties.getVersionName() + "\"})";
                        Logger.d("local server response /getpackageinfo : response string : " + response);
                        return response;
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLocalHttpServer != null) {
            mLocalHttpServer.stop();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_REQ_LOCATION.equals(action)) {
                LocationServer.asInstance().location(new LocationServer.OnLocationResultListener() {
                    @Override
                    public void onLocationResult(boolean success, BDLocation location) {
                        if (success) {
                            Logger.d("location", "result true..");
                        }
                    }
                });
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 请求定位
     *
     * @param context
     */
    public static void startLocation(Context context) {
        if (context != null) {
            Intent intent = new Intent();
            intent.setClass(context, LocalService.class);
            intent.setAction(ACTION_REQ_LOCATION);
            context.startService(intent);
        }
    }

    /**
     * 启动
     *
     * @param context
     */
    public static void start(Context context) {
        if (context != null) {
            context.startService(new Intent(context, LocalService.class));
        }
    }


}
