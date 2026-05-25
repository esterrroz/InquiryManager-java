package backgroundProcesses;

import data.ActiveInquiry;
import data.Inquiry;
import data.Representative;
import service.InquiryManager;

public class LinkRepresentativesToInquiries implements Runnable{
    @Override
    public void run() {

        while(true){
            Inquiry inquiry=null;
            Representative representative=null;
            ActiveInquiry activeInquiry;
            synchronized (InquiryManager.getInquiryQueue()) {
                if (!InquiryManager.getInquiryQueue().isEmpty()) {
                    synchronized (InquiryManager.getRepresentatives()) {
                        if (!InquiryManager.getRepresentatives().isEmpty()) {
                            inquiry = InquiryManager.getInquiryQueue().poll();
                            if (inquiry != null) {
                                representative = InquiryManager.getRepresentatives().get(0);
                                if (representative != null) {
                                    InquiryManager.getRepresentatives().remove(0);
                                }
                            }
                        }
                    }
                }
                else {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            if(representative!=null&&inquiry!=null){
                activeInquiry = new ActiveInquiry(inquiry, representative);
                synchronized (InquiryManager.getActiveInquiries()) {
                    InquiryManager.getActiveInquiries().add(activeInquiry);
                }
                System.out.println("[SERVER DEBUG] A new ActiveInquiry was created for inquiry no. " + inquiry.getCode() + " with rep no. " + representative.getId());
            }

        }
    }
}
