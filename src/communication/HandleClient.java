package communication;

import communication.dto.InquiryManagerActions;
import communication.dto.RequestData;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class HandleClient extends Thread  {
   private Socket clientSocket;
    public HandleClient(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }
    @Override
    public void run() {
        handleClientRequest();
    }
    private void handleClientRequest() {
        try {
            ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
            RequestData requestObj = (RequestData) in.readObject();
            InquiryManagerActions action = requestObj.getAction();
            switch (action) {
                case ADD_INQUIRY:
                    System.out.println("Adding a new inquiry...");
                    break;

                case ALL_INQUIRY:
                    System.out.println("Fetching all inquiries...");
                    break;
                case TEST:
                    System.out.println("Test successful! Hello from Client.");
                    break;

                default:
                    System.out.println("Unknown request: " + action);
            }

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Communication error: " + e.getMessage());
        }
    }


}
