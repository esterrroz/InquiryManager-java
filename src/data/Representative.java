package data;

import java.io.Serializable;

public class Representative implements Serializable {
    private static final long serialVersionUID = 1L;

    private static int counter = 1;
    private int employeeCode;
    private String name;
    private String id;
    public Representative() {
    }
    public Representative(String name, String id) {
        this.employeeCode = counter++;
        this.name = name;
        this.id = id;
    }
    public int getEmployeeCode() {
        return employeeCode;
    }
    public String getName() {
        return name;
    }
    public String getId() {
        return id;
    }
    @Override
    public String toString() {
        return "Representative{" + "employeeCode=" + employeeCode + ", name='" + name + '\'' + ", id='" + id + '\'' + '}';
    }
}