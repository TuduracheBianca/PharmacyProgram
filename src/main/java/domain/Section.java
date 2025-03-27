package domain;

public class Section {
    private String name;
    private int code;
    public Section(String name, int code) {

        this.name = name;
        this.code = code;
    }
    public String getName() {
        return name;
    }
    public int getCode() {
        return code;
    }
    @Override
    public String toString() {
        return "Section [name=" + name + ", code=" + code + "]";
    }
}
