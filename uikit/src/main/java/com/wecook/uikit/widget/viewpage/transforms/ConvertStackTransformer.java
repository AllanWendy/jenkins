package com.wecook.uikit.widget.viewpage.transforms;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;

public class ConvertStackTransformer extends ABaseTransformer {

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
	protected void onTransform(View view, float position) {
        view.setTranslationX(position > 0 ? -view.getWidth() * position * 0.9f : view.getWidth() * position * 0.2f);
    }

}
