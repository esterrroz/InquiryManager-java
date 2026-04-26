import communication.InquiryManagerServer;

public class Main {
    public static void main(String[] args) {
        InquiryManagerServer server = new InquiryManagerServer(8080);
        server.startServer();
    }
}