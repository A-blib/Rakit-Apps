package com.aris.templateapp.core.template;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.aris.templateapp.data.model.ProjectMode;
import com.aris.templateapp.data.model.ProjectValues;
import com.aris.templateapp.data.model.Website;
import com.aris.templateapp.data.model.WebsiteDetail;
import com.aris.templateapp.data.model.WebsiteStatus;
import com.google.gson.Gson;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Tes behavior integrasi editor untuk website online.
 */
public class WebsiteEditorBehaviorTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Test
    public void projectStoreWritesFromMapCorrectly() throws IOException {
        File baseDir = temp.newFolder("files");
        Gson gson = new Gson();
        TemplateFiles files = new TemplateFiles(new File(baseDir, "templates"), new File(baseDir, "projects"), gson);
        ProjectStore store = new ProjectStore(files, gson);

        String projectId = "test-proj-1";
        Map<String, Object> contentMap = new HashMap<>();

        Map<String, Object> fieldsMap = new HashMap<>();
        Map<String, Object> titleField = new HashMap<>();
        titleField.put("text", "Judul Website Organisasi");
        titleField.put("href", "https://example.com");
        fieldsMap.put("judul_utama", titleField);

        Map<String, Object> imageField = new HashMap<>();
        imageField.put("image", "images/logo.webp");
        fieldsMap.put("logo_organisasi", imageField);

        contentMap.put("fields", fieldsMap);

        Map<String, Object> stylesMap = new HashMap<>();
        Map<String, String> titleStyle = new HashMap<>();
        titleStyle.put("color", "#ff0000");
        titleStyle.put("font-size", "24");
        stylesMap.put("judul_utama", titleStyle);
        contentMap.put("styles", stylesMap);

        Map<String, String> themeMap = new HashMap<>();
        themeMap.put("--primary", "#123456");
        contentMap.put("theme", themeMap);

        // Tulis map ke values.json
        store.writeFromMap(projectId, contentMap);

        // Baca kembali
        ProjectValues values = store.read(projectId);
        assertNotNull(values);
        assertEquals(2, values.fields.size());
        assertEquals("Judul Website Organisasi", values.field("judul_utama").text);
        assertEquals("https://example.com", values.field("judul_utama").href);
        assertEquals("images/logo.webp", values.field("logo_organisasi").image);

        assertNotNull(values.styles.get("judul_utama"));
        assertEquals("#ff0000", values.styles.get("judul_utama").get("color"));
        assertEquals("24", values.styles.get("judul_utama").get("font-size"));

        assertEquals("#123456", values.theme.get("--primary"));
    }

    @Test
    public void websiteDetailPreservesTemplateInformation() {
        Website website = new Website("w-100", "org-1", "Web Sekolah", ProjectMode.TEMPLATE,
                WebsiteStatus.ONLINE, false, "http://localhost:8080/sites/web-sekolah/",
                0, null, 1000L, 2000L, "Aris", null);

        WebsiteDetail detail = new WebsiteDetail(website, 2000L, null, null,
                "tpl-sekolah", 3, new HashMap<>());

        assertEquals("tpl-sekolah", detail.sourceTemplateId);
        assertEquals(Integer.valueOf(3), detail.templateVersion);
        assertEquals("Web Sekolah", detail.website.name);
        assertEquals(ProjectMode.TEMPLATE, detail.website.mode);
        assertNotNull(detail.content);
    }

    @Test
    public void projectStoreWriteFromEmptyMapDoesNotThrow() throws IOException {
        File baseDir = temp.newFolder("files2");
        Gson gson = new Gson();
        TemplateFiles files = new TemplateFiles(new File(baseDir, "templates"), new File(baseDir, "projects"), gson);
        ProjectStore store = new ProjectStore(files, gson);

        store.writeFromMap("proj-empty", null);
        store.writeFromMap("proj-empty", new HashMap<>());

        ProjectValues values = store.read("proj-empty");
        assertNotNull(values);
        assertEquals(0, values.fields.size());
    }
}
