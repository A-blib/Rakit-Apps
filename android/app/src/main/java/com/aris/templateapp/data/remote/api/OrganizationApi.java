package com.aris.templateapp.data.remote.api;

import com.aris.templateapp.data.remote.dto.CategoriesDto;
import com.aris.templateapp.data.remote.dto.CreateOrganizationRequestDto;
import com.aris.templateapp.data.remote.dto.InvitationCodeRequestDto;
import com.aris.templateapp.data.remote.dto.InvitationPreviewDto;
import com.aris.templateapp.data.remote.dto.LastOrganizationRequestDto;
import com.aris.templateapp.data.remote.dto.OrganizationDto;
import com.aris.templateapp.data.remote.dto.OrganizationSummaryDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

/** Ruang kerja organisasi (revisi-fondasi-orgspace.md 4.4). Semua butuh access token kecuali kategori. */
public interface OrganizationApi {

    @GET("meta/categories")
    Call<CategoriesDto> categories();

    @GET("organizations")
    Call<List<OrganizationSummaryDto>> organizations();

    @POST("organizations")
    Call<OrganizationDto> create(@Body CreateOrganizationRequestDto body);

    @GET("organizations/{id}")
    Call<OrganizationDto> organization(@Path("id") String id);

    @POST("invitations/preview")
    Call<InvitationPreviewDto> previewInvitation(@Body InvitationCodeRequestDto body);

    @POST("invitations/accept")
    Call<OrganizationDto> acceptInvitation(@Body InvitationCodeRequestDto body);

    @PATCH("users/me/last-organization")
    Call<Void> setLastOrganization(@Body LastOrganizationRequestDto body);
}
