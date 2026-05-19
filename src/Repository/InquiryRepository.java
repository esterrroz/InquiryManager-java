package Repository;

import data.IForSaving;
import data.Inquiry;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class InquiryRepository {
    public void saveFile(IForSaving forSaving){
        File directory = new File(forSaving.getFolderName());
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File file = new File(directory, forSaving.getFileName()+".txt");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(forSaving.getData());
            System.out.println("הקובץ נשמר בהצלחה בכתובת: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("שגיאה בעת שמירת הקובץ: " + e.getMessage());
        }
    }
    public void deleteFile(IForSaving forSaving){
        File file = new File(forSaving.getFolderName(), forSaving.getFileName()+".txt");
        if (file.exists()) {
            boolean isDeleted = file.delete();
            if (isDeleted) {
                System.out.println("הקובץ נמחק בהצלחה: " + file.getName());
            } else {
                System.out.println("לא ניתן היה למחוק את הקובץ.");
            }
        } else {
            System.out.println("הקובץ לא נמצא בנתיב המבוקש.");
        }
    }
    public  void updateFile(IForSaving forSaving){
        File file = new File(forSaving.getFolderName(), forSaving.getFileName()+".txt");
        if (file.exists()) {
            try (FileWriter writer = new FileWriter(file))
            {
                writer.write(forSaving.getData());
                System.out.println("הקובץ " + forSaving.getFileName() + " עודכן בהצלחה בנתונים חדשים.");
            }
            catch (IOException e) {
                System.err.println("שגיאה בעת ניסיון לעדכן את הקובץ: " + e.getMessage());
            }
        } else {
            System.out.println("עדכון נכשל: הקובץ לא נמצא בנתיב המבוקש.");
        }

    }
    public  void saveFiles(List<IForSaving> forSavingList){
        for (IForSaving file : forSavingList) {
            saveFile(file);
        }
    }

    public void readFile(Inquiry inQ) {
        File file = new File(inQ.getFolderName(), inQ.getFileName() + ".txt");
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line = reader.readLine();
                if (line != null) {
                    String[] values = line.split(",");
                    List<String> valueList = new ArrayList<>();
                    for (String value : values) {
                        valueList.add(value.trim());
                    }
                    inQ.parseData(valueList);
                    System.out.println("הקובץ נקרא בהצלחה: " + file.getName());
                }
            } catch (IOException e) {
                System.err.println("שגיאה בעת קריאת הקובץ: " + e.getMessage());
            }
        } else {
            System.out.println("הקובץ לא נמצא בנתיב המבוקש.");
        }
    }
}
