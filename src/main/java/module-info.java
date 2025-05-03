module com.example.labiss {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires org.xerial.sqlitejdbc;
    requires java.desktop;

    opens com.example.labiss to javafx.fxml;
    exports com.example.labiss;
    exports com.example.labiss.controller;
    opens com.example.labiss.controller to javafx.fxml;
}