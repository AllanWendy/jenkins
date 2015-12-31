package com.wecook.uikit.window;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.PopupWindowCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.wecook.common.utils.ScreenUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.uikit.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 列表窗口
 *
 * @author kevin
 * @version v1.0
 * @since 2015-1/27/15
 */
public class PopListWindow extends BaseWindow {

    private static final String ITEM_NAME = "TITLE";
    private PopupWindow mWindow;
    private View mContentView;
    private SimpleAdapter mItemAdapter;
    private ListView mListView;
    private TextView mTextView;
    private List<Map<String, Object>> mContents = new ArrayList<Map<String, Object>>();

    private int mBackgroundColor;
    private Drawable mBackground;
    private int mListSelector;
    private boolean mFocusable;
    private int mViewX;
    private int mViewY;
    private View mAnchorView;

    private Context mContext;

    public PopListWindow(Context context) {
        mContext = context;
        if (mContentView == null) {
            mContentView = LayoutInflater.from(context).inflate(R.layout.uikit_window_list, null);
        }
        mListView = (ListView) mContentView.findViewById(R.id.uikit_listview);
        mTextView = (TextView) mContentView.findViewById(R.id.uikit_invisible_text);
        mItemAdapter = new SimpleAdapter(context, mContents, R.layout.uikit_window_list_item,
                new String[]{ITEM_NAME}, new int[]{R.id.uikit_text});
        mListView.setAdapter(mItemAdapter);
    }

    public PopListWindow addItem(String itemName) {
        if (StringUtils.isEmpty(itemName)) {
            return this;
        }
        Map<String, Object> item = new HashMap<String, Object>();
        item.put(ITEM_NAME, itemName);
        mContents.add(item);
        mTextView.setText(mTextView.getText().toString() + itemName + "\n");
        if (mItemAdapter != null) {
            mItemAdapter.notifyDataSetChanged();
        }
        return this;
    }

    public PopListWindow setContentBackgroundColor(int color) {
        mBackgroundColor = color;
        return this;
    }

    public PopListWindow setWindowBackground(Drawable drawable) {
        mBackground = drawable;
        return this;
    }

    public PopListWindow setListSelector(int resId) {
        mListSelector = resId;
        return this;
    }

    public PopListWindow setFocusable(boolean focusable) {
        mFocusable = focusable;
        return this;
    }

    public PopListWindow addItems(List<String> itemNames) {
        String text = "";
        for (String itemName : itemNames) {
            if (StringUtils.isEmpty(itemName)) {
                continue;
            }
            Map<String, Object> item = new HashMap<String, Object>();
            item.put(ITEM_NAME, itemName);
            text += itemName + "\n";
            mContents.add(item);
        }
        mTextView.setText(mTextView.getText().toString() + text);
        if (mItemAdapter != null) {
            mItemAdapter.notifyDataSetChanged();
        }
        return this;
    }

    public PopListWindow addItems(String[] itemNames) {
        String text = "";
        for (String itemName : itemNames) {
            if (StringUtils.isEmpty(itemName)) {
                continue;
            }
            Map<String, Object> item = new HashMap<String, Object>();
            item.put(ITEM_NAME, itemName);
            text += itemName + "\n";
            mContents.add(item);
        }
        mTextView.setText(mTextView.getText().toString() + text);
        if (mItemAdapter != null) {
            mItemAdapter.notifyDataSetChanged();
        }
        return this;
    }

    public void clear() {
        mContents.clear();
        if (mItemAdapter != null) {
            mItemAdapter.notifyDataSetChanged();
        }

        update();
    }

    public String getItemName(int position) {
        if (position >= 0 && position < mContents.size()) {
            Map<String, Object> item = mContents.get(position);
            return item.get(ITEM_NAME).toString();
        }
        return "";
    }

    public PopListWindow setItemClickListener(AdapterView.OnItemClickListener listener) {
        if (mListView != null) {
            mListView.setOnItemClickListener(listener);
        }
        return this;
    }

    public PopListWindow showAtViewSide(View view, int x, int y) {
        if (mBackgroundColor != 0) {
            mContentView.setBackgroundColor(mBackgroundColor);
        }

        if (mListSelector != 0) {
            mListView.setSelector(mListSelector);
        }

        mWindow = new PopupWindow(mContentView, WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT, mFocusable);
        if (mBackground != null) {
            mWindow.setBackgroundDrawable(mBackground);
        } else {
            mWindow.setBackgroundDrawable(new BitmapDrawable());
        }
        mWindow.setOutsideTouchable(true);
        mWindow.setTouchable(true);
        mWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NOT_NEEDED);
        mWindow.setFocusable(true);
        PopupWindowCompat.showAsDropDown(mWindow, view, x, y, Gravity.TOP | Gravity.START);
        return this;
    }

    public PopListWindow showInView(View view, int x, int y) {
        mWindow = new PopupWindow(mContentView, WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT, mFocusable);
        mWindow.setBackgroundDrawable(new BitmapDrawable());
        mWindow.setOutsideTouchable(true);
        mWindow.setTouchable(true);
        mWindow.showAtLocation(view, Gravity.CENTER, x, y);
        return this;
    }

    public void dismiss() {
        if (mWindow != null) {
            mWindow.dismiss();
        }
    }

    public PopupWindow getWindow() {
        return mWindow;
    }

    public void update(View view, int x, int y) {
        if (mWindow != null && view != null) {
            mViewX = x;
            mViewY = y;
            mAnchorView = view;
            PopupWindowCompat.showAsDropDown(mWindow, view, x, y, Gravity.TOP | Gravity.START);
        }
    }

    private void update() {
        update(mAnchorView, mViewX, mViewY);
    }

    public void setListAdapter(ListAdapter adapter, List<String> names, boolean hasIcon) {
        mListView.setAdapter(adapter);
        addItems(names);
        if (hasIcon) {
            mTextView.setPadding(ScreenUtils.dip2px(mContext, 50), mTextView.getTop(),
                    mTextView.getRight(), mTextView.getBottom());
        }
    }
}
