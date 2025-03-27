package repository;

import domain.Section;
import org.sqlite.SQLiteDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLSectionRepository implements AutoCloseable {
    private Connection connection;
    private final String DB_URL = "jdbc:sqlite:src/main/java/spital.db";
    private final List<Section> sections = new ArrayList<>();

    public SQLSectionRepository() {
        openConnection();
        createTableIfNotExists();
//        initDatabase();
        loadData();
    }

    private void initDatabase() {
        String insertSql = "INSERT OR IGNORE INTO Sections (code, name) VALUES (?, ?)";
        Object[][] sectionsData = {
                {"1002", "Cardiologie"},
                {"1003", "Pediatrie"},
                {"1004", "Ortopedie"},
                {"1005", "Neurologie"},
                {"1006", "Oncologie"},
                {"1007", "Terapie Intensivă (ATI)"},
                {"1008", "Medicină Internă"},
                {"1009", "Ginecologie"},
                {"1010", "Urologie"},
                {"1011", "Dermatologie"},
                {"1012", "Oftalmologie"},
                {"1013", "Endocrinologie"},
                {"1014", "ORL (Otorinolaringologie)"},
                {"1015", "Reumatologie"},
                {"1016", "Pneumologie"},
                {"1017", "Boli Infecțioase"}
        };

        try (PreparedStatement pstmt = connection.prepareStatement(insertSql)) {
            for (Object[] section : sectionsData) {
                pstmt.setString(1, (String) section[0]);
                pstmt.setString(2, (String) section[1]);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (SQLException e) {
            System.err.println("Error initializing Sections data: " + e.getMessage());
        }
    }

    private void openConnection() {
        SQLiteDataSource ds = new SQLiteDataSource();
        ds.setUrl(DB_URL);
        try {
            if (connection == null || connection.isClosed()) {
                connection = ds.getConnection();
            }
        } catch (SQLException e) {
            System.err.println("Error creating connection: " + e.getMessage());
        }
    }

    private void createTableIfNotExists() {
        String createTableSql = "CREATE TABLE IF NOT EXISTS Sections (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "code TEXT NOT NULL, " +
                "name TEXT NOT NULL)";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTableSql);
        } catch (SQLException e) {
            System.err.println("Error creating table: " + e.getMessage());
        }
    }

    private void loadData() {
        String sql = "SELECT * FROM Sections";
        try (Statement stmt = connection.createStatement();
             ResultSet resultSet = stmt.executeQuery(sql)) {

            while (resultSet.next()) {
                String name = resultSet.getString("name");
                String code = resultSet.getString("code");

                Section section = new Section(name, Integer.parseInt(code));
                sections.add(section);
            }
        } catch (SQLException e) {
            System.err.println("Error loading data: " + e.getMessage());
        }
    }

    public List<Section> getSections() {
        return new ArrayList<>(sections);
    }

    public int getCode(String name){
        for (Section section : sections) {
            if (section.getName().equals(name)) {
                return section.getCode();
            }
        }
        return 0;
    }

    @Override
    public void close() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}

//package repository;
//
//import domain.Section;
//import org.sqlite.SQLiteDataSource;
//
//import java.sql.*;
//import java.util.ArrayList;
//import java.util.List;
//
//public class SQLSectionRepository implements AutoCloseable {
//    private Connection connection;
//    private String DB_URL = "jdbc:sqlite:src/main/java/spital.db";
//    private List<Section> sections = new ArrayList<>();
//
//    public SQLSectionRepository() {
//        openConnection();
//        createTableIfNotExists();
//        initDataBase();
//        loadData();
//    }
//
//    private void initDataBase() {
//        String insertSql = "INSERT INTO Sections (code, name) VALUES (?, ?)";
//        Object[][] sectionsData = {
//                {"1000", "Farmacie"},
//                {"1001", "Chirurgie"},
//                {"1002", "Cardiologie"},
//                {"1003", "Pediatrie"},
//                {"1004", "Ortopedie"},
//                {"1005", "Neurologie"},
//                {"1006", "Oncologie"},
//                {"1007", "Terapie Intensivă (ATI)"},
//                {"1008", "Medicină Internă"},
//                {"1009", "Ginecologie"},
//                {"1010", "Urologie"},
//                {"1011", "Dermatologie"},
//                {"1012", "Oftalmologie"},
//                {"1013", "Endocrinologie"},
//                {"1014", "ORL (Otorinolaringologie)"},
//                {"1015", "Reumatologie"},
//                {"1016", "Pneumologie"},
//                {"1017", "Boli Infecțioase"}
//        };
//
//        try (PreparedStatement pstmt = connection.prepareStatement(insertSql)) {
//            for (Object[] section : sectionsData) {
//                pstmt.setString(1, (String) section[0]); // Setează codul secției
//                pstmt.setString(2, (String) section[1]); // Setează denumirea secției
//                pstmt.addBatch(); // Adaugă la batch
//            }
//            pstmt.executeBatch(); // Execută toate inserțiile
//        } catch (SQLException e) {
//            System.out.println("Error initializing Sections data: " + e.getMessage());
//        }
//    }
//
//    private void openConnection() {
//        SQLiteDataSource ds = new SQLiteDataSource();
//        ds.setUrl(DB_URL);
//        try {
//            if(connection == null || connection.isClosed()) {
//                connection = ds.getConnection();
//            }
//        }catch (SQLException e) {
//            System.out.println("Error creating connection: " + e.getMessage());
//        }
//    }
//
//    private void createTableIfNotExists() {
//        String createTableSql = "CREATE TABLE IF NOT EXISTS Section (" +
//                "id INTEGER AUTOINCREMENT PRIMARY KEY, " +
//                "name TEXT NOT NULL, " +
//                "section_code INTEGER NOT NULL)";
//        try(Statement stmt = connection.createStatement()){
//            stmt.execute(createTableSql);
//
//        }catch (SQLException e){
//            System.out.println("Error creating table " + e.getMessage());
//        }
//    }
//
//    private void loadData(){
//        String sql = "SELECT * FROM Section";
//        try(Statement stmt = connection.createStatement())
//        {
//            ResultSet resultSet = stmt.executeQuery(sql);
//            while(resultSet.next())
//            {
//                int id = resultSet.getInt("id");
//                String name = resultSet.getString("name");
//                int section_code = resultSet.getInt("section_code");
//
//                Section section = new Section(name,section_code);
//                sections.add(section);
//            }
//        }catch (SQLException e)
//        {
//            System.out.println("Error loading data: " + e.getMessage());
//        }
//    }
//
//    @Override
//    public void close() throws Exception {
//        if (connection != null && !connection.isClosed()) {
//            connection.close();
//        }
//    }
//}
