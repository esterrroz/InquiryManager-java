package communication.dto;

import communication.dto.InquiryManagerActions;

import java.util.List;

public class RequestData {
    InquiryManagerActions action;
    List <Object>parameters;//איזה סוג הרשימה צריך להכיל?
    public void setAction(InquiryManagerActions action) {
        this.action = action;
    }
    public InquiryManagerActions getAction() {
        return action;
    }

}
