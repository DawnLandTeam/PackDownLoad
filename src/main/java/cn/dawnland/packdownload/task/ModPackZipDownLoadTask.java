package cn.dawnland.packdownload.task;

import cn.dawnland.packdownload.controller.PackDownLoadNewController;
import cn.dawnland.packdownload.utils.CommonUtils;
import cn.dawnland.packdownload.utils.MessageUtils;
import com.jfoenix.controls.JFXListView;

/**
 * @author Cap_Sub
 */
public class ModPackZipDownLoadTask implements Runnable {

    private String zipFilePath;
    private JFXListView taskList;

    public ModPackZipDownLoadTask(String zipFilePath, JFXListView taskList) {
        this.zipFilePath = zipFilePath;
        this.taskList = taskList;
    }

    @Override
    public void run() {
        PackDownLoadNewController.setDisplay();
        MessageUtils.info("正在解析整合包ZIP...");
        CommonUtils.getPool().submit(new JsonJXTask(zipFilePath));
    }
}
