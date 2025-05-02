package com.example.labiss;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import repository.SQLMedicationRepository;
import repository.SQLSectionRepository;
import repository.SQLUserRepository;
import service.MedicationService;
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
        SQLMedicationRepository medicationRepository = new SQLMedicationRepository();
        MedicationService medicationService = new MedicationService(medicationRepository);

    }

    public static void main(String[] args) {
        launch(args);
    }
}
//TODO
//Pentru interfata sa pun la medicamente doar putine detalii si sa am un pop up cu detalii dupa ce dau click pe medicament