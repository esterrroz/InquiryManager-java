package service;

import data.*;

public class InquiryHandling extends Thread {
    private Inquiry currentInquiry;
    public InquiryHandling(Inquiry currentInquiry) {
        this.currentInquiry = currentInquiry;
    }
    @Override
    public void run() {
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
            // move inquiry to history
            System.out.println("Finished handling inquiry code: " + currentInquiry.getCode());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    // Getters & Setters
    public Inquiry getCurrentInquiry() { return currentInquiry; }
    public void setCurrentInquiry(Inquiry currentInquiry) { this.currentInquiry = currentInquiry; }

    // Function to move inquiry to history
}