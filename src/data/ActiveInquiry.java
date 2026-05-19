package data;

public class ActiveInquiry {
    private Inquiry inquiry;
    private Representative representative;

    public ActiveInquiry(Inquiry inquiry, Representative representative) {
        this.inquiry = inquiry;
        this.representative = representative;
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
}