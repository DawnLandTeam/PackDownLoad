package cn.dawnland.packdownload.listener;

import cn.dawnland.packdownload.model.manifest.ManifestFile;
import cn.dawnland.packdownload.utils.CommonUtils;
import cn.dawnland.packdownload.utils.DownLoadUtils;
import cn.dawnland.packdownload.utils.MessageUtils;
import com.jfoenix.controls.JFXProgressBar;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.io.File;
import java.util.Objects;

/**
 * @author Created by cap_sub@dawnland.cn
 */
public abstract class DownloadListener{

    public final Label titleLabel = new Label();
    public final JFXProgressBar modsBar = new JFXProgressBar();
    public final Label barLabel = new Label();
    public final HBox hb = new HBox();
    public String defaultText;
    private ManifestFile manifestFile;

    public DownloadListener() {
        init();
    }

    public DownloadListener(ManifestFile manifestFile) {
        this.manifestFile = manifestFile;
        this.defaultText = manifestFile.getDisName();
        init();
    }

    private void init(){
        hb.setPrefWidth(360D);
        hb.setSpacing(10D);
        hb.setAlignment(Pos.CENTER);
        if(Objects.nonNull(manifestFile)){
            hb.setOnMouseClicked(event -> {
                CommonUtils.setClipboardString(manifestFile.getDownloadUrl());
                MessageUtils.info("复制下载地址成功", "获取链接");
            });
        }
        modsBar.setPrefWidth(70D);
        modsBar.setMaxHeight(5D);
        modsBar.setProgress(0);
        titleLabel.setText("解析中:" + (defaultText == null || "".equals(defaultText) ? Thread.currentThread().getName() : defaultText));
        titleLabel.setPrefWidth(150D);
        titleLabel.setMaxHeight(5);
        barLabel.setAlignment(Pos.CENTER_RIGHT);
        barLabel.setPrefWidth(40D);
        barLabel.setAlignment(Pos.CENTER_LEFT);
        Platform.runLater(() -> {
            hb.getChildren().addAll(titleLabel, modsBar, barLabel);
            DownLoadUtils.taskList.getItems().add(hb);
        });
    }

    /**
     * 下载完成通知
     * 如果重写此方法则必须在方法首行执行super.onSuccess(file)
     * @param file
     */
    public void onSuccess(File file){
        this.removeHb();
    }

    /**
     * 下载进度通知
     */
    public void onProgress(int progress, String filename){
        Platform.runLater(() -> {
            titleLabel.setText("下载中:" + filename);
            barLabel.setText(progress + "%");
            modsBar.setProgress(progress / 100D);
        });
    }

    /**
     * 下载失败通知
     */
    public void onFailed(String filename, String url){
        System.out.println("下载失败:" + filename + "-" + url);
        DownLoadUtils.downloadFaildModS.put(filename, url);
    }

    /**
     * 下载暂停通知
     */
    public void onPaused(){
        Platform.runLater(() -> barLabel.setText("下载暂停"));
    }

    /**
     * 下载取消通知
     */
    public void onCanceled(){
        this.removeHb();
    }

    private void removeHb(){
        if(DownLoadUtils.taskList.getItems().contains(this.hb)){
            Platform.runLater(() -> DownLoadUtils.taskList.getItems().remove(this.hb));
        }
    }
}
