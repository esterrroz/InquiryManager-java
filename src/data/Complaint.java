package data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

public class Complaint extends Inquiry implements IForSaving , Serializable {
    private static final long serialVersionUID = 1L;
    public Complaint() {
    }
    public Complaint(boolean loadingMode) {
        super(loadingMode);
    }
    private String assignedBranch;

    public String getAssignedBranch() {
        return assignedBranch;
    }

    public void setAssignedBranch(String assignedBranch) {
        this.assignedBranch = assignedBranch;
    }

    public void fillDataByUser(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("Describe the complaint.");
        String dis= scanner.nextLine();
        super.fillDataByUser(LocalDateTime.now(), dis);
        System.out.println("Enter the name of the branch that the complaint is related to:");
        assignedBranch= scanner.nextLine();
    }
    @Override
    public  void handling(){
        System.out.print("Our system is taking care of complaint number: "+getCode());
        System.out.println();
    }
    @Override
    public String getFolderName() {
        return "data/Complaint";
    }

    @Override
    public String getFileName() {
        return getCode().toString();
    }

    @Override
    public String getData() {
        return getCode() + "," + getCreationDate() + "," + getDescription() + "," +getStatus()+","+ assignedBranch;
    }
    @Override
    public void parseData(List<String> values) {
        this.setCode(Integer.parseInt(values.get(0)));
        this.setCreationDate(LocalDateTime.parse(values.get(1)));
        this.setDescription(values.get(2));
        this.setStatus(InquiryStatus.valueOf(values.get(3)));
        if (values.size() > 4) {
            this.setAssignedBranch(values.get(4));
        }
    }
}
