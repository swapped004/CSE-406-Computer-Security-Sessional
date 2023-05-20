public class RequestInfo {

    private String request_id;
    private String description;
    private String sender;
    private String receiver;
    private boolean grant;

    public RequestInfo(String request_id, String description, String sender, String receiver, boolean grant) {
        this.request_id = request_id;
        this.description = description;
        this.sender = sender;
        this.receiver = receiver;
        this.grant = grant;
    }

    public String getRequest_id() {
        return request_id;
    }

    public String getDescription() {
        return description;
    }

    public String getSender() {
        return sender;
    }

    public boolean isGrant() {
        return grant;
    }

    public String getReceiver() {
        return receiver;
    }
}
