package com.example.labiss.controller;

import domain.Medication;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import repository.SQLMedicationRepository;
import service.MedicationService;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

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
    private ObservableList<Medication> allMedications = FXCollections.observableArrayList();
    private FilteredList<Medication> filteredMedications = new FilteredList<>(allMedications);

    public MedicalOrder() {
        SQLMedicationRepository repository = new SQLMedicationRepository();
        this.medicationService = new MedicationService(repository);
    }

    @FXML
    public void initialize() {
        configureColumns();
        loadMed();
        setUpClickHandler();
        setUpSearchHandler();

        // Adaugă acest cod pentru meniul contextual
        Tabel_comanda.setRowFactory(tv -> {
            TableRow<Medication> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();
            MenuItem editItem = new MenuItem("Editează cantitatea");

            editItem.setOnAction(e -> {
                Medication med = row.getItem();
                if (med != null) {
                    // Dialog pentru editare cantitate
                    TextInputDialog dialog = new TextInputDialog(String.valueOf(med.getQuantity()));
                    dialog.setTitle("Editare cantitate");
                    dialog.setHeaderText("Editare cantitate pentru " + med.getName());
                    dialog.setContentText("Introdu noua cantitate:");

                    Optional<String> result = dialog.showAndWait();
                    result.ifPresent(newQuantity -> {
                        try {
                            int quantity = Integer.parseInt(newQuantity);
                            med.setQuantity(quantity);
                            Tabel_comanda.refresh(); // Actualizează vizualizarea tabelului
                        } catch (NumberFormatException ex) {
                            showError("Introduceți un număr valid!");
                        }
                    });
                }
            });

            contextMenu.getItems().add(editItem);

            // Meniul contextual apare doar pe rânduri care conțin date
            row.contextMenuProperty().bind(
                    Bindings.when(row.emptyProperty())
                            .then((ContextMenu)null)
                            .otherwise(contextMenu)
            );
            return row;
        });
    }

    private void setUpSearchHandler() {
        search.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredMedications();
        });
    }

    private void filteredMedications() {
        String keyword = search.getText().trim().toLowerCase();
        filteredMedications.setPredicate(med ->
                keyword.isEmpty() ||
                med.getName().toLowerCase().contains(keyword) ||
                med.getCategory().toLowerCase().contains(keyword)
        );
    }



    @FXML
    private void showMedicationsTable() {
        Tabel_medicamente.setVisible(true);
        Tabel_comanda.setVisible(false);
    }

    @FXML
    private void showOrderTable() {
        Tabel_medicamente.setVisible(false);
        Tabel_comanda.setVisible(true);
    }

    private void setUpClickHandler() {
        Tabel_medicamente.setRowFactory(tv -> {
            TableRow<Medication> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Medication selected = row.getItem();
                    addToOrder(selected);
                }
            });
            return row;
        });
    }

    private void configureColumns() {
        colName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        colUnit.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUnitOfMeasure()));
        colAvailability.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getAvailability()).asObject());
        colCategory.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategory()));
    }

    private void loadMed() {
       try{
           allMedications.setAll(medicationService.getAllMedications());
           Tabel_medicamente.setItems(filteredMedications);
       }catch (Exception e){
           showError("Eroare la inacarcare:" +e.getMessage());
       }
    }

//    @FXML
//    private void showDetails() {
//        Medication selected = medicationsTable.getSelectionModel().getSelectedItem();
//        if (selected != null) {
//            Alert alert = new Alert(Alert.AlertType.INFORMATION);
//            alert.setTitle("Detalii complete");
//            alert.setHeaderText(selected.getName());
//            alert.setContentText(
//                    "Descriere: " + selected.getDescription() + "\n" +
//                            "Producător: " + selected.getManufacturer() + "\n" +
//                            "Unități disponibile: " + selected.getAvailability() + " " + selected.getUnitOfMeasure()
//            );
//            alert.showAndWait();
//        }
//    }
    @FXML
    public void addToOrder(Medication medication) {
        boolean alreadyin = Tabel_comanda.getItems().contains(medication);
        if (alreadyin) {
            showError("medicamentul exista in comanda deja, puteti modifica cantitatea");
        }else{
            Medication orderedMed = new Medication(
                    medication.getName(),
                    medication.getDescription(),
                    medication.getUnitOfMeasure(),
                    1,
                    medication.getManufacturer(),
                    medication.getCategory()
            );
            Tabel_comanda.getItems().add(orderedMed);
            showSucces("Medicament adaugat cu succes");
        }


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
