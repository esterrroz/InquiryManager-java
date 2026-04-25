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
import java.util.Scanner;

public class InquiryManager extends Thread {
    private static final Queue<Inquiry> q = new LinkedList<>();
    private  Inquiry currentInquiry;
    Scanner scanner= new Scanner(System.in);
    private static ArrayList<Representative> representatives = new ArrayList<>();
    static {
        loadInquiriesFromFiles();
        loadRepresentativesFromFiles();
    }
    public static void loadInquiriesFromFiles() {
        HandleFiles handleFiles = new HandleFiles();
        File nextValFile = new File("data/naxtVal");
        if (nextValFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(nextValFile))) {
                Inquiry.setNextCodeVal( Integer.valueOf(reader.readLine()));
            } catch (IOException e) {
                System.out.println("שגיאה בקריאת קובץ naxtVal: " + e.getMessage());
            }
        } else {
            System.out.println("קובץ naxtVal לא נמצא");
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
    public  void inquiryCreation()
    {    boolean flag=true;
        while (flag) {
            System.out.println("press 1 for question 2 for request 3 for complaint ,press any other number to finish.");
            try {
                int ans = scanner.nextInt();
                scanner.nextLine(); // Clear the buffer
                switch (ans) {
                    case 1:
                        currentInquiry = new Question();
                        currentInquiry.fillDataByUser();
                        q.add(currentInquiry);
                        break;
                    case 2:
                        currentInquiry = new Request();
                        currentInquiry.fillDataByUser();
                        q.add(currentInquiry);
                        break;
                    case 3:
                        currentInquiry = new Complaint();
                        currentInquiry.fillDataByUser();
                        q.add(currentInquiry);
                        break;
                    default:flag=false;
                        break;
                }
                if (flag) {
                    HandleFiles handleFiles = new HandleFiles();
                    handleFiles.saveFile(currentInquiry);
                }
            } catch (java.util.InputMismatchException e) {
                System.out.println("Please enter a valid number (1, 2, or 3).");
                scanner.nextLine(); // Clear the invalid input
            }
        }
    }
    public void processInquiryManager(){
        InquiryHandling inquiryHandling;
        while (!q.isEmpty()){
            currentInquiry= q.poll();
            inquiryHandling=new InquiryHandling(currentInquiry);
            inquiryHandling.start();
        }
    }


    public void defineRepresentative() {
        Scanner scanner = new Scanner(System.in);
        HandleFilesReflection handler = new HandleFilesReflection();
        while (true) {
            System.out.println("Enter representative name (or 'exit' to stop):");
            String name = scanner.nextLine();
            if (name.equalsIgnoreCase("exit")) {
                break;
            }
            System.out.println("Enter ID:");
            String id = scanner.nextLine();
            Representative rep = new Representative(name, id);
            representatives.add(rep);
            String filePath = "Representative/" + rep.getEmployeeCode() + ".csv";
            handler.saveCSV(rep, filePath);
            System.out.println("Representative saved!");
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
}

