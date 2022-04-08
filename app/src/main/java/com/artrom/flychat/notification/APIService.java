package com.artrom.flychat.notification;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAACO5Bfsw:APA91bGMWDhgllAhGfhDSyXXzjRZGTtHHcJuNkCnNn5Jk9Qb8BWBe3Xx2eMHr3OJ19nVfRmneHemG1PWZ_BfL_P3tyibKspqn_zkMWOe_GLZFvUi0wPdKo9YqI8xV3tTmCsfNyK5_0Ep"
    })
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
