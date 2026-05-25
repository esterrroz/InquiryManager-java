package backgroundProcesses;

import data.ActiveInquiry;
import service.InquiryHandling;
import service.InquiryManager;

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

            if(currentActive!=null) {
                inquiryHandling = new InquiryHandling(currentActive);
                inquiryHandling.start();

            }

        }
    }
}
