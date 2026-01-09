package io.github.johntortoise.core.callback;


public interface StreamCallBack{

    void send(String content);

    void finish();

    void onFailure();

    void tortoiseMsg(String content);
}
