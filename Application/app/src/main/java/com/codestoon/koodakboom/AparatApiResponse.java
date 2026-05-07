package com.codestoon.koodakboom;

import com.google.gson.annotations.SerializedName;

public class AparatApiResponse {

    @SerializedName("video")
    private AparatVideoResponse video;

    public AparatVideoResponse getVideo() {
        return video;
    }
}