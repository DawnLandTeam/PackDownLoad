package cn.dawnland.packdownload.utils;

import cn.dawnland.packdownload.listener.DownloadListener;
import cn.dawnland.packdownload.model.ForgeVersion;
import cn.dawnland.packdownload.task.DownloadTask;
import cn.dawnland.packdownload.task.TaskProfile;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfoenix.controls.JFXListView;
import javafx.application.Platform;
import javafx.scene.layout.HBox;

import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

/**
 * @author Cap_Sub
 */
public class DownLoadUtils {


    public static ConcurrentMap<String, String> downloadFaildModS = new ConcurrentHashMap();

    public static JFXListView<HBox> taskList;

    private static Pattern FilePattern = Pattern.compile("[\\\\/:*?\"<>|]");

    private static String rootPath;
    private static String packPath;

    static {
        rootPath = DownLoadUtils.getRootPath();
    }

    public static String getPackPath(){
        if(packPath == null){
            packPath = getRootPath() + "/.minecraft";
        }
        return packPath;
    }

    public static void setPackPath(String path){
        if(!Paths.get(path).toFile().exists()){
            try {
                Files.createDirectories(Paths.get(path));
            } catch (IOException e) {
                MessageUtils.error(e);
            }
        }
        packPath = path;
    }

    /**
     * 下载整合包文件
     * @param url 下载链接
     * @param path 相对于.minecraft的路径(.minecraft = 根目录)
     */
    public static boolean downLoadMod(String url, String path, DownloadListener downloadListener) {
        if(path == null || "".equals(path)){
            path = rootPath;
        }
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        DownLoadUtils.downLoadModFromUrl(url, path, downloadListener);
        return true;
    }


    /**
     * 获取运行目录
     * @return
     */
    public static String getRootPath(){
        if(rootPath == null){
            return System.getProperty("user.dir");
        }
        return rootPath;
    }

    public static void setRootPath(String path){
        rootPath = path;
    }

    /**
     * 从网络Url中下载文件
     * @param urlStr
     * @param savePath
     * @param downloadListener
     */
    public static void downLoadModFromUrl(String urlStr, String savePath, DownloadListener downloadListener) {
        new DownloadTask(savePath, downloadListener).startDownload(urlStr);
    }

    /**
     * 从网络Url中下载文件到指定目录 通用方法
     * @param urlStr
     * @param savePath
     * @param downloadListener
     */
    public static void downLoadFromUrl(String urlStr, String savePath, DownloadListener downloadListener) {
        new DownloadTask(savePath, downloadListener).startDownload(urlStr);
    }

    public static void downloadVersionJson(String mcVersion, String forgeVersion, String installUrl) throws IOException {
        MessageUtils.info("正在安装核心,获取Mojang配置中...");
        String s = OkHttpUtils.get().get(MojangUtils.getJsonUrl(mcVersion));
        JSONObject jsonObject = JSONObject.parseObject(s);
        MessageUtils.info("正在下载Forge...");
        DownLoadUtils.downLoadFromUrl(installUrl, DownLoadUtils.getPackPath(), new DownloadListener() {
            @Override
            public void onSuccess(File file){
                Platform.runLater(() -> {
                    if(this.hb.getParent() != null){
                        UIUpdateUtils.taskList.getItems().remove(this.hb);
                    }
                });
                MessageUtils.info("Forge下载完成,正在安装Forge...");
                File versionJsonFile = null;
                try {
                    versionJsonFile = ZipUtils.getZipEntryFile(file.getPath(), "version.json");
                } catch (IOException e) {
                    MessageUtils.error(e);
                }
                String versionJson = FileUtils.readJsonData(versionJsonFile.toPath());
                JSONObject versionObject = JSONObject.parseObject(versionJson);
                JSONArray libraries = (JSONArray) versionObject.get("libraries");
                libraries.addAll((JSONArray)jsonObject.get("libraries"));
                MessageUtils.info("正在添加依赖");
                jsonObject.put("libraries", libraries);
                jsonObject.put("minecraftArguments", versionObject.get("minecraftArguments"));
                jsonObject.put("mainClass", versionObject.get("mainClass"));
                MessageUtils.info("正在下载配置文件...");
                DownLoadUtils.downLoadFromUrl("https://dawnland.cn/hmclversion.cfg", DownLoadUtils.getPackPath(), new DownloadListener() {
                    @Override
                    public void onSuccess(File file) {
                        Platform.runLater(() -> {
                            if(this.hb.getParent() != null){
                                UIUpdateUtils.taskList.getItems().remove(this.hb);
                            }
                        });
                        String tempStr = packPath.substring(packPath.lastIndexOf("/") + 1);
                        File f = new File(packPath + "/" + (".minecraft".equals(tempStr) ? "versions/" + mcVersion + "/" + mcVersion + ".json" : tempStr + ".json"));
                        if(!f.isDirectory()) {
                            File tempF = new File(f.getPath().substring(0, f.getPath().lastIndexOf("\\")));
                            tempF.mkdirs();
                            f = new File(packPath + "/" + (".minecraft".equals(tempStr) ? "versions/" + mcVersion + "/" + mcVersion + ".json" : tempStr + ".json"));
                        }
                        FileOutputStream fio = null;
                        try {
                            fio = new FileOutputStream(f);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        PrintStream ps = new PrintStream(fio);
                        ps.print(jsonObject.toJSONString());
                        ps.close();
                        try {
                            fio.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if(DownLoadUtils.downloadFaildModS.size() > 0){
//                        CurseUtils.failsMod(DownLoadUtils.downloadFaildModS);
                            Platform.runLater(() -> {
                                LogUtils.error("有" + DownLoadUtils.downloadFaildModS.size() + "个mod下载失败 请尝试重新下载");
                            });
                        }else{
                            MessageUtils.info("安装完成");
                        }
                    }
                });
            }
        });
    }

    public static void installForge(ForgeVersion forgeVersion) throws IOException {
        String mcVersion = forgeVersion.getMcVersion();
        MessageUtils.info("正在安装核心,获取Mojang配置中...");
        String s = OkHttpUtils.get().get(MojangUtils.getJsonUrl(mcVersion));
        JSONObject jsonObject = JSONObject.parseObject(s);
        MessageUtils.info("正在获取Forge配置...");
        String versionJson = forgeVersion.getForgeVersionJson();
        String versionJsonStr = (String) JSONObject.parseObject(versionJson).get("versionJson");
        versionJsonStr = versionJsonStr.replaceAll("\r\n", "");
        JSONObject versionObject = JSONObject.parseObject(versionJsonStr);

        JSONArray libraries = (JSONArray) versionObject.get("libraries");
        libraries.addAll((JSONArray)jsonObject.get("libraries"));
        MessageUtils.info("正在添加依赖");
        jsonObject.put("libraries", libraries);
        jsonObject.put("minecraftArguments", versionObject.get("minecraftArguments"));
        jsonObject.put("mainClass", versionObject.get("mainClass"));
        MessageUtils.info("正在下载配置文件...");
        DownLoadUtils.downLoadFromUrl("https://dawnland.cn/hmclversion.cfg", DownLoadUtils.getPackPath(), new DownloadListener() {
            @Override
            public void onSuccess(File file) {
                Platform.runLater(() -> {
                    if(this.hb.getParent() != null){
                        UIUpdateUtils.taskList.getItems().remove(this.hb);
                    }
                });
                String tempStr = packPath.substring(packPath.lastIndexOf("/") + 1);
                File f = new File(packPath + "/" + (".minecraft".equals(tempStr) ? "versions/" + mcVersion + "/" + mcVersion + ".json" : tempStr + ".json"));
                if(!f.isDirectory()) {
                    File tempF = new File(f.getPath().substring(0, f.getPath().lastIndexOf("\\")));
                    tempF.mkdirs();
                    f = new File(packPath + "/" + (".minecraft".equals(tempStr) ? "versions/" + mcVersion + "/" + mcVersion + ".json" : tempStr + ".json"));
                }
                FileOutputStream fio = null;
                try {
                    fio = new FileOutputStream(f);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                PrintStream ps = new PrintStream(fio);
                ps.print(jsonObject.toJSONString());
                ps.close();
                try {
                    fio.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                MessageUtils.info("安装完成");
                try {
                    Files.deleteIfExists(Paths.get(DownLoadUtils.getPackPath() + "/successMod.txt"));
                } catch (IOException e) {
                    Platform.runLater(() -> JOptionPane.showMessageDialog(null, "请手动删除目录下的successMod.txt", "啊删除临时文件失败啦", JOptionPane.ERROR_MESSAGE));
                }
                isOpenLanauch();
            }
        });
    }

    public static void isOpenLanauch(){
        JOptionPane.showMessageDialog(null, "打开启动器开始玩耍吧!", "安装完成", 1);
        Runtime runtime = null;
        try {
            Runtime.getRuntime().exec("cmd /c start explorer " + DownLoadUtils.getRootPath());
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (null != runtime) {
                runtime.runFinalization();
            }
        }
        CommonUtils.appExit();
    }

    public static void downloadFailModsAdd(String filename, String url, String savePath, DownloadListener listener){
        downloadFaildModS.put(filename, url);
        new DownloadTask(savePath, listener).startDownload(url);
    }

}
