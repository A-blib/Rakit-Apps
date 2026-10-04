package com.aris.templateapp.data.remote.api;

import com.aris.templateapp.data.remote.dto.ActiveModeRequestDto;
import com.aris.templateapp.data.remote.dto.CreatorOnboardingRequestDto;
import com.aris.templateapp.data.remote.dto.GoogleIdTokenRequestDto;
import com.aris.templateapp.data.remote.dto.IdentityDto;
import com.aris.templateapp.data.remote.dto.ProviderOnboardingRequestDto;
import com.aris.templateapp.data.remote.dto.UrlResponseDto;
import com.aris.templateapp.data.remote.dto.UserDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

/** Endpoint /users/me/* (butuh access token; dipasang otomatis oleh AuthInterceptor). */
public interface UserApi {

    @GET("users/me")
    Call<UserDto> me();

    @POST("users/me/onboarding/creator")
    Call<UserDto> onboardCreator(@Body CreatorOnboardingRequestDto body);

    @POST("users/me/onboarding/provider")
    Call<UserDto> onboardProvider(@Body ProviderOnboardingRequestDto body);

    /** Body-nya sama persis dengan form onboarding pembuat website, jadi DTO-nya dipakai ulang. */
    @PATCH("users/me/creator-profile")
    Call<UserDto> updateCreatorProfile(@Body CreatorOnboardingRequestDto body);

    @PATCH("users/me/active-mode")
    Call<UserDto> changeActiveMode(@Body ActiveModeRequestDto body);

    @GET("users/me/identities")
    Call<List<IdentityDto>> identities();

    @POST("users/me/identities/google")
    Call<List<IdentityDto>> linkGoogle(@Body GoogleIdTokenRequestDto body);

    @POST("users/me/identities/github/authorize-url")
    Call<UrlResponseDto> linkGitHubUrl();

    /** @param provider "local", "google", atau "github" */
    @DELETE("users/me/identities/{provider}")
    Call<Void> unlink(@Path("provider") String provider);
}
