package backgroundProcesses;

import data.ActiveInquiry;
import data.Inquiry;
import data.Representative;
import service.InquiryManager;

public class LinkRepresentativesToInquiries implements Runnable{
    @Override
    public void run() {

        while(true){
            Inquiry inquiry;
            Representative representative;
            ActiveInquiry activeInquiry;
            if(!InquiryManager.getInquiryQueue().isEmpty()){
                if(!InquiryManager.getRepresentatives().isEmpty()){
                    synchronized (InquiryManager.getRepresentatives()){
                        inquiry = InquiryManager.getInquiryQueue().poll();
                        if(inquiry!=null) {
                            representative = InquiryManager.getRepresentatives().get(0);
                            if(representative!=null) {
                                InquiryManager.getRepresentatives().remove(0);
                                activeInquiry = new ActiveInquiry(inquiry, representative);
                                InquiryManager.getActiveInquiries().add(activeInquiry);
                                System.out.println("[SERVER DEBUG] A new ActiveInquiry was created for inquiry no. "+inquiry.getCode()+" with rep no. "+representative.getId());
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
    }
}
