package service; // תיקון 1: התאמה לתיקייה בתמונה

import data.*;
import Repository.InquiryRepository;
import Repository.ReflectionRepository;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class InquiryManager {
    private static final Queue<Inquiry> q = new LinkedList<>();
    private static ArrayList<Representative> representatives = new ArrayList<>();

    private static final InquiryRepository inquiryRepo = new InquiryRepository();
    private static final ReflectionRepository reflectionRepo = new ReflectionRepository();
    static {
        loadInquiriesFromFiles();
        loadRepresentativesFromFiles();
    }
    public static void loadInquiriesFromFiles() {
        InquiryRepository inquiryRepo = new InquiryRepository();
        File nextValFile = new File("data/naxtVal");
        if (nextValFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(nextValFile))) {
                String line = reader.readLine();
                if (line != null) {
                    Inquiry.setNextCodeVal(Integer.valueOf(line));
                }
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
                            String fileNameOnly = file.getName().replace(".txt", "");
                            try {
                                Integer fileCode = Integer.valueOf(fileNameOnly);
                                inQ.setCode(fileCode);
                                inquiryRepo.readFile(inQ);
                                q.add(inQ);
                            } catch (NumberFormatException e) {
                                System.out.println("Skipping invalid file name: " + file.getName());
                            }
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


    public static synchronized void addInquiryFromClient(Inquiry inq) {
        if (inq != null) {
            q.add(inq);
            inquiryRepo.saveFile(inq);
        }
    }

    public void processInquiryManager(){
        while (!q.isEmpty()){
            Inquiry currentInquiry = q.poll();
            InquiryHandling inquiryHandling = new InquiryHandling(currentInquiry);
            inquiryHandling.start();
        }
    }

    public static void loadRepresentativesFromFiles() {
        File folder = new File("Representative");
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    // קריאה דרך כלי הרפלקשיין החדש שלך
                    Object obj = reflectionRepo.readCsv(file.getPath());
                    if (obj instanceof Representative) {
                        representatives.add((Representative) obj);
                    }
                }
            }
        }
    }
}