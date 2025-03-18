import java.util.ArrayList;

public class PutMessage extends Message{

    private Resource resource;

    public PutMessage(Node source, Resource resource) {
        super(source);
        this.resource = resource;
    }

    public PutMessage(Node source, boolean forward, ArrayList<Node> path, Resource resource) {
        super(source, forward, path);
        this.resource = resource;
    }

    public Resource getResource() {
        return resource;
    }
}