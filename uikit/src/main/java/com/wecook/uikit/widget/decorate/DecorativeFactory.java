package com.wecook.uikit.widget.decorate;

/**
 * 装饰工具工厂
 *
 * @author kevin
 * @version v1.0
 * @since 2014-12/24/14
 */
public class DecorativeFactory {
    public static final int TYPE_GRID = 1;

    public static Decorative create(int type) {
        Decorative decorative = null;
        switch (type) {
            case TYPE_GRID:
                decorative = new GridDecorative();
                break;
        }
        return decorative;
    }
}
