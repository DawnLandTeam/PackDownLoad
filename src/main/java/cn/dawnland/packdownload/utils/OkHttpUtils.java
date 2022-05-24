package cn.dawnland.packdownload.utils;

import cn.dawnland.packdownload.configs.RetryInterceptor;
import cn.dawnland.packdownload.listener.DownloadListener;
import okhttp3.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * @author Cap_Sub
 * 文件下载工具类（单例模式）
 */
public class OkHttpUtils{

    private static OkHttpUtils downloadUtil;
    private final OkHttpClient okHttpClient;

    public final static ConcurrentMap<String, Object> cells = new ConcurrentHashMap<>();

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public static OkHttpUtils get() {
        if (Objects.isNull(downloadUtil)) {
            downloadUtil = new OkHttpUtils();
        }
        return downloadUtil;
    }

    public OkHttpUtils() {
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(10, TimeUnit.SECONDS)
//                .addInterceptor(new RetryInterceptor(10))
                .addNetworkInterceptor(chain -> {
                    System.out.println("url: " + chain.request().url());
                    return chain.proceed(chain.request());
                })
                .build();
        okHttpClient.dispatcher().setMaxRequests(20);
    }

    /**
     * @param url          下载连接
     * @param saveFilePath 文件储存完整路径+文件名
     * @param listener     下载监听
     */
    public void download(String url, final String saveFilePath, final DownloadListener listener) {
        if(!Paths.get(saveFilePath).toFile().exists()){
            try {
                Files.createDirectories(Paths.get(saveFilePath));
            } catch (IOException e) {
                listener.onFailed(null, url);
            }
        }
        Request request = new Request.Builder().url(url).build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 下载失败监听回调
                listener.onFailed(null, url);
                System.out.println(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String filename = response.request().url().uri().getPath();
                filename = filename.substring(filename.lastIndexOf("/") + 1);
                cells.putIfAbsent(filename, call);
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;

                //储存下载文件
                File file = Paths.get(saveFilePath + File.separator + filename).toFile();
                if(file.exists() && file.length() == response.body().contentLength()){
                    listener.onSuccess(file);
                    return;
                }
                try {

                    is = response.body().byteStream();
                    long total = response.body().contentLength();
                    fos = new FileOutputStream(file);
                    long sum = 0;
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                        sum += len;
                        MessageUtils.sizeAI.addAndGet(len);
                        int progress = (int) (sum * 1.0f / total * 100);
                        //下载中更新进度条
                        listener.onProgress(progress, filename);
                    }
                    fos.flush();
                    //下载完成
                    listener.onSuccess(file);
                } catch (Exception e) {
                    listener.onFailed(null, url);

                }finally {

                    try {
                        if (is != null) {
                            is.close();
                        }
                        if (fos != null) {
                            fos.close();
                        }
                    } catch (IOException e) {

                    }

                }


            }
        });
    }

    public String get(final String url) throws IOException {
        Request request = new Request.Builder().url(url).header("x-api-key", "$2a$10$pHPi1WiZ7pWAcIr6ap89r.FefOLhk2Q0whs4bADku3Nj5D4v9Wqde").build();
        Response response = okHttpClient.newCall(request).execute();
        return response.body().string();
    }

}