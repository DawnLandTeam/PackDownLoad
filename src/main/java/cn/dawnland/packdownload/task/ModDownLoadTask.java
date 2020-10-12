package cn.dawnland.packdownload.task;

import cn.dawnland.packdownload.listener.DownloadListener;
import cn.dawnland.packdownload.model.manifest.Manifest;
import cn.dawnland.packdownload.model.manifest.ManifestFile;
import cn.dawnland.packdownload.utils.DownLoadUtils;
import cn.dawnland.packdownload.utils.UIUpdateUtils;
import javafx.application.Platform;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

public class ModDownLoadTask extends BaseTask<ManifestFile> {

    private final Manifest manifest;
    private final ManifestFile manifestFile;
    private final String path;

    public ModDownLoadTask(Manifest manifest, ManifestFile manifestFile, String path) {
        this.manifest = manifest;
        this.manifestFile = manifestFile;
        this.path = path;
    }
    @Override
    protected void subTask() {
        AtomicInteger progressIndex = new AtomicInteger(0);
        DownLoadUtils.downLoadMod(manifestFile.getDownloadUrl(), path, new DownloadListener(manifestFile) {
            @Override
            public void onSuccess(File file) {
                Platform.runLater(() -> {
                    super.onSuccess(file);
                    manifestFile.setDownloadSucceed(Boolean.TRUE);
                    UIUpdateUtils.modsBarAddOne();
                    manifest.save();
                });
                callback.progressCallback(progressIndex.addAndGet(1), file);
            }
        });
        callback.successCallback();
    }
}
