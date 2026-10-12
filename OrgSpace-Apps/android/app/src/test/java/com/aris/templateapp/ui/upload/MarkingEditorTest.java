package com.aris.templateapp.ui.upload;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.aris.templateapp.data.remote.dto.MarkingDto;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Logika editor Tandai bagian (alur-fitur-upload.md bagian 7). */
public class MarkingEditorTest {

    private MarkingEditor editor;

    @Before
    public void setUp() {
        editor = new MarkingEditor();
        editor.loadSaved(new MarkingDto());
        editor.ensurePages(Arrays.asList("index.html", "tentang-kami.html"));
        editor.markSaved();
        editor.applyDetectedSections("index.html", Arrays.asList(section("Header", 8), section("Hero", 13),
                section("Footer", 40)));
    }

    @Test
    public void pageNamesAndDetectedSectionsAreNotUnsavedChanges() {
        assertEquals("Beranda", editor.page("index.html").name);
        assertEquals("Tentang Kami", editor.page("tentang-kami.html").name);
        assertEquals(3, editor.sectionsOf("index.html").size());
        assertFalse(editor.hasUnsavedChanges());
    }

    @Test
    public void newFieldGetsUniqueKeyAndCountsAsUnsaved() {
        MarkingDto.Field first = editor.saveField(null, field("Judul utama"), element(14));
        MarkingDto.Field second = editor.saveField(null, field("Judul utama"), element(15));
        MarkingDto.Field accented = editor.saveField(null, field("Ñama Toko!"), element(16));
        assertEquals("judul_utama", first.key);
        assertEquals("judul_utama_2", second.key);
        assertEquals("nama_toko", accented.key);
        assertEquals(3, editor.unsavedCount());
        assertEquals(0, editor.savedFieldCount());
        editor.markSaved();
        assertFalse(editor.hasUnsavedChanges());
        assertEquals(3, editor.savedFieldCount());
    }

    @Test
    public void fieldsFollowPageOrderNotMarkingOrder() {
        editor.saveField(null, field("Deskripsi"), element(16));
        editor.saveField(null, field("Judul"), element(14));
        MarkingDto.Field footer = editor.saveField(null, field("Footer"), element(40));
        assertEquals("judul", editor.fields().get(0).key);
        assertEquals("deskripsi", editor.fields().get(1).key);
        assertEquals(1, editor.fields().get(0).order);
        assertEquals(3, footer.order);
    }

    @Test
    public void editingKeepsKeyAndElements() {
        MarkingDto.Field created = editor.saveField(null, field("Judul"), element(14));
        MarkingDto.Field edited = field("Judul besar");
        edited.maxLength = 40;
        editor.saveField(created.key, edited, element(14));
        MarkingDto.Field result = editor.field("judul");
        assertNotNull(result);
        assertEquals("Judul besar", result.label);
        assertEquals(Integer.valueOf(40), result.maxLength);
        assertEquals(1, result.elements.size());
    }

    @Test
    public void linkingMovesElementAndRemovesEmptyField() {
        editor.saveField(null, field("Judul HP"), element(14));
        MarkingDto.Field desktop = editor.saveField(null, field("Judul desktop"), element(20));
        editor.linkElement("judul_hp", element(20));
        assertNull(editor.field(desktop.key));
        assertEquals(2, editor.field("judul_hp").elements.size());
        assertEquals(editor.field("judul_hp"), editor.fieldOf("index.html", 20));
    }

    @Test
    public void undoRedoRestoreState() {
        editor.saveField(null, field("Judul"), element(14));
        editor.unmark("index.html", 14);
        assertTrue(editor.fields().isEmpty());
        editor.undo();
        assertEquals(1, editor.fields().size());
        editor.redo();
        assertTrue(editor.fields().isEmpty());
        editor.undo();
        editor.undo();
        assertTrue(editor.fields().isEmpty());
        assertFalse(editor.canUndo());
    }

    @Test
    public void sectionCorrections() {
        List<MarkingDto.Section> sections = editor.sectionsOf("index.html");
        MarkingDto.Field field = field("Judul");
        field.sectionId = sections.get(1).id;
        editor.saveField(null, field, element(14));

        editor.mergeWithNext(sections.get(0).id);
        assertEquals(2, editor.sectionsOf("index.html").size());
        // Isian di section yang dihapus pindah ke section tujuan.
        assertEquals(sections.get(0).id, editor.field("judul").sectionId);

        String created = editor.splitAt("index.html", 30, "Galeri", 1);
        assertEquals("Galeri", editor.sectionsOf("index.html").get(1).name);
        editor.renameSection(created, "Foto");
        assertEquals("Foto", editor.section(created).name);
        editor.deleteSection(created);
        assertEquals(2, editor.sectionsOf("index.html").size());
    }

    @Test
    public void suggestionsSkipMarkedElements() {
        editor.saveField(null, field("Judul"), element(14));
        MarkingEditor.Suggestion marked = suggestion(14, "text", "Judul");
        MarkingEditor.Suggestion image = suggestion(16, "image", "Gambar 1");
        assertEquals(1, editor.addSuggestions("index.html", null, Arrays.asList(marked, image)));
        assertEquals("image", editor.fieldOf("index.html", 16).type);
    }

    @Test
    public void themeAndProblems() {
        editor.setTheme("--primary", "Warna utama", MarkingEditor.themeType("#2563eb"));
        assertEquals("color", editor.theme("--primary").type);
        assertEquals("size", MarkingEditor.themeType("8px"));
        assertEquals("Font heading", MarkingEditor.themeLabel("--font-heading"));
        editor.setTheme("--primary", null, "color");
        assertNull(editor.theme("--primary"));

        editor.saveField(null, field("Judul"), element(14));
        assertNull(editor.firstProblem());
        editor.field("judul").label = " ";
        assertEquals("LABEL", editor.firstProblem());
    }

    @Test
    public void visibilityKeepsAtLeastOneView() {
        editor.saveField(null, field("Menu"), element(14));
        editor.updateVisibility("index.html", "desktop", Collections.singletonMap(14, true));
        assertEquals(Arrays.asList("mobile", "desktop"), editor.fieldOf("index.html", 14).elements.get(0).visibleIn);
        editor.updateVisibility("index.html", "mobile", Collections.singletonMap(14, false));
        assertEquals(Collections.singletonList("desktop"), editor.fieldOf("index.html", 14).elements.get(0).visibleIn);
    }

    private static SectionInfo section(String name, int tplId) {
        SectionInfo info = new SectionInfo();
        info.name = name;
        info.tplId = tplId;
        return info;
    }

    private static MarkingDto.Field field(String label) {
        MarkingDto.Field field = new MarkingDto.Field();
        field.label = label;
        field.type = "text";
        return field;
    }

    private static MarkingDto.Element element(int tplId) {
        return new MarkingDto.Element("index.html", tplId, new ArrayList<>(Collections.singletonList("mobile")));
    }

    private static MarkingEditor.Suggestion suggestion(int id, String kind, String label) {
        MarkingEditor.Suggestion s = new MarkingEditor.Suggestion();
        s.id = id;
        s.kind = kind;
        s.label = label;
        return s;
    }
}
