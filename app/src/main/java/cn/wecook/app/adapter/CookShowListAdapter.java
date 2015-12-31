package cn.wecook.app.adapter;

import android.os.Bundle;
import android.view.View;

import com.wecook.sdk.api.model.CookShow;
import com.wecook.uikit.adapter.UIAdapter;
import com.wecook.uikit.fragment.BaseFragment;

import java.util.List;

import cn.wecook.app.R;
import cn.wecook.app.main.recommend.detail.cookshow.CookShowDetailFragment;
import cn.wecook.app.main.recommend.detail.cookshow.CookShowScoreView;

/**
 * 晒厨艺
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/23/14
 */
public class CookShowListAdapter extends UIAdapter<CookShow> {

    private BaseFragment baseFragment;

    public CookShowListAdapter(BaseFragment fragment, List<CookShow> data) {
        super(fragment.getContext(), data);
        this.baseFragment = fragment;
    }

    @Override
    protected View newView(int viewType) {
        return View.inflate(getContext(), R.layout.listview_item_cookshow_style_detail, null);
    }

    @Override
    public void updateView(int position, int viewType, final CookShow data, Bundle extra) {
        super.updateView(position, viewType, data, extra);

        if (getItemView() instanceof CookShowScoreView) {
            ((CookShowScoreView) getItemView()).updateView(baseFragment, data);
            ((CookShowScoreView) getItemView()).whereFrom(CookShowScoreView.FROM_LIST);
        }
        getItemView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putSerializable(CookShowDetailFragment.EXTRA_COOK_SHOW, data);
                baseFragment.next(CookShowDetailFragment.class, bundle);
            }
        });
    }
}
