package communication.dto;

import java.io.Serializable;

public class ResponseData implements Serializable {
    private static final long serialVersionUID = 1L;
    ResponseStatus status;
    String message;
    Object result;
    public ResponseData() {
    }
    public ResponseData(ResponseStatus status, String message, Object result) {
        this.status = status;
        this.message = message;
        this.result = result;
    }

    public ResponseStatus getStatus() { return status; }
    public String getMessage() { return message; }
    public Object getResult() { return result; }
    public void setStatus(ResponseStatus status) { this.status = status; }
    public void setMessage(String message) { this.message = message; }
    public void setResult(Object result) { this.result = result; }

}
