package service;

import data.Inquiry;
import data.Question;
import data.Request;
import data.Complaint;

public class InquiryHandling extends Thread {
    private Inquiry currentInquiry;
    public InquiryHandling(Inquiry currentInquiry) {
        this.currentInquiry = currentInquiry;
    }
    @Override
    public void run() {
        try {
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
            System.out.println("Finished handling inquiry code: " + currentInquiry.getCode());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    // Getters & Setters
    public Inquiry getCurrentInquiry() { return currentInquiry; }
    public void setCurrentInquiry(Inquiry currentInquiry) { this.currentInquiry = currentInquiry; }
}