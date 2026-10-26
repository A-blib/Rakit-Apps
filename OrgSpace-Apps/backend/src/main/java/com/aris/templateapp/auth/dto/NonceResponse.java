package com.aris.templateapp.auth.dto;

/** @param nonce dipasang app ke GetSignInWithGoogleOption.setNonce; berlaku 5 menit dan hanya untuk satu ID token */
public record NonceResponse(String nonce) {
}
