package com.wecook.common.modules.thirdport.object;

/**
 * 订单信息
 *
 * @author kevin
 * @version v1.0
 * @since 2014-12/1/14
 */
public class OrderInfo {

    /**
     * 交易订单号
     */
    private String tradeNo;

    /**
     * 卖家账号ID
     */
    private String sellerId;

    /**
     * 交易描述（商品标题、交易标题、订单标题等）
     */
    private String subject;

    /**
     * 交易详情（单个或者多个商品信息串联）
     */
    private String body;

    /**
     * 总金额
     */
    private String total;

    /**
     * 超时时间间隔
     */
    private String expireTime;

    public String getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(String expireTime) {
        this.expireTime = expireTime;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getTradeNo() {
        return tradeNo;
    }

    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
    }
}
