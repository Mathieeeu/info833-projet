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
        if (!this.locked) {
            this.locked = true;
            System.out.println("\u001B[38;5;198m[INFO] node " + this.id + " locked\u001B[0m");
        }
        else {
            // TODO : mettre en queue l'opération "leave"
        }
        App.des.deliver(this.left, DES.DEFAULT_MIN_TIME_TO_DELIVER, DES.DEFAULT_MAX_TIME_TO_DELIVER, new LeaveMessage(this, this.right, "right"));
        App.des.deliver(this.right, DES.DEFAULT_MIN_TIME_TO_DELIVER, DES.DEFAULT_MAX_TIME_TO_DELIVER, new LeaveMessage(this, this.left, "left"));
    }

    public void deliver(Message message) {

        if (!this.locked && !this.queue.isEmpty()) {
            this.checkQueue();
        }

        if (this.locked && !(message instanceof InsertMessage)) {
            this.queue.add(message);
            System.out.println("\u001B[38;5;198m[INFO] message " + message.toString() + " added to queue of node " + this.id + "\u001B[0m");
        }

        else {
            // JoinMessage : Message qui circule pour trouver la bonne position d'un noeud à insérer
            switch (message) {
                case JoinMessage joinMessage -> {
                    
                    // Si le noeud voisin droit est plus grand que le noeud à insérer (ie le noeud s'insèrera entre le noeud courant et ce fameux voisin), le noeud courant se bloque (dans le cas contraire il retransmet juste le message et on s'en fout)
                    if (this.right.getId() > joinMessage.getIdNodeToInsert()) {
                        this.locked = true;
                        System.out.println("\u001B[38;5;198m[INFO] node " + this.id + " locked\u001B[0m");
                    }
                    
                    if (this.id > joinMessage.getIdNodeToInsert()) {
                        // Le noued courant est plus grand que le noeud à placer, donc on place le noeud
                        App.des.deliver(joinMessage.getNodeToInsert(), new InsertMessage(this, this.left, this));
                    }
                    
                    else {
                        if (this.left.getId() > this.id && this.left.getId() < joinMessage.getIdNodeToInsert()) {
                            // Si le noeud voisin gauche est plus grand que le noeud courant mais plus petit que le noeud à insérer (ie on est sur le premier noeud de la DHT, son voisin gauche est le dernier) donc on insère le noeud entre le plus grand et le plus petit (noeud courant)
                            this.locked = true;
                            System.out.println("\u001B[38;5;198m[INFO] node " + this.id + " locked\u001B[0m");
                            App.des.deliver(joinMessage.getNodeToInsert(), new InsertMessage(this, this.left, this));
                        }
                        else {
                            // Le noeud courant est plus petit que le noeud à placer, on transfère le message au noeud droit du noeud courant
                            message.addNodeToPath(this);
                            App.des.deliver(this.right, DES.DEFAULT_MIN_TIME_TO_DELIVER, DES.DEFAULT_MAX_TIME_TO_DELIVER, new JoinMessage(this, true, message.getPath(), joinMessage.getNodeToInsert(), joinMessage.getIdNodeToInsert()));
                        }
                    }
                }
                case InsertMessage insertMessage -> {
                    
                    if (insertMessage.getLeft() != null) {
                        this.old_left = this.left;
                        this.left = insertMessage.getLeft();
                    }
                    if (insertMessage.getRight() != null) {
                        this.old_right = this.right;
                        this.right = insertMessage.getRight();
                    }
                    if (insertMessage.getRight() != null && insertMessage.getLeft() != null) {
                        App.des.deliver(this.left, new InsertMessage(this, this, "right"));
                        App.des.deliver(this.right, new InsertMessage(this, this, "left"));
                    }
                    System.out.println("\u001B[38;5;198m[INFO] node " + this.id + " unlocked\u001B[0m");
                    this.locked = false;
                    if (!this.queue.isEmpty()) {
                        this.checkQueue();
                    }
                }
                case LeaveMessage leaveMessage -> {
                    if (leaveMessage.getNodeSide().equals("left")) {
                        this.left = leaveMessage.getNode();
                        leaveMessage.getSource().deliver(new AckMessage(this, "leave"));
                    }
                    else if (leaveMessage.getNodeSide().equals("right")) {
                        this.right = leaveMessage.getNode();
                        leaveMessage.getSource().deliver(new AckMessage(this, "leave"));
                    }
                }
                case AckMessage ackMessage -> {
                    this.queue.add(ackMessage);
                    
                    // Si la queue contient DEUX ack de type "leave", on fait bien la suppression dans le noeud courant
                    if (queueContains2Messages("leave")) {
                        this.left = null;
                        this.right = null;
                        this.locked = false;
                        System.out.println("\u001B[38;5;198m[INFO] node " + this.id + " unlocked\u001B[0m");
                        if (!this.queue.isEmpty()) {
                            this.checkQueue();
                        }
                    }
                }
                default -> {
                    System.out.println("\u001B[38;5;20m[ERROR] message type not recognized\u001B[0m");
                }
            }
        }
    }

    @Override
    public String toString() {
        if (this.left == null && this.right == null) {
            return "Node " + this.id + " (left : null, right : null, queue : " + this.queue.size() + ", locked : " + this.locked + ")";
        }
        else if (this.left == null) {
            return "Node " + this.id + " (left : null, right : " + this.right.getId() + ", queue : " + this.queue.size() + ", locked : " + this.locked + ")";
        }
        else if (this.right == null) {
            return "Node " + this.id + " (left : " + this.left.getId() + ", right : null, queue : " + this.queue.size() + ", locked : " + this.locked + ")";
        }
        return "Node " + this.id + " (left : " + this.left.getId() + ", right : " + this.right.getId() + ", queue : " + this.queue.size() + ", locked : " + this.locked + ")";
    }

    public boolean queueContains2Messages(String ackType){
        int cpt = 0;

        for(Message message : this.queue){
            if (message instanceof AckMessage && ((AckMessage) message).getType().equals(ackType)){
                cpt ++;
            }
        }
        if (cpt == 2) {
            for(Message message : this.queue){
                if (message instanceof AckMessage && ((AckMessage) message).getType().equals(ackType)){
                    this.queue.remove(message);
                }
            }
            return true;
        }
        return false;
    }

    public void checkQueue() {
        // Si la queue n'est pas vide, on récupère le premier message et on le livre
        Message nextMessage = this.queue.get(0);
        System.out.println("\u001B[38;5;198m[INFO] message " + nextMessage.toString() + " delivered to node " + this.id + "\u001B[0m");
        this.queue.remove(nextMessage);
        this.deliver(nextMessage);
    }
}
