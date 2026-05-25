package service;

import data.*;

public class InquiryHandling extends Thread {
    private ActiveInquiry activeInquiry;
    public InquiryHandling(ActiveInquiry activeInquiry){
        this.activeInquiry=activeInquiry;
    }
    @Override
    public void run() {
        if(activeInquiry==null||activeInquiry.getInquiry()==null) return;
        Inquiry currentInquiry = activeInquiry.getInquiry();
        try {
            currentInquiry.setStatus(InquiryStatus.HANDLED);
            if (currentInquiry == null) return;
            if (currentInquiry instanceof Question) {
                Thread.currentThread().setPriority(Thread.MIN_PRIORITY); // עדיפות 1
            }
            if (currentInquiry instanceof Request) {
                Thread.sleep(300);
            } else {
                Thread.sleep(500);
            }
            currentInquiry.handling();
            currentInquiry.setStatus(InquiryStatus.HISTORY);
            InquiryManager.moveInquiryToHistoryFiles(currentInquiry);
            System.out.println("Finished handling inquiry code: " + currentInquiry.getCode());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(activeInquiry.isRepresentativeActive()){
            synchronized (InquiryManager.getRepresentatives()){
                InquiryManager.getRepresentatives().add(activeInquiry.getRepresentative());
            }
        }
    }
    // Getters & Setters
    public ActiveInquiry getActiveInquiry() { return activeInquiry; }
    public void setActiveInquiry(ActiveInquiry activeInquiry) { this.activeInquiry = activeInquiry; }

    // Function to move inquiry to history
}