package com.example.labiss.controller;

import domain.Medication;
import domain.OrderItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import repository.SQLMedicationRepository;
import service.MedicationService;
import service.OrderService;

public class MedicalOrder {
    @FXML public MenuButton Options;
    @FXML public TextField search;
    @FXML public TableView<Medication> Tabel_medicamente;
    @FXML public TableColumn<Medication, String> colName;
    @FXML public TableColumn<Medication, String> colUnit;
    @FXML public TableColumn<Medication, Integer> colAvailability;
    @FXML public TableColumn<Medication, String> colCategory;

    @FXML public TableView<OrderItem> Tabel_comanda;
    @FXML public TableColumn<OrderItem, String> colComandaName;
    @FXML public TableColumn<OrderItem, String> colComandaUnit;
    @FXML public TableColumn<OrderItem, Integer> colComandaQuantity;
    @FXML public MenuItem comanda;
    @FXML public MenuItem cos;
    @FXML public CheckBox urgentCheckBox;

    private MedicationService medicationService;
    private OrderService orderService;
    private ObservableList<Medication> medicationList;
    private FilteredList<Medication> filteredMedications;
    private ObservableList<OrderItem> orderItems;

    public MedicalOrder() {
        // Inițializarea serviciilor se va face în metoda initialize()
    }

    @FXML
    public void initialize() {
        // Inițializarea repository și servicii
        SQLMedicationRepository repository = new SQLMedicationRepository();
        this.medicationService = new MedicationService(repository);
        this.orderService = new OrderService(medicationService);

        // Inițializare liste observabile
        this.medicationList = FXCollections.observableArrayList(medicationService.getAllMedications());
        this.orderItems = FXCollections.observableArrayList();

        // Ascunde ambele tabele inițial
        Tabel_medicamente.setVisible(false);
        Tabel_comanda.setVisible(false);

        // Configurarea coloanelor pentru tabelul de medicamente
        colName.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        colUnit.setCellValueFactory(cellData -> cellData.getValue().unitOfMeasureProperty());
        colAvailability.setCellValueFactory(cellData -> cellData.getValue().availabilityProperty().asObject());
        colCategory.setCellValueFactory(cellData -> cellData.getValue().categoryProperty());

        // Configurarea coloanelor pentru tabelul de comandă
        colComandaName.setCellValueFactory(cellData -> cellData.getValue().medicationNameProperty());
        colComandaUnit.setCellValueFactory(cellData -> cellData.getValue().unitOfMeasureProperty());
        colComandaQuantity.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());

        // Configurare lista filtrată pentru căutare
        filteredMedications = new FilteredList<>(medicationList, p -> true);
        Tabel_medicamente.setItems(filteredMedications);

        // Configurare listener pentru căutare
        setupSearch();

        // Configurare handler pentru dublu-click
        setupDoubleClickHandler();

        // Adăugare listener pentru checkbox-ul de urgență
        if (urgentCheckBox != null) {
            urgentCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                orderService.setUrgent(newValue);
            });
        }
    }

    private void setupSearch() {
        search.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredMedications.setPredicate(medication -> {
                // Dacă câmpul de căutare este gol, afișează toate medicamentele
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                // Caută în numele medicamentului
                if (medication.getName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }

                // Caută în categoria medicamentului
                if (medication.getCategory().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }

                // Caută în descrierea medicamentului
                if (medication.getDescription().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }

                // Caută în producătorul medicamentului
                if (medication.getManufacturer().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }

                return false; // Nu s-a găsit potrivire
            });
        });
    }

    private void setupDoubleClickHandler() {
        Tabel_medicamente.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                addSelectedMedicationToOrder(event);
            }
        });
    }

    private void addSelectedMedicationToOrder(MouseEvent event) {
        Medication selectedMedication = Tabel_medicamente.getSelectionModel().getSelectedItem();
        if (selectedMedication != null) {
            // Creare dialog pentru a cere cantitatea
            TextInputDialog dialog = new TextInputDialog("1");
            dialog.setTitle("Adăugare în comandă");
            dialog.setHeaderText("Adăugare " + selectedMedication.getName());
            dialog.setContentText("Introduceți cantitatea:");

            dialog.showAndWait().ifPresent(quantity -> {
                try {
                    int qty = Integer.parseInt(quantity);
                    if (qty <= 0) {
                        showError("Cantitatea trebuie să fie mai mare decât zero");
                        return;
                    }

                    // Adăugare în comandă
                    try {
                        orderService.addMedicationToOrder(selectedMedication.getName(), qty);

                        // Actualizare listă comandă
                        refreshOrderItems();

                        showSucces("Medicament adăugat în comandă: " + selectedMedication.getName());
                    } catch (IllegalArgumentException e) {
                        showError(e.getMessage());
                    }

                } catch (NumberFormatException e) {
                    showError("Vă rugăm să introduceți un număr valid");
                }
            });
        }
    }

    private void refreshOrderItems() {
        orderItems.setAll(orderService.getCurrentOrderItems());
        Tabel_comanda.setItems(orderItems);
    }

    @FXML
    public void Order() {
        // Metodă apelată când se selectează opțiunea "Creeaza comanda" din meniu
        showMedicineList(null);
    }

    @FXML
    public void showMedicineList(ActionEvent event) {
        Tabel_medicamente.setVisible(true);
        Tabel_comanda.setVisible(false);
    }

    @FXML
    public void showCart(ActionEvent event) {
        Tabel_medicamente.setVisible(false);
        Tabel_comanda.setVisible(true);

        // Actualizare listă comandă
        refreshOrderItems();
    }

    @FXML
    public void placeOrder() {
        try {
            orderService.placeOrder();

            // Reîmprospătare date
            refreshOrderItems();
            medicationList.setAll(medicationService.getAllMedications());

            showSucces("Comanda a fost plasată cu succes!");
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    public void clearOrder() {
        orderService.clearOrder();
        refreshOrderItems();
        showSucces("Comanda a fost golită");
    }

    public void showSucces(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succes");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Eroare");
        alert.setHeaderText("A apărut o eroare");
        alert.setContentText(message);
        alert.showAndWait();
    }
}