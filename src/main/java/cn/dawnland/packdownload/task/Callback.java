package cn.dawnland.packdownload.task;

import cn.dawnland.packdownload.utils.MessageUtils;

public abstract class Callback<T> {

    abstract void progressCallback(int progress, Object temp);

    abstract void successCallback();

    void exceptionCallback(Exception e){
        MessageUtils.error(e);
    }

}
