package service;

import communication.dto.RequestData;
import data.Representative;
import data.*;
import Repository.InquiryRepository;
import Repository.NextCodeValRepository;
import Repository.ReflectionRepository;

import java.io.File;
import java.io.ObjectOutputStream;
import java.util.*;

public class InquiryManager {
    private static final Queue<Inquiry> q = new LinkedList<>();
    private static ArrayList<Representative> representatives = new ArrayList<>();
    private static Queue<ActiveInquiry> activeInquiries = new LinkedList<ActiveInquiry>();
    private static final InquiryRepository inquiryRepo = new InquiryRepository();
    private static final ReflectionRepository reflectionRepo = new ReflectionRepository();
    private static final NextCodeValRepository nextCodeValRepo = new NextCodeValRepository();
    private static boolean isProcessing = false;
    private static String nameAdministrator="chani pappenhaim"; //מוזמנות להפוך את זה למערך ולהוסיף תשם שלכן;)
    private static String passwordAdministrator="12345678";
    private static int idAdministrator=328281111 ;

    public static int getIdAdministrator() {
        return idAdministrator;
    }

    public static void setIdAdministrator(int idAdministrator) {
        InquiryManager.idAdministrator = idAdministrator;
    }

    public static String getPasswordAdministrator() {
        return passwordAdministrator;
    }

    public static void setPasswordAdministrator(String passwordAdministrator) {
        InquiryManager.passwordAdministrator = passwordAdministrator;
    }

    public static String getNameAdministrator() {
        return nameAdministrator;
    }

    public static void setNameAdministrator(String nameAdministrator) {
        InquiryManager.nameAdministrator = nameAdministrator;
    }

    static {
        loadInquiriesFromFiles();
//        loadRepresentativesFromFiles();

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
    public static void handleAddRepresentative(RequestData requestObj) {
        if (requestObj != null && requestObj.getParameters() != null && requestObj.getParameters().size() >= 2) {
            Representative representative = new Representative();
            representative.setId(requestObj.getParameters().get(0).toString());
            representative.setName(requestObj.getParameters().get(1).toString());
            reflectionRepo.saveCSV(representative, "Representatives");
        }
    }
}