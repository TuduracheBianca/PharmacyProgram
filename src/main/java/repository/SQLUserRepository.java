package repository;

import domain.Section;
import domain.User;
import org.sqlite.SQLiteDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLUserRepository implements AutoCloseable {
    private Connection connection;
    private final String DB_URL = "jdbc:sqlite:src/main/java/spital.db";
    private final List<User> users = new ArrayList<>();

    public SQLUserRepository() {
        openConnection();
        //dropTable();
        createTableIfNotExists();
        loadData();
    }
    public void dropTable() {
        String dropTableSql = "DROP TABLE IF EXISTS Users";
        try (Statement statement = connection.createStatement()) {
            statement.execute(dropTableSql);
            users.clear(); // Clear the in-memory list after dropping the table
            System.out.println("Users table dropped successfully");
        } catch (SQLException e) {
            System.err.println("Error dropping Users table: " + e.getMessage());
        }
    }

    private void openConnection() {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(DB_URL);
        try {
            if (connection == null || connection.isClosed()) {
                connection = dataSource.getConnection();
            }
        } catch (SQLException e) {
            System.err.println("Error creating connection: " + e.getMessage());
        }
    }

    private void createTableIfNotExists() {
        String createTableSql = "CREATE TABLE IF NOT EXISTS Users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "password TEXT NOT NULL, " +
                "role TEXT NOT NULL, " +
                "section_code INTEGER NOT NULL, " +
                "FOREIGN KEY (section_code) REFERENCES Sections(code))";
        try (Statement statement = connection.createStatement()) {
            statement.execute(createTableSql);
        } catch (SQLException e) {
            System.err.println("Error creating table: " + e.getMessage());
        }
    }

    private void loadData() {
        String sql = "SELECT * FROM Users";
        try (Statement stmt = connection.createStatement();
             ResultSet resultSet = stmt.executeQuery(sql)) {

            users.clear();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String password = resultSet.getString("password");
                String role = resultSet.getString("role");
                int section_code = resultSet.getInt("section_code");

                User user = new User(id, name, password, role, section_code);
                users.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Error loading data: " + e.getMessage());
        }
    }

    private boolean isSectionExists(int section_code) {
        String selectSql = "SELECT COUNT(*) FROM Sections WHERE code = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(selectSql)) {
            pstmt.setInt(1, section_code);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking section existence: " + e.getMessage());
            return false;
        }
    }

    public void registerUser(String name, String password, String role, int section_code) {
        if (!isSectionExists(section_code)) {
            System.err.println("Error: Section with code " + section_code + " does not exist.");
            return;
        }

        String sql = "INSERT INTO Users (name, password, role, section_code) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, password);
            stmt.setString(3, role);
            stmt.setInt(4, section_code);
            stmt.executeUpdate();
            loadData();
        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
        }
    }

    public boolean loginUser(String name, String password) {
        String sql = "SELECT * FROM Users WHERE name = ? AND password = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, password);
            try (ResultSet result = stmt.executeQuery()) {
                return result.next();
            }
        } catch (SQLException e) {
            System.err.println("Error logging in user: " + e.getMessage());
        }
        return false;
    }
    public int getCodeByUsername(String username) {
        for (User user : users) {
            if (username.equals(user.getName())) {
                return user.getSectionCode();
            }
        }
        return -1;
    }

    public List<User> getUsers() {
        return new ArrayList<>(users);
    }

    @Override
    public void close() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}