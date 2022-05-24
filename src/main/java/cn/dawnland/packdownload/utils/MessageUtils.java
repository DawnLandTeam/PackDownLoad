package cn.dawnland.packdownload.utils;

import cn.dawnland.packdownload.controller.PackDownLoadNewController;
import javafx.application.Platform;
import javafx.scene.control.Label;

import javax.swing.*;
import java.text.DecimalFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Cap_Sub
 * 信息提示相关工具
 * @version 创建时间：2019/05/30 14:42
 */
public class MessageUtils {

    public static Label resultLabel;
    public static AtomicInteger sizeAI = new AtomicInteger(0);
    private static final ExecutorService pool = Executors.newFixedThreadPool(1);
    public static Label downloadSpeed;

    public static int status = 0;

    public static Boolean isOk(){
        return status >= 2;
    }

    public synchronized static boolean check(){
        return checkUnzip() && checkMod();
    }

    private static boolean checkUnzip(){
        return UIUpdateUtils.unzipPoint.get() == UIUpdateUtils.unzipCount;
    }

    private static boolean checkMod(){
        return UIUpdateUtils.modsPoint.get() == UIUpdateUtils.modsCount;
    }

    public synchronized static void setStatus(){
        status = status + 1;
    }

    public static void error(Exception e){
        Platform.runLater(() -> {
            JOptionPane.showMessageDialog(null, "未知错误", e.getStackTrace().toString(), 0);
            UIUpdateUtils.startButton.setDisable(false);
        });
        LogUtils.error(e);
        e.printStackTrace();
        PackDownLoadNewController.restart();
    }

    public static void error(String msg, String title){
        Platform.runLater(() -> JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE));
        PackDownLoadNewController.restart();
    }

    public static void info(String msg, String title){
        Platform.runLater(() -> JOptionPane.showMessageDialog(null, msg, title, 1));
    }

    public static void info (String msg) {
        try {
            Platform.runLater(() -> resultLabel.setText(msg));
        } catch (Exception e) {
            LogUtils.info(msg);
        }
    }

    public static void downloadSpeedStart(){
        if(downloadSpeed == null || "".equals(downloadSpeed.getText())){
            pool.submit((Runnable) () -> {
                while (true) {
                    int size = sizeAI.get();
                    try {
                        Thread.sleep(500);
                        int speed = sizeAI.get() - size;
                        if(speed > 0 && downloadSpeed != null){
                            UIUpdateUtils.updateLabel(downloadSpeed, readableFileSize(speed / 0.5) + "/s");
                            sizeAI.set(0);
                        }else if(downloadSpeed != null){
                            UIUpdateUtils.updateLabel(downloadSpeed, "0kb/s");
                        }
                    } catch (InterruptedException e) {
                        MessageUtils.error(e);
                    }
                }
            });
        }
    }

    public static String readableFileSize(double size) {
        if (size <= 0) { return "0"; }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

}
