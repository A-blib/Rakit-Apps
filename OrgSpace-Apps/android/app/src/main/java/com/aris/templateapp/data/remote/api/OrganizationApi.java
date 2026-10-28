package com.aris.templateapp.data.remote.api;

import com.aris.templateapp.data.remote.dto.ActivityPageDto;
import com.aris.templateapp.data.remote.dto.CategoriesDto;
import com.aris.templateapp.data.remote.dto.CreateInvitationRequestDto;
import com.aris.templateapp.data.remote.dto.CreateOrganizationRequestDto;
import com.aris.templateapp.data.remote.dto.DeleteOrganizationRequestDto;
import com.aris.templateapp.data.remote.dto.InvitationCodeRequestDto;
import com.aris.templateapp.data.remote.dto.InvitationCreatedDto;
import com.aris.templateapp.data.remote.dto.InvitationDto;
import com.aris.templateapp.data.remote.dto.InvitationPreviewDto;
import com.aris.templateapp.data.remote.dto.LastOrganizationRequestDto;
import com.aris.templateapp.data.remote.dto.MemberDto;
import com.aris.templateapp.data.remote.dto.MemberUpdateRequestDto;
import com.aris.templateapp.data.remote.dto.OrganizationDto;
import com.aris.templateapp.data.remote.dto.PortalCredentialDto;
import com.aris.templateapp.data.remote.dto.PortalCredentialRequestDto;
import com.aris.templateapp.data.remote.dto.OrganizationSummaryDto;
import com.aris.templateapp.data.remote.dto.TransferOwnershipRequestDto;
import com.aris.templateapp.data.remote.dto.UpdateOrganizationRequestDto;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

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

    @PATCH("organizations/{id}")
    Call<OrganizationDto> update(@Path("id") String id, @Body UpdateOrganizationRequestDto body);

    @Multipart
    @POST("organizations/{id}/logo")
    Call<OrganizationDto> uploadLogo(@Path("id") String id, @Part MultipartBody.Part file);

    /** DELETE dengan body perlu @HTTP karena @DELETE biasa tidak boleh membawa body. */
    @HTTP(method = "DELETE", path = "organizations/{id}", hasBody = true)
    Call<Void> delete(@Path("id") String id, @Body DeleteOrganizationRequestDto body);

    @GET("organizations/{id}/members")
    Call<List<MemberDto>> members(@Path("id") String id);

    @PATCH("organizations/{id}/members/{userId}")
    Call<MemberDto> updateMember(@Path("id") String id, @Path("userId") String userId, @Body MemberUpdateRequestDto body);

    @DELETE("organizations/{id}/members/{userId}")
    Call<Void> removeMember(@Path("id") String id, @Path("userId") String userId);

    @POST("organizations/{id}/leave")
    Call<Void> leave(@Path("id") String id);

    /** Balasan 204 tanpa isi jika Pemilik lama memilih keluar. */
    @POST("organizations/{id}/transfer-ownership")
    Call<OrganizationDto> transferOwnership(@Path("id") String id, @Body TransferOwnershipRequestDto body);

    @POST("organizations/{id}/invitations")
    Call<InvitationCreatedDto> createInvitation(@Path("id") String id, @Body CreateInvitationRequestDto body);

    @GET("organizations/{id}/invitations")
    Call<List<InvitationDto>> invitations(@Path("id") String id);

    @DELETE("organizations/{id}/invitations/{invitationId}")
    Call<Void> revokeInvitation(@Path("id") String id, @Path("invitationId") String invitationId);

    @GET("organizations/{id}/portal-credentials")
    Call<List<PortalCredentialDto>> portalCredentials(@Path("id") String id);

    @PUT("organizations/{id}/portal-credentials/{role}")
    Call<PortalCredentialDto> savePortalCredential(@Path("id") String id, @Path("role") String role,
                                                   @Body PortalCredentialRequestDto body);

    @GET("organizations/{id}/activities")
    Call<ActivityPageDto> activities(@Path("id") String id, @Query("page") int page);

    @PATCH("users/me/last-organization")
    Call<Void> setLastOrganization(@Body LastOrganizationRequestDto body);
}
