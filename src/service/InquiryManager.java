package service;

import communication.dto.RequestData;
import communication.dto.ResponseStatus;
import data.Representative;
import data.*;
import Repository.InquiryRepository;
import Repository.NextCodeValRepository;
import Repository.ReflectionRepository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.*;

public class InquiryManager {
    private static final Queue<Inquiry> q = new LinkedList<>();
    private static ArrayList<Representative> representatives = new ArrayList<>();
    private static final Queue<ActiveInquiry> activeInquiries = new LinkedList<>();
    private static final InquiryRepository inquiryRepo = new InquiryRepository();
    private static final ReflectionRepository reflectionRepo = new ReflectionRepository();
    private static final NextCodeValRepository nextCodeValRepo = new NextCodeValRepository();
    private static Integer currentInquiryCodeToCancel;
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
        System.out.println("[SERVER DEBUG] Starting loadInquiriesFromFiles");
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
    }


//    public static synchronized Queue<Inquiry> getInquiryQueue() {
//        return new LinkedList<>(q);
//    }

    public static synchronized Queue<Inquiry> getInquiryQueue() {
        return q;
    }

    public static ArrayList<Representative> getRepresentatives() {
        return representatives;
    }

    public static Queue<ActiveInquiry> getActiveInquiries() {
        return activeInquiries;
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

    public static synchronized boolean cancelInquiry(int inquiryCode) {
        Iterator<Inquiry> qIterator = q.iterator();
        while (qIterator.hasNext()) {
            Inquiry inq = qIterator.next();
            if (inq.getCode() != null && inq.getCode().equals(inquiryCode)) {
                inq.setStatus(InquiryStatus.CANCELED);
                qIterator.remove();
                moveInquiryToHistoryFiles(inq);
                return true;
            }
        }
        Iterator<ActiveInquiry> activeIterator = activeInquiries.iterator();
        while (activeIterator.hasNext()) {
            ActiveInquiry active = activeIterator.next();
            if (active.getInquiry() != null && active.getInquiry().getCode() != null && active.getInquiry().getCode().equals(inquiryCode)) {
                Inquiry inq = active.getInquiry();
                inq.setStatus(InquiryStatus.CANCELED);
                activeIterator.remove();
                moveInquiryToHistoryFiles(inq);
                Representative rep = active.getRepresentative();
                if (rep != null) {
                    representatives.add(rep);
                    System.out.println("Representative " + rep.getName() + " is now free and returned to the queue.");
                }
                System.out.println("Active Inquiry " + inquiryCode + " was successfully cancelled.");
                return true;
            }
        }

        return false;
    }

    public static void moveInquiryToHistoryFiles(Inquiry inq) {
        inquiryRepo.deleteFile(inq);
        File historyDir = new File("data/History");
        if (!historyDir.exists()) {
            historyDir.mkdirs();
        }
        String historyPath = "data/History/" + inq.getFileName() + ".txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(historyPath))) {
            writer.write(inq.getData());
            System.out.println("Inquiry moved to history file: " + historyPath);
        } catch (IOException e) {
            System.out.println("Failed to move inquiry no. "+inq.getCode()+" to history: " + e.getMessage());
        }
    }

    public static boolean handleAddRepresentative(RequestData requestObj) {
        if (requestObj != null && requestObj.getParameters() != null && requestObj.getParameters().size() >= 2) {
            Representative representative = new Representative();
            representative.setId(requestObj.getParameters().get(1).toString());
            representative.setName(requestObj.getParameters().get(0).toString());
            synchronized (representatives) {
                boolean idExists = representatives.stream().anyMatch(r -> r.getId().equals(representative.getId()));
                if (idExists) {
                    return false;
                }
                representatives.add(representative);
                reflectionRepo.saveCSV(representative, "data/representatives/"+representative.getId()+".csv");
            }
        }
        return true;
    }
    public static synchronized boolean representativeLoginQueueSearch(Representative representative) {
        for (Representative r : representatives) {
            if (r.getId().equals(representative.getId())) {
                return true;
            }
        }
        for (ActiveInquiry active : activeInquiries) {
            if (active.getRepresentative().getId().equals(representative.getId())) {
                return true;
            }
        }
        representatives.add(representative);
        return true;
    }
    public static synchronized boolean representativeExitQueueSearch(Representative representative) {
        for (Representative r : representatives) {
            if (r.getId().equals(representative.getId())) {
                {
                    representatives.remove(representative);
                    return true;
                }
            }
        }
        for (ActiveInquiry active : activeInquiries) {
            if (active.getRepresentative().getId().equals(representative.getId())) {
                {   active.setRepresentativeIsActive(false);
                    return true;
                }
            }
        }
        return false;
    }
}