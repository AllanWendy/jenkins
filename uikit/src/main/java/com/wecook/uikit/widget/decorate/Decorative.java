package com.wecook.uikit.widget.decorate;

import android.graphics.Canvas;

/**
 * 可装饰
 *
 * @author kevin
 * @version v1.0
 * @since 2014-12/24/14
 */
public interface Decorative {

    /**
     * 粉刷
     *
     * @param canvas
     */
    public void render(Canvas canvas);

    /**
     * 大小变化
     *
     * @param w
     * @param h
     */
    public void size(int w, int h);

    /**
     * 位置变化
     *
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    public void rect(int left, int top, int right, int bottom);
}
