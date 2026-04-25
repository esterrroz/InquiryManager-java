import communication.InquiryManagerServer;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        InquiryManagerServer server = new InquiryManagerServer(8080);
        Thread serverThread = new Thread(() -> server.startServer());
        serverThread.start();
        System.out.println("Press ENTER to stop the server...");
        new Scanner(System.in).nextLine();
        server.stop();
    }
}