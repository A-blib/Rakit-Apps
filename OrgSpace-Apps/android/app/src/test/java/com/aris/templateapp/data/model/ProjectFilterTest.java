package com.aris.templateapp.data.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ProjectFilterTest {

    @Test
    public void defaultIsNotFilteredAndSortsByLastEdit() {
        assertFalse(ProjectFilter.DEFAULT.isFiltered());
        assertEquals(ProjectFilter.Sort.UPDATED, ProjectFilter.DEFAULT.sort);
    }

    @Test
    public void clearKeepsSortButDropsStatusAndQuery() {
        ProjectFilter filter = ProjectFilter.DEFAULT.withStatus(ProjectStatus.DRAFT).withQuery("  kue ")
                .withSort(ProjectFilter.Sort.NAME);
        assertTrue(filter.isFiltered());
        assertEquals("kue", filter.query);

        ProjectFilter cleared = filter.cleared();
        assertNull(cleared.status);
        assertEquals("", cleared.query);
        assertEquals(ProjectFilter.Sort.NAME, cleared.sort);
        assertEquals(cleared, new ProjectFilter(null, "", ProjectFilter.Sort.NAME));
    }
}
