package com.aris.templateapp.ui.upload;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.List;
import java.util.Set;

public class ContentTagSuggesterTest {

    @Test
    public void pageNamesAndFileNameGiveSuggestionsInOfficialOrder() {
        Set<String> tags = ContentTagSuggester.suggest(
                List.of("index.html", "kontak.html", "struktur-pengurus.html", "Tentang.HTML"), "aksara-osis-berita.zip");

        assertEquals(List.of("profil", "struktur", "kontak", "berita"), List.copyOf(tags));
    }

    @Test
    public void nothingToSuggest() {
        assertTrue(ContentTagSuggester.suggest(List.of("index.html"), null).isEmpty());
        assertTrue(ContentTagSuggester.suggest(null, null).isEmpty());
    }
}
