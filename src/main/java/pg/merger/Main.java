package pg.merger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

public class Main extends Application {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    @Override
    public void start(Stage primaryStage) {
        try {
            primaryStage.setOnCloseRequest(windowEvent -> System.exit(0));
            primaryStage.setTitle("PdfMerger 1.0.6");
            primaryStage.setWidth(831.0);
            primaryStage.setHeight(320.0);
            primaryStage.show();
            primaryStage.setResizable(false);
            Scene scene = new Scene(root(primaryStage));
            primaryStage.setScene(scene);
            primaryStage.sizeToScene();
        } catch (IOException ex) {
            logger.error("Building scene error.", ex);
        }
    }

    private Parent root(Window parent) throws IOException {
        URL url = Main.class.getClassLoader().getResource(String.format("%s/%s", "fxml", "main.fxml"));
        FXMLLoader loader = new FXMLLoader(url);
        loader.setController(new Controller(parent));
        return loader.load();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
