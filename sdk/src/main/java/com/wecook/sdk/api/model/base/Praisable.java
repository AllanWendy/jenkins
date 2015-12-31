package com.wecook.sdk.api.model.base;

/**
 * 抽象可收藏的数据模型
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/29/14
 */
public interface Praisable {

    public void setPraiseCount(int count);

    public int increasePraiseCount();

    public int decreasePraiseCount();

    public int getPraiseCount();

    public void setPraise(boolean praise);

    public boolean isPraised();
}
