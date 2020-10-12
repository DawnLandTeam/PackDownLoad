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
    private final String ADDON_URL = "https://addons-ecs.forgesvc.net/api/v2/addon/%s/file/%s/download-url";

    public void request(ManifestFile manifestFile) {
        CommonUtils.getPool().submit(() -> {
            try {
                if(manifestFile.getDownloadUrl() == null || manifestFile.getDownloadUrl().trim().length() < 1){
                    manifestFile.setDownloadUrl(OkHttpUtils.get().get(String.format(ADDON_URL, manifestFile.getProjectID(), manifestFile.getFileID())));
                }
            } catch (IOException e) {
                MessageUtils.error(e);
            }
            manifestFile.setDisName(manifestFile.getProjectID() + ":" + manifestFile.getFileID());
            new ModDownLoadTask(manifest, manifestFile, MODS_PATH).subTask();
        });
    }
}
