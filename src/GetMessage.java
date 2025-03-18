import java.util.ArrayList;

public class GetMessage extends Message {
    
    private Node requestingNode;
    private int idResource;
    
    public GetMessage(Node source, Node requestingNode, int idResource) {
        super(source);
        this.requestingNode = requestingNode;
        this.idResource = idResource;
    }

    public GetMessage(Node source, boolean forward, ArrayList<Node> path, Node requestingNode, int idResource) {
        super(source, forward, path);
        this.requestingNode = requestingNode;
        this.idResource = idResource;
    }

    public Node getRequestingNode() {
        return requestingNode;
    }

    public int getIdResource() {
        return idResource;
    }

    public String toString() {
        return "GetMessage from " + this.requestingNode + " for resource " + this.idResource;
    }
}
