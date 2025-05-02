package com.example.labiss.controller;

import domain.Medication;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import service.MedicationService;

public class MedicalOrder {
    public MenuButton Options;
    public TextField search;
    public TableView<Medication> Tabel_medicamente;
    public TableColumn<Medication, String> colName;
    public TableColumn<Medication, String> colUnit;
    public TableColumn<Medication, Integer> colAvailability;
    public TableColumn<Medication, String> colCategory;

    public TableView<Medication> Tabel_comanda;
    public TableColumn<Medication, String> colOrderName;
    public TableColumn<Medication, String> colOrderUnit;
    public TableColumn<Medication, Integer> colOrderQuantity;
    public MedicationService medicationService;

//    public MedicalOrderController() {
//        this.medicationService = new MedicationService(new MedicationRepository(DatabaseConnection.getConnection()));
//    }

    @FXML
    public void initialize() {
//        Options.getItems().clear();
//        MenuItem newOrder = new MenuItem("Comandă nouă");
//        MenuItem cart = new MenuItem("Coș");
//
//        // Adaugă acțiuni pentru fiecare opțiune
//        newOrder.setOnAction(event -> System.out.println("Comandă nouă selectată"));
//        cart.setOnAction(event -> System.out.println("Coș selectat"));
//
//        // Adaugă elementele în meniul butonului
//        Options.getItems().addAll(newOrder, cart);
    }

    @FXML
    public void Order(){

    }

    public void showSucces(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("An error occured");
        alert.setContentText(message);
        alert.showAndWait();//shows the alert
    }
}
