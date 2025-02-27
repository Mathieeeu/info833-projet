import java.util.ArrayList;

public class Node {

    //Attributs
    private  int id;
    private Node left;
    private Node right; // on regarde de notre point de vue
    private boolean locked; // true si le noeud est occupé, false sinon
    private Node old_left;
    private Node old_right;
    private ArrayList<Message> queue;

    //Constructeur
    public Node(int id) {
        this.id = id;
        this.left = null;
        this.right = null;
        this.locked = false;
        this.old_left = null;
        this.old_right = null;
        this.queue = new ArrayList<Message>();
    }
    
    //Getter (il sert à tricher au tout début :)
    public int getId(){
        return this.id;
    }

    //Méthodes
    public void setLeft(Node left) {
        this.left = left;
    }

    public void setRight(Node right) {
        this.right = right;
    }

    public void leave() {
        // On envoie aux voisins que le noeud veut quitter
        this.locked = true;
        this.left.deliver(new LeaveMessage(this, this.right, "right"));
        this.right.deliver(new LeaveMessage(this, this.left, "left"));
    }

    public void deliver(Message message) {

        // JoinMessage : Message qui circule pour trouver la bonne position d'un noeud à insérer
        if (message instanceof JoinMessage) {

            if (this.id > ((JoinMessage) message).getIdNodeToInsert()) {
                // Le noeud courant est plus grand que le noeud à placer, donc on place le noeud
                ((JoinMessage) message).getNodeToInsert().deliver(new InsertMessage(this, this.left, this));
            }
            else {
                if (((JoinMessage) message).getPath().contains(this)) {
                    // On a fait tout le tour de la DHT, on insère le noeud entre le plus grand et le plus petit (noeud courant)
                    ((JoinMessage) message).getNodeToInsert().deliver(new InsertMessage(this, this.left, this));
                } 
                else {
                    // Le noeud courant est plus petit que le noeud à placer, on transfère le message au noeud droit du noeud courant
                    message.addNodeToPath(this);
                    this.right.deliver(new JoinMessage(this, true, message.getPath(), ((JoinMessage) message).getNodeToInsert(), ((JoinMessage) message).getIdNodeToInsert()));  
                }
            }
        }

        // InsertMessage : Requête pour faire changer les voisins d'un noeud
        else if (message instanceof InsertMessage) {

            if (((InsertMessage) message).getLeft() != null) {
                this.old_left = this.left;
                this.left = ((InsertMessage) message).getLeft();
            }
            if (((InsertMessage) message).getRight() != null) {
                this.old_right = this.right;
                this.right = ((InsertMessage) message).getRight();
            }
            if (((InsertMessage) message).getRight() != null && ((InsertMessage) message).getLeft() != null) {
                this.left.deliver(new InsertMessage(this, this, "right"));
                this.right.deliver(new InsertMessage(this, this, "left"));
            }
        }

        // LeaveMessage : requête pour mettre à jour les voisins suite au départ d'un noeud
        else if (message instanceof LeaveMessage) {
            if (((LeaveMessage) message).getNodeSide().equals("left")) {
                this.left = ((LeaveMessage)message).getNode();
                ((LeaveMessage) message).getSource().deliver(new AckMessage(this, "leave"));
            }
            else if (((LeaveMessage) message).getNodeSide().equals("right")) {
                this.right = ((LeaveMessage)message).getNode();
                ((LeaveMessage) message).getSource().deliver(new AckMessage(this, "leave"));
            }
        }

        // AckMessage : Une opération a bien été acceptée
        else if (message instanceof AckMessage) {
            this.queue.add(message);

            // Si la queue contient DEUX ack de type "leave", on fait bien la suppression dans le noeud courant
            if (queueContains2LeaveMessage()) {
                this.left = null;
                this.right = null;
                this.locked = false;
            }
        }
    }

    @Override
    public String toString() {
        if (this.left == null && this.right == null) {
            return "Node " + this.id + " (left : null, right : null)";
        }
        else if (this.left == null) {
            return "Node " + this.id + " (left : null, right : " + this.right.getId() + ")";
        }
        else if (this.right == null) {
            return "Node " + this.id + " (left : " + this.left.getId() + ", right : null)";
        }
        return "Node " + this.id + " (left : " + this.left.getId() + ", right : " + this.right.getId() + ")";
    }

    public boolean queueContains2LeaveMessage(){
        int cpt = 0;
        for(Message message : this.queue){
            if (message instanceof AckMessage && ((AckMessage) message).getType().equals("leave")){
                cpt ++;
            }
        }
        if (cpt == 2) {
            for(Message message : this.queue){
                if (message instanceof AckMessage && ((AckMessage) message).getType().equals("leave")){
                    this.queue.remove(message);
                }
            }
            return true;
        }
        return false;
    }
}
