package cz.cuni.mff.mandelbrot;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

/**
 * Main class starts the GUI.
 *
 * @author Filip Cizmar
 * @version 1.0
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader
                .load(Objects.requireNonNull(Main.class.getClassLoader().getResource("mandelbrot.fxml")));
        primaryStage.setTitle("Mandelbrot Set");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
