
import java.util.ArrayList;

public class Node {
    private  int id;
    private Node left;
    private Node right;
    private boolean locked; // true si le noeud est occupé, false sinon
    private Node old_left;
    private Node old_right;
    private ArrayList<Event> queue;
    

    public Node(int id) {
        this.id = id;
        this.left = null;
        this.right = null;
        this.locked = false;
        this.old_left = null;
        this.old_right = null;
        this.queue = new ArrayList<Event>();
    }

    public void send(Message message) {
        // TODO : envoyer le message
    }

}
