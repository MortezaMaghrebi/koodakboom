package com.codestoon.koodakboom;

import com.google.gson.annotations.SerializedName;

public class AparatVideoResponse {

    @SerializedName("title")
    private String title;

    @SerializedName("visit_cnt")
    private String visitCnt;

    @SerializedName("duration")
    private String duration;

    @SerializedName("small_poster")
    private String smallPoster;

    @SerializedName("big_poster")
    private String bigPoster;

    @SerializedName("frame")
    private String frame;

    @SerializedName("username")
    private String username;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("like_cnt")
    private String likeCnt;

    @SerializedName("comment_cnt")
    private String commentCnt;

    @SerializedName("uid")
    private String uid;

    // Getters
    public String getTitle() { return title != null ? title : "بدون عنوان"; }
    public String getVisitCnt() { return visitCnt != null ? visitCnt : "0"; }
    public String getDuration() { return duration != null ? duration : "0:00"; }
    public String getSmallPoster() { return smallPoster; }
    public String getBigPoster() { return bigPoster; }
    public String getFrame() { return frame; }
    public String getUsername() { return username != null ? username : "کاربر آپارات"; }
    public String getUserId() { return userId; }
    public String getLikeCnt() { return likeCnt != null ? likeCnt : "0"; }
    public String getCommentCnt() { return commentCnt != null ? commentCnt : "0"; }
    public String getUid() { return uid; }
}