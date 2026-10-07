package com.aris.templateapp.ui.upload;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

/** Satu section hasil {@code assets/upload/sections.js}; posisi dalam piksel CSS. */
public class SectionInfo {
    public String name;
    public int top;
    public int height;
    public String tag;

    /** Hasil evaluateJavascript berupa string JSON yang dibungkus tanda kutip, jadi di-parse dua kali. */
    static List<SectionInfo> parse(String evaluated) {
        try {
            String json = JsonParser.parseString(evaluated).getAsString();
            List<SectionInfo> list = new Gson().fromJson(json, new TypeToken<List<SectionInfo>>() { }.getType());
            return list == null ? new ArrayList<>() : list;
        } catch (RuntimeException e) {
            return new ArrayList<>();
        }
    }
}
