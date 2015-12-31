package com.wecook.common.core.internet.server;

import com.wecook.common.core.debug.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 本地HttpServer
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/10/20
 */
public class LocalHttpServer extends NanoHTTPD {

    private List<RequestIntercept> intercepts;

    public LocalHttpServer(int port) throws IOException {
        super(port);
        intercepts = new ArrayList<>();
        start();
        Logger.i("local server start! http://localhost:" + port + "/");
    }

    @Override
    public Response serve(IHTTPSession session) {
        Logger.i("local server request session:" + session.getUri());
        for (RequestIntercept intercept : intercepts) {
            if (intercept.intercepted(session)) {
                return newFixedLengthResponse(intercept.response());
            }
        }
        return super.serve(session);
    }

    public void interceptRequest(RequestIntercept intercept) {
        intercepts.remove(intercept);
        intercepts.add(intercept);
    }

    public interface RequestIntercept {
        public boolean intercepted(IHTTPSession session);

        public String response();
    }
}
