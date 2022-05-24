package cn.dawnland.packdownload.task;

import cn.dawnland.packdownload.listener.DownloadListener;
import cn.dawnland.packdownload.types.DownloadStatusType;
import cn.dawnland.packdownload.utils.DownLoadUtils;
import cn.dawnland.packdownload.utils.MessageUtils;
import cn.dawnland.packdownload.utils.OkHttpUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.*;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author Created by cap_sub@dawnland.cn
 * String 在执行AsyncTask时需要传入的参数，可用于在后台任务中使用。
 */
public class DownloadTask {

    private DownloadListener listener;
    private boolean isCanceled = false;
    private boolean isPaused = false;
    private int lastProgress;
    private String url;
    private String savePath;
    private String filename;
    private File file;

    public DownloadTask(String savePath, DownloadListener listener) {
        this.savePath = savePath;
        this.listener = listener;
    }

    public void startDownload(String url){
        this.url = url;
        onPostExecute(doInBackground());
    }

    /**
     * 这个方法中的所有代码都会在子线程中运行，我们应该在这里处理所有的耗时任务。
     * @return DownloadStatusType
     */
    public DownloadStatusType doInBackground() {
        MessageUtils.downloadSpeedStart();
        if(!Paths.get(savePath).toFile().exists()){
            try {
                Files.createDirectories(Paths.get(savePath));
            } catch (IOException e) {
                listener.onFailed(null, url);
            }
        }
        InputStream is = null;
        RandomAccessFile savedFile = null;
        //记录已经下载的文件长度
        long downloadLength = 0;
        //下载文件的名称
        try {
            this.filename = URLDecoder.decode(url.substring(url.lastIndexOf("/") + 1), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            this.filename = url.substring(url.lastIndexOf("/") + 1);
        }
        //下载文件存放的目录
        String directory = savePath;
        //创建一个文件
        this.file = new File(directory + File.separator +  this.filename);
        if (this.file.exists()) {
            //如果文件存在的话，得到文件的大小
            downloadLength = this.file.length();
        }

        OkHttpClient client = OkHttpUtils.get().getOkHttpClient();
        /**
         * HTTP请求是有一个Header的，里面有个Range属性是定义下载区域的，它接收的值是一个区间范围，
         * 比如：Range:bytes=0-10000。这样我们就可以按照一定的规则，将一个大文件拆分为若干很小的部分，
         * 然后分批次的下载，每个小块下载完成之后，再合并到文件中；这样即使下载中断了，重新下载时，
         * 也可以通过文件的字节长度来判断下载的起始点，然后重启断点续传的过程，直到最后完成下载过程。
         * 此处需要注意 Curse下载地址均经过302跳转 如果在跳转前加上断点续传头 则会出现请求异常
         */
        Request request = new Request.Builder()
                .url(url)
                .build();
//        try {
//            /**
//             * 此处需要注意 Curse下载地址均经过302跳转 如果在跳转前加上断点续传头 则会出现请求异常
//             * 所以得获得最终重定向的url后再添加断点续传头
//             * 但需要找到更好的解决方案 目前每个下载均需建立两次HTTP连接 严重增加耗时
//             */
//            request = client.newCall(request).execute()
//                    .request().newBuilder()
//                    .header("RANGE", "bytes=" + downloadLength + "-")
//                    .build();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        try {
            Response response = client.newCall(request).execute();
            if(response.code() == 404){
                request = new Request.Builder()
                        .url(url.replace("https://media", "https://edge"))
                        .build();
                response = client.newCall(request).execute();
            }
            //得到下载内容的大小
            long contentLength = response.body().contentLength();
            if (contentLength == 0) {
                return DownloadStatusType.FAILED;
            } else if (contentLength == downloadLength) {
                //已下载字节和文件总字节相等，说明已经下载完成了
                return DownloadStatusType.SUCCESS;
            }
            if (response != null) {
                is = response.body().byteStream();
                savedFile = new RandomAccessFile(file, "rw");
                savedFile.seek(downloadLength);//跳过已经下载的字节
                byte[] b = new byte[1024];
                int total = 0;
                int len;
                while ((len = is.read(b)) != -1) {
                    if (isCanceled) {
                        return DownloadStatusType.CANCELED;
                    } else if (isPaused) {
                        return DownloadStatusType.PAUSED;
                    } else {
                        total += len;
                        url:          savedFile.write(b, 0, len);
                        MessageUtils.sizeAI.addAndGet(len);
                        //计算已经下载的百分比
                        int progress = (int) ((long) total * 100 / contentLength);
                        //注意：在doInBackground()中是不可以进行UI操作的，如果需要更新UI,比如说反馈当前任务的执行进度，
                        //可以调用publishProgress()方法完成。
                        listener.onProgress(progress, this.filename);
                    }

                }
                response.body().close();
                if(DownLoadUtils.downloadFaildModS.get(filename) != null) {
                    DownLoadUtils.downloadFaildModS.remove(filename);
                }
                return DownloadStatusType.SUCCESS;
            }
        } catch (IOException e) {
            DownLoadUtils.downloadFailModsAdd(filename, url, savePath, listener);
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (savedFile != null) {
                    savedFile.close();
                }
                if (isCanceled && file != null) {
                    file.delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return DownloadStatusType.FAILED;
    }

    /**
     * 当在后台任务中调用了publishProgress(Progress...)方法之后，onProgressUpdate()方法
     * 就会很快被调用，该方法中携带的参数就是在后台任务中传递过来的。在这个方法中可以对UI进行操作，利用参数中的数值就可以
     * 对界面进行相应的更新。
     *
     * @param values
     */
    protected void onProgressUpdate(Integer... values) {
        int progress = values[0];
        if (progress > lastProgress) {
            listener.onProgress(progress, null);
            lastProgress = progress;
        }
    }

    /**
     * 当后台任务执行完毕并通过Return语句进行返回时，这个方法就很快被调用。返回的数据会作为参数
     * 传递到此方法中，可以利用返回的数据来进行一些UI操作。
     * @param status
     */
    protected void onPostExecute(DownloadStatusType status) {
        System.out.println(url + ":" + status.getMsg());
        switch (status) {
            case SUCCESS:
                listener.onSuccess(this.file);
                break;
            case FAILED:
                listener.onFailed(filename, url);
                break;
            case PAUSED:
                listener.onPaused();
                break;
            case CANCELED:
                listener.onCanceled();
                break;
            default:
                break;
        }
    }

    public void pauseDownload() {
        isPaused = true;
    }

    public void cancelDownload() {
        isCanceled = true;
    }

    /**
     * 得到下载内容的大小
     *
     * @param downloadUrl
     * @return
     */
    private long getContentLength(String downloadUrl) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(downloadUrl).build();
        try {
            Response response = client.newCall(request).execute();
            if (response != null && response.isSuccessful()) {
                long contentLength = response.body().contentLength();
                response.body().close();
                return contentLength;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }
}