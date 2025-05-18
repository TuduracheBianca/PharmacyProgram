package com.example.labiss.controller;

import domain.Medication;
import domain.Order;
import domain.OrderItem;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import repository.SQLMedicationRepository;
import service.MedicationService;
import service.OrderService;

public class Pharmacy {
    @FXML
    private TableView<Order> ordersTable;
    @FXML
    private TableColumn<Order, Integer> colOrderId;
    @FXML
    private TableColumn<Order, String> colOrderDate;
    @FXML
    private TableColumn<Order, String> colOrderStatus;
    @FXML
    private TableColumn<Order, String> colOrderUrgent;

    @FXML
    private TableView<Medication> itemsTable;
    @FXML
    private TableColumn<Medication, String> colItemName;
    @FXML
    private TableColumn<Medication, String> colItemUnit;
    @FXML
    private TableColumn<Medication, Integer> colItemQuantity;

    @FXML
    private ComboBox<String> statusComboBox;

    @FXML
    private Button Orders;

    private OrderService orderService;
    private MedicationService medicationService;
    private ObservableList<Order> orders;

    @FXML
    public void initialize() {
        // Initialize services
        medicationService = new MedicationService(new SQLMedicationRepository());
        this.orderService = new OrderService(medicationService);

        // Configure orders table
        colOrderId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colOrderDate.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getOrderDate().toString()));
        colOrderStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colOrderUrgent.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().isUrgent() ? "URGENT" : "Normal"));

        // Configure items table for Medication objects
        colItemName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colItemUnit.setCellValueFactory(new PropertyValueFactory<>("unitOfMeasure"));
        colItemQuantity.setCellValueFactory(new PropertyValueFactory<>("availability"));

        // Setup double-click handler
        setupDoubleClickHandler();

        // Configure status combo box
        statusComboBox.getItems().addAll("PENDING", "PROCESSING", "COMPLETED", "CANCELLED");
        statusComboBox.setVisible(false);
        statusComboBox.setOnAction(event -> updateOrderStatus());

        // Initially hide tables
        ordersTable.setVisible(false);
        itemsTable.setVisible(false);
    }

    private void loadOrders() {
        orders = FXCollections.observableArrayList(orderService.getAllOrders());
        ordersTable.setItems(orders);
        ordersTable.setVisible(true);
        itemsTable.setVisible(false); // Hide medications table when loading orders
    }

    private void setupDoubleClickHandler() {
        ordersTable.setRowFactory(tv -> {
            TableRow<Order> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Order selectedOrder = row.getItem();
                    showOrderMedications(selectedOrder);

                    // Show status combo box for pharmacy
                    statusComboBox.setVisible(true);
                    statusComboBox.setValue(selectedOrder.getStatus());

                    // Show medications table
                    itemsTable.setVisible(true);
                }
            });
            return row;
        });
    }

    private void showOrderMedications(Order order) {
        // Get the medications from order items
        ObservableList<Medication> medications = FXCollections.observableArrayList();

        for (OrderItem item : order.getOrderItems()) {
            // Fetch the full medication details using the medicationId from the OrderItem
            Medication medication = medicationService.findMedicationById(item.getMedicationId());

            // If medication exists, add it to the list
            if (medication != null) {
                medications.add(medication);
            }
        }

        itemsTable.setItems(medications);
    }

    private void updateOrderStatus() {
        Order selectedOrder = ordersTable.getSelectionModel().getSelectedItem();
        if (selectedOrder != null) {
            String newStatus = statusComboBox.getValue();
            orderService.updateOrderStatus(selectedOrder.getId(), newStatus);
            selectedOrder.setStatus(newStatus);
            ordersTable.refresh();
        }
    }

    @FXML
    public void ShowOrders(ActionEvent event) {
        loadOrders();
        statusComboBox.setVisible(false);
    }
}