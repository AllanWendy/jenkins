package cn.wecook.app.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wecook.common.modules.property.SharePreferenceProperties;
import com.wecook.uikit.alarm.DialogAlarm;

import cn.wecook.app.R;

/**
 * 定位前提示对话框
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/5/14
 */
public class AreaServerTipDialog extends DialogAlarm {

    public static final String IS_SHOW = "area_server_tip_has_shown";

    public AreaServerTipDialog(Context context) {
        super(context);
        cancelable(false);
    }

    @Override
    public View getView(ViewGroup parent) {
        return LayoutInflater.from(getContext()).inflate(R.layout.dialog_area_server_tip, parent, true);
    }

    @Override
    public void onViewCreated(View view) {

        final ImageView select = (ImageView) view.findViewById(R.id.app_area_server_tip_select);
        TextView iknown = (TextView) view.findViewById(R.id.app_area_server_tip_button);
        select.setSelected(isDontShowAgain());
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDontShowAgain(!isDontShowAgain());
                select.setSelected(isDontShowAgain());
            }
        });

        iknown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private boolean isDontShowAgain() {
        return (boolean) SharePreferenceProperties.get(IS_SHOW, false);
    }

    private void setDontShowAgain(boolean showAgain) {
        SharePreferenceProperties.set(IS_SHOW, showAgain);
    }
}
