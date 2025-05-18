package repository;

import domain.Medication;
import org.sqlite.SQLiteDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLMedicationRepository implements AutoCloseable {

    private Connection connection;
    private final String DB_URL="jdbc:sqlite:src/main/java/spital.db";
    private final List<Medication> medications=new ArrayList<>();

    public SQLMedicationRepository() {
        openConnection();
        dropMedicationsTable();
        createTableIfNotExists();
        initDatabase();
        loadData();
    }

    public List<Medication> getMedications() {
        return medications;
    }

    private void openConnection() {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(DB_URL);
        try {
            if(connection==null || connection.isClosed()) {
                connection = dataSource.getConnection();
                connection.createStatement().execute("PRAGMA foreign_keys = ON");
            }
        } catch (SQLException e) {
            System.err.println("error creating connection: " + e.getMessage());
        }
    }
    public void dropMedicationsTable() {
        String sql = "DROP TABLE IF EXISTS medications";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            System.out.println("Table 'medications' deleted successfully");
            medications.clear(); // Golește lista locală
        } catch (SQLException e) {
            System.err.println("Error deleting table: " + e.getMessage());
        }
    }

    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS medications (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "description TEXT," +
                "unit_of_measure TEXT," +
                "availability INTEGER NOT NULL," +
                //"reserved INTEGER NOT NULL DEFAULT 0," + // Adăugat câmpul reserved
                "manufacturer TEXT NOT NULL," +
                "category TEXT NOT NULL)";
        try(Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            System.out.println("Table 'medications' created or already exists");
        } catch (SQLException e) {
            System.err.println("error creating table: " + e.getMessage());
        }
    }

    public void loadData() {
        String sql = "SELECT * FROM medications";
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                medications.add(new Medication(
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("unit_of_measure"),
                        rs.getInt("availability"),
                       //rs.getInt("reserved"), // Adăugat parametrul reserved
                        rs.getString("manufacturer"),
                        rs.getString("category")
                ));
            }
        } catch (SQLException e) {
            System.err.println("error loading data: " + e.getMessage());
        }
    }

    private void initDatabase() {
        System.out.println("initDatabase");
        String countSQL = "SELECT COUNT(*) AS count FROM medications";
        String insertDataSQL = "INSERT INTO medications (name, description, unit_of_measure, availability, manufacturer, category) VALUES " +
                "('Paracetamol', 'Analgezic și antipiretic', 'comprimate', 500, 'PharmaCorp', 'Analgezice')," +
                "('Ibuprofen', 'Antiinflamator nesteroidian', 'comprimate', 350, 'MediPlus', 'Antiinflamatoare')," +
                "('Amoxicilina', 'Antibiotic beta-lactamic', 'capsule', 200, 'BioHealth', 'Antibiotice')," +
                "('Omeprazol', 'Inhibitor de pompă de protoni', 'capsule', 300, 'GastroPharm', 'Antiulceroase')," +
                "('Metformina', 'Antidiabetic oral', 'comprimate', 400, 'DiaCare', 'Antidiabetice')," +
                "('Atorvastatina', 'Hipolipemiant', 'comprimate', 250, 'CardioLife', 'Cardiovasculare')," +
                "('Salbutamol', 'Bronhodilatator', 'dozator', 150, 'PulmoTech', 'Respiratorii')," +
                "('Ciprofloxacina', 'Antibiotic fluorchinolon', 'comprimate', 180, 'InfecStop', 'Antibiotice')," +
                "('Diazepam', 'Anxiolitic și relaxant muscular', 'comprimate', 120, 'NeuroCalm', 'Psihotrope')," +
                "('Losartan', 'Antihipertensiv', 'comprimate', 220, 'PressNorm', 'Cardiovasculare')," +
                "('Sertralina', 'Antidepresiv', 'comprimate', 190, 'MenteSană', 'Psihotrope')," +
                "('Metoclopramidă', 'Antiemetic', 'comprimate', 160, 'GastroRelief', 'Digestive')," +
                "('Warfarină', 'Anticoagulant', 'comprimate', 90, 'HemoCare', 'Hematologice')," +
                "('Levotiroxina', 'Hormon tiroidian', 'comprimate', 280, 'ThyroHealth', 'Endocrine')," +
                "('Prednison', 'Corticosteroid', 'comprimate', 130, 'InflaStop', 'Antiinflamatoare')," +
                "('Insulină glargină', 'Insulină acțune prelungită', 'flacon', 80, 'DiaCare', 'Antidiabetice')," +
                "('Amlodipină', 'Blocant canale de calciu', 'comprimate', 210, 'CardioLife', 'Cardiovasculare')," +
                "('Fluconazol', 'Antifungic', 'capsule', 170, 'FungiStop', 'Antifungice')," +
                "('Tramadol', 'Analgezic opioid', 'capsule', 110, 'PainRelief', 'Analgezice')," +
                "('Alprazolam', 'Anxiolitic', 'comprimate', 95, 'NeuroCalm', 'Psihotrope')," +
                "('Montelukast', 'Antileucotrien', 'comprimate', 140, 'PulmoTech', 'Respiratorii')," +
                "('Esomeprazol', 'Inhibitor de pompă de protoni', 'capsule', 240, 'GastroPharm', 'Antiulceroase')," +
                "('Metronidazol', 'Antibiotic și antiprotozoar', 'comprimate', 160, 'InfecStop', 'Antibiotice')," +
                "('Simvastatină', 'Hipolipemiant', 'comprimate', 200, 'CardioLife', 'Cardiovasculare')," +
                "('Citalopram', 'Antidepresiv', 'comprimate', 175, 'MenteSană', 'Psihotrope')," +
                "('Furosemidă', 'Diuretic', 'comprimate', 185, 'AquaCare', 'Diuretice')," +
                "('Clopidogrel', 'Antiagregant plachetar', 'comprimate', 155, 'HemoCare', 'Hematologice')," +
                "('Bisoprolol', 'Beta-blocant', 'comprimate', 165, 'CardioLife', 'Cardiovasculare')," +
                "('Diclofenac', 'Antiinflamator nesteroidian', 'comprimate', 145, 'InflaStop', 'Antiinflamatoare')," +
                "('Lansoprazol', 'Inhibitor de pompă de protoni', 'capsule', 195, 'GastroPharm', 'Antiulceroase')," +
                "('Atenolol', 'Beta-blocant', 'comprimate', 125, 'CardioLife', 'Cardiovasculare')," +
                "('Fluoxetina', 'Antidepresiv', 'capsule', 135, 'MenteSană', 'Psihotrope')," +
                "('Spironolactonă', 'Diuretic', 'comprimate', 115, 'AquaCare', 'Diuretice')," +
                "('Carbamazepină', 'Antiepileptic', 'comprimate', 105, 'NeuroCalm', 'Psihotrope')," +
                "('Ranitidină', 'Antihistaminic H2', 'comprimate', 85, 'GastroRelief', 'Antiulceroase')," +
                "('Pregabalină', 'Antiepileptic și analgezic', 'capsule', 75, 'NeuroCare', 'Psihotrope')," +
                "('Tamsulozină', 'Blocant alfa', 'capsule', 65, 'UroHealth', 'Urologice')," +
                "('Rosuvastatină', 'Hipolipemiant', 'comprimate', 70, 'CardioLife', 'Cardiovasculare')," +
                "('Venlafaxină', 'Antidepresiv', 'capsule', 60, 'MenteSană', 'Psihotrope')," +
                "('Tiotropiu', 'Bronhodilatator', 'inhalator', 55, 'PulmoTech', 'Respiratorii')," +
                "('Memantină', 'Antidementic', 'comprimate', 45, 'NeuroCare', 'Psihotrope')," +
                "('Valsartan', 'Antihipertensiv', 'comprimate', 50, 'PressNorm', 'Cardiovasculare')," +
                "('Duloxetină', 'Antidepresiv', 'capsule', 40, 'MenteSană', 'Psihotrope')," +
                "('Finasteridă', 'Inhibitor 5-alfa-reductază', 'comprimate', 35, 'UroHealth', 'Urologice')," +
                "('Piroxicam', 'Antiinflamator nesteroidian', 'capsule', 30, 'InflaStop', 'Antiinflamatoare')," +
                "('Levetiracetam', 'Antiepileptic', 'comprimate', 25, 'NeuroCare', 'Psihotrope')," +
                "('Quetiapină', 'Antipsihotic', 'comprimate', 20, 'MenteSană', 'Psihotrope')," +
                "('Telmisartan', 'Antihipertensiv', 'comprimate', 15, 'PressNorm', 'Cardiovasculare')," +
                "('Escitalopram', 'Antidepresiv', 'comprimate', 10, 'MenteSană', 'Psihotrope')," +
                "('Budesonidă', 'Corticosteroid inhalat', 'inhalator', 5, 'PulmoTech', 'Respiratorii');";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(countSQL)) {

            // Adaugă medicamente doar dacă tabelul este gol
            if (rs.next() && rs.getInt("count") == 0) {
                stmt.executeUpdate(insertDataSQL);
                System.out.println("Added 50+ sample medications to the database");
            } else {
                System.out.println("Database already contains medications (skipping population)");
            }
        } catch (SQLException e) {
            System.err.println("Error populating medications: " + e.getMessage());
        }
    }

//    public void reserveStock(String medicationName, int quantity) throws SQLException {
//        String query = "UPDATE medications SET availability = availability - ?, reserved = reserved + ? WHERE name = ? AND availability >= ?";
//
//        try (PreparedStatement stmt = connection.prepareStatement(query)) {
//            stmt.setInt(1, quantity);
//            stmt.setInt(2, quantity);
//            stmt.setString(3, medicationName);
//            stmt.setInt(4, quantity);
//
//            int rowsAffected = stmt.executeUpdate();
//            if (rowsAffected == 0) {
//                throw new SQLException("Stoc insuficient sau medicament inexistent");
//            }
//
//            // Actualizează și în memorie
//            for (Medication med : medications) {
//                if (med.getName().equals(medicationName)) {
//                    med.setAvailability(med.getAvailability() - quantity);
//                    med.setQuantityReserved(med.getReserved() + quantity);
//                    break;
//                }
//            }
//        }
//    }
//
//    public void confirmStockReduction(String medicationName, int quantity) throws SQLException {
//        String query = "UPDATE medications SET reserved = reserved - ? WHERE name = ? AND reserved >= ?";
//
//        try (PreparedStatement stmt = connection.prepareStatement(query)) {
//            stmt.setInt(1, quantity);
//            stmt.setString(2, medicationName);
//            stmt.setInt(3, quantity);
//
//            stmt.executeUpdate();
//
//            // Actualizează și în memorie
//            for (Medication med : medications) {
//                if (med.getName().equals(medicationName)) {
//                    med.setQuantityReserved(med.getReserved() - quantity);
//                    break;
//                }
//            }
//        }
//    }
//
//    public void cancelReservation(String medicationName, int quantity) throws SQLException {
//        String query = "UPDATE medications SET availability = availability + ?, reserved = reserved - ? WHERE name = ? AND reserved >= ?";
//
//        try (PreparedStatement stmt = connection.prepareStatement(query)) {
//            stmt.setInt(1, quantity);
//            stmt.setInt(2, quantity);
//            stmt.setString(3, medicationName);
//            stmt.setInt(4, quantity);
//
//            stmt.executeUpdate();
//
//            // Actualizează și în memorie
//            for (Medication med : medications) {
//                if (med.getName().equals(medicationName)) {
//                    med.setAvailability(med.getAvailability() + quantity);
//                    med.setQuantityReserved(med.getReserved() - quantity);
//                    break;
//                }
//            }
//        }
//    }

    public void updateMedicationStock(int medicationId, int newStock) throws SQLException {
        String query = "UPDATE medications SET availability = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, newStock);
            stmt.setInt(2, medicationId);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("Actualizarea stocului a eșuat: medicamentul cu ID-ul " + medicationId + " nu a fost găsit");
            }

            // Actualizare și în lista din memorie
            for (Medication medication : medications) {
                if (findMedicationIdByName(medication.getName()) == medicationId) {
                    medication.setAvailability(newStock);
                    break;
                }
            }
        }
    }

    public int findMedicationIdByName(String medicationName) throws SQLException {
        String query = "SELECT id FROM medications WHERE name = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, medicationName);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }

        return -1; // Nu s-a găsit medicamentul
    }

    @Override
    public void close() throws Exception {
        if(connection!=null && !connection.isClosed()) {
            connection.close();
        }
    }
}