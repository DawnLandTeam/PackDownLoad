package cn.dawnland.packdownload.task;

import cn.dawnland.packdownload.listener.DownloadListener;
import cn.dawnland.packdownload.model.CurseModInfo;
import cn.dawnland.packdownload.utils.DownLoadUtils;
import cn.dawnland.packdownload.utils.UIUpdateUtils;
import javafx.application.Platform;

import java.io.File;

public class ModDownLoadTask extends BaseTask<String> {

    private final CurseModInfo curseModInfo;
    private final String path;

    public ModDownLoadTask(Callback<String> callback, CurseModInfo curseModInfo, String path) {
        super(callback);
        this.curseModInfo = curseModInfo;
        this.path = path;
    }
    @Override
    void subTask() {
        DownLoadUtils.downLoadMod(curseModInfo.getDownloadUrl(), path, new DownloadListener() {
            @Override
            public void onSuccess(File file) {
                Platform.runLater(() -> {
                    super.onSuccess(file);
                    UIUpdateUtils.modsBarAddOne();
                });
            }
        });
    }
}
