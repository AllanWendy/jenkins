package cn.wecook.app.server;

import android.content.Intent;
/**
 * 提供进程间通讯和数据共享
 * 
 * @author 	kevin
 * @since 	2015-1/26/15
 * @version	v1.0
 */
interface IPCLocalServer {

    void requestServer(int requestCode, in Intent intent);

    void handleServerCallback(int requestCode, int resultCode, in Intent data);

}
