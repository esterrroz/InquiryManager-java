package communication;

import communication.dto.*;
import data.Inquiry;
import service.InquiryManager;

import java.io.*;
import java.net.Socket;
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
        ObjectOutputStream out = null;
        ObjectInputStream in = null;

        try {
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());
            RequestData requestObj = (RequestData) in.readObject();
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
                default:
                    sendResponse(out, ResponseStatus.FAIL, "Unknown action", null);
            }

        } catch (Exception e) {
            try {
                if (out != null) {
                    sendResponse(out, ResponseStatus.FAIL, "Error: " + e.getMessage(), null);
                }
            } catch (IOException ex) {
                System.err.println("Could not send error response: " + ex.getMessage());
            }
        }
        finally {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (clientSocket != null) clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
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