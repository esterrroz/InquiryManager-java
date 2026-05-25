package systemExecuging;

import backgroundProcesses.HandleInquiriesDemo;
import backgroundProcesses.LinkRepresentativesToInquiries;
import communication.InquiryManagerServer;

public class startSystemProcess {
    static void main(String[] args) {

        // Launching a server
        InquiryManagerServer server = new InquiryManagerServer(8888);
        new Thread(server).start();

        // Starting linking representatives to waiting inquiries
        new Thread(new HandleInquiriesDemo()).start();

        // Starting inquiries handling
        new Thread(new LinkRepresentativesToInquiries()).start();
    }
}
