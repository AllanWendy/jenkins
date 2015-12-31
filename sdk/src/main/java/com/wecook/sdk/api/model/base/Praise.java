package com.wecook.sdk.api.model.base;

import com.google.gson.annotations.SerializedName;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.utils.StringUtils;

/**
 * 点赞抽象
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/7/14
 */
public abstract class Praise extends ApiModel implements Praisable {

    @SerializedName("is_praise")
    public String isPraised;

    @SerializedName("praise")
    public String praise;

    public void setPraiseCount(int count) {
        praise = "" + count;
    }

    public int increasePraiseCount() {
        int count = getPraiseCount() + 1;
        setPraiseCount(count);
        return count;
    }

    public int decreasePraiseCount() {
        int count = getPraiseCount() - 1;
        if (count < 0) {
            count = 0;
        }
        setPraiseCount(count);
        return count;
    }

    public int getPraiseCount() {
        if (!StringUtils.isEmpty(praise)) {
            return StringUtils.parseInt(praise);
        }
        return 0;
    }

    public void setPraise(boolean praise) {
        isPraised = praise ? "1" : "0";
    }

    public boolean isPraised() {
        return "1".equals(isPraised);
    }

    public void copyPraise(Praise from, Praise to) {
        to.isPraised = from.isPraised;
        to.praise = from.praise;
    }
}
