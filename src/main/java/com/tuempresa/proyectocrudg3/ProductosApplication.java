package com.tuempresa.proyectocrudg3;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ProductosApplication extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("productos-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 900, 560);
        stage.setTitle("Bienvenido a Gestion de Productos");
        stage.setScene(scene);
        stage.show();
    }
}
