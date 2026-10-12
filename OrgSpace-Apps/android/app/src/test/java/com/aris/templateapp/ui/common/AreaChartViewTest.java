package com.aris.templateapp.ui.common;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** Nilai atas sumbu Y harus "bulat", tidak kurang dari nilai terbesar, dan habis dibagi 4 garis grid. */
public class AreaChartViewTest {

    @Test
    public void smallValuesUseMinimumScale() {
        assertEquals(4, AreaChartView.niceMax(0));
        assertEquals(4, AreaChartView.niceMax(3));
        assertEquals(4, AreaChartView.niceMax(4));
    }

    @Test
    public void largerValuesRoundUpToNiceSteps() {
        assertEquals(8, AreaChartView.niceMax(5));
        assertEquals(20, AreaChartView.niceMax(12));
        assertEquals(40, AreaChartView.niceMax(23));
        assertEquals(400, AreaChartView.niceMax(301));
        assertEquals(4000, AreaChartView.niceMax(3999));
    }
}
