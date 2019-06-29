package task;

import com.alibaba.fastjson.JSONObject;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import utils.DownLoadUtils;

/**
 * @author Cap_Sub
 */
public class FilesDownLoadTask extends Task {

    private JSONObject jsonObject;
    private ProgressBar progressBar;
    private Integer filesCount;
    private Label proLabel;

    private final String MODS_PATH = ".minecraft/mods";

    public FilesDownLoadTask(JSONObject jsonObject, ProgressBar progressBar, Label proLabel, Integer filesCount) {
        this.jsonObject = jsonObject;
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
        //下载路径格式https://minecraft.curseforge.com/projects/319466/files/2706079/download
        //                                                     项目id        文件id
        String url = "https://www.curseforge.com/minecraft/mc-mods/projectID/download/fileID/file";
        int count = 3;
        for (int i = reCount; i < count; i++) {
            try {
                String enter = "\n";
                url = url.replaceFirst("projectID", jsonObject.get("projectID") + "");
                url = url.replaceFirst("fileID", jsonObject.get("fileID") + "");
                DownLoadUtils.downLoadFile(url, null, MODS_PATH, null, progressBar, proLabel, 1D/filesCount);
                break;
            } catch (Exception e) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                if((reCount + 1) == count){
                    //*(新)下载路径格式https://www.curseforge.com/minecraft/mc-mods/projectID/download/fileID/file
                    //                                                     项目id        文件id
                    url = "https://www.curseforge.com/minecraft/mc-mods/projectID/download/fileID/file";
                    System.out.print("尝试新下载连接:{}");
                    count = 6;
                }
                System.out.println(url + "{连接失败正在重试:" + (reCount + 1) + "}");
                reCount++;
            }
        }
    }






}
