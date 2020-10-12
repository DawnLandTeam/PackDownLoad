package cn.dawnland.packdownload.utils;

import cn.dawnland.packdownload.controller.PackDownLoadNewController;
import cn.dawnland.packdownload.model.InstallInfo;
import cn.dawnland.packdownload.task.JsonJXTask;
import cn.dawnland.packdownload.task.TaskProfile;
import com.jfoenix.controls.JFXListView;
import javafx.scene.layout.HBox;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newFixedThreadPool;

/**
 * @author Cap_Sub
 * 公共工具
 */
public class CommonUtils {

    private static JFXListView<HBox> taskList;
    private static ExecutorService pool;

    public static ExecutorService getPool() {
        if(Objects.isNull(pool)){
            pool = newFixedThreadPool(50);
        }
        return pool;
    }

    public static void setPool(ExecutorService pool) {
        if(Objects.isNull(pool)){
            pool = newFixedThreadPool(50);
        }
        CommonUtils.pool = pool;
    }

    /**
     * 退出软件
     */
    public static void appExit(){
//        FileUtils.deleteTmpFile();
        if(Objects.nonNull(JsonJXTask.manifest)){
            JsonJXTask.manifest.save();
        }
        if(!MessageUtils.check()){
            InstallInfo installInfo = InstallInfo.builder()
                    .packPath(DownLoadUtils.getPackPath())
                    .rootPath(DownLoadUtils.getRootPath())
                    .zipFilePath(PackDownLoadNewController.zipFile.getPath())
                    .threadCount(PackDownLoadNewController.threadCountInt)
                    .build();
            new FileToObjectUtils().save(Paths.get(System.getProperty("user.dir"), "PreviouslyUnfinished.json"), installInfo);
        }else{
            File file = Paths.get(System.getProperty("user.dir"), "PreviouslyUnfinished.json").toFile();
            if(file.exists()){
                file.deleteOnExit();
            }
        }
        System.exit(0);
    }

    public static void setClipboardString(String text) {
        // 获取系统剪贴板
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        // 封装文本内容
        Transferable trans = new StringSelection(text);
        // 把文本内容设置到系统剪贴板
        clipboard.setContents(trans, null);
    }

    public static JFXListView<HBox> getTaskList(){
        return taskList;
    }

}
