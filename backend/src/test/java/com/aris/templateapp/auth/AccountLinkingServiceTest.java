package com.aris.templateapp.auth;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.user.User;
import com.aris.templateapp.user.UserRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test semua cabang bagian 6.3. Repository dan AuthTicketService diganti mock (Mockito),
 * jadi yang diuji murni logika keputusan AccountLinkingService, tanpa database.
 */
@ExtendWith(MockitoExtension.class)
class AccountLinkingServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserIdentityRepository identityRepository;
    @Mock
    private AuthTicketService ticketService;
    @InjectMocks
    private AccountLinkingService service;

    @Nested
    class SignIn {

        @Test
        void knownIdentitySignsInToItsOwner() {
            User owner = user("aris@mail.com");
            SocialProfile google = google("g-1", "aris@mail.com", true);
            when(identityRepository.findByProviderAndProviderUserId(IdentityProvider.GOOGLE, "g-1"))
                    .thenReturn(Optional.of(UserIdentity.social(owner, google)));

            AccountLinkingService.SignInResult result = service.signIn(google);

            assertThat(result.user()).isSameAs(owner);
            assertThat(result.isNewUser()).isFalse();
            verify(userRepository, never()).save(any());
        }

        @Test
        void verifiedEmailOwnedByOtherUserRequiresLinkInsteadOfAutoLinking() {
            User owner = user("aris@mail.com");
            SocialProfile github = github("gh-9", "Aris@Mail.com", true);
            when(identityRepository.findByProviderAndProviderUserId(IdentityProvider.GITHUB, "gh-9"))
                    .thenReturn(Optional.empty());
            when(userRepository.findByEmailIgnoreCase("aris@mail.com")).thenReturn(Optional.of(owner));
            when(identityRepository.findByUserId(owner.getId()))
                    .thenReturn(List.of(UserIdentity.social(owner, google("g-1", "aris@mail.com", true))));
            when(ticketService.issue(eq(AuthTicketType.LINK), eq(owner.getId()), anyMap())).thenReturn("link-123");

            assertThatThrownBy(() -> service.signIn(github))
                    .isInstanceOfSatisfying(ApiException.class, e -> {
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_LINK_REQUIRED);
                        assertThat(e.getLinkToken()).isEqualTo("link-123");
                        assertThat(e.getExistingMethods()).containsExactly("google");
                        assertThat(e.getMessage()).contains("Google").contains("GitHub");
                    });

            // Identitas GitHub disimpan di tiket, bukan langsung ke akun.
            @SuppressWarnings("unchecked")
            ArgumentCaptor<Map<String, Object>> payload = ArgumentCaptor.forClass(Map.class);
            verify(ticketService).issue(eq(AuthTicketType.LINK), eq(owner.getId()), payload.capture());
            assertThat(payload.getValue()).containsEntry("provider", "github").containsEntry("providerUserId", "gh-9");
            verify(identityRepository, never()).save(any());
            verify(userRepository, never()).save(any());
        }

        @Test
        void unverifiedEmailNeverLinksAndIsNotStoredOnNewUser() {
            SocialProfile github = github("gh-9", "aris@mail.com", false);
            when(identityRepository.findByProviderAndProviderUserId(IdentityProvider.GITHUB, "gh-9"))
                    .thenReturn(Optional.empty());

            AccountLinkingService.SignInResult result = service.signIn(github);

            assertThat(result.isNewUser()).isTrue();
            assertThat(result.user().getEmail()).isNull();
            // Email belum terverifikasi tidak dipakai mencari akun lain.
            verify(userRepository, never()).findByEmailIgnoreCase(any());
            verify(ticketService, never()).issue(any(), any(), any());
        }

        @Test
        void missingEmailCreatesNewUser() {
            SocialProfile github = new SocialProfile(IdentityProvider.GITHUB, "gh-9", null, false, null, null);
            when(identityRepository.findByProviderAndProviderUserId(IdentityProvider.GITHUB, "gh-9"))
                    .thenReturn(Optional.empty());

            AccountLinkingService.SignInResult result = service.signIn(github);

            assertThat(result.isNewUser()).isTrue();
            assertThat(result.user().getDisplayName()).isEqualTo("Pengguna");
            verify(identityRepository).save(any(UserIdentity.class));
        }

        @Test
        void verifiedUnusedEmailCreatesNewUserWithThatEmail() {
            SocialProfile google = google("g-1", "Baru@Mail.com", true);
            when(identityRepository.findByProviderAndProviderUserId(IdentityProvider.GOOGLE, "g-1"))
                    .thenReturn(Optional.empty());
            when(userRepository.findByEmailIgnoreCase("baru@mail.com")).thenReturn(Optional.empty());

            AccountLinkingService.SignInResult result = service.signIn(google);

            assertThat(result.isNewUser()).isTrue();
            assertThat(result.user().getEmail()).isEqualTo("baru@mail.com");
            assertThat(result.user().getDisplayName()).isEqualTo("Nama Google");
            assertThat(result.user().getAvatarUrl()).isEqualTo("https://foto");
            verify(userRepository).save(result.user());
        }
    }

    @Nested
    class CompletePendingLink {

        @Test
        void sameUserAsTicketOwnerGetsPendingIdentity() {
            User owner = user("aris@mail.com");
            when(ticketService.consume(AuthTicketType.LINK, "link-123", ErrorCode.LINK_TOKEN_INVALID))
                    .thenReturn(linkTicket(owner.getId()));
            when(identityRepository.findByProviderAndProviderUserId(IdentityProvider.GITHUB, "gh-9"))
                    .thenReturn(Optional.empty());
            when(identityRepository.findByUserIdAndProvider(owner.getId(), IdentityProvider.GITHUB))
                    .thenReturn(Optional.empty());

            service.completePendingLink(owner, "link-123");

            ArgumentCaptor<UserIdentity> saved = ArgumentCaptor.forClass(UserIdentity.class);
            verify(identityRepository).save(saved.capture());
            assertThat(saved.getValue().getUser()).isSameAs(owner);
            assertThat(saved.getValue().getProvider()).isEqualTo(IdentityProvider.GITHUB);
            assertThat(saved.getValue().getProviderUserId()).isEqualTo("gh-9");
            assertThat(saved.getValue().isEmailVerified()).isTrue();
        }

        @Test
        void differentUserIsRejected() {
            User other = user("lain@mail.com");
            when(ticketService.consume(AuthTicketType.LINK, "link-123", ErrorCode.LINK_TOKEN_INVALID))
                    .thenReturn(linkTicket(UUID.randomUUID()));

            assertThatThrownBy(() -> service.completePendingLink(other, "link-123"))
                    .isInstanceOfSatisfying(ApiException.class,
                            e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.LINK_USER_MISMATCH));
            verify(identityRepository, never()).save(any());
        }

        @Test
        void invalidOrExpiredTokenIsRejected() {
            when(ticketService.consume(AuthTicketType.LINK, "basi", ErrorCode.LINK_TOKEN_INVALID))
                    .thenThrow(new ApiException(ErrorCode.LINK_TOKEN_INVALID));

            assertThatThrownBy(() -> service.completePendingLink(user("aris@mail.com"), "basi"))
                    .isInstanceOfSatisfying(ApiException.class,
                            e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.LINK_TOKEN_INVALID));
        }
    }

    @Nested
    class LinkToUser {

        @Test
        void identityOwnedByAnotherUserIsRejected() {
            User me = user("aris@mail.com");
            SocialProfile github = github("gh-9", "x@mail.com", true);
            when(identityRepository.findByProviderAndProviderUserId(IdentityProvider.GITHUB, "gh-9"))
                    .thenReturn(Optional.of(UserIdentity.social(user("orang@mail.com"), github)));

            assertThatThrownBy(() -> service.linkToUser(me, github))
                    .isInstanceOfSatisfying(ApiException.class,
                            e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.IDENTITY_IN_USE));
        }

        @Test
        void secondAccountOfSameProviderIsRejected() {
            User me = user("aris@mail.com");
            SocialProfile newGithub = github("gh-baru", "x@mail.com", true);
            when(identityRepository.findByProviderAndProviderUserId(IdentityProvider.GITHUB, "gh-baru"))
                    .thenReturn(Optional.empty());
            when(identityRepository.findByUserIdAndProvider(me.getId(), IdentityProvider.GITHUB))
                    .thenReturn(Optional.of(UserIdentity.social(me, github("gh-lama", "x@mail.com", true))));

            assertThatThrownBy(() -> service.linkToUser(me, newGithub))
                    .isInstanceOfSatisfying(ApiException.class,
                            e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.IDENTITY_IN_USE));
        }

        @Test
        void alreadyLinkedToSameUserDoesNothing() {
            User me = user("aris@mail.com");
            SocialProfile github = github("gh-9", "x@mail.com", true);
            when(identityRepository.findByProviderAndProviderUserId(IdentityProvider.GITHUB, "gh-9"))
                    .thenReturn(Optional.of(UserIdentity.social(me, github)));

            service.linkToUser(me, github);

            verify(identityRepository, never()).save(any());
        }

        @Test
        void differentEmailCanStillBeLinkedManually() {
            User me = user("aris@mail.com");
            SocialProfile github = github("gh-9", "email-lain@mail.com", false);
            when(identityRepository.findByProviderAndProviderUserId(IdentityProvider.GITHUB, "gh-9"))
                    .thenReturn(Optional.empty());
            when(identityRepository.findByUserIdAndProvider(me.getId(), IdentityProvider.GITHUB))
                    .thenReturn(Optional.empty());

            service.linkToUser(me, github);

            verify(identityRepository).save(any(UserIdentity.class));
        }
    }

    private static User user(String email) {
        User user = new User("Aris", email);
        user.setId(UUID.randomUUID());
        return user;
    }

    private static SocialProfile google(String sub, String email, boolean verified) {
        return new SocialProfile(IdentityProvider.GOOGLE, sub, email, verified, "Nama Google", "https://foto");
    }

    private static SocialProfile github(String id, String email, boolean verified) {
        return new SocialProfile(IdentityProvider.GITHUB, id, email, verified, "Nama GitHub", null);
    }

    private static AuthTicket linkTicket(UUID ownerId) {
        return new AuthTicket(AuthTicketType.LINK, "hash", ownerId,
                Map.of("provider", "github", "providerUserId", "gh-9", "email", "aris@mail.com", "emailVerified", true),
                Instant.now().plusSeconds(600));
    }
}
