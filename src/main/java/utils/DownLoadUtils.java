package utils;

import com.alibaba.fastjson.JSONObject;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import model.DownLoadModel;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;

public class DownLoadUtils {

    private static String rootPath = DownLoadUtils.getRootPath() + "/.minecraft";

    /**
     * 下载整合包文件
     * @param url 下载链接
     * @param fileName 保存文件的文件名
     * @param path 相对于.minecraft的路径(.minecraft = 根目录)
     */
    public static boolean downLoadFile(String url, String fileName, String path, Long size, ProgressBar progressBar, Label proLabel, Double proSize) throws IOException {
        path = rootPath + "/" + path;
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        //如果传入文件size 则判断本地是否已经下载该文件且大小一致(则不下载直接返回)
        if(size != null && size > 0){
            File localFile = new File(path + "/" + fileName);
            if(localFile.exists() && size.equals(localFile.length())){
                return false;
            }
        }
        DownLoadUtils.downLoadFromUrl(url, fileName, path, progressBar, proLabel, proSize);
        return true;
    }

    /**
     * 下载整合包文件
     * @param url 下载链接
     * @param fileName 保存文件的文件名
     * @param path 相对于.minecraft的路径(.minecraft = 根目录)
     */
    public static boolean downLoadFile(String url, String fileName, String path, Long size) throws IOException {
        path = rootPath + "/" + path;
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        //如果传入文件size 则判断本地是否已经下载该文件且大小一致(则不下载直接返回)
        if(size != null && size > 0){
            File localFile = new File(path + "/" + fileName);
            if(localFile.exists() && size.equals(localFile.length())){
                return false;
            }
        }
        DownLoadUtils.downLoadFromUrl(url, fileName, path);
        return true;
    }

    /**
     * 获取运行目录
     * @return
     */
    public static String getRootPath(){
        return System.getProperty("user.dir");
    }

    /**
     * 从网络Url中下载文件
     * @param urlStr
     * @param fileName
     * @param savePath
     * @throws IOException
     */
    public static void  downLoadFromUrl(String urlStr, String fileName, String savePath) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        //设置超时间为3秒
        conn.setConnectTimeout(3*1000);
        //防止屏蔽程序抓取而返回403错误
        conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        //得到输入流
        InputStream inputStream = conn.getInputStream();
        //获取自己数组
        byte[] getData = readInputStream(inputStream);
        //文件保存位置
        fileName = conn.getURL().getPath().substring(conn.getURL().getPath().lastIndexOf("/") + 1);
        long fileSize = Long.valueOf(conn.getHeaderField("Content-Length"));

        File saveDir = new File(savePath);
        String path = saveDir + File.separator + fileName;
        File file = new File(path);

        //如果传入文件size 则判断本地是否已经下载该文件且大小一致(则不下载直接返回)
        if(fileSize > 0){
            File localFile = new File(path);
            if(localFile.exists() && fileSize == localFile.length()){
                System.out.println(fileName + "已存在:大小" + fileSize);
                return;
            }
        }

        FileOutputStream fos = new FileOutputStream(file);
        fos.write(getData);
        if(fos!=null){
            fos.close();
        }
        if(inputStream!=null){
            inputStream.close();
        }
    }

    /**
     * 从网络Url中下载文件
     * @param urlStr
     * @param fileName
     * @param savePath
     * @throws IOException
     */
    public static void  downLoadFromUrl(String urlStr, String fileName, String savePath, ProgressBar progressBar, Label proLabel, Double proSize) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        //设置超时间为3秒
        conn.setConnectTimeout(3*1000);
        //防止屏蔽程序抓取而返回403错误
        conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        //得到输入流
        InputStream inputStream = conn.getInputStream();
        //获取自己数组
        byte[] getData = readInputStream(inputStream);
        //文件保存位置
        fileName = conn.getURL().getPath().substring(conn.getURL().getPath().lastIndexOf("/") + 1);
        long fileSize = Long.valueOf(conn.getHeaderField("Content-Length"));

        File saveDir = new File(savePath);
        String path = saveDir + File.separator + fileName;
        File file = new File(path);

        //判断文件size 本地是否已经下载该文件且大小一致(则不下载直接返回)
        if(fileSize > 0){
            File localFile = new File(path);
            if(localFile.exists() && fileSize == localFile.length()){
                System.out.println(fileName + "已存在:大小" + fileSize);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setProgress(progressBar.getProgress() + proSize);
                        String[] split = proLabel.getText().split("/");
                        proLabel.setText(Integer.valueOf(split[0]) + 1 + "/" + split[1]);
                        if((Integer.valueOf(split[0]) + 1) == Integer.valueOf(split[1])){
                            Parent parent = progressBar.getParent();
                            AnchorPane anchorPane = (AnchorPane) parent.getParent().getParent();
                            BorderPane borderPane = (BorderPane) parent.getParent();
//                            borderPane.getChildren().remove(parent);
                            HBox hBox = (HBox) anchorPane.getChildren().get(1);
                            Label resultLabel = (Label)hBox.getChildren().get(0);
                            if(resultLabel.getText().equals("解压完成")){
                                resultLabel.setText("安装完成");
                            }else{
                                resultLabel.setText("下载完成");
                            }
                        }
                    }
                });
                return;
            }
        }

        FileOutputStream fos = new FileOutputStream(file);
        fos.write(getData);
        if(fos!=null){
            fos.close();
        }
        if(inputStream!=null){
            inputStream.close();
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                progressBar.setProgress(progressBar.getProgress() + proSize);
                String[] split = proLabel.getText().split("/");
                proLabel.setText(Integer.valueOf(split[0]) + 1 + "/" + split[1]);
            }
        });
    }

    /**
     * 从输入流中获取字节数组
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static  byte[] readInputStream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int len;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while((len = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        bos.close();
        return bos.toByteArray();
    }

    /**
     * 获取文件大小
     * @param file
     */
    public static long getFileSize(File file) {
        if (file.exists() && file.isFile()) {
            return file.length();
        }
        return 0L;
    }

    /**
     * packCode 获取 需下载JSON数据
     */
    public static List<DownLoadModel> getDownLoadPacks(String packCode) throws Exception {
        URL url = new URL("http://mc.dawnland.cn:8443/api/proxy/" + packCode);
        InputStream ins = url.openConnection().getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(ins));
        String str = reader.readLine();
        Object oStr = JSONObject.parseObject(str).get("proxies");
        List<DownLoadModel> list = JSONObject.parseArray(oStr.toString(), DownLoadModel.class);
        return list;
    }



}
