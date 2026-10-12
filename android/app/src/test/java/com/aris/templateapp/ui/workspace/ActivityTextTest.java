package com.aris.templateapp.ui.workspace;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import com.aris.templateapp.R;
import com.aris.templateapp.data.model.Activity;

import org.junit.Test;

import java.util.Map;

public class ActivityTextTest {

    @Test
    public void roleChangeNamesMemberAndNewRole() {
        Activity activity = new Activity("1", "Rina", "member.role_changed",
                Map.of("memberName", "Budi", "from", "editor", "to", "admin"), null);

        ActivityText.Sentence sentence = ActivityText.of(activity, "Seseorang");

        assertEquals(R.string.activity_role_changed, sentence.text);
        assertArrayEquals(new Object[] {"Rina", "Budi", R.string.role_admin}, sentence.args);
    }

    @Test
    public void missingActorAndUnknownAction() {
        Activity activity = new Activity("2", null, "site.published", null, null);

        ActivityText.Sentence sentence = ActivityText.of(activity, "Seseorang");

        assertEquals(R.string.activity_other, sentence.text);
        assertArrayEquals(new Object[] {"Seseorang"}, sentence.args);
    }

    @Test
    public void transferUsesReceiverName() {
        Activity activity = new Activity("3", "Rina", "org.ownership_transferred",
                Map.of("memberName", "Budi", "previousOwnerNow", "leave"), null);

        assertArrayEquals(new Object[] {"Rina", "Budi"}, ActivityText.of(activity, "Seseorang").args);
    }

    @Test
    public void websiteRenameShowsOldAndNewName() {
        Activity activity = new Activity("4", "Sari", "website.renamed", Map.of("from", "Aksara", "to", "Pensi"),
                null);

        ActivityText.Sentence sentence = ActivityText.of(activity, "Seseorang");

        assertEquals(R.string.activity_website_renamed, sentence.text);
        assertArrayEquals(new Object[] {"Sari", "Aksara", "Pensi"}, sentence.args);
    }
}
