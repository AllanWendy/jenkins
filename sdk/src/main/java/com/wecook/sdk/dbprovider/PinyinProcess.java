package com.wecook.sdk.dbprovider;

import android.content.Context;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.wecook.common.core.debug.Logger;
import com.wecook.common.utils.FileUtils;
import com.wecook.common.utils.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/4/10
 */
public class PinyinProcess {

    public static List<PinyinData> mList = new ArrayList<PinyinData>();

    public static void processPinyin(final Context context, final String ingredientInfoJson) {

        Logger.d("inspire-search", "start..");
        InputStream is = null;
        InputStreamReader reader = null;
        ByteArrayOutputStream out = null;
        //读取食材信息
        try {
            is = context.getAssets().open(ingredientInfoJson);
            reader = new InputStreamReader(is);
            JsonReader jsonReader = new JsonReader(reader);
            jsonReader.setLenient(false);

            readData(context, jsonReader);


            out = new ByteArrayOutputStream();
            JsonWriter jsonWriter = new JsonWriter(new OutputStreamWriter(out, "utf-8"));
            jsonWriter.setIndent("");

            jsonWriter.beginObject();
            for (PinyinData data : mList) {
                jsonWriter.name(data.data);
                jsonWriter.beginArray();
                jsonWriter.value(data.firstLetter);
                jsonWriter.value(data.fullpinyin);
                jsonWriter.endArray();
            }
            jsonWriter.endObject();

            jsonWriter.flush();
            byte[] bytes = out.toByteArray();

            Logger.d("inspire-search", "bytes size " + bytes.length);

            FileUtils.newFile(bytes, "ingredients.json");

            Logger.d("inspire-search", "end..");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }

    private static void readData(Context context, JsonReader jsonReader) throws IOException {
        JsonToken token = jsonReader.peek();
        switch (token) {
            case BEGIN_ARRAY:
                jsonReader.beginArray();
                while (jsonReader.hasNext()) {
                    PinyinData pinyinData = new PinyinData();
                    pinyinData.data = jsonReader.nextString();
                    pinyinData.fullpinyin = StringUtils.getPinyinString(context, pinyinData.data);
                    pinyinData.firstLetter = StringUtils.getPinyinString(context, pinyinData.data, true);
                    mList.add(pinyinData);
                }
                jsonReader.endArray();
                break;
        }
    }

    private static class PinyinData {
        String data;
        String fullpinyin;
        String firstLetter;
    }
}
