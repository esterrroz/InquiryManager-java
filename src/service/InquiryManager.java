package service;

import data.*;
import Repository.InquiryRepository;
import Repository.NextCodeValRepository;
import Repository.ReflectionRepository;
import java.util.Iterator;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class InquiryManager {
    private static final Queue<Inquiry> q = new LinkedList<>();
    private static ArrayList<Representative> representatives = new ArrayList<>();
    private static final Queue<ActiveInquiry> activeInquiries = new LinkedList<>();
    private static final InquiryRepository inquiryRepo = new InquiryRepository();
    private static final ReflectionRepository reflectionRepo = new ReflectionRepository();
    private static final NextCodeValRepository nextCodeValRepo = new NextCodeValRepository();
    private static Integer currentInquiryCodeToCancel;
    public static final ThreadLocal<Integer> currentInquiryCode = new ThreadLocal<>();
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
            inq.setCode(nextCodeValRepo.load());
            inquiryRepo.saveFile(inq);
            nextCodeValRepo.save(inq.getCode()+1);
            q.add(inq);
        }
//        startProcessingIfNeeded();
    }
//    public static synchronized void processInitialInquiries() {
//        startProcessingIfNeeded();
//    }
//
//    private static void startProcessingIfNeeded() {
//        if (!isProcessing && !q.isEmpty()) {
//            isProcessing = true;
//            new Thread(() -> {
//                while (true) {
//                    Inquiry current;
//                    synchronized (InquiryManager.class) {
//                        current = q.poll();
//                        if (current == null) {
//                            isProcessing = false;
//                            return;
//                        }
//                    }
//                    new InquiryHandling(current).start();
//                }
//            }).start();
//        }
//    }

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



    public static void setCurrentInquiryCodeToCancel(Integer code) {
        currentInquiryCodeToCancel = code;
    }

    public static synchronized boolean cancelInquiry() {
        Integer inquiryCode = currentInquiryCode.get();
        if (inquiryCode == null) return false;

        Iterator<Inquiry> qIterator = q.iterator();
        while (qIterator.hasNext()) {
            Inquiry inq = qIterator.next();
            if (inq.getCode() != null && inq.getCode().equals(inquiryCode)) {
                inq.setStatus("CANCELLED");
                qIterator.remove();
                moveInquiryToHistoryFiles(inq);
                currentInquiryCode.remove();
                return true;
            }
        }

        Iterator<ActiveInquiry> activeIterator = activeInquiries.iterator();
        while (activeIterator.hasNext()) {
            ActiveInquiry active = activeIterator.next();
            if (active.getInquiry() != null && active.getInquiry().getCode() != null && active.getInquiry().getCode().equals(inquiryCode)) {
                Inquiry inq = active.getInquiry();
                inq.setStatus("CANCELLED");

                activeIterator.remove();
                moveInquiryToHistoryFiles(inq);

                Representative rep = active.getRepresentative();
                if (rep != null) {
                    representatives.add(rep);
                    System.out.println("Representative " + rep.getName() + " is now free and returned to the queue.");
                }

                System.out.println("Active Inquiry " + inquiryCode + " was successfully cancelled.");
                currentInquiryCode.remove();
                return true;
            }
        }

        currentInquiryCode.remove();
        return false;
    }
    private static void moveInquiryToHistoryFiles(Inquiry inq) {
        String originalPath = "data/" + inq.getFolderName() + "/" + inq.getFileName() + ".txt";
        File originalFile = new File(originalPath);

        if (originalFile.exists()) {
            if (originalFile.delete()) {
                System.out.println("Original file deleted: " + originalPath);
            } else {
                System.err.println("Failed to delete original file: " + originalPath);
            }
        }

        File historyDir = new File("data/History");
        if (!historyDir.exists()) {
            historyDir.mkdirs();
        }

        String historyPath = "data/History/" + inq.getFileName() + ".txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(historyPath))) {
            writer.write(inq.getData());
            System.out.println("Inquiry moved to history file: " + historyPath);
        } catch (IOException e) {
            System.err.println("Error writing to history file: " + e.getMessage());
        }
    }
}