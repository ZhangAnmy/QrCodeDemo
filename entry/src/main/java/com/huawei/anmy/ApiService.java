package com.huawei.anmy;

import com.huawei.anmy.model.Sentence;
import com.huawei.anmy.model.Token;
import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface ApiService {
    @GET("token")
    Observable<Token> getToken();

    @GET("sentence")
    Observable<Sentence> getSentence(@Header("X-User-Token") String userToken);
}
