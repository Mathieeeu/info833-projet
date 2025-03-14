
public class InsertMessage extends Message {
    
    private Node left;
    private Node right;

    public InsertMessage(Node source, Node left, Node right) {
        super(source);
        this.left = left;
        this.right = right;
    }

    public InsertMessage(Node source, Node node, String nodeSide) {
        super(source);
        if (nodeSide.equals("left")) {
            this.left = node;
            this.right = null;
        }
        else if (nodeSide.equals("right")) {
            this.left = null;
            this.right = node;
        }
        else {
            this.left = null;
            this.right = null;
        }
    }

    public Node getLeft() {
        return this.left;
    }

    public Node getRight() {
        return this.right;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("InsertMessage: ");
        if (left != null) {
            sb.append("left: ").append(left.toString()).append(", ");
        }
        if (right != null) {
            sb.append("right: ").append(right.toString());
        }
        return sb.toString();
    }
}
