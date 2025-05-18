package com.example.labiss.controller;

import domain.Medication;
import domain.Order;
import domain.OrderItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import repository.SQLMedicationRepository;
import service.MedicationService;
import service.OrderService;

import java.sql.SQLException;
import java.util.List;

public class MedicalOrder {
    @FXML
    public TextField search;
    @FXML
    public TableView<Medication> Tabel_medicamente;
    @FXML
    public TableColumn<Medication, String> colName;
    @FXML
    public TableColumn<Medication, String> colUnit;
    @FXML
    public TableColumn<Medication, Integer> colAvailability;
    @FXML
    public TableColumn<Medication, String> colCategory;

    @FXML
    public TableView<OrderItem> Tabel_comanda;
    @FXML
    public TableColumn<OrderItem, String> colComandaName;
    @FXML
    public TableColumn<OrderItem, String> colComandaUnit;
    @FXML
    public TableColumn<OrderItem, Integer> colComandaQuantity;

    @FXML
    public TableView<Order> Tabel_istoric;
//    @FXML
//    public TableColumn<Order, Integer> colOrderId;
    @FXML
    public TableColumn<Order, String> colOrderDate;
    @FXML
    public TableColumn<Order, String> colOrderStatus;
    @FXML
    public TableColumn<Order, Boolean> colOrderPriority;

    @FXML
    public CheckBox urgentCheckBox;
    @FXML
    public Button btnPlaceOrder;
    @FXML
    public Button btnOrdersHistory;

    private MedicationService medicationService;
    private OrderService orderService;
    private ObservableList<Medication> medicationList;
    private FilteredList<Medication> filteredMedications;
    private ObservableList<OrderItem> orderItems;
    private ObservableList<Order> orderHistory;


    @FXML
    public void initialize() {
        // Inițializarea repository și servicii
        SQLMedicationRepository repository = new SQLMedicationRepository();
        this.medicationService = new MedicationService(repository);
        this.orderService = new OrderService(medicationService);

        // Inițializare liste observabile
        this.medicationList = FXCollections.observableArrayList(medicationService.getAllMedications());
        this.orderItems = FXCollections.observableArrayList();
        this.orderHistory = FXCollections.observableArrayList();

        // Ascunde ambele tabele inițial
        Tabel_medicamente.setVisible(false);
        Tabel_comanda.setVisible(false);
        if (Tabel_istoric != null) {
            Tabel_istoric.setVisible(false);
        }
        urgentCheckBox.setVisible(false);
        btnPlaceOrder.setVisible(false);
        search.setVisible(false);


        // Configurarea coloanelor pentru tabelul de medicamente
        colName.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        colUnit.setCellValueFactory(cellData -> cellData.getValue().unitOfMeasureProperty());
        colAvailability.setCellValueFactory(cellData -> cellData.getValue().availabilityProperty().asObject());
        colCategory.setCellValueFactory(cellData -> cellData.getValue().categoryProperty());

        // Configurarea coloanelor pentru tabelul de comandă
        colComandaName.setCellValueFactory(cellData -> cellData.getValue().medicationNameProperty());
        colComandaUnit.setCellValueFactory(cellData -> cellData.getValue().unitOfMeasureProperty());
        colComandaQuantity.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());

        // Configurare coloane pentru istoric comenzi
        if ( colOrderDate != null && colOrderStatus != null && colOrderPriority != null) {
            colOrderDate.setCellValueFactory(cellData -> cellData.getValue().orderDateProperty());
            colOrderStatus.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
            colOrderPriority.setCellValueFactory(cellData -> cellData.getValue().urgentProperty().asObject());
        }

        // Configurare lista filtrată pentru căutare
        filteredMedications = new FilteredList<>(medicationList, p -> true);
        Tabel_medicamente.setItems(filteredMedications);

        // Configurare listener pentru căutare
        setupSearch();

        // Configurare handler pentru dublu-click
        setupDoubleClickHandler();

        // Configurare handler pentru click dreapta pe tabelul de comenzi
        setRightClickHandler();

        // Configurare handler pentru click dreapta pe tabelul de istoric
        if (Tabel_istoric != null) {
            setupHistoryRightClickHandler();
        }

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

    private void setRightClickHandler() {
        Tabel_comanda.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                OrderItem selectedItem = Tabel_comanda.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    // Creăm un meniu contextual cu opțiuni
                    ContextMenu contextMenu = new ContextMenu();

                    // Opțiunea pentru a vedea detaliile
                    MenuItem detailsItem = new MenuItem("View Details");
                    detailsItem.setOnAction(e -> {
                        try {
                            showOrderDetails(selectedItem);
                        } catch (SQLException ex) {
                            throw new RuntimeException(ex);
                        }
                    });

                    // Opțiunile existente pentru editare și ștergere
                    MenuItem editItem = new MenuItem("Edit quantity");
                    editItem.setOnAction(e -> {
                        TextInputDialog dialog = new TextInputDialog();
                        dialog.setTitle("Edit Quantity");
                        dialog.setHeaderText("Edit Quantity for " + selectedItem.getMedicationName());
                        dialog.setContentText("Enter Quantity to edit medication");
                        dialog.showAndWait().ifPresent(quantity -> {
                            try {
                                int qty = Integer.parseInt(quantity);
                                if (qty <= 0) {
                                    showError("Quantity must be greater than 0");
                                    return;
                                }
                                selectedItem.setQuantity(qty);
                                refreshOrderItems();
                                showSucces("Edited");
                            } catch (NumberFormatException ex) {
                                showError("Quantity must be an integer");
                            }
                        });
                    });

                    MenuItem deleteItem = new MenuItem("Delete");
                    deleteItem.setOnAction(e -> {
                        orderService.removeMedicationFromOrder(selectedItem.getMedicationName());
                        refreshOrderItems();
                        showSucces("Deleted");
                    });

                    contextMenu.getItems().addAll(detailsItem, editItem, deleteItem);
                    contextMenu.show(Tabel_comanda, event.getScreenX(), event.getScreenY());
                }
            }
        });
    }

    private void setupHistoryRightClickHandler() {
        Tabel_istoric.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                Order selectedOrder = Tabel_istoric.getSelectionModel().getSelectedItem();
                if (selectedOrder != null) {
                    ContextMenu contextMenu = new ContextMenu();

                    // Opțiunea pentru a vedea detaliile comenzii
                    MenuItem detailsItem = new MenuItem("View Order Details");
                    detailsItem.setOnAction(e -> {
                        showOrderHistoryDetails(selectedOrder);
                    });

                    // Adăugăm opțiunea de ștergere doar pentru comenzile cu status "Completed"
                    if ("Completed".equals(selectedOrder.getStatus())) {
                        MenuItem deleteItem = new MenuItem("Delete Order");
                        deleteItem.setOnAction(e -> {
                            try {
                                // Apelăm metoda de ștergere din service
                                orderService.deleteOrder(selectedOrder.getId());
                                // Actualizăm lista de comenzi
                                refreshOrderHistory();
                                showSucces("Order deleted successfully");
                            } catch (Exception ex) {
                                showError("Failed to delete order: " + ex.getMessage());
                            }
                        });
                        contextMenu.getItems().addAll(detailsItem, deleteItem);
                    } else {
                        contextMenu.getItems().add(detailsItem);
                    }

                    contextMenu.show(Tabel_istoric, event.getScreenX(), event.getScreenY());
                }
            }
        });
    }

    private void showOrderHistoryDetails(Order order) {
        // Creăm un dialog personalizat
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Order History Details");
        dialog.setHeaderText("Details for Order #" + order.getId());

        // Creăm conținutul dialogului
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        // Adăugăm informațiile despre comandă
        content.getChildren().add(new Label("Order ID: " + order.getId()));
        content.getChildren().add(new Label("Date: " + order.getOrderDate()));
        content.getChildren().add(new Label("Status: " + order.getStatus()));
        content.getChildren().add(new Label("Urgent: " + (order.isUrgent() ? "Yes" : "No")));

        // Adăugăm un separator
        content.getChildren().add(new Separator());

        // Adăugăm titlul pentru medicamente
        content.getChildren().add(new Label("Medications in this order:"));

        // Adăugăm fiecare medicament din comandă
        for (OrderItem item : order.getOrderItems()) {
            HBox itemBox = new HBox(5);
            itemBox.getChildren().addAll(
                    new Label(item.getMedicationName() + " - "),
                    new Label("Quantity: " + item.getQuantity()),
                    new Label("Unit: " + item.getUnitOfMeasure())
            );
            content.getChildren().add(itemBox);
        }

        // Setăm conținutul dialogului
        dialog.getDialogPane().setContent(content);

        // Adăugăm butonul de OK
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

        // Afișăm dialogul
        dialog.showAndWait();
    }

    private void showOrderDetails(OrderItem orderItem) throws SQLException {
        // Obținem toate detaliile comenzii din serviciu
        Order order = orderService.getOrderDetailsForItem(orderItem);

        // Creăm un dialog personalizat
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Order Details");
        dialog.setHeaderText("Details for order item: " + orderItem.getMedicationName());

        // Creăm conținutul dialogului
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        // Adăugăm informațiile despre comandă
        content.getChildren().add(new Label("Order ID: " + order.getId()));
        content.getChildren().add(new Label("Date: " + order.getOrderDate()));
        content.getChildren().add(new Label("Status: " + order.getStatus()));
        content.getChildren().add(new Label("Urgent: " + (order.isUrgent() ? "Yes" : "No")));

        // Adăugăm un separator
        content.getChildren().add(new Separator());

        // Adăugăm titlul pentru medicamente
        content.getChildren().add(new Label("Medications in this order:"));

        // Adăugăm fiecare medicament din comandă
        for (OrderItem item : order.getOrderItems()) {
            HBox itemBox = new HBox(5);
            itemBox.getChildren().addAll(
                    new Label(item.getMedicationName() + " - "),
                    new Label("Quantity: " + item.getQuantity()),
                    new Label("Unit: " + item.getUnitOfMeasure())
            );
            content.getChildren().add(itemBox);
        }

        // Setăm conținutul dialogului
        dialog.getDialogPane().setContent(content);

        // Adăugăm butonul de OK
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

        // Afișăm dialogul
        dialog.showAndWait();
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

    private void refreshOrderHistory() {
        // Get all orders from the service
        List<Order> orders = orderService.getAllOrders();
        orderHistory.setAll(orders);
        Tabel_istoric.setItems(orderHistory);
    }


    @FXML
    public void showMedicineList(ActionEvent event) {
        Tabel_medicamente.setVisible(true);
        Tabel_comanda.setVisible(false);
        if (Tabel_istoric != null) {
            Tabel_istoric.setVisible(false);
        }
        btnPlaceOrder.setVisible(false);
        urgentCheckBox.setVisible(false);
        search.setVisible(true);
    }

    @FXML
    public void showCart(ActionEvent event) {
        Tabel_medicamente.setVisible(false);
        Tabel_comanda.setVisible(true);
        if (Tabel_istoric != null) {
            Tabel_istoric.setVisible(false);
        }
        btnPlaceOrder.setVisible(true);
        urgentCheckBox.setVisible(true);
        search.setVisible(false);


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
            urgentCheckBox.setSelected(false);
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

    @FXML
    public void showHistory(ActionEvent actionEvent) {
        // Ascunde celelalte tabele
        Tabel_medicamente.setVisible(false);
        Tabel_comanda.setVisible(false);
        if (Tabel_istoric != null) {
            Tabel_istoric.setVisible(true);
        }
        btnPlaceOrder.setVisible(false);
        urgentCheckBox.setVisible(false);
        search.setVisible(false);

        // Încarcă și afișează istoricul comenzilor
        refreshOrderHistory();
    }
}