package repository;

import com.example.labiss.controller.Pharmacy;
import domain.Order;
import domain.OrderItem;
import org.sqlite.SQLiteDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SQLOrderRepository implements AutoCloseable {

    private Connection connection;
    private final String DB_URL = "jdbc:sqlite:src/main/java/spital.db";
    private final List<Order> orders = new ArrayList<>();

    public SQLOrderRepository() {
        openConnection();
        //dropOrderTables();
        createTablesIfNotExist();
        loadData();
    }

    public List<Order> getOrders() {
        return orders;
    }

    private void openConnection() {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(DB_URL);
        try {
            if (connection == null || connection.isClosed()) {
                connection = dataSource.getConnection();
                connection.createStatement().execute("PRAGMA foreign_keys = ON");
            }
        } catch (SQLException e) {
            System.err.println("Error creating connection: " + e.getMessage());
        }
    }

    public void dropOrderTables() {
        String dropOrderItemsSQL = "DROP TABLE IF EXISTS order_items";
        String dropOrdersSQL = "DROP TABLE IF EXISTS orders";

        try (Statement stmt = connection.createStatement()) {
            // Drop order_items first due to foreign key constraints
            stmt.execute(dropOrderItemsSQL);
            System.out.println("Table 'order_items' deleted successfully");

            stmt.execute(dropOrdersSQL);
            System.out.println("Table 'orders' deleted successfully");

            orders.clear(); // Clear local list
        } catch (SQLException e) {
            System.err.println("Error deleting tables: " + e.getMessage());
        }
    }

    private void createTablesIfNotExist() {
        String createOrdersSQL = "CREATE TABLE IF NOT EXISTS orders (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "order_date DATE NOT NULL," +
                "status TEXT NOT NULL," +
                "urgent BOOLEAN NOT NULL DEFAULT 0)";

        String createOrderItemsSQL = "CREATE TABLE IF NOT EXISTS order_items (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "order_id INTEGER NOT NULL," +
                "medication_id INTEGER NOT NULL," +
                "quantity INTEGER NOT NULL," +
                "FOREIGN KEY (order_id) REFERENCES orders(id)," +
                "FOREIGN KEY (medication_id) REFERENCES medications(id))";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createOrdersSQL);
            stmt.execute(createOrderItemsSQL);
            System.out.println("Order tables created or already exist");
        } catch (SQLException e) {
            System.err.println("Error creating tables: " + e.getMessage());
        }
    }

    private void loadData() {
        String ordersSQL = "SELECT * FROM orders";
        String orderItemsSQL = "SELECT * FROM order_items WHERE order_id = ?";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(ordersSQL)) {

            while (rs.next()) {
                int orderId = rs.getInt("id");
                Date orderDate = rs.getDate("order_date");
                String status = rs.getString("status");
                boolean urgent = rs.getBoolean("urgent");

                Order order = new Order(orderId, orderDate, status, urgent);

                // Load order items for this order
                try (PreparedStatement pstmt = connection.prepareStatement(orderItemsSQL)) {
                    pstmt.setInt(1, orderId);
                    ResultSet itemsRs = pstmt.executeQuery();

                    while (itemsRs.next()) {
                        OrderItem item = new OrderItem(
                                itemsRs.getInt("id"),
                                itemsRs.getInt("medication_id"),
                                itemsRs.getInt("quantity")
                        );
                        order.addOrderItem(item);
                    }
                }

                orders.add(order);
            }

            System.out.println("Loaded " + orders.size() + " orders from database");
        } catch (SQLException e) {
            System.err.println("Error loading orders data: " + e.getMessage());
        }
    }

    public int createOrder(boolean urgent) throws SQLException {
        String query = "INSERT INTO orders (order_date, status, urgent) VALUES (?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            // Set current date
            java.sql.Date sqlDate = new java.sql.Date(new Date().getTime());
            statement.setDate(1, sqlDate);
            statement.setString(2, "PENDING");
            statement.setBoolean(3, urgent);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Failed to create order");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int orderId = generatedKeys.getInt(1);

                    // Add the new order to our local list
                    Order newOrder = new Order(orderId, new Date(), "PENDING", urgent);
                    orders.add(newOrder);

                    return orderId;
                } else {
                    throw new SQLException("Failed to get order ID");
                }
            }
        }
    }

    // Overload for backward compatibility
    public int createOrder() throws SQLException {
        return createOrder(false); // Default to non-urgent
    }

    public void addOrderItem(int orderId, int medicationId, int quantity) throws SQLException {
        String query = "INSERT INTO order_items (order_id, medication_id, quantity) VALUES (?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, orderId);
            statement.setInt(2, medicationId);
            statement.setInt(3, quantity);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Failed to add order item");
            }

            // Add the item to our in-memory order
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int itemId = generatedKeys.getInt(1);

                    // Find the order and add the new item
                    for (Order order : orders) {
                        if (order.getId() == orderId) {
                            OrderItem newItem = new OrderItem(itemId, medicationId, quantity);
                            order.addOrderItem(newItem);
                            break;
                        }
                    }
                }
            }
        }
    }

    public void updateOrderStatus(int orderId, String status) throws SQLException {
        String query = "UPDATE orders SET status = ? WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, status);
            statement.setInt(2, orderId);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Failed to update order status");
            }

            // Update status in our local list
            for (Order order : orders) {
                if (order.getId() == orderId) {
                    order.setStatus(status);
                    break;
                }
            }
        }
    }



    public boolean deleteOrder(int orderId) throws SQLException {
        // Check if the order exists
        if (getOrderById(orderId) == null) {
            return false;
        }

        // Use a transaction to ensure data integrity
        connection.setAutoCommit(false);
        try {
            // First delete associated order items
            String deleteItemsSQL = "DELETE FROM order_items WHERE order_id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(deleteItemsSQL)) {
                pstmt.setInt(1, orderId);
                pstmt.executeUpdate();
            }

            // Then delete the order itself
            String deleteOrderSQL = "DELETE FROM orders WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(deleteOrderSQL)) {
                pstmt.setInt(1, orderId);
                int affectedRows = pstmt.executeUpdate();

                connection.commit();
                return affectedRows > 0;
            }
        } catch (SQLException e) {
            // Roll back the transaction if something goes wrong
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public List<Order> getSortedOrdersByUrgencyAndDate() {
        List<Order> sortedOrders = new ArrayList<>();
        String ordersSQL = "SELECT * FROM orders ORDER BY urgent DESC, order_date DESC";
        String orderItemsSQL = "SELECT * FROM order_items WHERE order_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(ordersSQL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int orderId = rs.getInt("id");
                Date orderDate = rs.getDate("order_date");
                String status = rs.getString("status");
                boolean urgent = rs.getBoolean("urgent");

                Order order = new Order(orderId, orderDate, status, urgent);

                // Adaugă itemii comenzii
                try (PreparedStatement itemStmt = connection.prepareStatement(orderItemsSQL)) {
                    itemStmt.setInt(1, orderId);
                    ResultSet itemsRs = itemStmt.executeQuery();

                    while (itemsRs.next()) {
                        OrderItem item = new OrderItem(
                                itemsRs.getInt("id"),
                                itemsRs.getInt("medication_id"),
                                itemsRs.getInt("quantity")
                        );
                        order.addOrderItem(item);
                    }
                }

                sortedOrders.add(order);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving sorted orders: " + e.getMessage());
        }

        return sortedOrders;
    }

    public List<Pharmacy.MedicationRaport> getWeeklyMedicationReport(int daysBack) throws SQLException {
        List<Pharmacy.MedicationRaport> reports = new ArrayList<>();

        String sql = "SELECT m.name, m.unit_of_measure, SUM(oi.quantity) as total_quantity " +
                "FROM order_items oi " +
                "JOIN medications m ON oi.medication_id = m.id " +
                "JOIN orders o ON oi.order_id = o.id " +
                "WHERE o.status = 'PENDING' " +  // Filtrează doar comenzile PENDING
                "GROUP BY m.name, m.unit_of_measure " +
                "ORDER BY total_quantity DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                reports.add(new Pharmacy.MedicationRaport(
                        rs.getString("name"),
                        rs.getInt("total_quantity"),
                        rs.getString("unit_of_measure")
                ));
            }
        }

        return reports;
    }

    public void updateOrderUrgency(int orderId, boolean urgent) throws SQLException {
        String query = "UPDATE orders SET urgent = ? WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setBoolean(1, urgent);
            statement.setInt(2, orderId);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Failed to update order urgency");
            }

            // Update urgency in our local list
            for (Order order : orders) {
                if (order.getId() == orderId) {
                    order.setUrgent(urgent);
                    break;
                }
            }
        }
    }

    public List<OrderItem> getOrderItems(int orderId) throws SQLException {
        String query = "SELECT * FROM order_items WHERE order_id = ?";
        List<OrderItem> items = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, orderId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem(
                            rs.getInt("id"),
                            rs.getInt("medication_id"),
                            rs.getInt("quantity")
                    );
                    items.add(item);
                }
            }
        }

        return items;
    }

    public Order getOrderById(int orderId) {
        for (Order order : orders) {
            if (order.getId() == orderId) {
                return order;
            }
        }
        return null;
    }



    @Override
    public void close() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}