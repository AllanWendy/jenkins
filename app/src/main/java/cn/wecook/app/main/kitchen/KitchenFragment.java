package cn.wecook.app.main.kitchen;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.sdk.api.model.Restaurant;
import com.wecook.uikit.fragment.BaseTitleFragment;
import com.wecook.uikit.widget.TitleBar;

import java.util.List;
import java.util.Random;

import cn.wecook.app.R;
import cn.wecook.app.main.MainActivity;
import cn.wecook.app.main.recommend.list.cookshow.CookShowPageActivity;
import cn.wecook.app.main.recommend.list.food.FoodMenuListFragment;

/**
 * “厨房”界面
 *
 * @author kevin created at 9/18/14
 * @version 1.0
 */
public class KitchenFragment extends BaseTitleFragment {
    private LinearLayout layout_left, layout_right;
    private ImageView img_bg;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_kitchen, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TitleBar titleBar = getTitleBar();
        titleBar.setBottomDivLineColor(getResources().getColor(R.color.uikit_orange));
        titleBar.enableBack(false);
        titleBar.setTitle(getString(R.string.app_title_kitchen));
        titleBar.enableBottomDiv(false);

        TitleBar.ActionTextView myFoods = new TitleBar.ActionTextView(getContext(), getString(R.string.app_button_title_myfoods));
        myFoods.setTextColor(getResources().getColor(R.color.uikit_333));
        myFoods.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), KitchenHomeActivity.class));
            }
        });
        titleBar.addActionView(myFoods);
        layout_left = (LinearLayout) view.findViewById(R.id.kitchen_left);
        layout_right = (LinearLayout) view.findViewById(R.id.kitchen_right);
        img_bg = (ImageView) view.findViewById(R.id.kitchen_img_bg);
        layout_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                next(FoodMenuListFragment.class);
            }
        });
        layout_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), CookShowPageActivity.class));
            }
        });
        view.findViewById(R.id.app_kitchen_garnish).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), KitchenGarnishActivity.class));
            }
        });
        loadBackImg();
    }

    @Override
    public void onCardOut() {
        super.onCardOut();
        loadBackImg();
    }


    /**
     * 加载背景图片
     */
    public void loadBackImg() {
        if (getActivity() instanceof MainActivity) {
            List<Restaurant> food_feed = ((MainActivity) getActivity()).getFood_feed();
            if (null != img_bg && null != food_feed && food_feed.size() > 1) {
                int random = new Random().nextInt(food_feed.size() - 1);
                if (random < 0) {
                    random = 0;
                }
                ImageFetcher.asInstance().loadBlur(food_feed.get(random).getDish().getImage(), img_bg, 10);
            }
        }
    }

}
