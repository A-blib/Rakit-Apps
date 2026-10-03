package com.aris.templateapp.ui.onboarding;

import com.aris.templateapp.R;

import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Pattern;

/** Validasi form onboarding di HP; aturannya sama dengan backend (bagian 6.6). Tanpa kelas Android agar mudah diuji. */
public final class OnboardingFormValidator {

    public enum Field { DISPLAY_NAME, CREATOR_NAME, BIO, PORTFOLIO_URL, TERMS }

    static final int NAME_MAX = 100;
    static final int BIO_MAX = 300;
    // Sama dengan @Pattern di ProviderOnboardingRequest backend.
    private static final Pattern URL = Pattern.compile("^https?://[^\\s/$.?#][^\\s]*$");

    private OnboardingFormValidator() {
    }

    public static Map<Field, Integer> validateCreator(String displayName) {
        Map<Field, Integer> errors = new EnumMap<>(Field.class);
        Integer nameError = nameError(displayName, R.string.error_display_name_required);
        if (nameError != null) {
            errors.put(Field.DISPLAY_NAME, nameError);
        }
        return errors;
    }

    public static Map<Field, Integer> validateProvider(String creatorName, String bio, String portfolioUrl,
                                                       boolean agreedToTerms) {
        Map<Field, Integer> errors = new EnumMap<>(Field.class);
        Integer nameError = nameError(creatorName, R.string.error_creator_name_required);
        if (nameError != null) {
            errors.put(Field.CREATOR_NAME, nameError);
        }
        if (bio != null && bio.trim().length() > BIO_MAX) {
            errors.put(Field.BIO, R.string.error_bio_too_long);
        }
        if (portfolioUrl != null && !portfolioUrl.trim().isEmpty() && !URL.matcher(portfolioUrl.trim()).matches()) {
            errors.put(Field.PORTFOLIO_URL, R.string.error_url_invalid);
        }
        if (!agreedToTerms) {
            errors.put(Field.TERMS, R.string.error_terms_required);
        }
        return errors;
    }

    private static Integer nameError(String name, int requiredMessage) {
        if (name == null || name.trim().isEmpty()) {
            return requiredMessage;
        }
        return name.trim().length() > NAME_MAX ? R.string.error_name_too_long : null;
    }
}
