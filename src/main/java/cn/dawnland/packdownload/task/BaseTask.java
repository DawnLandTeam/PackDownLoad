package cn.dawnland.packdownload.task;

import cn.dawnland.packdownload.utils.CommonUtils;
import cn.dawnland.packdownload.utils.DownLoadUtils;
import cn.dawnland.packdownload.utils.MessageUtils;
import cn.dawnland.packdownload.utils.UIUpdateUtils;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXProgressBar;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public abstract class BaseTask<T> extends Task<T> {

    protected Callback<T> callback;
    protected int progress = 0;
    protected final JFXListView<HBox> taskList = CommonUtils.getTaskList();
    protected JFXProgressBar taskProgressBar = new JFXProgressBar();
    protected Label messageLabel = new Label();
    protected int maxSize;
    protected TaskProfile taskProfile;
    protected HBox taskHb;
    protected Label taskProgressLabel;

    @Override
    protected T call() { return null; }

    void initProgress(){
        Platform.runLater(() -> {
            Label modsLabel = new Label(taskProfile.getDisplayTitle());
            taskHb = new HBox();
            taskProgressLabel = new Label();
            taskHb.setPrefWidth(350D);
            taskHb.setSpacing(10D);
            taskHb.setAlignment(Pos.CENTER);
            taskProgressBar.setPrefWidth(130D);
            taskProgressBar.setMaxHeight(5D);
            taskProgressBar.setProgress(progress);
            modsLabel.setPrefWidth(60D);
            modsLabel.setMaxHeight(5);
            modsLabel.setAlignment(Pos.CENTER_LEFT);
            taskProgressLabel.setPrefWidth(100D);
            taskProgressLabel.setAlignment(Pos.CENTER_RIGHT);
            taskProgressLabel.setText("0/" + maxSize);
            MessageUtils.info(taskProfile.getStartMessage());
            Platform.runLater(() -> {
                taskHb.getChildren().addAll(modsLabel, taskProgressBar, messageLabel);
                DownLoadUtils.taskList.getItems().add(taskHb);
            });
            UIUpdateUtils.modsBar = taskProgressBar;
            UIUpdateUtils.modsLabel = messageLabel;
            UIUpdateUtils.modsCount = maxSize;
        });
    }

    @Override
    public void run() {
        try {
            subTask();
        } catch (Exception e) {
            callback.exceptionCallback(e);
        }
    }

    /**
     * @return String filename
     * @throws Exception
     */
    protected abstract void subTask() throws Exception;

}
