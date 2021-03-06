package cn.dawnland.packdownload.launcher;

import cn.dawnland.packdownload.configs.Config;
import cn.dawnland.packdownload.utils.CommonUtils;
import cn.dawnland.packdownload.utils.FxmlUtils;
import cn.dawnland.packdownload.utils.MessageUtils;
import cn.dawnland.packdownload.utils.Upgrader;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.IOException;

public class Launcher extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        showPackDownLoad(primaryStage);
    }

    public void showPackDownLoad(Stage primaryStage) throws IOException {
        primaryStage.getIcons().add(new Image("/img/logo.jpg"));
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setWidth(50D);
        alert.setHeight(10D);
        alert.setTitle("程序初始化");
        alert.setHeaderText("请等待初始化完成");
        alert.setContentText("正在初始化配置...");
        alert.show();
        //初始化大多配置信息
        Config.init();
        alert.close();
        //如果version.json 永远比当前版本高 实现每次打开强制更新
        Scene scene = new Scene(FxmlUtils.LoadFxml("PackDownLoadNew.fxml"), 644, 300);;
        if(Upgrader.isNewVersion()){
            JOptionPane.showMessageDialog(null, "开始自动更新,请耐心等待\n点击确定开始更新", "发现新版本 " + Upgrader.newVersion, 1);
            scene = new Scene(FxmlUtils.LoadFxml("Update.fxml"), 334, 118);
            primaryStage.setTitle("自动更新");
            primaryStage.setScene(scene);
            primaryStage.show();
        }
        primaryStage.setTitle("整合包下载器");
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setOnCloseRequest(e -> CommonUtils.appExit());
    }

    public static void main(String[] args) {
        try{
            launch(args);
        }catch (Exception e){
            MessageUtils.error(e);
        }
    }
}
