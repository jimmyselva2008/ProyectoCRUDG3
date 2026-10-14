module com.tuempresa.proyectocrudg3 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.tuempresa.proyectocrudg3 to javafx.fxml;
    opens com.tuempresa.proyectocrudg3.controller to javafx.fxml;
    opens com.tuempresa.proyectocrudg3.model to javafx.base;

    exports com.tuempresa.proyectocrudg3;
    exports com.tuempresa.proyectocrudg3.controller;
    exports com.tuempresa.proyectocrudg3.model;
}