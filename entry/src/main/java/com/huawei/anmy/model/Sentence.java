package com.huawei.anmy.model;

import com.google.gson.annotations.SerializedName;

public class Sentence {

    @SerializedName("data")
    SentenceData sentenceData;

    public SentenceData getSentenceData() {
        return sentenceData;
    }

    public void setSentenceData(SentenceData sentenceData) {
        this.sentenceData = sentenceData;
    }

    public Sentence(SentenceData sentenceData){
        this.sentenceData = sentenceData;
    }
}
