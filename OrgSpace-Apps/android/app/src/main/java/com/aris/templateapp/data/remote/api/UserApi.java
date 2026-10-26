package com.aris.templateapp.data.remote.api;

import com.aris.templateapp.data.remote.dto.TicketExchangeRequestDto;
import com.aris.templateapp.data.remote.dto.GitHubAuthorizeRequestDto;
import com.aris.templateapp.data.remote.dto.ActiveModeRequestDto;
import com.aris.templateapp.data.remote.dto.CreatorOnboardingRequestDto;
import com.aris.templateapp.data.remote.dto.GoogleIdTokenRequestDto;
import com.aris.templateapp.data.remote.dto.IdentityDto;
import com.aris.templateapp.data.remote.dto.ProviderOnboardingRequestDto;
import com.aris.templateapp.data.remote.dto.UrlResponseDto;
import com.aris.templateapp.data.remote.dto.UserDto;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

/** Endpoint /users/me/* (butuh access token; dipasang otomatis oleh AuthInterceptor). */
public interface UserApi {

    @GET("users/me")
    Call<UserDto> me();

    @POST("users/me/onboarding/creator")
    Call<UserDto> onboardCreator(@Body CreatorOnboardingRequestDto body);

    @POST("users/me/onboarding/provider")
    Call<UserDto> onboardProvider(@Body ProviderOnboardingRequestDto body);

    /** Body-nya sama dengan onboarding pembuat website (hanya nama tampilan), jadi DTO-nya dipakai ulang. */
    @PATCH("users/me")
    Call<UserDto> updateMe(@Body CreatorOnboardingRequestDto body);

    /** Foto profil: PNG persegi yang sudah diperkecil di HP; server memotong & menulis ulang lagi. */
    @Multipart
    @POST("users/me/avatar")
    Call<UserDto> uploadAvatar(@Part MultipartBody.Part file);

    @DELETE("users/me/avatar")
    Call<UserDto> removeAvatar();

    @PATCH("users/me/active-mode")
    Call<UserDto> changeActiveMode(@Body ActiveModeRequestDto body);

    @GET("users/me/identities")
    Call<List<IdentityDto>> identities();

    @POST("users/me/identities/google")
    Call<List<IdentityDto>> linkGoogle(@Body GoogleIdTokenRequestDto body);

    @POST("users/me/identities/github/authorize-url")
    Call<UrlResponseDto> linkGitHubUrl(@Body GitHubAuthorizeRequestDto body);

    /** Menyelesaikan penyambungan GitHub dengan tiket {@code linkTicket} dari deep link. */
    @POST("users/me/identities/github/exchange")
    Call<List<IdentityDto>> exchangeGitHubLink(@Body TicketExchangeRequestDto body);

    /** @param provider "local", "google", atau "github" */
    @DELETE("users/me/identities/{provider}")
    Call<Void> unlink(@Path("provider") String provider);
}
