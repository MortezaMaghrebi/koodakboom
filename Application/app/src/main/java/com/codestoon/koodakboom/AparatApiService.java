package com.codestoon.koodakboom;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface AparatApiService {

    @GET("api/v1/video/video/show/{uid}")
    Call<AparatApiResponse> getVideoInfo(@Path("uid") String videoId);
}