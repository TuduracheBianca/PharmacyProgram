package com.example.labiss.controller;

import domain.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import repository.SQLSectionRepository;
import repository.SQLUserRepository;
import service.UserService;

public class LoginController {
    public PasswordField password;
    public TextField username;
    public TextField new_username;
//    public Button login;
    public PasswordField new_password;
//    public Button register;
    public ComboBox<String> select_role;
    public ComboBox<String> select_department;
    private UserService userservice;

    public LoginController() {
        SQLUserRepository sqluserrepository = new SQLUserRepository();
        SQLSectionRepository sqlsectionrepository = new SQLSectionRepository();
        this.userservice = new UserService(sqluserrepository);
    }

    @FXML
    public void initialize() {
        select_role.getItems().addAll("Pharmacist", "Medical Staff");
        select_department.getItems().addAll("Farmacie", "Chirurgie","Cardiologie","Pediatrie","Ortopedie","Neurologie","Oncologie","Terapie intensiva","Ginecologie","Medicina Interna");
        // Load the image from resources
//        Image image = new Image(getClass().getResourceAsStream("/com/example/labiss/1.jpg"));
//        imageView.setImage(image);
    }


    @FXML
    public void Register(){
        try{
            String username = new_username.getText().trim();
            String password = new_password.getText().trim();
            String selectedRole = this.select_role.getValue();
            int codeDepartment = getCode(this.select_department.getValue());
            if(new_username.getText().isEmpty() || new_password.getText().isEmpty() || selectedRole == null|| codeDepartment == 0){
                showError("You need to fill in all the fields!");
            }
            userservice.registerUser(username,password,selectedRole,codeDepartment);
            showSucces("Succsesfully registered!");
        } catch (Exception e) {
            showError("Error at register :" + e.getMessage());
        }finally {
            new_username.clear();
            new_password.clear();
        }

    }

    private int getCode(String section) {
        switch (section) {
            case "Farmacie": return 1000;
            case "Chirurgie": return 1001;
            case "Cardiologie": return 1002;
            case "Pediatrie": return 1003;
            case "Ortopedie": return 1004;
            case "Neurologie": return 1005;
            case "Oncologie": return 1006;
            case "Terapie Intensivă (ATI)": return 1007;
            case "Medicină Internă": return 1008;
            case "Ginecologie": return 1009;
            default: throw new IllegalArgumentException("Invalid department: " + section);
        }
    }

    @FXML
    public void Login(){
        try {
            String username_used = this.username.getText().trim();
            String password_used = this.password.getText().trim();

            if(username.getText().isEmpty() || password.getText().isEmpty()){
                showError("You need to fill in all the fields!");
            }

            boolean loginSuccessful = userservice.loginUser(username_used,password_used);
            if(loginSuccessful){
                showSucces("Succsesfully logged in!");
            }else{
                showError("Incorrect username or password!");
            }
        }catch (Exception e){
            showError("Error at loging in" + e.getMessage());
        }finally {
            username.clear();
            password.clear();
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