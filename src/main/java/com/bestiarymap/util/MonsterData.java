package com.bestiarymap.util;

import com.google.gson.Gson;
import lombok.Getter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Singleton
public class MonsterData {
    // TODO: Temporary URL + need to add caching
    private static final String DATA_URL = "https://www.seanscorner.com/sites/osrs_monsters/read_monster_data.php";

    private final OkHttpClient okHttpClient;
    private final Gson gson;

    @Getter
    private volatile List<Monster> monsters = Collections.emptyList();

    @Inject
    public MonsterData(OkHttpClient okHttpClient, Gson gson) {
        this.okHttpClient = okHttpClient;
        this.gson = gson;
    }

    public void loadMonsterData() {
        Request request = new Request.Builder().url(DATA_URL).build();

        okHttpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                monsters = Collections.emptyList();

                // TODO: Log an error to console (Web request failed)
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    monsters = Collections.emptyList();

                    // TODO: Log an error to console (blank response)
                    return;
                }

                monsters = Arrays.asList(gson.fromJson(response.body().string(), Monster[].class));
            }
        });
    }
}
