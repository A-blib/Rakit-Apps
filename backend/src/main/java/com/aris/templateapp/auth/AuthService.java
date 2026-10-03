package com.aris.templateapp.auth;

import com.aris.templateapp.auth.dto.AuthResponse;
import com.aris.templateapp.auth.dto.GoogleLoginRequest;
import com.aris.templateapp.auth.dto.LoginRequest;
import com.aris.templateapp.auth.dto.RefreshTokenRequest;
import com.aris.templateapp.auth.dto.RegisterRequest;
import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.common.util.Emails;
import com.aris.templateapp.security.GoogleTokenVerifier;
import com.aris.templateapp.security.JwtService;
import com.aris.templateapp.user.User;
import com.aris.templateapp.user.UserRepository;
import com.aris.templateapp.user.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/** Daftar, masuk (email, Google, hasil GitHub), refresh, dan keluar. */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserIdentityRepository identityRepository;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AccountLinkingService accountLinkingService;
    private final AuthTicketService ticketService;
    private final GoogleTokenVerifier googleTokenVerifier;

    /**
     * Hash palsu untuk dicocokkan saat email tidak ditemukan. Tujuannya agar waktu respons
     * "email tidak ada" dan "password salah" sama lamanya, sehingga penyerang tidak bisa
     * menebak email mana yang terdaftar dari lamanya respons.
     */
    private final String dummyPasswordHash;

    public AuthService(UserRepository userRepository, UserIdentityRepository identityRepository,
                       RefreshTokenService refreshTokenService, UserService userService, JwtService jwtService,
                       PasswordEncoder passwordEncoder, AccountLinkingService accountLinkingService,
                       AuthTicketService ticketService, GoogleTokenVerifier googleTokenVerifier) {
        this.userRepository = userRepository;
        this.identityRepository = identityRepository;
        this.refreshTokenService = refreshTokenService;
        this.userService = userService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.accountLinkingService = accountLinkingService;
        this.ticketService = ticketService;
        this.googleTokenVerifier = googleTokenVerifier;
        this.dummyPasswordHash = passwordEncoder.encode("password-palsu-untuk-penyeimbang-waktu");
    }

    @Transactional
    public AuthResponse register(RegisterRequest request, String deviceName) {
        String email = Emails.normalize(request.email());
        // Dicek terhadap tabel users (bukan hanya identitas local), agar email yang sudah dipakai
        // lewat Google/GitHub juga tidak bisa didaftarkan ulang sebagai akun terpisah.
        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new ApiException(ErrorCode.EMAIL_ALREADY_USED);
        }

        User user = userRepository.save(new User(request.displayName().trim(), email));
        identityRepository.save(UserIdentity.local(user, email, passwordEncoder.encode(request.password())));
        return issueTokens(user, deviceName, true);
    }

    @Transactional
    public AuthResponse login(LoginRequest request, String deviceName) {
        User user = userRepository.findByEmailIgnoreCase(Emails.normalize(request.email())).orElse(null);
        if (user == null) {
            passwordEncoder.matches(request.password(), dummyPasswordHash);
            throw new ApiException(ErrorCode.INVALID_CREDENTIALS);
        }

        UserIdentity local = identityRepository.findByUserIdAndProvider(user.getId(), IdentityProvider.LOCAL)
                .orElseThrow(() -> useSocialLogin(user));
        if (!passwordEncoder.matches(request.password(), local.getPasswordHash())) {
            throw new ApiException(ErrorCode.INVALID_CREDENTIALS);
        }
        linkPendingIdentity(user, request.linkToken());
        return issueTokens(user, deviceName, false);
    }

    /**
     * Masuk/daftar dengan Google. Akun dibuat otomatis jika belum ada (tanpa menu Daftar).
     * Jika email Google sudah dipakai akun lain, {@code ACCOUNT_LINK_REQUIRED} dilempar oleh AccountLinkingService.
     */
    @Transactional
    public AuthResponse loginWithGoogle(GoogleLoginRequest request, String deviceName) {
        SocialProfile profile = googleTokenVerifier.verify(request.idToken());
        AccountLinkingService.SignInResult result = accountLinkingService.signIn(profile);
        linkPendingIdentity(result.user(), request.linkToken());
        return issueTokens(result.user(), deviceName, result.isNewUser());
    }

    /** Menukar tiket LOGIN_RESULT dari deep link GitHub menjadi token. Tiket hanya bisa dipakai sekali. */
    @Transactional
    public AuthResponse exchangeLoginTicket(String rawTicket, String deviceName) {
        AuthTicket ticket = ticketService.consume(AuthTicketType.LOGIN_RESULT, rawTicket, ErrorCode.TICKET_INVALID);
        User user = userRepository.findById(ticket.getUserId())
                .orElseThrow(() -> new ApiException(ErrorCode.TICKET_INVALID));
        boolean isNewUser = Boolean.parseBoolean(ticket.payloadString(GitHubAuthService.KEY_IS_NEW_USER));
        return issueTokens(user, deviceName, isNewUser);
    }

    // noRollbackFor: lihat penjelasan di RefreshTokenService.rotate.
    @Transactional(noRollbackFor = ApiException.class)
    public AuthResponse refresh(RefreshTokenRequest request, String deviceName) {
        RefreshTokenService.Rotation rotation = refreshTokenService.rotate(request.refreshToken(), deviceName);
        return buildResponse(rotation.user(), rotation.refreshToken(), false);
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        refreshTokenService.revoke(request.refreshToken());
    }

    /** Langkah 4 bagian 6.3: jika request membawa linkToken, sambungkan identitas yang tertunda. */
    private void linkPendingIdentity(User user, String linkToken) {
        if (linkToken != null && !linkToken.isBlank()) {
            accountLinkingService.completePendingLink(user, linkToken);
        }
    }

    private AuthResponse issueTokens(User user, String deviceName, boolean isNewUser) {
        return buildResponse(user, refreshTokenService.issue(user, deviceName), isNewUser);
    }

    private AuthResponse buildResponse(User user, String refreshToken, boolean isNewUser) {
        return new AuthResponse(
                jwtService.createAccessToken(user.getId()),
                refreshToken,
                jwtService.accessTtlSeconds(),
                isNewUser,
                userService.toResponse(user));
    }

    /** Akun ada tetapi tidak punya password (dibuat lewat Google/GitHub). */
    private ApiException useSocialLogin(User user) {
        List<IdentityProvider> providers = identityRepository.findByUserId(user.getId()).stream()
                .map(UserIdentity::getProvider)
                .sorted()
                .toList();
        String labels = providers.stream().map(IdentityProvider::label).collect(Collectors.joining(" atau "));
        return new ApiException(ErrorCode.USE_SOCIAL_LOGIN,
                "Akun ini terdaftar dengan " + labels + ". Silakan masuk dengan " + labels + ".",
                providers.stream().map(IdentityProvider::value).toList(), null);
    }
}
