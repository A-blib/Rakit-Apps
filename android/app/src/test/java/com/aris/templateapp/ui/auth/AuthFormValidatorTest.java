package com.aris.templateapp.ui.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.aris.templateapp.R;
import com.aris.templateapp.ui.auth.AuthFormValidator.Field;

import org.junit.Test;

import java.util.Map;

public class AuthFormValidatorTest {

    @Test
    public void validLoginHasNoErrors() {
        assertTrue(AuthFormValidator.validateLogin("aris@mail.com", "apa-saja").isEmpty());
    }

    @Test
    public void loginRequiresEmailAndPassword() {
        Map<Field, Integer> errors = AuthFormValidator.validateLogin("", "");

        assertEquals(Integer.valueOf(R.string.error_email_required), errors.get(Field.EMAIL));
        assertEquals(Integer.valueOf(R.string.error_password_required), errors.get(Field.PASSWORD));
    }

    @Test
    public void emailFormatIsChecked() {
        assertEquals(Integer.valueOf(R.string.error_email_invalid), AuthFormValidator.emailError("aris@mail"));
        assertEquals(Integer.valueOf(R.string.error_email_invalid), AuthFormValidator.emailError("aris mail.com"));
        assertNull(AuthFormValidator.emailError("  aris@mail.com  "));
    }

    @Test
    public void registerChecksNameAndPasswordLength() {
        Map<Field, Integer> errors = AuthFormValidator.validateRegister(" ", "aris@mail.com", "pendek");

        assertEquals(Integer.valueOf(R.string.error_name_required), errors.get(Field.NAME));
        assertEquals(Integer.valueOf(R.string.error_password_length), errors.get(Field.PASSWORD));
        assertNull(errors.get(Field.EMAIL));
    }

    @Test
    public void passwordLongerThanBcryptLimitIsRejected() {
        assertEquals(Integer.valueOf(R.string.error_password_too_long), AuthFormValidator.passwordError("x".repeat(73)));
        assertNull(AuthFormValidator.passwordError("x".repeat(72)));
    }
}
