package cn.dawnland.packdownload.task;

import cn.dawnland.packdownload.configs.Config;
import cn.dawnland.packdownload.listener.DownloadListener;
import cn.dawnland.packdownload.model.manifest.Manifest;
import cn.dawnland.packdownload.model.manifest.ManifestFile;
import cn.dawnland.packdownload.utils.*;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Cap_Sub
 */
public class JsonJXTask extends BaseTask<Manifest> {

    private final String zipFilePath;

    public static Manifest manifest;

    public JsonJXTask(String zipFilePath) {
        this.zipFilePath = zipFilePath;
    }

    @Override
    void initProgress() {
        super.initProgress();
    }

    @Override
    public void run() {
        try {
            Path jsonPath = Paths.get(DownLoadUtils.getPackPath(), "manifest.json");
            if (!jsonPath.toFile().exists()) {
                try{
                    jsonPath = ZipUtils.getZipEntryFile(zipFilePath, "manifest.json").toPath();
                }catch (Exception e){
                    MessageUtils.error("不支持的整合包ZIP", "异常");
                    MessageUtils.info("不支持的整合包ZIP");
                    Thread.currentThread().stop();
                }
            }
            MessageUtils.downloadSpeedStart();
            CommonUtils.getPool().submit(() -> {
                MessageUtils.info("正在下载启动器...");
                Upgrader.downLoadFromUrl(Config.lancherUrl, "", new DownloadListener() {});
                Upgrader.downLoadFromUrl(Config.authlibInjectorsUrl, "", new DownloadListener() {});
            });
            String fileJson = FileUtils.readJsonData(jsonPath);
            try{
                manifest = JSONObject.parseObject(fileJson, Manifest.class);
            }catch (JSONException je){
                try{
                    jsonPath = ZipUtils.getZipEntryFile(zipFilePath, "manifest.json").toPath();
                    fileJson = FileUtils.readJsonData(jsonPath);
                }catch (Exception e){
                    MessageUtils.error("不支持的整合包ZIP", "异常");
                    MessageUtils.info("不支持的整合包ZIP");
                    Thread.currentThread().stop();
                }
                manifest = JSONObject.parseObject(fileJson, Manifest.class);
            }
            manifest.setThisJsonFilePath(jsonPath.toString());

            ZipUtils.unzip(manifest, zipFilePath, DownLoadUtils.getPackPath(), this.taskList);
            List<ManifestFile> files = manifest.getFiles();

            Set<ManifestFile> processedFiles = manifest.getFiles().stream().filter(f -> !f.isDownloadSucceed()).collect(Collectors.toSet());
            UIUpdateUtils.modsCount = processedFiles.size();
            processedFiles.forEach(this::request);
            String mcVersion = manifest.getMinecraft().getVersion();
            String forgeVersionStr = manifest.getMinecraft().getModLoaders().get(0).getId();
            ForgeUtils.downloadForgeNew(mcVersion, forgeVersionStr);
        } catch (Exception e) {
            MessageUtils.error(e);
        }

    }

    @Override
    protected void subTask() {
        run();
    }

    private final String MODS_PATH = DownLoadUtils.getPackPath() + "/mods";
    private final String ADDON_URL = "https://api.curseforge.com/v1/mods/%s/files/%s";

    private final String FILE_DOWNLOAD_BASE_URL = "https://media.forgecdn.net/files/%s/%s/%s";

    public void request(ManifestFile manifestFile) {
        CommonUtils.getPool().submit(() -> {
            if(!manifestFile.isDownloadSucceed()){
                String downloadUrl = getDownloadUrl(manifestFile.getProjectID().toString(), manifestFile.getFileID().toString());
                if(downloadUrl == null || downloadUrl.isEmpty()){
                    LogUtils.error(manifestFile.getProjectID() + ":" + manifestFile.getFileID() + "未获取到下载地址");
                }else{
                    LogUtils.info(manifestFile.getProjectID() + ":" + manifestFile.getFileID() + "获取下载地址成功:" + downloadUrl);
                    manifestFile.setDownloadUrl(downloadUrl);
                }
            }
            manifestFile.setDisName(manifestFile.getProjectID() + ":" + manifestFile.getFileID());
            new ModDownLoadTask(manifest, manifestFile, MODS_PATH).subTask();
        });
    }
    public String getDownloadUrl(String projectId, String fileId){
        try{
            String responseStr = OkHttpUtils.get().get(String.format(ADDON_URL, projectId, fileId));
            JSONObject object = JSONObject.parseObject(responseStr);
            JSONObject data = object.getObject("data", JSONObject.class);
            String fileName = (String) data.get("fileName");
            String requestDownloadUrl = (String) data.get("downloadUrl");
            String downloadUrl = requestDownloadUrl == null || requestDownloadUrl.isEmpty() ?
                    String.format(FILE_DOWNLOAD_BASE_URL, fileId.substring(0, 4), fileId.substring(4), fileName):
                    requestDownloadUrl.split("\\?")[0];
            return downloadUrl;
        }catch (IOException e){
            MessageUtils.error(e);
            return null;
        }
    }
}
