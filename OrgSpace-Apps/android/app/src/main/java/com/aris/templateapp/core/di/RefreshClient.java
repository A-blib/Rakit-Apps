package com.aris.templateapp.core.di;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;

/**
 * Penanda (qualifier) untuk AuthApi khusus refresh token. Ada dua AuthApi di Hilt: yang biasa
 * dan yang ini; anotasi ini memberi tahu Hilt mana yang diminta.
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface RefreshClient {
}
