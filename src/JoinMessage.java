import java.util.ArrayList;

public class JoinMessage extends Message {
    private Node nodeToInsert;
    private int idNodeToInsert;

    public JoinMessage(Node source, Node nodeToInsert, int idNodeToInsert) {
        super(source);
        this.nodeToInsert = nodeToInsert;
        this.idNodeToInsert = idNodeToInsert;
    }

    public JoinMessage(Node source, boolean forward, ArrayList<Node> path, Node nodeToInsert, int idNodeToInsert) {
        super(source, forward, path);
        this.nodeToInsert = nodeToInsert;
        this.idNodeToInsert = idNodeToInsert;
    }

    public Node getNodeToInsert() {
        return nodeToInsert;
    }

    public int getIdNodeToInsert() {
        return idNodeToInsert;
    }

    public String toString() {
        return "JoinMessage{" +
                "idNodeToInsert=" + this.idNodeToInsert +
                ", source=" + super.getSource() +
                '}';
    }
}
