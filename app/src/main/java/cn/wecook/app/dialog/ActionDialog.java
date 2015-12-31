package cn.wecook.app.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wecook.common.utils.StringUtils;
import com.wecook.uikit.alarm.DialogAlarm;

import java.util.ArrayList;
import java.util.List;

import cn.wecook.app.R;


/**
 * 动作组dialog
 *
 * @author lenovo
 * @since 2014/12/25.
 */
public class ActionDialog extends DialogAlarm {

    class Action {
        String name;
        View.OnClickListener clickListener;
    }

    private List<Action> mActions = new ArrayList<Action>();

    private String mTitle;

    public ActionDialog(Context context) {
        super(context);
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setTitle(int titleId) {
        mTitle = getContext().getString(titleId);
    }

    /**
     * 添加动作
     *
     * @param nameId
     * @param clickListener
     */
    public void addAction(int nameId, View.OnClickListener clickListener) {
        String name = getContext().getString(nameId);
        addAction(name, clickListener);
    }

    /**
     * 添加动作
     *
     * @param name
     * @param clickListener
     */
    public void addAction(String name, View.OnClickListener clickListener) {
        Action action = new Action();
        action.name = name;
        action.clickListener = clickListener;
        mActions.add(action);
    }

    @Override
    public View getView(ViewGroup parent) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_action, parent, true);
        ViewGroup titleGroup = (ViewGroup) view.findViewById(R.id.app_dialog_action_title_layout);
        TextView title = (TextView) view.findViewById(R.id.app_dialog_action_title);
        if (!StringUtils.isEmpty(mTitle)) {
            titleGroup.setVisibility(View.VISIBLE);
            title.setText(mTitle);
        } else {
            titleGroup.setVisibility(View.GONE);
        }

        ViewGroup group = (ViewGroup) view.findViewById(R.id.app_dialog_action_layout);
        for (Action action : mActions) {
            View child = LayoutInflater.from(getContext()).inflate(R.layout.dialog_action_item, null);
            TextView textView = (TextView) child.findViewById(R.id.app_dialog_action_name);
            textView.setText(action.name);
            textView.setOnClickListener(action.clickListener);
            group.addView(child);
        }
        return view;
    }

    @Override
    public void onViewCreated(View view) {

    }
}
