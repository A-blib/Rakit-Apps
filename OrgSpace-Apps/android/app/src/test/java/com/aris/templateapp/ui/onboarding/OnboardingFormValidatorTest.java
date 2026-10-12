package com.aris.templateapp.ui.onboarding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.aris.templateapp.R;
import com.aris.templateapp.ui.onboarding.OnboardingFormValidator.Field;

import org.junit.Test;

import java.util.Map;

public class OnboardingFormValidatorTest {

    @Test
    public void creatorNeedsDisplayName() {
        assertEquals(Integer.valueOf(R.string.error_display_name_required),
                OnboardingFormValidator.validateCreator("  ").get(Field.DISPLAY_NAME));
        assertTrue(OnboardingFormValidator.validateCreator("Aris").isEmpty());
    }

    @Test
    public void validProviderWithOnlyRequiredFields() {
        assertTrue(OnboardingFormValidator.validateProvider("Studio Aris", "", "", true).isEmpty());
    }

    @Test
    public void providerRulesMatchBackend() {
        Map<Field, Integer> errors = OnboardingFormValidator.validateProvider(
                "", "x".repeat(301), "github.com/aris", false);

        assertEquals(Integer.valueOf(R.string.error_creator_name_required), errors.get(Field.CREATOR_NAME));
        assertEquals(Integer.valueOf(R.string.error_bio_too_long), errors.get(Field.BIO));
        assertEquals(Integer.valueOf(R.string.error_url_invalid), errors.get(Field.PORTFOLIO_URL));
        assertEquals(Integer.valueOf(R.string.error_terms_required), errors.get(Field.TERMS));
    }

    @Test
    public void httpAndHttpsUrlsAreAccepted() {
        assertTrue(OnboardingFormValidator.validateProvider("A", null, "https://github.com/aris", true).isEmpty());
        assertTrue(OnboardingFormValidator.validateProvider("A", null, "http://aris.dev", true).isEmpty());
    }
}
