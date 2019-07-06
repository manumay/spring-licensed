package info.manuelmayer.licensed.tool;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class LicenseGeneratorApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(LicenseGeneratorApp.class.getResource("/info/manuelmayer/licensed/tool/LicenseGenerator.fxml"));
            BorderPane layout = (BorderPane) loader.load();
            Scene scene = new Scene(layout);
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.setTitle("License Generator");
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
