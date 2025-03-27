package com.example.labiss;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import repository.SQLSectionRepository;
import repository.SQLUserRepository;
import service.UserService;

import java.util.Scanner;

public class MainApp extends Application{
    @Override
    public void start(Stage primarystage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("login-register.fxml"));
        Scene loginScene =  new Scene(loader.load());
        primarystage.setTitle("Autentificare");
        primarystage.setScene(loginScene);
        primarystage.show();
        SQLUserRepository userRepository = new SQLUserRepository();
        SQLSectionRepository sectionRepository = new SQLSectionRepository();
        UserService userService = new UserService(userRepository);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
