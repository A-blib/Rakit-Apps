package com.aris.templateapp.ui.auth;

import android.app.Activity;
import android.os.CancellationSignal;
import android.util.Log;

import androidx.core.content.ContextCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialCancellationException;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.credentials.exceptions.NoCredentialException;

import com.aris.templateapp.BuildConfig;
import com.aris.templateapp.R;
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

/**
 * Login Google lewat Credential Manager (bagian 6.5): menampilkan lembar pilih akun Google milik sistem,
 * lalu mengembalikan idToken untuk dikirim ke backend (POST /auth/google).
 * <p>
 * {@code serverClientId} = Client ID tipe WEB. Google memasukkannya ke "audience" idToken,
 * sehingga backend (yang memeriksa audience yang sama) mau menerima token itu.
 */
public final class GoogleSignInHelper {

    private static final String TAG = "GoogleSignIn";

    /** Hasil login Google; dipanggil di thread utama. */
    public interface Callback {
        void onIdToken(String idToken);

        /** User menutup lembar pilih akun: tidak perlu menampilkan error. */
        void onCancelled();

        void onError(int messageRes);
    }

    private GoogleSignInHelper() {
    }

    public static void signIn(Activity activity, Callback callback) {
        if (BuildConfig.GOOGLE_WEB_CLIENT_ID.isEmpty()) {
            callback.onError(R.string.error_google_not_configured);
            return;
        }
        GetSignInWithGoogleOption option = new GetSignInWithGoogleOption.Builder(BuildConfig.GOOGLE_WEB_CLIENT_ID).build();
        GetCredentialRequest request = new GetCredentialRequest.Builder().addCredentialOption(option).build();

        CredentialManager.create(activity).getCredentialAsync(activity, request, new CancellationSignal(),
                ContextCompat.getMainExecutor(activity),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse response) {
                        Credential credential = response.getCredential();
                        if (credential instanceof CustomCredential
                                && GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(credential.getType())) {
                            callback.onIdToken(GoogleIdTokenCredential.createFrom(credential.getData()).getIdToken());
                        } else {
                            callback.onError(R.string.error_social_auth_failed);
                        }
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        if (e instanceof GetCredentialCancellationException) {
                            callback.onCancelled();
                        } else if (e instanceof NoCredentialException) {
                            callback.onError(R.string.error_google_no_account);
                        } else {
                            // Jenis & pesan error membantu mencari penyebab di Logcat (mis. SHA-1 belum didaftarkan).
                            Log.w(TAG, "Login Google gagal: " + e.getClass().getSimpleName() + " " + e.getMessage());
                            callback.onError(R.string.error_social_auth_failed);
                        }
                    }
                });
    }
}
