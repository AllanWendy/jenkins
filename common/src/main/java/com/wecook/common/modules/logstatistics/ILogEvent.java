package com.wecook.common.modules.logstatistics;

import java.io.Serializable;

/**
 * 日志接口
 *
 * @author kevin created at 9/25/14
 * @version 1.0
 */
public interface ILogEvent extends Serializable {
    public void begin();

    public void end();

    public void send();

    public String getLogMessage();

    public String getSingleLogMessage();

    public boolean isRecording();

    public boolean isRoot();

    public void click(String key);

    void assignRoot(boolean b);

    void assignGroup(ILogEvent current);

    void addChild(ILogEvent current);

    void clean();

    LogParams getLogParams();

    boolean hasChild();
}
