public class AckMessage extends Message {
    
    private String type;
    
    public AckMessage(Node source, String type) {
        super(source);
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    public String toString() {
        return "ackMessage from " + this.getSource().getId() + " (type : " + this.type + ")";
    }
}