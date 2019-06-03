package task;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import utils.MessageUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;

/**
 * @author Cap_Sub
 */
public class UnZipTask implements Runnable{

    private String location;
    private ZipEntry ze;
    private ProgressBar progressBar;
    private List<Integer> cs;
    private Double proSize;
    private Label proLabel;

    private FileOutputStream fos;

    public UnZipTask(String location, ZipEntry ze, ProgressBar progressBar, List<Integer> cs, Double proSize, Label proLabel) {
        this.location = location;
        this.ze = ze;
        this.progressBar = progressBar;
        this.cs = cs;
        this.proSize = proSize;
        this.proLabel = proLabel;
    }

    @Override
    public void run() {
        try {
            String path = location + "\\" + ze.getName();
            path = path.replaceFirst("overrides", ".minecraft");
            if (ze.isDirectory()) {
                File unzipFile = new File(path);
                if(!unzipFile.isDirectory()) {
                    unzipFile.mkdirs();
                }
            }
            else {
                fos = new FileOutputStream(path, false);
                cs.forEach(c -> {
                    try {
                        fos.write(c);
                    } catch (IOException e) {
                        MessageUtils.error(e);
                        e.printStackTrace();
                    }
                });
            }
        }catch (Exception e){

        }finally {
            Platform.runLater(() -> {
                progressBar.setProgress(progressBar.getProgress() + proSize);
                String[] split = proLabel.getText().split("/");
                proLabel.setText(Integer.valueOf(split[0]) + 1 + "/" + split[1]);
                if((Integer.valueOf(split[0]) + 1) == Integer.valueOf(split[1])){
                    Label resultLabel = MessageUtils.resultLabel;
                    Label downloadSpeed = MessageUtils.downloadSpeed;
                    if("下载完成".equals(downloadSpeed.getText()) || "下载完成".equals(resultLabel.getText())){
                        resultLabel.setText("安装完成");
                    }else{
                        resultLabel.setText("解压完成");
                    }
                }
            });
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    MessageUtils.error(e);
                    e.printStackTrace();
                }
            }
        }
    }
}
