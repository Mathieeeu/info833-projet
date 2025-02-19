
import java.util.ArrayList;

public class Message {
    private int source;
    private int target;
    private String type;
    private boolean forward;
    private ArrayList<Integer> path;

    public Message(int source, int target, String type) {
        this.source = source;
        this.target = target;
        this.type = type;
        this.forward = false;
        this.path = new ArrayList<Integer>();
    }

    public Message(int source, int target, String type, boolean forward, ArrayList<Integer> path) {
        this.source = source;
        this.target = target;
        this.type = type;
        this.forward = forward;
        this.path = path;
    }

}
