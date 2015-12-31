package cn.wecook.app.fragment.shopcard;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.wecook.sdk.policy.DishPolicy;
import com.wecook.uikit.fragment.BaseTitleFragment;
import com.wecook.uikit.widget.TitleBar;

import cn.wecook.app.utils.IntentUtils;

/**
 * Created by LK on 2015/10/30.
 */
public class BaseTitleShopCardFragment extends BaseTitleFragment {
    private TitleBar titleBar;
    private boolean useShopCartPolice = true;

    private DishPolicy.OnShopCartListener listener = new DishPolicy.OnShopCartListener() {
        @Override
        public void onUpdate(int count) {
            updateShopCartCount(count, titleBar.getShopCardText());
        }
    };

    /**
     * 设置购物车数量
     *
     * @param count
     */
    private static void updateShopCartCount(int count, TextView textView) {
        if (count == 0) {
            textView.setVisibility(View.GONE);
        } else {
            if (count > 99) {
                textView.setText("99+");
            } else {
                textView.setText("" + count);
            }
            textView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        titleBar = getTitleBar();
        if (null != titleBar) {
            titleBar.enableShoppingCard(useShopCartPolice);
            if (useShopCartPolice) {
                DishPolicy.get().addOnShopCartListener(listener);
                DishPolicy.get().update();
                titleBar.getShoppingCartLayout().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //跳转到购物车
                        getContext().startActivity(IntentUtils.toShopCard(getContext()));
                    }
                });
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != listener) {
            DishPolicy.get().removeOnShopCartListener(listener);
        }
    }

    public void setUseShopCartPolice(boolean useShopCartPolice) {
        this.useShopCartPolice = useShopCartPolice;
    }
}
