package service;

import data.Complaint;
import data.Inquiry;
import data.Question;
import data.Request;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class InquiryManager { // הורדנו את ה-Thread, השרת מנהל את ה-Threads

    // משתני הנתונים נשארים - זה הזיכרון של השרת
    private static final Queue<Inquiry> q = new LinkedList<>();
    private static ArrayList<Representative> representatives = new ArrayList<>();

    // בלוק סטטי לטעינה ראשונית - חייב להישאר
    static {
        loadInquiriesFromFiles();
        loadRepresentativesFromFiles();
    }

    // ניהול קבצים וטעינה - נשאר (זה התפקיד של השרת)
    public static void loadInquiriesFromFiles() {
        HandleFiles handleFiles = new HandleFiles();
        File nextValFile = new File("data/naxtVal");
        if (nextValFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(nextValFile))) {
                Inquiry.setNextCodeVal(Integer.valueOf(reader.readLine()));
            } catch (IOException e) {
                System.out.println("Error reading nextVal: " + e.getMessage());
            }
        }

        String[] types = {"Complaint", "Questions", "Request"};
        for (String type : types) {
            File folder = new File("data/" + type);
            if (folder.exists() && folder.isDirectory()) {
                File[] files = folder.listFiles();
                if (files != null) {
                    for (File file : files) {
                        Inquiry inQ = createEmptyInquiry(type);
                        if (inQ != null) {
                            handleFiles.readFile(inQ);
                            q.add(inQ);
                        }
                    }
                }
            }
        }
    }

    private static Inquiry createEmptyInquiry(String type) {
        switch (type) {
            case "Complaint": return new Complaint();
            case "Questions": return new Question();
            case "Request": return new Request();
            default: return null;
        }
    }

    public static void loadRepresentativesFromFiles() {
        HandleFilesReflection handler = new HandleFilesReflection();
        File folder = new File("Representative");
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    Object obj = handler.readCsv(file.getPath());
                    if (obj instanceof Representative) {
                        representatives.add((Representative) obj);
                    }
                }
            }
        }
    }

    // עיבוד הפניות - נשאר (זה קורה בתוך השרת)
    public void processInquiryManager(){
        InquiryHandling inquiryHandling;
        while (!q.isEmpty()){
            Inquiry currentInquiry = q.poll();
            inquiryHandling = new InquiryHandling(currentInquiry);
            inquiryHandling.start();
        }
    }
}