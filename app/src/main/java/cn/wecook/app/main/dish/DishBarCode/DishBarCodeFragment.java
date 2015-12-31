package cn.wecook.app.main.dish.DishBarCode;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wecook.uikit.fragment.BaseTitleFragment;
import com.wecook.uikit.widget.EmptyView;

import cn.wecook.app.R;

/**
 * 条码展示界面
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/18/14
 */
public class DishBarCodeFragment extends BaseTitleFragment {

    private EmptyView emptyView;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_barcode, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle("扫码展示");
        getTitleBar().enableBack(false);
        getTitleBar().setBackTitle("重新扫描");
        getTitleBar().setBackTitleClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back();
            }
        });
        emptyView = (EmptyView) view.findViewById(R.id.uikit_empty);
        emptyView.setTitle(getString(R.string.app_empty_title_barcode));
        emptyView.setSecondTitle(getString(R.string.app_empty_second_title_barcode));
    }

    public void showEmpty() {
        if (emptyView != null) {
            emptyView.setVisibility(View.VISIBLE);
        }
    }

    public void hideEmpty() {
        if (emptyView != null) {
            emptyView.setVisibility(View.GONE);
        }
    }
}
