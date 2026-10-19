package com.aris.templateapp.data.remote.api;

import com.aris.templateapp.data.remote.dto.AddDomainRequestDto;
import com.aris.templateapp.data.remote.dto.ConfirmNameRequestDto;
import com.aris.templateapp.data.remote.dto.DirectPublishRequestDto;
import com.aris.templateapp.data.remote.dto.DirectPublishResponseDto;
import com.aris.templateapp.data.remote.dto.DomainResponseDto;
import com.aris.templateapp.data.remote.dto.PublishRequestDto;
import com.aris.templateapp.data.remote.dto.PublishRequestNoteDto;
import com.aris.templateapp.data.remote.dto.SlugCheckDto;
import com.aris.templateapp.data.remote.dto.WebsiteDetailDto;
import com.aris.templateapp.data.remote.dto.WebsiteListDto;
import com.aris.templateapp.data.remote.dto.WebsiteNameRequestDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/** Tab Website & halaman kelola website (alur-pembuatan-website.md 4–5, 10). Semua butuh access token. */
public interface WebsiteApi {

    /**
     * @param status all/draft/online/changed
     * @param sort   updated/newest/name
     */
    @GET("organizations/{orgId}/websites")
    Call<WebsiteListDto> websites(@Path("orgId") String organizationId, @Query("status") String status,
                                  @Query("q") String query, @Query("sort") String sort, @Query("page") int page,
                                  @Query("size") int size);

    @GET("projects/{id}")
    Call<WebsiteDetailDto> website(@Path("id") String id);

    @PATCH("projects/{id}")
    Call<WebsiteDetailDto> rename(@Path("id") String id, @Body WebsiteNameRequestDto body);

    @POST("projects/{id}/duplicate")
    Call<WebsiteDetailDto> duplicate(@Path("id") String id, @Body WebsiteNameRequestDto body);

    /** DELETE dengan body perlu @HTTP karena @DELETE biasa tidak boleh membawa body. */
    @HTTP(method = "DELETE", path = "projects/{id}", hasBody = true)
    Call<Void> delete(@Path("id") String id, @Body ConfirmNameRequestDto body);

    @POST("projects/{id}/publish-requests")
    Call<PublishRequestDto> requestPublish(@Path("id") String id, @Body PublishRequestNoteDto body);

    @GET("organizations/{orgId}/publish-requests")
    Call<List<PublishRequestDto>> openRequests(@Path("orgId") String organizationId);

    @POST("publish-requests/{id}/dismiss")
    Call<Void> dismissRequest(@Path("id") String id);

    @POST("projects/publish-direct")
    Call<DirectPublishResponseDto> publishDirect(@Body DirectPublishRequestDto body);

    /** {@code slug} null = minta saran dari nama project. */
    @GET("projects/publish-direct/slug")
    Call<SlugCheckDto> checkSlug(@Query("name") String name, @Query("organizationId") String organizationId,
                                 @Query("projectId") String projectId, @Query("slug") String slug);

    @GET("projects/{projectId}/domains")
    Call<DomainResponseDto> getDomain(@Path("projectId") String projectId);

    @POST("projects/{projectId}/domains")
    Call<DomainResponseDto> addDomain(@Path("projectId") String projectId, @Body AddDomainRequestDto body);

    @POST("projects/{projectId}/domains/{domainId}/verify")
    Call<DomainResponseDto> verifyDomain(@Path("projectId") String projectId, @Path("domainId") String domainId,
                                         @Query("bypassDns") boolean bypassDns);

    @DELETE("projects/{projectId}/domains/{domainId}")
    Call<Void> removeDomain(@Path("projectId") String projectId, @Path("domainId") String domainId);
}
