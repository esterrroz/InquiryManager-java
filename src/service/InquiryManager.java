package service;

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
    private static boolean isProcessing = false;

    static {
        loadInquiriesFromFiles();
        loadRepresentativesFromFiles();
    }

    public static void loadInquiriesFromFiles() {
        File nextValFile = new File("data/naxtVal");
        if (nextValFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(nextValFile))) {
                String line = reader.readLine();
                if (line != null) {
                    Inquiry.setNextCodeVal(Integer.valueOf(line));
                }
            } catch (IOException | NumberFormatException e) {
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
                            } catch (NumberFormatException e) {System.out.println("Skipping invalid file name: " + file.getName());
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
            inquiryRepo.saveFile(inq);
            q.add(inq);
            if (!isProcessing) {
                isProcessing = true;
                new Thread(() -> {
                    while (true) {
                        Inquiry current;
                        synchronized (q) {
                            current = q.poll();
                            if (current == null) {
                                isProcessing = false;
                                return;
                            }
                        }
                        new InquiryHandling(current).run();
                    }
                }).start();
            }
        }
    }

    public static void processInitialInquiries() {
        if (!isProcessing) {
            addInquiryFromClient(null);
        }
    }

    public static synchronized Queue<Inquiry> getInquiryQueue() {
        return new LinkedList<>(q);
    }

    public static void loadRepresentativesFromFiles() {
        File folder = new File("Representative");
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    Object obj = reflectionRepo.readCsv(file.getPath());
                    if (obj instanceof Representative) {
                        representatives.add((Representative) obj);
                    }
                }
            }
        }
    }
}