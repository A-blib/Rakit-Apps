package com.aris.templateapp.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class EventTest {

    @Test
    public void contentIsDeliveredOnlyOnce() {
        Event<String> event = new Event<>("pesan");

        assertEquals("pesan", event.getContentIfNotHandled());
        assertNull(event.getContentIfNotHandled());
        // peekContent tetap bisa membaca isi walau sudah dipakai.
        assertEquals("pesan", event.peekContent());
    }
}
