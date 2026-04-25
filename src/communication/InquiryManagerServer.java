package communication;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class InquiryManagerServer {
    private ServerSocket myServer;
    private boolean isRunning;
    public InquiryManagerServer(ServerSocket myServer) {
        this.myServer = myServer;
        this.isRunning = false;
    }
    public void startServer() {
        isRunning = true;
        while (isRunning) {
            try {
                Socket connectedClientSocket = myServer.accept();
                HandleClient clientHandler = new HandleClient(connectedClientSocket);
                clientHandler.start();
            }
            catch (IOException e) {
                if (isRunning) {
                    System.err.println("Error accepting client: " + e.getMessage());
                }
                else
                    System.out.println( e.getMessage());
            }
        }
    }
    public void stop() {
        isRunning = false;
        try {
            if (myServer != null && !myServer.isClosed()) {
                myServer.close();
                System.out.println("Server stopped.");
            }
        } catch (IOException e) {
            System.err.println("Error closing server: " + e.getMessage());
        }
    }
}