package communication;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class InquiryManagerServer {
    private ServerSocket myServer;
    private boolean isRunning;


    public InquiryManagerServer(int port) {
        try {
            this.myServer = new ServerSocket(port);
            this.isRunning = false;
        } catch (IOException e) {
            System.err.println("Server initialization failed: " + e.getMessage());
        }
    }
    public void startServer() {
        isRunning = true;
        System.out.println("Server is running and waiting for clients...");
        while (isRunning) {
            try {
                Socket connectedClientSocket = myServer.accept();
                HandleClient clientHandler = new HandleClient(connectedClientSocket);
                clientHandler.start();

            } catch (IOException e) {
                if (isRunning) {
                    System.err.println("Error accepting client: " + e.getMessage());
                }
            }
        }
    }

    public void stop() {
        isRunning = false;
        try {
            if (myServer != null && !myServer.isClosed()) {
                myServer.close();
                System.out.println("Server stopped successfully.");
            }
        } catch (IOException e) {
            System.err.println("Error while stopping server: " + e.getMessage());
        }
    }

}