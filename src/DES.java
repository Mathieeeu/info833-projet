import java.util.ArrayList;
import java.util.Random;

// DES = Discrete-Event Simulation
public class DES {
    private int time;
    private ArrayList<Node> nodes;
    private ArrayList<Event> queue;

    private Random rand = new Random();

    public static final int DEFAULT_MIN_TIME_TO_DELIVER = 3;
    public static final int DEFAULT_MAX_TIME_TO_DELIVER = 5;

    public DES() {
        this.time = 0;
        this.nodes = new ArrayList<Node>();
        this.queue = new ArrayList<Event>();

        // TRICHE : Le réseau contient déjà 4 noeuds reliés
        Node node1 = new Node(000);
        Node node2 = new Node(333);
        Node node3 = new Node(666);
        Node node4 = new Node(999);
        node1.setRight(node2);
        node2.setRight(node3);
        node3.setRight(node4);
        node4.setRight(node1);
        node1.setLeft(node4);
        node2.setLeft(node1);
        node3.setLeft(node2);
        node4.setLeft(node3);
        this.nodes.add(node1);
        this.nodes.add(node2);
        this.nodes.add(node3);
        this.nodes.add(node4);
    }

    public void startSimulation(int timeLimit) {

        // TODO : Ici il faut corriger un peu car la queue peut se vider quand les évenements arrivent sur des noeuds bloqués
        while (!this.queue.isEmpty() && this.time < timeLimit) {
        // while (this.time < timeLimit) {
            for (int i = 0; i < this.queue.size(); i++) {
                Event event = this.queue.get(i);
                event.decreaseTime();
                if (event.getTimeToDeliver() == 0) {
                    if (event.getMessage() instanceof JoinMessage) {
                        System.out.println("\u001B[38;5;150m[EVENT] " + event.getMessage().getClass().getSimpleName() + " delivered to " + event.getTarget().toString() + " from " + ((JoinMessage)event.getMessage()).getIdNodeToInsert() + "\u001B[0m");
                    }
                    else if (event.getMessage() instanceof InsertMessage) {
                        System.out.println("\u001B[38;5;150m[EVENT] " + event.getMessage().getClass().getSimpleName() + " delivered to " + event.getTarget().toString() + " from " + ((InsertMessage)event.getMessage()).getSource().getId() + "\u001B[0m");
                    }
                    else {
                        System.out.println("\u001B[38;5;150m[EVENT] " + event.getMessage().getClass().getSimpleName() + " delivered to " + event.getTarget().toString() + "\u001B[0m");
                    }
                    event.getTarget().deliver(event.getMessage());
                    this.queue.remove(i);
                    i--;
                }
            }
            this.time++;
            System.out.println("\n" + this);

            // // Appuie sur entrée pour passer à la prochaine étape
            // try {
            //     System.in.read();
            // } catch (Exception e) {
            //     e.printStackTrace();
            // }

        }
    }

    public void addNode(Node node) {
        this.nodes.add(node);
    }

    public void addEvent(Event event) {
        this.queue.add(event);
    }

    public void removeNode(Node node) {
        this.nodes.remove(node);
    }

    public void removeEvent(Event event) {
        this.queue.remove(event);
    }

    public void join(Node node) {
        // TODO : ajouter le noeud à la liste des noeuds après avoir trouvé sa position
        Node nodeToVisit = this.nodes.get(0); // Peut etre à changer parce que c'est un peu de la triche de prendre un noeud comme ça
        this.deliver(nodeToVisit, new JoinMessage(null, node, node.getId())); // TODO : on met null ou node en source ???
    }

    public void leave(Node node) {
        // TODO : retirer le noeud de la liste des noeuds après avoir changé les liens de ses voisins
        node.leave();
    }

    public void deliver(Node target, Message message) {
        if (message instanceof JoinMessage) {
            System.out.println("\u001B[38;5;200m[MESSAGE] " + message.getClass().getSimpleName() + " sent to " + target.toString() + " from " + ((JoinMessage)message).getIdNodeToInsert() + "\u001B[0m");
        }
        else if (message instanceof InsertMessage) {
            System.out.println("\u001B[38;5;200m[MESSAGE] " + message.getClass().getSimpleName() + " sent to " + target.toString() + " from " + ((InsertMessage)message).getSource().getId() + "\u001B[0m");
        }
        else {
            System.out.println("\u001B[38;5;200m[MESSAGE] " + message.getClass().getSimpleName() + " sent to " + target.toString() + "\u001B[0m");
        }
        this.addEvent(new Event(target, rand.nextInt(DEFAULT_MAX_TIME_TO_DELIVER) + 1, message));
    }
    
    public void deliver(Node target, int minTimeToDeliver, int maxTimeToDeliver, Message message) {
        System.out.println("\u001B[38;5;200m[MESSAGE] " + message.getClass().getSimpleName() + " sent to " + target.toString() + "\u001B[0m");
        this.addEvent(new Event(target, rand.nextInt(minTimeToDeliver, maxTimeToDeliver + 1), message));
    }

    @Override
    public String toString() {
        String result = "\u001B[38;5;33m[TIME : " + String.valueOf(time) + ", EVENTS : " + String.valueOf(this.queue.size());
        
        result += " (times : ";
        for (Event event : queue) {
            result += event.getTimeToDeliver() + " ";
        }
        result += ")]\u001B[0m\n";

        for (Node node : nodes) {
            result += node.toString() + "\n";
        }
        return result;
    }
}
