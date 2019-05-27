package task;

import com.alibaba.fastjson.JSONObject;
import controller.PackDownLoadController;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import main.MainController;
import model.DownLoadModel;
import utils.DownLoadUtils;
import utils.UIUpdateUtils;

/**
 * @author Cap_Sub
 */
public class FilesDownLoadTask extends Task {

    private JSONObject jsonObject;
    private PackDownLoadController packDownLoadController;
    private String rootPath;
    private ProgressBar progressBar;
    private Integer filesCount;
    private Label proLabel;

    public FilesDownLoadTask(JSONObject jsonObject, PackDownLoadController packDownLoadController, ProgressBar progressBar, Label proLabel, Integer filesCount) {
        this.jsonObject = jsonObject;
        this.packDownLoadController = packDownLoadController;
        this.progressBar = progressBar;
        this.proLabel = proLabel;
        this.filesCount = filesCount;
    }

    @Override
    protected Object call() {
        return null;
    }

    private int reCount = 0;

    @Override
    public void run() {
        String url = null;
        for (int i = reCount; i < 6; i++) {
            try {
                String enter = "\n";
                //下载路径格式https://minecraft.curseforge.com/projects/319466/files/2706079/download
                //                                                     项目id        文件id
                url = "https://minecraft.curseforge.com/projects/projectID/files/fileID/download";
                url = url.replaceFirst("projectID", jsonObject.get("projectID") + "");
                url = url.replaceFirst("fileID", jsonObject.get("fileID") + "");
                boolean flag = DownLoadUtils.downLoadFile(url, null, "mods", null, progressBar, proLabel, 1D/filesCount);
                System.out.println(flag ? jsonObject.get("projectID") + "成功" : jsonObject.get("projectID") + "失败");
                break;
            } catch (Exception e) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                System.out.println(url + "{连接失败正在重试:" + (reCount + 1) + "}");
                reCount++;
            }
        }
    }






}