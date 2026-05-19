package data;

public class ActiveInquiry {

    private Inquiry inquiry;
    private Representative representative;

    boolean representativeIsActive;

    public ActiveInquiry(Inquiry inquiry, Representative representative) {
        this.inquiry = inquiry;
        this.representative = representative;

        this.representativeIsActive = false;
    }

    public Inquiry getInquiry() {
        return inquiry;
    }

    public void setInquiry(Inquiry inquiry) {
        this.inquiry = inquiry;
    }

    public Representative getRepresentative() {
        return representative;
    }

    public void setRepresentative(Representative representative) {
        this.representative = representative;
    }

    public boolean isRepresentativeIsActive() {
        return representativeIsActive;
    }

    public void setRepresentativeIsActive(boolean representativeIsActive) {
        this.representativeIsActive = representativeIsActive;
    }
}

