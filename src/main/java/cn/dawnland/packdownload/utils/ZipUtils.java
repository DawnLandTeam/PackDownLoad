package cn.dawnland.packdownload.utils;

import cn.dawnland.packdownload.model.manifest.Manifest;
import cn.dawnland.packdownload.task.UnZipSubTask;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXProgressBar;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * @author Cap_Sub
 * ZIP相关工具 只针对该程序
 */
public class ZipUtils {

    public static void unzip(Manifest manifest, String zipFile, String location, JFXListView taskList) throws IOException {
        try {

            File f = new File(location);
            if(!f.isDirectory()) {
                f.mkdirs();
            }

            ZipFile zf = new ZipFile(zipFile);
            UIUpdateUtils.unzipCount = zf.size();

            ZipInputStream zin = new ZipInputStream(new FileInputStream(zipFile));

            Platform.runLater(() -> {
                JFXProgressBar unzipBar = new JFXProgressBar();
                Label unzipLabel = new Label("解压进度");
                unzipBar.setPrefWidth(70);
                HBox hb = new HBox();
                Label label = new Label();
                hb.setPrefWidth(350D);
                hb.setSpacing(10D);
                hb.setAlignment(Pos.CENTER);
                unzipBar.setPrefWidth(130D);
                unzipBar.setMaxHeight(5D);
                unzipBar.setProgress(0);
                unzipLabel.setAlignment(Pos.CENTER_LEFT);
                unzipLabel.setPrefWidth(60D);
                unzipLabel.setMaxHeight(5);
                label.setPrefWidth(100D);
                label.setAlignment(Pos.CENTER_RIGHT);
                label.setText("0/" + zf.size());
                hb.getChildren().addAll(unzipLabel, unzipBar, label);
                taskList.getItems().add((hb));
                MessageUtils.info("正在读取压缩文件，稍等即可");
                CommonUtils.getPool().submit(()-> {
                    UIUpdateUtils.unzipBar = unzipBar;
                    UIUpdateUtils.unzipLabel = label;
                });
            });
            CommonUtils.getPool().submit(new UnZipSubTask(manifest, zin, location));
        }
        catch (Exception e) {
            MessageUtils.error(e);
            e.printStackTrace();
        }
    }


    public static List<FileOutputStream> copyFile(String zipFilePath, String ... files){
        System.setProperty("sun.zip.encoding", System.getProperty("sun.jnu.encoding")); //防止文件名中有中文时出错

        FileInputStream fis;
        List<FileOutputStream> foss = new ArrayList<>();
        try
        {
            ZipFile zipFile = new ZipFile(zipFilePath);
            fis = new FileInputStream(zipFilePath);
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry zipEntry = null;
            while((zipEntry = zis.getNextEntry()) != null){
                if(isHave(files, zipEntry.getName())){
                    InputStream is = zipFile.getInputStream(zipEntry);
                    FileOutputStream fos = new FileOutputStream(zipEntry.getName());
                    int len;
                    while((len = is.read()) != -1){
                        fos.write(len);
                    }
                    foss.add(fos);
                }
            }
            zis.closeEntry();
            zis.close();
            fis.close();
        }
        catch (IOException e)
        {
            MessageUtils.error(e);
            e.printStackTrace();
        }
        return foss;
    }

    public static boolean isHave(String[] strs,String s){

        /*此方法有两个参数，第一个是要查找的字符串数组，第二个是要查找的字符或字符串*/
        int i = strs.length;
        while (i-- > 0){
            if(strs[i].equals(s) || s.startsWith(strs[i])){
                return true;
            }
        }
        return false;
    }

    public static File getZipEntryFile(String zipFilePath, String key) throws IOException {
        FileInputStream fis = new FileInputStream(zipFilePath);
        ZipInputStream zis = new ZipInputStream(fis);
        ZipFile zipFile = new ZipFile(zipFilePath);
        ZipEntry zipEntry = null;
        while ((zipEntry = zis.getNextEntry()) != null) {
            System.out.println(zipEntry.getName());
            if (key.equals(zipEntry.getName())) {
                InputStream is = zipFile.getInputStream(zipEntry);
                FileOutputStream fos = new FileOutputStream(DownLoadUtils.getPackPath() + File.separator + zipEntry.getName());
                int len;
                while ((len = is.read()) != -1) {
                    fos.write(len);
                }
                break;
            }
        }
        return Paths.get(DownLoadUtils.getPackPath() + File.separator + zipEntry.getName()).toFile();
    }


}
