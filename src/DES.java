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
    public static final int TIME_LIMIT = 25;


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

    public void startSimulation() {
        while (!this.queue.isEmpty() && this.time < TIME_LIMIT) {
            for (int i = 0; i < this.queue.size(); i++) {
                Event event = this.queue.get(i);
                if (event.getTime() == 0) {
                    System.out.println(event);
                    event.getTarget().deliver(event.getMessage());
                    this.queue.remove(i);
                    i--;
                }
                event.decreaseTime();
            }
            this.time++;
            System.out.println(this);

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
        System.out.println("aaa");
        this.addEvent(new Event(target, rand.nextInt(DEFAULT_MAX_TIME_TO_DELIVER), message));
    }
    
    public void deliver(Node target, int minTimeToDeliver, int maxTimeToDeliver, Message message) {
        if (message instanceof JoinMessage){
            System.out.println("bb" + target.toString() + minTimeToDeliver + maxTimeToDeliver + ((JoinMessage)message).getNodeToInsert());
        }
        this.addEvent(new Event(target, rand.nextInt(minTimeToDeliver, maxTimeToDeliver), message));
    }

    @Override
    public String toString() {
        String result = "\u001B[38;5;33m[TIME : " + String.valueOf(time) + ", EVENTS : " + String.valueOf(this.queue.size()) + "]\u001B[0m \n";
        
        result += "(";
        for (Event event : queue) {
            result += event.getTime() + " ";
        }
        result += ")\n";

        for (Node node : nodes) {
            result += node.toString() + "\n";
        }
        return result;
    }
}
