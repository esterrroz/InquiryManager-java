package backgroundProcesses;

import data.ActiveInquiry;
import data.Inquiry;
import data.Representative;
import service.InquiryHandling;
import service.InquiryManager;

import static service.InquiryManager.*;

public class HandleInquiriesDemo implements Runnable{

    @Override
    public void run() {

        InquiryHandling inquiryHandling;

        while(true){
            ActiveInquiry currentActive = null;
            synchronized (InquiryManager.getActiveInquiries()) {
                if (!InquiryManager.getActiveInquiries().isEmpty()) {
                    currentActive=InquiryManager.getActiveInquiries().poll();
                }
            }

            inquiryHandling = new InquiryHandling(currentActive.getInquiry());
            inquiryHandling.start();

        }
    }
}
