public class LeaveMessage extends Message {
    private Node node;
    private String nodeSide;

    public LeaveMessage(Node source, Node node, String nodeSide) {
        super(source);
        this.node = node;
        this.nodeSide = nodeSide;
    }

    public Node getNode(){
        return node;
    }

    public String getNodeSide(){
        return nodeSide;
    }
    
}