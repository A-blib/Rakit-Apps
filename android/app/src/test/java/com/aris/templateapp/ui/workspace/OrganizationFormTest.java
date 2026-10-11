package com.aris.templateapp.ui.workspace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.aris.templateapp.R;

import org.junit.Test;

import java.util.Map;

public class OrganizationFormTest {

    private static OrganizationForm valid() {
        OrganizationForm form = new OrganizationForm();
        form.name = "OSIS SMA Negeri 1 Bandung";
        form.level = "sma";
        form.category = "osis_mpk";
        form.institution = "SMA Negeri 1 Bandung";
        return form;
    }

    @Test
    public void requiredFieldsOnly() {
        assertTrue(valid().validate().isEmpty());
    }

    @Test
    public void missingRequiredFields() {
        Map<OrganizationForm.Field, Integer> errors = new OrganizationForm().validate();
        assertEquals(R.string.org_error_name_required, (int) errors.get(OrganizationForm.Field.NAME));
        assertEquals(R.string.org_error_level_required, (int) errors.get(OrganizationForm.Field.LEVEL));
        assertEquals(R.string.org_error_category_required, (int) errors.get(OrganizationForm.Field.CATEGORY));
        assertEquals(R.string.org_error_institution_required, (int) errors.get(OrganizationForm.Field.INSTITUTION));
    }

    @Test
    public void periodMustBeOrderedYears() {
        OrganizationForm form = valid();
        form.periodStart = "2026";
        form.periodEnd = "2025";
        assertEquals(R.string.org_error_period_order, (int) form.validate().get(OrganizationForm.Field.PERIOD_END));

        form.periodStart = "26";
        assertEquals(R.string.org_error_year_invalid, (int) form.validate().get(OrganizationForm.Field.PERIOD_START));
        assertNull(OrganizationForm.year("1999"));
        assertEquals(Integer.valueOf(2027), OrganizationForm.year(" 2027 "));
    }

    @Test
    public void lengthLimits() {
        OrganizationForm form = valid();
        form.shortName = "x".repeat(41);
        form.position = "x".repeat(61);
        Map<OrganizationForm.Field, Integer> errors = form.validate();
        assertEquals(R.string.org_error_short_name_too_long, (int) errors.get(OrganizationForm.Field.SHORT_NAME));
        assertEquals(R.string.org_error_position_too_long, (int) errors.get(OrganizationForm.Field.POSITION));
    }
}
