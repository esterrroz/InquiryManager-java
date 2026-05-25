package communication;

import Repository.InquiryRepository;
import Repository.ReflectionRepository;
import communication.dto.*;
import data.*;
import service.InquiryManager;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;



public class HandleClient extends Thread {
    private Socket clientSocket;
    private boolean isAdminAuthenticated = false;

    public boolean isAdminAuthenticated() {
        return isAdminAuthenticated;
    }

    public void setAdminAuthenticated(boolean adminAuthenticated) {
        isAdminAuthenticated = adminAuthenticated;
    }

    public HandleClient(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        handleClientRequest();
    }

    private void handleClientRequest() {
        try (ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {
            out.flush();
            boolean clientConnected = true;
            while (clientConnected) {
                try {
                    Object receivedData = in.readObject();
                    if (!(receivedData instanceof RequestData)) continue;
                    RequestData requestObj = (RequestData) receivedData;
                    InquiryManagerActions action = requestObj.getAction();
                    switch (action) {
                        case ADD_INQUIRY:
                            handleAddInquiry(requestObj, out);
                            break;
                        case ALL_INQUIRY:
                            handleGetAllInquiries(out);
                            break;
                        case GET_NUMBER_OF_INQUIRIES_ON_MONTH:
                            handleGetInquiriesOnMonth(requestObj, out);
                            break;
                        case TEST:
                            sendResponse(out, ResponseStatus.SUCCESS, "Server is alive!", null);
                            break;
                        case CANCEL_INQUIRY:
                            handleCancelInquiry(requestObj, out);
                            break;
                        case MANAGER_LOGIN:
                            isAdminAuthenticated = UsernameAndPasswordVerification(requestObj, out);
                                break;
                        case GET_INQUIRY_STATUS:
                            getInquiryStatus(requestObj,out);
                            break;
                        case ADD_REPRESENTATIVE:
                            if (isAdminAuthenticated) {
                                boolean representativeExist=InquiryManager.handleAddRepresentative(requestObj);
                                if(representativeExist)
                                sendResponse(out, ResponseStatus.SUCCESS, "Representative added successfully", null);
                                else
                                    sendResponse(out, ResponseStatus.FAIL, "Representative already exists", null);
                            } else {
                                sendResponse(out, ResponseStatus.FAIL, "Unauthorized: Manager login required", null);
                            }
                            break;
                        case REPRESENTATIVE_ENTRY:
                              representativeLogin(requestObj,out);
                            break;
                        case REPRESENTATIVE_EXIT:
                            representativeExit(requestObj,out);
                            break;

                        default:
                            sendResponse(out, ResponseStatus.FAIL, "Unknown action", null);

                    }
                } catch (EOFException | SocketException e) {
                    clientConnected = false;
                } catch (Exception e) {
                    System.err.println("Error handling request: " + e.getMessage());
                    sendResponse(out, ResponseStatus.FAIL, "Error: " + e.getMessage(), null);
                    clientConnected = false;
                }
            }
        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
        } finally {
            try {
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
                System.out.println("Client disconnected.");
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
    }

    private void representativeLogin(RequestData request, ObjectOutputStream out) throws IOException {
        Representative representative = representativeSearchFiles(request);
        if (representative != null) {
            InquiryManager.representativeLoginQueueSearch(representative);
            sendResponse(out, ResponseStatus.SUCCESS, "Representative logged in successfully", representative);
        } else {
            sendResponse(out, ResponseStatus.FAIL, "Representative not found in system files", null);
        }
    }

    private void representativeExit(RequestData request, ObjectOutputStream out) throws IOException {
        Representative representative = representativeSearchFiles(request);
        if (representative != null) {
            boolean removed = InquiryManager.representativeExitQueueSearch(representative);
            if (removed) {
                sendResponse(out, ResponseStatus.SUCCESS, "Representative logged out successfully", null);
            } else {
                sendResponse(out, ResponseStatus.FAIL, "Representative was not active", null);
            }
        } else {
            sendResponse(out, ResponseStatus.FAIL, "Representative not found", null);
        }
    }
    private static Representative representativeSearchFiles(RequestData request) {
        try {
            if (request.getParameters() == null || request.getParameters().isEmpty()) {
                System.out.println("[SERVER DEBUG] No parameters found in request.");
                return null;
            }

            String representativeId = request.getParameters().get(0).toString().trim();
            ReflectionRepository reflectionRepo = new ReflectionRepository();
            File folder = new File("data/representatives");

            if (folder.exists() && folder.isDirectory()) {
                File[] files = folder.listFiles();
                if (files == null || files.length == 0) {
                    System.out.println("[SERVER DEBUG] Folder is EMPTY! No files found.");
                } else {
                    for (File file : files) {
                        Object obj = reflectionRepo.readCsv(file.getPath());
                        if (obj instanceof Representative) {
                            Representative rep = (Representative) obj;
                            if (rep.getId() != null && rep.getId().trim().equals(representativeId)) {
                                return rep;
                            }
                        } else {
                        }
                    }
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean UsernameAndPasswordVerification(RequestData request, ObjectOutputStream out) throws IOException {
        int id = (int) request.getParameters().get(0);
        String password = (String) request.getParameters().get(1);
        ResponseData response = new ResponseData();
        if (id == InquiryManager.getIdAdministrator() && password.equals(InquiryManager.getPasswordAdministrator())) {
            response.setStatus(ResponseStatus.SUCCESS);
            sendResponse(out, ResponseStatus.SUCCESS, "Peace and blessings to: " + InquiryManager.getNameAdministrator(), null);
            return true;
        } else {
            sendResponse(out, ResponseStatus.FAIL, "Invalid ID or Password.", null);
            return false;
        }

    }

    private void handleAddInquiry(RequestData request, ObjectOutputStream out) throws IOException {
        List<Object> params = request.getParameters();
        if (params != null && !params.isEmpty() && params.get(0) instanceof Inquiry) {
            Inquiry newInQ = (Inquiry) params.get(0);
            newInQ.setStatus(InquiryStatus.OPEN);
            InquiryManager.addInquiryFromClient(newInQ);
            sendResponse(out, ResponseStatus.SUCCESS, "Inquiry added successfully", newInQ);
        } else {
            sendResponse(out, ResponseStatus.FAIL, "Invalid data: Inquiry object missing", null);
        }
    }

    private void getInquiryStatus(RequestData request, ObjectOutputStream out){
        int inquiryCode = (int)request.getParameters().get(0);

        Inquiry inquiry = findInquiryWithCode(inquiryCode);
        try {
            if (inquiry != null) {

                sendResponse(out, ResponseStatus.SUCCESS, "Getting inquiry's status for inquiry no. " + inquiryCode + " was performed successfully", inquiry.getStatus());

            } else{
                sendResponse(out, ResponseStatus.FAIL, "Inquiry with code " + inquiryCode + " was not found", null);
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Inquiry findInquiryWithCode(int inquiryCode) {
        System.out.println("findInquiryWithCode function");

        InquiryRepository inquiryRepository = new InquiryRepository();

        String[] types = {"Complaint", "Questions", "Request","History"};
        Inquiry inquiry;
        for (String type : types) {
            File folder = new File("data/" + type);
            if (folder.exists() && folder.isDirectory()) {
                File[] files = folder.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if(file.getName().equals(String.valueOf(inquiryCode+".txt"))) {
                            inquiry = createEmptyInquiry(type);
                            if(inquiry!=null){
                                String fileNameOnly = file.getName().replace(".txt", "");
                                Integer fileCode = Integer.valueOf(fileNameOnly);
                                inquiry.setCode(fileCode);
                                inquiryRepository.readFile(inquiry);
                                return inquiry;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private void handleGetAllInquiries(ObjectOutputStream out) throws IOException {
        System.out.println("Fetching all inquiries...");
        ArrayList<Inquiry> list = new ArrayList<>(InquiryManager.getInquiryQueue());
        sendResponse(out, ResponseStatus.SUCCESS, "Success", list);
    }

    private void handleGetInquiriesOnMonth(RequestData request, ObjectOutputStream out) {

        int month = (int) request.getParameters().get(0);
        System.out.println("Fetching inquiries created on month: " + month);
        try {

            InquiryRepository inquiryRepository = new InquiryRepository();
            int result=0;

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
                                    inquiryRepository.readFile(inQ);
                                    if (inQ.getCreationDate().getMonth() == Month.of(month))
                                        result++;
                                } catch (NumberFormatException e) {
                                    System.out.println("Skipping invalid file name: " + file.getName());
                                }
                            }
                        }
                    }
                }
            }
            sendResponse(out, ResponseStatus.SUCCESS, "Getting inquiries created on month " + month + " was performed successfully", result);
        } catch (Exception ex) {
            try {
                sendResponse(out, ResponseStatus.FAIL, "Error: " + ex.getMessage(), null);
            } catch (IOException e) {
                ex.printStackTrace();
            }
        }
    }

    private static Inquiry createEmptyInquiry(String type) {
        switch (type) {
            case "Complaint":
                return new Complaint(true);
            case "Questions":
                return new Question(true);
            case "Request":
                return new Request(true);
            default:
                return null;
        }
    }
    private void sendResponse(ObjectOutputStream out, ResponseStatus status, String message, Object data) throws IOException {
        ResponseData response = new ResponseData(status, message, data);
        out.writeObject(response);
        out.flush();

    }
    private void handleCancelInquiry(RequestData request, ObjectOutputStream out) throws IOException {
        List<Object> params = request.getParameters();

        if (params != null && !params.isEmpty() && params.get(0) != null) {
            try {
                Integer inquiryCode = Integer.parseInt(params.get(0).toString());
                boolean isCancelled = InquiryManager.cancelInquiry(inquiryCode);
                if (isCancelled) {
                    sendResponse(out, ResponseStatus.SUCCESS, "הפנייה בוטלה בהצלחה והועברה להיסטוריה.", null);
                } else {
                    sendResponse(out, ResponseStatus.FAIL, "ביטול נכשל: הפנייה לא נמצאה בתורים הפעילים.", null);
                }
            } catch (NumberFormatException e) {
                sendResponse(out, ResponseStatus.FAIL, "שגיאה: קוד הפנייה שהוזן אינו תקין.", null);
            }
        } else {
            sendResponse(out, ResponseStatus.FAIL, "שגיאה: קוד פנייה חסר בבקשה.", null);
        }
    }
}