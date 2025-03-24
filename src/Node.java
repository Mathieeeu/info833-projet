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
    private ArrayList<Resource> resources;

    //Constructeur
    public Node(int id) {
        this.id = id;
        this.left = null;
        this.right = null;
        this.locked = false;
        this.old_left = null;
        this.old_right = null;
        this.queue = new ArrayList<Message>();
        this.resources = new ArrayList<Resource>();
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

    public ArrayList<Message> getQueue() {
        return this.queue;
    }

    public Node getLeft() {
        return this.left;
    }

    public Node getRight() {
        return this.right;
    }

    public void leave() {
        // On envoie aux voisins que le noeud veut quitter
        System.out.println("\u001B[38;5;198m[INFO] node " + this.id + " wants to leave\u001B[0m");
        if (!this.locked) {
            this.locked = true;
            System.out.println("\u001B[38;5;198m[INFO] node " + this.id + " locked\u001B[0m");
        }
        App.des.deliver(this.left, DES.DEFAULT_MIN_TIME_TO_DELIVER, DES.DEFAULT_MAX_TIME_TO_DELIVER, new LeaveMessage(this, this.right, "right"));
        App.des.deliver(this.right, DES.DEFAULT_MIN_TIME_TO_DELIVER, DES.DEFAULT_MAX_TIME_TO_DELIVER, new LeaveMessage(this, this.left, "left"));
    }

    public void deliver(Message message) {

        if (!this.locked && !this.queue.isEmpty()) {
            this.checkQueue();
        }

        if (this.locked && !(message instanceof InsertMessage) && !(message instanceof AckMessage)) {
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
                        //Redistribuer les ressources entre le noeud courant et le noeud à insérer
                        for (int i = this.resources.size() - 1; i >= 0; i--) {
                            if (this.resources.get(i).getId() < insertMessage.getLeft().getId()) {
                                App.des.deliver(insertMessage.getLeft(), new ResourceMessage(this, this.resources.get(i), false));
                                //Le noeud informe son ancien voisin de gauche qu'il doit supprimer la ressource
                                App.des.deliver(this.getRight(), new DeleteMessage(this, this.resources.get(i).getId()));
                            }
                        }
                    }
                    
                    if (insertMessage.getRight() != null) {
                        this.old_right = this.right;
                        this.right = insertMessage.getRight();
                        //Redistribuer les ressources entre le noeud courant et le noeud à insérer
                        for (int i = this.resources.size() - 1; i >= 0; i--) {
                            if (this.resources.get(i).getId() > insertMessage.getRight().getId()) {
                                App.des.deliver(insertMessage.getRight(), new ResourceMessage(this, this.resources.get(i), false));
                                //Le noeud informe son ancien voisin de droite qu'il doit supprimer la ressource
                                App.des.deliver(this.getLeft(), new DeleteMessage(this, this.resources.get(i).getId()));
                            }
                        }
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
                    if (leaveMessage.getSource() != null) {
                        if (leaveMessage.getNodeSide().equals("left")) {
                            this.left = leaveMessage.getNode();
                            leaveMessage.getSource().deliver(new AckMessage(this, "leave"));
                        }
                        else if (leaveMessage.getNodeSide().equals("right")) {
                            this.right = leaveMessage.getNode();
                            leaveMessage.getSource().deliver(new AckMessage(this, "leave"));
                        }
                    }
                }
                case AckMessage ackMessage -> {
                    System.out.println("\u001B[38;5;198m[INFO] ack received by node " + this.id + "\u001B[0m");
                    this.queue.add(ackMessage);
                    
                    // Si la queue contient DEUX ack de type "leave", on fait bien la suppression dans le noeud courant
                    if (queueContains2Messages("leave")) {
                        //On redistribue les resources à ces voisins
                        for (int i = this.resources.size() - 1; i >= 0; i--) {
                            //On envoie au noeud gauche un message au noeud gauche pour rechercher le noeud centre de la ressource
                            App.des.deliver(this.left, new PutMessage(this, this.resources.get(i)));
                            this.resources.remove(i);
                        }
                        this.left = null;
                        this.right = null;
                        this.locked = false;
                        System.out.println("\u001B[38;5;198m[INFO] node " + this.id + " unlocked\u001B[0m");
                        if (!this.queue.isEmpty()) {
                            this.checkQueue();
                        }
                    }
                }
                case PutMessage putMessage -> {
                    if (putMessage.getResource().getId() == this.id) {
                        // Si la resource a l'id exact du noeud
                        if (this.resources.contains(putMessage.getResource())) {
                            // Si la ressource est déjà présente dans le noeud courant, on ne fait rien
                            System.out.println("\u001B[38;5;198m[INFO] resource " + putMessage.getResource().getId() + " already present in node " + this.id + "\u001B[0m");
                        }
                        else{
                            this.resources.add(putMessage.getResource());
                            System.out.println("\u001B[38;5;198m[INFO] resource " + putMessage.getResource().getId() + " added to node " + this.id + "\u001B[0m");
                        }
                        //Dupliquer la ressource sur le noeud voisin gauche et de droite
                        App.des.deliver(this.left, new ResourceMessage(this, putMessage.getResource(), false));
                        App.des.deliver(this.right, new ResourceMessage(this, putMessage.getResource(), false));
                    }
                    else if (this.id < putMessage.getResource().getId() &&  putMessage.getResource().getId() < this.right.getId()) {
                        // Si la resource a un id plus grand que le noeud courant et plus petit que le noeud droit, on l'ajoute au noeud le plus proche
                        if ((putMessage.getResource().getId() - this.id) <= (this.right.getId() - putMessage.getResource().getId())) {
                            // Le noeud courant est le plus proche, on ajoute la ressource pour lui (et on le communique à ses deux voisins)
                            if (this.resources.contains(putMessage.getResource())) {
                                // Si la ressource est déjà présente dans le noeud courant, on ne fait rien
                                System.out.println("\u001B[38;5;198m[INFO] resource " + putMessage.getResource().getId() + " already present in node " + this.id + "\u001B[0m");
                            }
                            else{
                                this.resources.add(putMessage.getResource());
                            }
                            App.des.deliver(this.left, new ResourceMessage(this, putMessage.getResource(), false));
                            App.des.deliver(this.right, new ResourceMessage(this, putMessage.getResource(), false));

                        } else {
                            // Le noeud voisin droit est le plus proche, on lui ajoute la ressource/ transfère la ressource
                            App.des.deliver(this.right, new ResourceMessage(this, putMessage.getResource(), true));
                        }
                    }
                    else if (this.left.getId() > this.id && putMessage.getResource().getId() > this.left.getId()) {
                        // Dans le cas du premier noeud du réseau, l'id gauche est plus grand que l'id courant, si l'id de la resource est plus grand que le noeud gauche, on l'ajoute entre le dernier et le premier noeud car c'est le nouveau plus grand 
                        App.des.deliver(this.left, new ResourceMessage(this, putMessage.getResource(), true));
                    } else {
                        // Si l'id est plus grand, on transfère le message au noeud droit du noeud courant
                        message.addNodeToPath(this);
                        App.des.deliver(this.right, DES.DEFAULT_MIN_TIME_TO_DELIVER, DES.DEFAULT_MAX_TIME_TO_DELIVER, new PutMessage(this, true, message.getPath(), putMessage.getResource()));
                    }
                }

                case ResourceMessage resourceMessage -> {
                    if (resourceMessage.isForwardingAResource()) {
                        // La ressource demandée avec un GET arrive via ce message
                        System.out.println("\u001B[38;5;198m[INFO] resource " + resourceMessage.getResource().getId() + " received by node " + this.id + "\u001B[0m");
                        // this.resources.add(resourceMessage.getResource()); // TODO : est-ce qu'on laisse ça ? Ou est ce que la ressource demandée doit juste être lue ?
                    }
                    else if (resourceMessage.isCenter()) {
                        // Si le noeud courant est le plus proche de la position de la ressource, on veut qu'il soit ajouté par ses deux voisins (et on ajoute la ressource)
                        if (this.resources.contains(resourceMessage.getResource())) {
                            // Si la ressource est déjà présente dans le noeud courant, on ne fait rien
                            System.out.println("\u001B[38;5;198m[INFO] resource " + resourceMessage.getResource().getId() + " already present in node " + this.id + "\u001B[0m");
                        }
                        else {
                            this.resources.add(resourceMessage.getResource());
                        }
                        App.des.deliver(this.left, new ResourceMessage(this, resourceMessage.getResource(), false));
                        App.des.deliver(this.right, new ResourceMessage(this, resourceMessage.getResource(), false));
                    } else {
                        // Si le noeud courant n'est pas le centre de la ressource
                        if (this.resources.contains(resourceMessage.getResource())) {
                            // Si la ressource est déjà présente dans le noeud courant, on ne fait rien
                            System.out.println("\u001B[38;5;198m[INFO] resource " + resourceMessage.getResource().getId() + " already present in node " + this.id + "\u001B[0m");
                        }
                        else {
                            this.resources.add(resourceMessage.getResource());
                        }
                    }
                }

                case GetMessage getMessage -> {
                    int index = indexOfResource(getMessage.getIdResource());
                    if (index != -1) {
                        // Ce noeud contient la ressource recherchée
                        App.des.deliver(getMessage.getRequestingNode(), new ResourceMessage(this, this.resources.get(index)), DES.DEFAULT_MAX_TIME_TO_DELIVER);
                    } else {
                        if (getMessage.getIdResource() < this.id) {
                            // L'id de la ressource est inférieur à l'id du noeud courant donc on envoie le message à gauche
                            getMessage.addNodeToPath(this);
                            App.des.deliver(this.left, DES.DEFAULT_MIN_TIME_TO_DELIVER, DES.DEFAULT_MAX_TIME_TO_DELIVER, new GetMessage(this, true, getMessage.getPath(), getMessage.getRequestingNode(), getMessage.getIdResource()));
                            
                        } else {
                            // L'id de la ressource est supérieur à l'id du noeud courant, donc on envoie le message à droite
                            getMessage.addNodeToPath(this);
                            App.des.deliver(this.right, DES.DEFAULT_MIN_TIME_TO_DELIVER, DES.DEFAULT_MAX_TIME_TO_DELIVER, new GetMessage(this, true, getMessage.getPath(), getMessage.getRequestingNode(), getMessage.getIdResource()));
                        }
                    }
                }

                case DeleteMessage deleteMessage -> {
                    int index = indexOfResource(deleteMessage.getIdRessourceToDelete());
                    if (index != -1) {
                        // Ce noeud contient la ressource recherchée
                        this.resources.remove(index);
                        System.out.println("\u001B[38;5;198m[INFO] resource " + deleteMessage.getIdRessourceToDelete() + " deleted from node " + this.id + "\u001B[0m");
                    } else {
                        System.out.println("\u001B[38;5;198m[INFO] resource " + deleteMessage.getIdRessourceToDelete() + " not found in node " + this.id + "\u001B[0m");
                        // Si la ressource n'est pas trouvée sur le noeud courant, il faut renvoyer un message à celui qui vient de nous envoyer le deleteMessage pour lui dire que c'est LUI qui a la ressource à supprimer 
                        App.des.deliver(deleteMessage.getSource(), new DeleteMessage(this, deleteMessage.getIdRessourceToDelete()));
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

    private boolean queueContains2Messages(String ackType){
        int cpt = 0;
        ArrayList<Message> toRemove = new ArrayList<Message>();
        for(Message message : this.queue){
            if (message instanceof AckMessage ackMessage && ackMessage.getType().equals(ackType)){
                cpt ++;
                toRemove.add(ackMessage);
            }
        }
        if (cpt == 2) {
            for (Message message : toRemove){
                this.queue.remove(message);
            }
             return true;
        }
        return false;
    }

    private int indexOfResource(int id) {
        for (int i = 0; i < resources.size(); i++) {
            if (resources.get(i).getId() == id) {
                return i;
            }
        }
        return -1;
    }

    private void checkQueue() {
        // Si la queue n'est pas vide, on récupère le premier message et on le livre
        Message nextMessage = this.queue.get(0);
        if (!(nextMessage instanceof AckMessage)) {
            System.out.println("\u001B[38;5;198m[INFO] message " + nextMessage.toString() + " delivered to node " + this.id + "\u001B[0m");
            this.queue.remove(nextMessage);
            this.deliver(nextMessage);
        }
    }

    public String printResources() {
        String res = "(";
        if (this.resources.isEmpty()) {
            res += ".";
        } else if (this.resources.size() == 1) {
            res += resources.get(0).getId();
        } else {
            for (int i = 0; i<this.resources.size() - 1; i++) {
                res += resources.get(i).getId() + ", ";
            }
            res += resources.get(resources.size() - 1).getId();
        }
        res += ")";
        return res;
    }
}
