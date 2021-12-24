package com.huawei.anmy.slice;

import com.huawei.anmy.ApiService;
import com.huawei.anmy.model.Sentence;
import com.huawei.anmy.model.Token;
import com.huawei.anmy.ResourceTable;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Text;
import ohos.agp.window.service.WindowManager;
import ohos.app.dispatcher.task.TaskPriority;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.wifi.WifiDevice;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import java.net.HttpURLConnection;
import java.net.URL;

public class MainAbilitySlice extends AbilitySlice {
    public static final HiLogLabel LABEL_LOG = new HiLogLabel(3, 0xD001100, "#MainAbilitySlice");
    private ApiService apiService;
    private String poemSentence;
    private String[] stringSentence = new String[0];
    private String sentences = "";
    private Text poemText;
    private static long openTime;
    private static final int MIN_LOADING_TIME = 5000;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_main);
        getWindow().addFlags(WindowManager.LayoutConfig.MARK_SCREEN_ON_ALWAYS);
        poemText = (Text) findComponentById(ResourceTable.Id_poemtext);
        queryNetworkStatusAndGetPoem();
    }

    private void queryNetworkStatusAndGetPoem() {
        WifiDevice mWifiDevice = WifiDevice.getInstance(getContext());
        boolean isConnected = mWifiDevice.isConnected();
        if (!isConnected) {
            checkNetworkStatusByHttpRequestAndGetPoem();
        } else {
            getGlobalTaskDispatcher(TaskPriority.DEFAULT).asyncDispatch(() -> {
                try {
                    getPoemContent();
                } catch (Exception e) {
                    poemText.setText("竹杖芒鞋轻胜马,\r\n谁怕？\r\n一蓑烟雨任平生");
                    e.printStackTrace();
                }
            });
        }
    }

    private void checkNetworkStatusByHttpRequestAndGetPoem() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("https://www.google.com");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(3000);
                    int code = connection.getResponseCode();
                    if (code == 200) {
                        HiLog.info(LABEL_LOG, "===========connected=.");
                        getGlobalTaskDispatcher(TaskPriority.DEFAULT).asyncDispatch(() -> {
                            try {
                                getPoemContent();
                            } catch (Exception e) {
                                poemText.setText("竹杖芒鞋轻胜马,\r\n谁怕？\r\n一蓑烟雨任平生");
                                e.printStackTrace();
                            }
                        });
                    } else {
                        HiLog.error(LABEL_LOG, "===========disconnected=.");
                        present(new QrcodeAbilitySlice(), new Intent());
                        terminate();
                    }
                } catch (Exception e) {
                    present(new QrcodeAbilitySlice(), new Intent());
                    terminate();
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void initTextOnUIThread() {
        getUITaskDispatcher().asyncDispatch(() -> {
            poemText.setText(sentences);
        });
    }

    public static boolean isLongLoad() {
        boolean flag = false;
        long curTime = System.currentTimeMillis();
        if ((curTime - openTime) >= MIN_LOADING_TIME) {
            flag = true;
        }
        return flag;
    }

    private void getPoemContent() {
        openTime = System.currentTimeMillis();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://v2.jinrishici.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        getUserToken()
                .subscribeOn(Schedulers.io())
                .subscribe(s -> {
                    if (isLongLoad()) {
                        present(new QrcodeAbilitySlice(), new Intent());
                    } else {
                        getTokenSentence().subscribe(sentence -> {
                            poemSentence = sentence.getSentenceData().getContent();
                            poemSentence = poemSentence.substring(0, poemSentence.length() - 1);
                            if (poemSentence.contains("，")) {
                                stringSentence = poemSentence.split("，");
                                for (int i = 0; i < stringSentence.length; i++) {
                                    sentences = sentences + stringSentence[i] + "\r\n";
                                }
                            } else {
                                sentences = poemSentence;
                            }
                            initTextOnUIThread();
                            Thread.sleep(3000);
                            present(new QrcodeAbilitySlice(), new Intent());
                        });
                    }
                });
    }

    Observable<Token> getUserToken() {
        return apiService.getToken();
    }

    Observable<Sentence> getTokenSentence() {
        return apiService.getSentence("ReN4nsfzMfL9uRW9Ha3qbhgBYwlW/eb2");
    }

    @Override
    public void onActive() {
        super.onActive();
    }

    @Override
    public void onForeground(Intent intent) {
        super.onForeground(intent);
    }

}
