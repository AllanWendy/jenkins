package cn.wecook.app.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wecook.sdk.policy.UpdatePolicy;

import cn.wecook.app.R;

/**
 * 升级提示对话框
 */
public class AppUpdateDialog extends UpdatePolicy.UpdateDialog {

    public AppUpdateDialog(Context context) {
        super(context);
    }

    @Override
    public int getTitleId() {
        return R.id.app_update_title;
    }

    @Override
    public int getDescriptionId() {
        return R.id.app_update_content;
    }

    @Override
    public int getOkButtonId() {
        return R.id.app_update_id_ok;
    }

    @Override
    public int getCancelButtonId() {
        return R.id.app_update_id_cancel;
    }

    @Override
    public int getIgnoreCheckId() {
        return R.id.app_update_id_check;
    }

    @Override
    public int getCloseId() {
        return R.id.app_update_id_close;
    }

    @Override
    public View getView(ViewGroup parent) {
        return LayoutInflater.from(getContext()).inflate(R.layout.app_update_dialog, parent, true);
    }
}
