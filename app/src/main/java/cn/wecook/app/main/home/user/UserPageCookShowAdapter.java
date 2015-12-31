package cn.wecook.app.main.home.user;

import android.os.Bundle;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;

import com.wecook.common.core.internet.ApiModelGroup;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.modules.property.PhoneProperties;
import com.wecook.common.utils.ScreenUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.model.CookShow;
import com.wecook.uikit.adapter.UIAdapter;

import java.util.List;

import cn.wecook.app.R;
import cn.wecook.app.main.recommend.list.cookshow.CookShowDetailListFragment;

/**
 * 用户厨艺
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/4/14
 */
public class UserPageCookShowAdapter extends UIAdapter<ApiModelGroup<CookShow>> {

    private UserPageFragment mFragment;

    public UserPageCookShowAdapter(UserPageFragment fragment, List<ApiModelGroup<CookShow>> data) {
        super(fragment.getContext(), R.layout.listview_item_cookshow_style_user, data);
        mFragment = fragment;
    }

    @Override
    protected View newView(int viewType) {
        View view = super.newView(viewType);

        ViewStub left = (ViewStub) view.findViewById(R.id.app_cook_show_list_item_1);
        ViewStub center = (ViewStub) view.findViewById(R.id.app_cook_show_list_item_2);
        ViewStub right = (ViewStub) view.findViewById(R.id.app_cook_show_list_item_3);

        float space = getContext().getResources().getDimension(R.dimen.uikit_default_padding_small);
        int width = (int) ((StringUtils.parseInt(PhoneProperties.getScreenWidth()) - 4 * space) / 3);
        ScreenUtils.resizeView(left, width, 1);
        ScreenUtils.resizeView(center, width, 1);
        ScreenUtils.resizeView(right, width, 1);
        ScreenUtils.reMargin(left, (int) space, (int) space, (int) (space / 2), 0);
        ScreenUtils.reMargin(center, (int) (space / 2), (int) space, (int) (space / 2), 0);
        ScreenUtils.reMargin(right, (int) (space / 2), (int) space, (int) space, 0);
        return view;
    }

    @Override
    public void updateView(int position, int viewType, ApiModelGroup<CookShow> data, Bundle extra) {
        super.updateView(position, viewType, data, extra);

        ViewStub left = (ViewStub) findViewById(R.id.app_cook_show_list_item_1);
        ViewStub center = (ViewStub) findViewById(R.id.app_cook_show_list_item_2);
        ViewStub right = (ViewStub) findViewById(R.id.app_cook_show_list_item_3);

        if (left != null) {
            left.inflate();
        }
        if (center != null) {
            center.inflate();
        }
        if (right != null) {
            right.inflate();
        }

        updateItem(findViewById(R.id.app_cook_show_list_item_inflated_1), data.getItem(0), position * 3);
        updateItem(findViewById(R.id.app_cook_show_list_item_inflated_2), data.getItem(1), position * 3 + 1);
        updateItem(findViewById(R.id.app_cook_show_list_item_inflated_3), data.getItem(2), position * 3 + 2);
    }

    private void updateItem(View view, CookShow item, final int position) {
        if (item != null) {
            view.setVisibility(View.VISIBLE);
            ImageView imageView = (ImageView) view.findViewById(R.id.app_cook_show_cover);
            ImageFetcher.asInstance().load(item.getImage(), imageView);
        } else {
            view.setVisibility(View.INVISIBLE);
        }
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();

                bundle.putString(CookShowDetailListFragment.EXTRA_USER_ID, mFragment.getCurrentUserId());
                bundle.putInt(CookShowDetailListFragment.EXTRA_CURRENT_PAGE, mFragment.getLoadMore().getCurrentPage());
                bundle.putInt(CookShowDetailListFragment.EXTRA_PAGE_SIZE, mFragment.getLoadMore().getPageSize());
                bundle.putInt(CookShowDetailListFragment.EXTRA_CURRENT_POS, position);
                bundle.putSerializable(CookShowDetailListFragment.EXTRA_LIST, mFragment.getCookShowListData());

                mFragment.next(CookShowDetailListFragment.class, bundle);
            }
        });
    }
}
