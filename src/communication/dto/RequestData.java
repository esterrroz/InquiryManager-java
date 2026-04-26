package communication.dto;

import data.Inquiry;

import java.io.Serializable;
import java.util.List;

public class RequestData implements Serializable{
    private static final long serialVersionUID = 1L;
    private InquiryManagerActions action;
    private List<Inquiry> parameters;

    public void setAction(InquiryManagerActions action) {
        this.action = action;
    }
    public InquiryManagerActions getAction() {
        return action;
    }
    public List<Inquiry> getParameters() {
        return parameters;
    }

    public void setParameters(List<Inquiry> parameters) {
        this.parameters = parameters;
    }
}








