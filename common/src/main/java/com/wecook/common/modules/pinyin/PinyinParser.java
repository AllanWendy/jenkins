package com.wecook.common.modules.pinyin;

import android.content.Context;

import com.wecook.common.modules.pinyin.format.HanyuPinyinOutputFormat;
import com.wecook.common.modules.pinyin.format.exception.BadHanyuPinyinOutputFormatCombination;

public class PinyinParser {
	private ChineseToPinyinResource mCtp;
	
	public PinyinParser(Context context,String resourceName) {
		mCtp = new ChineseToPinyinResource(context, resourceName);
	}
	
	public String[] toHanyuPinyinStringArray(char ch,
            HanyuPinyinOutputFormat outputFormat)
            throws BadHanyuPinyinOutputFormatCombination
    {
        return getFormattedHanyuPinyinStringArray(ch, outputFormat);
    }
	
	private String[] getFormattedHanyuPinyinStringArray(char ch,
            HanyuPinyinOutputFormat outputFormat)
            throws BadHanyuPinyinOutputFormatCombination
    {
        String[] pinyinStrArray = getUnformattedHanyuPinyinStringArray(ch);

        if (null != pinyinStrArray)
        {

            for (int i = 0; i < pinyinStrArray.length; i++)
            {
                pinyinStrArray[i] = PinyinFormatter.formatHanyuPinyin(pinyinStrArray[i], outputFormat);
            }

            return pinyinStrArray;

        } else
            return null;
    }
	
	private String[] getUnformattedHanyuPinyinStringArray(char ch)
    {
        return mCtp.getHanyuPinyinStringArray(ch);
    }
}
