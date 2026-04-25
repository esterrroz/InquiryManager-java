package data;
import Repository.IForSaving;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

public class Question extends Inquiry implements IForSaving , Serializable
{
    private static final long serialVersionUID = 1L;
    public Question() {
    }
    public void fillDataByUser(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("Describe the question.");
        String dis= scanner.nextLine();
        super.fillDataByUser(LocalDateTime.now(), dis);
    }
    @Override
    public  void handling(){
        System.out.print("Our system is taking care of question number: "+getCode());
        System.out.println();
    }

    @Override
    public String getFolderName() {
        return "data/Questions";
    }

    @Override
    public String getFileName() {
        return getCode().toString();
    }

    @Override
    public String getData() {
        return getCode() + "," + getCreationDate() + "," + getDescription();
    }
    @Override
    public void parseData(List<String> values) {
        this.setCode(Integer.parseInt(values.get(0)));
        this.setCreationDate(LocalDateTime.parse(values.get(1)));
        this.setDescription(values.get(2));

    }
}
