package dse.explorer;

import atlantafx.base.theme.NordDark;
import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.util.Locale;
import java.util.Objects;

public class Main extends javafx.application.Application {

    @Override
    public void start(Stage stage) throws IOException {
        Locale.setDefault(new Locale("C"));
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("main.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        scene.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        stage.getIcons().add(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("/icon.png"))));
        stage.setTitle("ODS Explorer - Danish Sensor Engineering");
        stage.setScene(scene);
        stage.show();

        //this makes all stages close and the app exit when the main stage is closed
        stage.setOnCloseRequest(e -> Platform.exit());

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });
    }

    public static void main(String[] args) {
        launch();
    }

}