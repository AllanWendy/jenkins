package cn.wecook.app.server;

import android.content.Intent;
import android.os.IBinder;

import com.wecook.common.modules.logstatistics.LogServer;

/**
 * 日志统计服务
 *
 * @author kevin created at 9/25/14
 * @version 1.0
 */
public class AppLogServer extends LogServer {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
