package com.aris.templateapp.auth;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.common.persistence.PersistableEnum;
import com.aris.templateapp.common.util.Emails;
import com.aris.templateapp.user.User;
import com.aris.templateapp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Logika masuk lewat Google/GitHub dan penyambungan akun (bagian 6.3 instruksi).
 * <p>
 * Aturan utamanya: identitas baru TIDAK PERNAH disambungkan otomatis ke akun lama hanya karena emailnya sama.
 * User harus membuktikan dulu bahwa ia pemilik akun lama dengan masuk memakai metode lama.
 * Tanpa aturan ini, siapa pun yang bisa membuat akun GitHub dengan email orang lain dapat mengambil alih akunnya.
 */
@Service
@RequiredArgsConstructor
public class AccountLinkingService {

    // Kunci di payload tiket LINK: identitas yang menunggu disambungkan.
    static final String KEY_PROVIDER = "provider";
    static final String KEY_PROVIDER_USER_ID = "providerUserId";
    static final String KEY_EMAIL = "email";
    static final String KEY_EMAIL_VERIFIED = "emailVerified";

    private static final int DISPLAY_NAME_MAX = 100;
    private static final int AVATAR_URL_MAX = 500;
    private static final String DEFAULT_DISPLAY_NAME = "Pengguna";

    private final UserRepository userRepository;
    private final UserIdentityRepository identityRepository;
    private final AuthTicketService ticketService;

    /** Hasil masuk lewat Google/GitHub. */
    public record SignInResult(User user, boolean isNewUser) {
    }

    /**
     * Langkah 1–3 bagian 6.3: cari identitas → jika tidak ada, cek email → buat akun baru atau minta penyambungan.
     *
     * @throws ApiException {@code ACCOUNT_LINK_REQUIRED} (berisi linkToken) jika email terverifikasi sudah dipakai akun lain
     */
    @Transactional
    public SignInResult signIn(SocialProfile profile) {
        // 2. Identitas ini sudah pernah dipakai → langsung masuk ke pemiliknya.
        var existing = identityRepository.findByProviderAndProviderUserId(profile.provider(), profile.providerUserId());
        if (existing.isPresent()) {
            UserIdentity identity = existing.get();
            // Email di Google/GitHub bisa berubah; simpan yang terbaru untuk ditampilkan di Pengaturan.
            identity.setEmail(Emails.normalize(profile.email()));
            identity.setEmailVerified(profile.emailVerified());
            return new SignInResult(identity.getUser(), false);
        }

        // 3a–b. Hanya email TERVERIFIKASI yang dipakai untuk mencari akun lain.
        String verifiedEmail = Emails.normalize(profile.verifiedEmail());
        if (verifiedEmail != null) {
            var owner = userRepository.findByEmailIgnoreCase(verifiedEmail);
            if (owner.isPresent()) {
                throw linkRequired(owner.get(), profile);
            }
        }

        // 3c. Email tidak ada / tidak terverifikasi / belum dipakai → akun baru.
        // Email yang belum terverifikasi tidak disimpan ke users.email, agar tidak "mengklaim" email orang lain.
        User user = new User(displayNameFor(profile), verifiedEmail);
        user.setAvatarUrl(avatarUrlFor(profile));
        userRepository.save(user);
        identityRepository.save(UserIdentity.social(user, profile));
        return new SignInResult(user, true);
    }

    /**
     * Langkah 4 bagian 6.3: user sudah berhasil masuk dengan metode lama sambil membawa linkToken.
     * Jika user hasil masuk = pemilik email di tiket, identitas tertunda disambungkan ke akun itu.
     */
    @Transactional
    public void completePendingLink(User user, String linkToken) {
        AuthTicket ticket = ticketService.consume(AuthTicketType.LINK, linkToken, ErrorCode.LINK_TOKEN_INVALID);
        if (!user.getId().equals(ticket.getUserId())) {
            throw new ApiException(ErrorCode.LINK_USER_MISMATCH);
        }
        attach(user, pendingProfile(ticket));
    }

    /**
     * Menyambungkan identitas ke user yang sudah login (dari Pengaturan → Metode login terhubung).
     * Tidak butuh kecocokan email: user sudah membuktikan kepemilikan kedua akun dengan masuk ke keduanya.
     */
    @Transactional
    public void linkToUser(User user, SocialProfile profile) {
        attach(user, profile);
    }

    private void attach(User user, SocialProfile profile) {
        var owner = identityRepository.findByProviderAndProviderUserId(profile.provider(), profile.providerUserId());
        if (owner.isPresent()) {
            if (owner.get().getUser().getId().equals(user.getId())) {
                return; // Sudah tersambung ke akun ini; tidak perlu apa-apa.
            }
            throw new ApiException(ErrorCode.IDENTITY_IN_USE);
        }
        // Satu akun maksimal satu identitas per provider (UNIQUE(user_id, provider) di V2).
        if (identityRepository.findByUserIdAndProvider(user.getId(), profile.provider()).isPresent()) {
            throw new ApiException(ErrorCode.IDENTITY_IN_USE,
                    "Akunmu sudah tersambung dengan akun " + profile.provider().label()
                            + " lain. Lepaskan dulu yang lama di Pengaturan.");
        }
        identityRepository.save(UserIdentity.social(user, profile));
    }

    private ApiException linkRequired(User owner, SocialProfile profile) {
        Map<String, Object> pending = new HashMap<>();
        pending.put(KEY_PROVIDER, profile.provider().value());
        pending.put(KEY_PROVIDER_USER_ID, profile.providerUserId());
        pending.put(KEY_EMAIL, Emails.normalize(profile.email()));
        pending.put(KEY_EMAIL_VERIFIED, profile.emailVerified());
        String linkToken = ticketService.issue(AuthTicketType.LINK, owner.getId(), pending);

        List<IdentityProvider> methods = identityRepository.findByUserId(owner.getId()).stream()
                .map(UserIdentity::getProvider)
                .sorted()
                .toList();
        String labels = methods.stream().map(IdentityProvider::label).collect(Collectors.joining(" atau "));
        String message = "Email ini sudah terdaftar dengan " + labels + ". Masuk dengan " + labels
                + " untuk menyambungkan akun " + profile.provider().label() + ".";
        return new ApiException(ErrorCode.ACCOUNT_LINK_REQUIRED, message,
                methods.stream().map(IdentityProvider::value).toList(), linkToken);
    }

    private static SocialProfile pendingProfile(AuthTicket ticket) {
        return new SocialProfile(
                PersistableEnum.fromValue(IdentityProvider.class, ticket.payloadString(KEY_PROVIDER)),
                ticket.payloadString(KEY_PROVIDER_USER_ID),
                ticket.payloadString(KEY_EMAIL),
                Boolean.parseBoolean(ticket.payloadString(KEY_EMAIL_VERIFIED)),
                null,
                null);
    }

    private static String displayNameFor(SocialProfile profile) {
        String name = profile.displayName();
        if (name == null || name.isBlank()) {
            String email = profile.email();
            name = email != null && email.contains("@") ? email.substring(0, email.indexOf('@')) : DEFAULT_DISPLAY_NAME;
        }
        name = name.trim();
        return name.length() <= DISPLAY_NAME_MAX ? name : name.substring(0, DISPLAY_NAME_MAX);
    }

    private static String avatarUrlFor(SocialProfile profile) {
        String url = profile.avatarUrl();
        return url != null && url.length() <= AVATAR_URL_MAX ? url : null;
    }
}
