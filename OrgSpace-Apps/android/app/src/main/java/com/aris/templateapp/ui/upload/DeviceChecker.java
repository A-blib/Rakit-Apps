package com.aris.templateapp.ui.upload;

import android.content.Context;

import com.aris.templateapp.R;
import com.aris.templateapp.data.remote.dto.DeviceWarningsDto;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Tahap C pengecekan (alur-fitur-upload.md bagian 5.1 & 5.6): menjalankan setiap halaman di WebView HP dengan lebar
 * tampilan HP, lalu mencatat error JavaScript dan tampilan yang melebar ke samping. Hasilnya hanya Peringatan.
 */
class DeviceChecker {

    interface Done {
        void onDone(List<DeviceWarningsDto.Warning> warnings);
    }

    private static final int MAX_WARNINGS = 20;
    private static final String OVERFLOW_SCRIPT = "(function(){return JSON.stringify({w: window.innerWidth,"
            + " sw: document.documentElement.scrollWidth});})()";

    private final Context context;
    private final OffscreenPage page;
    private final List<DeviceWarningsDto.Warning> warnings = new ArrayList<>();

    DeviceChecker(Context context, OffscreenPage page) {
        this.context = context;
        this.page = page;
    }

    void run(File siteRoot, List<String> allowedHosts, List<String> pages, Done done) {
        warnings.clear();
        next(siteRoot, allowedHosts, new ArrayList<>(pages), done);
    }

    private void next(File siteRoot, List<String> allowedHosts, List<String> remaining, Done done) {
        if (remaining.isEmpty() || warnings.size() >= MAX_WARNINGS) {
            page.destroy();
            done.onDone(new ArrayList<>(warnings.subList(0, Math.min(MAX_WARNINGS, warnings.size()))));
            return;
        }
        String current = remaining.remove(0);
        try {
            page.load(siteRoot, allowedHosts, current, OffscreenPage.MOBILE_WIDTH, () ->
                    page.evaluate(OVERFLOW_SCRIPT, value -> {
                        collect(current, value);
                        next(siteRoot, allowedHosts, remaining, done);
                    }));
        } catch (IOException e) {
            next(siteRoot, allowedHosts, remaining, done);
        }
    }

    private void collect(String current, String overflowJson) {
        for (OffscreenPage.ConsoleError error : page.consoleErrors()) {
            String file = error.file.isEmpty() ? current : error.file;
            warnings.add(new DeviceWarningsDto.Warning("JS_RUNTIME_ERROR",
                    cut(context.getString(R.string.upload_device_js_error, file, error.line, error.message)),
                    file, error.line > 0 ? error.line : null));
        }
        try {
            // evaluateJavascript mengembalikan string JSON yang dibungkus tanda kutip, jadi di-parse dua kali.
            JsonObject size = JsonParser.parseString(JsonParser.parseString(overflowJson).getAsString()).getAsJsonObject();
            int width = size.get("w").getAsInt();
            int scrollWidth = size.get("sw").getAsInt();
            if (scrollWidth > width + 2) {
                warnings.add(new DeviceWarningsDto.Warning("HORIZONTAL_OVERFLOW",
                        context.getString(R.string.upload_device_overflow, current, scrollWidth, width), current, null));
            }
        } catch (RuntimeException ignored) {
            // Halaman gagal dijalankan sampai akhir; error JS-nya sudah tercatat di atas.
        }
    }

    private static String cut(String text) {
        return text.length() <= 300 ? text : text.substring(0, 299) + "…";
    }
}
