package cn.wecook.app.dialog;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;

/**
 * 列表项对话框
 *
 * @author kevin
 * @version v1.0
 * @since 2015-1/24/15
 */
public class ListActionDialog extends ActionDialog {

    private AdapterView.OnItemClickListener mItemClickListener;

    public ListActionDialog(Context context, String[] items, AdapterView.OnItemClickListener onItemClickListener) {
        this(context, "", items, onItemClickListener);
    }

    public ListActionDialog(Context context, String title, String[] items, AdapterView.OnItemClickListener onItemClickListener) {
        super(context);

        setTitle(title);
        int pos = 0;
        for (String action : items) {
            final int position = pos++;
            addAction(action, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    if (mItemClickListener != null) {
                        mItemClickListener.onItemClick(null, v, position, 0);
                    }
                }
            });
        }
        mItemClickListener = onItemClickListener;
    }


}
