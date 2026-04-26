package data;

import java.io.*;
import java.time.LocalDateTime;
import java.util.List;

public abstract class Inquiry implements IForSaving ,Serializable{
    private static final long serialVersionUID = 1L;
    private static final Object codeLock = new Object();
    static Integer nextCodeVal = 0;
    private Integer code;
    private String description;
    private LocalDateTime creationDate;
//    private String fileName;
//    private String folderName;

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }


    public void setCode(Integer code) {
        this.code = code;
    }

    public static Integer getNextCodeVal() {
        return nextCodeVal;
    }
    public static void setNextCodeVal(Integer nextCodeVal) {
        Inquiry.nextCodeVal = nextCodeVal;
    }
    //    public Inquiry(String description, LocalDateTime creationDate) {
//        this.description = description;
//        this.code =nextCodeVal++;
//        this.creationDate = creationDate;
//    }
    public Inquiry() {
        this.description = "";
        synchronized (codeLock) {
            this.code = nextCodeVal++;
        }
        this.creationDate = LocalDateTime.now();
    }

    protected Inquiry(boolean loadingMode) {
        this.description = "";
        this.creationDate = LocalDateTime.now();
    }
    public abstract void fillDataByUser();
    protected void fillDataByUser(LocalDateTime creationDate, String description) {
        this.creationDate = creationDate;
        this.description = description;
    }
    public abstract void handling();

    public abstract String getFolderName();

    public abstract String getData();

    public abstract void parseData(List<String> values);

    public abstract String getFileName();


//
//    public void setFileName(String fileName) {
//        this.fileName = fileName;
//    }
//
//    public void setFolderName(String folderName) {
//        this.folderName = folderName;
//    }

//    @Override
//    public String getFileName() {
//        // אם ה-fileName ריק, נחזיר את הקוד כברירת מחדל
//        return (fileName != null) ? fileName : getCode().toString();
//    }
//
//    @Override
//    public String getFolderName() {
//        return folderName;
//    }
}
