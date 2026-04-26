package communication.dto;

import java.io.Serializable;
import java.util.List;

public class RequestData implements Serializable{
    private static final long serialVersionUID = 1L;
    private InquiryManagerActions action;
    private List<Object> parameters;

    public void setAction(InquiryManagerActions action) {
        this.action = action;
    }
    public InquiryManagerActions getAction() {
        return action;
    }
    public List<Object> getParameters() {
        return parameters;
    }

    public void setParameters(List<Object> parameters) {
        this.parameters = parameters;
    }
}








