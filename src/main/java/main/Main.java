package main;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.FxmlUtils;
import utils.MessageUtils;
import utils.Upgrader;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        showPackDownLoad(primaryStage);
    }

    public void showPackDownLoad(Stage primaryStage) throws IOException {
        if(Upgrader.isNewVersion()){
            Upgrader.versionLog();
        }
        //自动更新
        //如果version.json 永远比当前版本高 实现每次打开强制更新
        Upgrader.autoupgrade();

        // 为按钮添加事件——点击时打开新的窗口
//        Button opinionButton = new Button("反馈");
//        opinionButton.setLayoutX(396);
//        opinionButton.setLayoutY(14);
//        opinionButton.setOnMouseClicked(event -> {
//            Parent root = null;
//            try {
//                root = FxmlUtils.LoadFxml("Opinion.fxml");
//            } catch (IOException e) {
//                MessageUtils.error(e);
//            }
//            primaryStage.setTitle("反馈");
//            primaryStage.setScene(new Scene(root, 400, 200));
//            primaryStage.initModality(Modality.APPLICATION_MODAL);
//            primaryStage.show();
//        });

        Parent root = FxmlUtils.LoadFxml("PackDownLoad.fxml");
//        ((AnchorPane)root).getChildren().add(opinionButton);
        primaryStage.setTitle("整合包下载器");
        primaryStage.setScene(new Scene(root, 450, 300));
        primaryStage.show();
        primaryStage.setOnCloseRequest(e -> System.exit(0));
    }

    public static void main(String[] args) {
        try{
            launch(args);
        }catch (Exception e){
            MessageUtils.error(e);
        }
    }
}
