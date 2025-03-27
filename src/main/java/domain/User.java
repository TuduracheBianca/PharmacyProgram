package domain;

public class User {
    private int ID;
    private String name;
    private String password;
    private String role;
    private int sectionCode;
    //constructor
    public User(String name, String password, String role, int sectionCode) {
        this.name = name;
        this.password = password;
        this.role = role;
        this.sectionCode = sectionCode;
    }
    //constructor cu id generat de baza de date
    public User(int ID, String name, String password, String role, int sectionCode) {
        this.ID = ID;
        this.name = name;
        this.password = password;
        this.role = role;
        this.sectionCode = sectionCode;
    }
    public int getID() {
        return ID;
    }
    public void setID(int ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }
    public String getPassword() {
        return password;
    }
    public String getRole() {
        return role;
    }
    public int getSectionCode() {
        return sectionCode;
    }
    @Override
    public String toString() {
        return "User [ID=" + ID + ", name=" + name + ", password=" + password + ", role=" + role;
    }
}
