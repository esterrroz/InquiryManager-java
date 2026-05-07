package communication;

import communication.dto.*;
import data.Inquiry;
import service.InquiryManager;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class HandleClient extends Thread {
    private Socket clientSocket;

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
                        case TEST:
                            sendResponse(out, ResponseStatus.SUCCESS, "Server is alive!", null);
                            break;
                        case REPRESENTATIVE_ENTRY:
                            UsernameAndPasswordVerification(requestObj,out);
                                break;
                       //הוספת case לפונצקיות של הנציגים
                        //בכל פעולה ששייכת למנהל, יש לבדוק האם הוא מחובר כעת isAdministrator אחרת הלקוח יוכל לעקוף את זה;)
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

    private void UsernameAndPasswordVerification(RequestData request, ObjectOutputStream out) throws IOException {
        int id = (int) request.getParameters().get(0);
        String password = (String) request.getParameters().get(1);
        ResponseData response = new ResponseData();
        if (id == InquiryManager.getIdAdministrator() && password.equals(InquiryManager.getPasswordAdministrator())) {
            response.setStatus(ResponseStatus.SUCCESS);
            sendResponse(out,ResponseStatus.SUCCESS,"Peace and blessings to: "+InquiryManager.getNameAdministrator(),null);
        }
        else {
            sendResponse(out,ResponseStatus.FAIL,"Invalid ID or Password.",null);
        }
    }

    private void handleAddInquiry(RequestData request, ObjectOutputStream out) throws IOException {
        List<Object> params = request.getParameters();
        if (params != null && !params.isEmpty() && params.get(0) instanceof Inquiry) {
            Inquiry newInQ = (Inquiry) params.get(0); // לוקחים את האיבר הראשון ועושים לו Casting
            InquiryManager.addInquiryFromClient(newInQ);
            sendResponse(out, ResponseStatus.SUCCESS, "Inquiry added successfully", newInQ);
        } else {
            sendResponse(out, ResponseStatus.FAIL, "Invalid data: Inquiry object missing", null);
        }
    }

    private void handleGetAllInquiries(ObjectOutputStream out) throws IOException {
        System.out.println("Fetching all inquiries...");
        ArrayList<Inquiry> list = new ArrayList<>(InquiryManager.getInquiryQueue());
        sendResponse(out, ResponseStatus.SUCCESS, "Success", list);
    }
    private void sendResponse(ObjectOutputStream out, ResponseStatus status, String message, Object data) throws IOException {
        ResponseData response = new ResponseData(status, message, data);
        out.writeObject(response);
        out.flush();
    }
}