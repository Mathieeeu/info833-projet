
import java.util.ArrayList;

public class Message {
    private Node source;
    private boolean forward;
    private ArrayList<Node> path;

    public Message(Node source) {
        this.source = source;
        this.forward = false;
        this.path = new ArrayList<Node>();
    }

    public Message(Node source, boolean forward, ArrayList<Node> path) {
        this.source = source;
        this.forward = forward;
        this.path = path;
    }

    public Node getSource() {
        return source;
    }

    public boolean isForwarded() {
        return forward;
    }

    public ArrayList<Node> getPath() {
        return path;
    }

    public void addNodeToPath(Node node) {
        path.add(node);
    }
}
