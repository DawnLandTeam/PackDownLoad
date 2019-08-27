package cn.dawnland.packdownload.utils;

import cn.dawnland.packdownload.task.UnZipSubTask;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXProgressBar;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * @author Cap_Sub
 */
public class ZipUtils {

    public static void unzip(String zipFile, String location, JFXListView taskList, ExecutorService pool) throws IOException {
        try {

            File f = new File(location);
            if(!f.isDirectory()) {
                f.mkdirs();
            }

            ZipFile zf = new ZipFile(zipFile);

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
                label.setPrefWidth(60D);
                label.setAlignment(Pos.CENTER_RIGHT);
                hb.getChildren().addAll(unzipLabel, unzipBar, label);
                taskList.getItems().add((hb));
                MessageUtils.info("正在读取压缩文件，稍等即可");
                pool.submit(()-> UIUpdateUtils.initUnzip(hb, unzipBar, label, zf.size()));
            });
            pool.submit(new UnZipSubTask(zin, pool, location));
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
            if(strs[i] == s || s.startsWith(strs[i])){
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
                while((zipEntry = zis.getNextEntry()) != null){
                    if(key.equals(zipEntry.getName())){
                        InputStream is = zipFile.getInputStream(zipEntry);
                        FileOutputStream fos = new FileOutputStream(zipEntry.getName());
                        int len;
                        while((len = is.read()) != -1){
                            fos.write(len);
                        }
                break;
            }
        }
        return new File(zipEntry.getName());
    }


}