package com.aris.templateapp.core.template;

import com.aris.templateapp.data.model.TemplateManifest;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/** Paket contoh di src/test/resources/template-packages/ untuk test template mode. */
final class TestPackages {

    private TestPackages() {
    }

    static File dir(String name) {
        URL url = TestPackages.class.getClassLoader().getResource("template-packages/" + name + "/manifest.json");
        try {
            return new File(url.toURI()).getParentFile();
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    static TemplateManifest manifest(String name) throws IOException {
        try (Reader reader = new InputStreamReader(new FileInputStream(new File(dir(name), "manifest.json")),
                StandardCharsets.UTF_8)) {
            return new Gson().fromJson(reader, TemplateManifest.class);
        }
    }
}
