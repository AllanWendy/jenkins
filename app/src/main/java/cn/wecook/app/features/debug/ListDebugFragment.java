package cn.wecook.app.features.debug;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.wecook.common.utils.JavaRefactorUtils;
import com.wecook.uikit.adapter.UIAdapter;
import com.wecook.uikit.fragment.BaseListFragment;
import com.wecook.uikit.widget.pulltorefresh.PullToRefreshBase;

import java.util.ArrayList;
import java.util.List;

import cn.wecook.app.R;

/**
 * 调试界面
 *
 * @author kevin
 * @version v1.0
 * @since 2015-1/12/15
 */
public class ListDebugFragment extends BaseListFragment {

    private List<MockClass> mMocks = new ArrayList<MockClass>();

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initList();
        setTitle("测试功能列表");
        getListView().setDivider(new ColorDrawable(getResources().getColor(R.color.uikit_grey_light)));
        getListView().setDividerHeight(5);
        getRefreshListLayout().setMode(PullToRefreshBase.Mode.DISABLED);
        setListAdapter(getListView(), new MockFragmentAdapter(getContext(), mMocks));
    }

    private void initList() {
        addMockClass(MorseCodeDebugFragment.class);
        addMockClass(PhoneInfoDebugFragment.class);
        addMockClass(ConfigInfoDebugFragment.class);
        addMockClass(JsBridgeDebugFragment.class);
    }

    private void addMockClass(final Class<? extends IDebug> fragment) {
        final IDebug debug = JavaRefactorUtils.newInstance(fragment);
        final String title = debug.getTitle();
        mMocks.add(new MockClass(title, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                next(debug.getFragmentCls(), title);
            }
        }));
    }

    private class MockClass {
        private String title;
        private View.OnClickListener click;

        private MockClass(String title, View.OnClickListener click) {
            this.title = title;
            this.click = click;
        }
    }

    public class MockFragmentAdapter extends UIAdapter<MockClass> {

        public MockFragmentAdapter(Context context, List<MockClass> data) {
            super(context, R.layout.listview_item_search_suggestion, data);
        }

        @Override
        public void updateView(int position, int viewType, MockClass data, Bundle extra) {
            super.updateView(position, viewType, data, extra);

            TextView textView = (TextView) findViewById(R.id.app_search_suggestion_name);
            textView.setText(data.title);
            getItemView().setOnClickListener(data.click);
        }
    }
}
