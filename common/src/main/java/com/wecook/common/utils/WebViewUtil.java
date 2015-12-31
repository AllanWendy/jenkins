package com.wecook.common.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.webkit.WebView;

import com.wecook.common.R;
import com.wecook.common.core.debug.Logger;

import org.apache.http.protocol.HTTP;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLDecoder;
import java.util.Locale;

public class WebViewUtil {
    public static final String HYBRID_HTTP = "http://";
    public static final String HYBRID_HTTPS = "https://";
    public static final String HYBRID_BROWSER = "browser://";
    public static final String PROTOCOL_TEL = "tel";
    public static final String PROTOCOL_SMS = "sms";
    public static final String PROTOCOL_MAIL = "mailto";
    public static final String PROTOCOL_GEO = "geo";
    public static final String SEMICOLON = ":";
    public static final String AND = "&";
    public static final String MAIL_SUBJECT = "subject=";
    public static final String MAIL_BODY = "body=";
    public static final String MAIL_TO = "to=";
    public static final String POSTFIX_JPG = ".jpg";
    public static final String POSTFIX_BMP = ".bmp";
    public static final String POSTFIX_PNG = ".png";
    public static final String IMAGE = "image";

    public static final String HYBRID_URI = "uri";
    public static final String HYBRID_ACTION = "action";
    public static final String HYBRID_TYPE = "type";
    public static final String HYBRID_ARGS = "args";

    public static final String URL = "url";
    
	private static final String TAG = "WebViewUtil";

	public static void startActivity(WeakReference<Activity> ref, Intent intent) {
		if (intent == null || ref == null)
			return;

		Activity activity = ref.get();
		if (activity == null)
			return;
		activity.startActivity(intent);
	}

	public static boolean checkExtendRedirect(WeakReference<Activity> ref,
			WebView webview, String url) {
		if (webview == null || TextUtils.isEmpty(url))
			return false;
		Context context = webview.getContext();
		try {
			url = URLDecoder.decode(url, HTTP.UTF_8);
			int index = url.indexOf(HYBRID_BROWSER);
			if (index != -1) {
				// 外部浏览器打开
				url = url.substring(index + HYBRID_BROWSER.length());
				if (StringUtils.isEmpty(url))
					return true;
				if (!url.startsWith(HYBRID_HTTP)
						&& !url.startsWith(HYBRID_HTTPS))
					url = HYBRID_HTTP + url;
				Uri uri = Uri.parse(url);
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(ref, intent);
				return true;
			}

			String data = url.substring(url.indexOf(SEMICOLON) + 1);

			if (url.startsWith(PROTOCOL_TEL)) {
				TelephonyManager telMgr = (TelephonyManager) context
						.getSystemService(Context.TELEPHONY_SERVICE);
				if (telMgr.getSimState() == TelephonyManager.SIM_STATE_READY)
					return tel(ref, url);
				return true;
			}
			if (url.startsWith(PROTOCOL_SMS))
				return sms(ref, data);
			if (url.startsWith(PROTOCOL_MAIL))
				return mail(ref, data);
			if (url.startsWith(PROTOCOL_GEO))
				return geo(ref, url);
		} catch (ActivityNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return false;
	}

	private static boolean tel(final WeakReference<Activity> ref,
			final String url) throws ActivityNotFoundException {
		startActivity(ref, new Intent(Intent.ACTION_DIAL, Uri.parse(url)));
		return true;
	}

	private static boolean sms(WeakReference<Activity> ref, String body)
			throws ActivityNotFoundException {
		Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"
				+ ""));
		intent.putExtra("sms_body", body);
		startActivity(ref, intent);
		return true;
	}

	private static boolean mail(WeakReference<Activity> ref, String content)
			throws ActivityNotFoundException {
		if (TextUtils.isEmpty(content))
			return false;
		String to = getMailPart(content, MAIL_TO);
        Logger.d(TAG, "mail to: " + to);
		String subject = getMailPart(content, MAIL_SUBJECT);
        Logger.d(TAG, "mail subject: " + subject);
		String body = getMailPart(content, MAIL_BODY);
		Logger.d(TAG, "mail body: " + body);
		Intent intent = new Intent(Intent.ACTION_SEND);

		String[] tos = { to };
		intent.putExtra("compose_mode", false);
		intent.putExtra(Intent.EXTRA_EMAIL, tos);
		intent.putExtra(Intent.EXTRA_SUBJECT, subject);
		intent.putExtra(Intent.EXTRA_TEXT, body);
		intent.setType("message/rfc822");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setClassName("com.android.mms",
				"com.android.mms.ui.ComposeMessageActivity");
		startActivity(ref, Intent.createChooser(intent, "使用以下方式发送"));
		return true;
	}

	private static boolean geo(WeakReference<Activity> ref, String url)
			throws ActivityNotFoundException {
		if (TextUtils.isEmpty(url))
			return false;
		Logger.d(TAG, "geo url: " + url);
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(ref, intent);
		return true;
	}

	private static String getMailPart(String content, String part) {
		if (TextUtils.isEmpty(part) || TextUtils.isEmpty(content))
			return null;
		int index = content.indexOf(part);
		if (index < 0 || index >= content.length())
			return null;
		content = content.substring(index + part.length());
		index = content.indexOf(AND);
		if (index < 0)
			return content;
		return content.substring(0, index);
	}

    /**
     * Looks at sLocale and mContext and returns current UserAgent String.
     * @return Current UserAgent String.
     */
    public static String getCurrentUserAgent(Context context) {
        Locale locale = Locale.getDefault();
        StringBuffer buffer = new StringBuffer();
        // Add version
        final String version = Build.VERSION.RELEASE;
        if (version.length() > 0) {
            if (Character.isDigit(version.charAt(0))) {
                // Release is a version, eg "3.1"
                buffer.append(version);
            } else {
                // Release is a codename, eg "Honeycomb"
                // In this case, use the previous release's version
                buffer.append("4.0");
            }
        } else {
            // default to "1.0"
            buffer.append("1.0");
        }
        buffer.append("; ");
        final String language = locale.getLanguage();
        if (language != null) {
            buffer.append(convertObsoleteLanguageCodeToNew(language));
            final String country = locale.getCountry();
            if (country != null) {
                buffer.append("-");
                buffer.append(country.toLowerCase());
            }
        } else {
            // default to "en"
            buffer.append("en");
        }
        buffer.append(";");
        // add the model for the release build
        if ("REL".equals(Build.VERSION.CODENAME)) {
            final String model = Build.MODEL;
            if (model.length() > 0) {
                buffer.append(" ");
                buffer.append(model);
            }
        }
        final String id = Build.ID;
        if (id.length() > 0) {
            buffer.append(" Build/");
            buffer.append(id);
        }
        String mobile = context.getResources().getText(
                R.string.web_user_agent_target_content).toString();
        final String base = context.getResources().getText(
                R.string.web_user_agent).toString();
        return String.format(base, buffer, mobile);
    }

    private static String convertObsoleteLanguageCodeToNew(String langCode) {
        if (langCode == null) {
            return null;
        }
        if ("iw".equals(langCode)) {
            // Hebrew
            return "he";
        } else if ("in".equals(langCode)) {
            // Indonesian
            return "id";
        } else if ("ji".equals(langCode)) {
            // Yiddish
            return "yi";
        }
        return langCode;
    }
}
