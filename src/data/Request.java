package data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

public class Request extends Inquiry implements IForSaving, Serializable
{private static final long serialVersionUID = 1L;
    public Request() {
    }
    public Request(boolean loadingMode) {
        super(loadingMode);
    }
    public void fillDataByUser(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("Describe the request:");
        String dis= scanner.nextLine();
        super.fillDataByUser(LocalDateTime.now(), dis);
    }
    @Override
    public  void handling(){
        System.out.print("Our system is taking care of request number: "+getCode());
        System.out.println();
    }
    @Override
    public String getFolderName() {
        return "data/Request";
    }

    @Override
    public String getFileName() {
        return getCode().toString();
    }

    @Override
    public String getData() {
        return getCode() + "," + getCreationDate() + "," + getDescription()+","+getStatus();
    }
    @Override
    public void parseData(List<String> values) {
        this.setCode(Integer.parseInt(values.get(0)));
        this.setCreationDate(LocalDateTime.parse(values.get(1)));
        this.setDescription(values.get(2));
        this.setStatus(InquiryStatus.valueOf(values.get(3)));

    }
}
