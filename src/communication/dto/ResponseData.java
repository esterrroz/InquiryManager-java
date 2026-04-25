package communication.dto;

public class ResponseData {
    ResponseStatus status;
    String message;
    Object result;

    public ResponseData(ResponseStatus status, String message, Object result) {
        this.status = status;
        this.message = message;
        this.result = result;
    }
}
