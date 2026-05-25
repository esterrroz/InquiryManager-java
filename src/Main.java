import backgroundProcesses.HandleInquiriesDemo;
import backgroundProcesses.LinkRepresentativesToInquiries;
import communication.InquiryManagerServer;

public class Main {
    public static void main(String[] args) {
        InquiryManagerServer server = new InquiryManagerServer(8888);
        server.startServer();
    }
}