package service;

import domain.User;
import repository.SQLSectionRepository;
import repository.SQLUserRepository;

import java.sql.SQLException;

public class UserService {
    private SQLUserRepository userRepository;
    private SQLSectionRepository sectionRepository;
    public UserService(SQLUserRepository userRepository) {
        this.userRepository = userRepository;
    }
//Add in database a new user
    public void registerUser(String username, String password, String role, int sectionCode) {
        userRepository.registerUser(username,password,role,sectionCode);

//        } catch (SQLException e) {
//            e.printStackTrace();
//            return null;
//        }
    }

    public boolean loginUser(String username, String password) {
        try {
            // Implement proper authentication logic
            // This could involve checking against a database or authentication service
            return userRepository.loginUser(username, password);
        } catch (Exception e) {
            System.err.println("Error logging in user: " + e.getMessage());
            return false;
        }
    }


    public int getUserSectionCode(String usernameUsed) {
        return userRepository.getCodeByUsername(usernameUsed);
    }
}
