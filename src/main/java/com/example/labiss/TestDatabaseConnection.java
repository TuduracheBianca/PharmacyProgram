//package com.example.labiss;
//
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.SQLException;
//
//public class TestDatabaseConnection {
//    public static void main(String[] args) {
//        // URL-ul corect pentru SQL Server Express
//        String url = "jdbc:sqlserver://localhost\\SQLEXPRESS;databaseName=spital;encrypt=true;trustServerCertificate=true";
//        String user = "sa"; // Sau utilizatorul tău
//        String password = "parola_ta"; // Parola utilizatorului SQL Server
//
//        try {
//            // Încărcăm explicit driverul JDBC (opțional pentru Java 8+ dar util în debugging)
//            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
//
//            // Creăm conexiunea
//            Connection connection = DriverManager.getConnection(url, user, password);
//            System.out.println("Conexiunea la baza de date a fost realizată cu succes!");
//
//        } catch (ClassNotFoundException e) {
//            System.out.println("Driver JDBC nu a fost găsit: " + e.getMessage());
//        } catch (SQLException e) {
//            System.out.println("Eroare la conectare: " + e.getMessage());
//        }
//    }
//}
