package service;

import data.*;
import Repository.InquiryRepository;
import Repository.NextCodeValRepository;
import Repository.ReflectionRepository;

import java.io.File;
import java.util.*;

public class InquiryManager {
    private static final Queue<Inquiry> q = new LinkedList<>();
    private static ArrayList<Representative> representatives = new ArrayList<>();
    private static final InquiryRepository inquiryRepo = new InquiryRepository();
    private static final ReflectionRepository reflectionRepo = new ReflectionRepository();
    private static final NextCodeValRepository nextCodeValRepo = new NextCodeValRepository();
    private static boolean isProcessing = false;

    static {
        loadInquiriesFromFiles();
        loadRepresentativesFromFiles();
    }

    public static void loadInquiriesFromFiles() {
        Inquiry.setNextCodeVal(nextCodeValRepo.load());
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
            case "Complaint": return new Complaint(true);
            case "Questions": return new Question(true);
            case "Request": return new Request(true);
            default: return null;
        }
    }
    public static synchronized void addInquiryFromClient(Inquiry inq) {
        if (inq != null) {
            inquiryRepo.saveFile(inq);
            nextCodeValRepo.save(Inquiry.getNextCodeVal());
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
                        new InquiryHandling(current).start();
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