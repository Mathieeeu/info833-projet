import java.util.ArrayList;

// DES = Discrete-Event Simulation
public class DES {
    private int time;
    private ArrayList<Node> nodes;
    private ArrayList<Event> events;

    public DES() {
        this.time = 0;
        this.nodes = new ArrayList<Node>();
        this.events = new ArrayList<Event>();

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
        while (!events.isEmpty()) {
            for (int i = 0; i < events.size(); i++) {
                Event event = events.get(i);
                if (event.getTime() == time) {
                    // TODO : faire quelque chose
                    events.remove(i);
                    i--;
                }
            }
            time++;
        }
    }

    public void addNode(Node node) {
        this.nodes.add(node);
    }

    public void addEvent(Event event) {
        this.events.add(event);
    }

    public void removeNode(Node node) {
        this.nodes.remove(node);
    }

    public void removeEvent(Event event) {
        this.events.remove(event);
    }

    public void join(Node node) {
        // TODO : ajouter le noeud à la liste des noeuds après avoir trouvé sa position
        Node nodeToVisit = this.nodes.get(0); // Peut etre à changer parce que c'est un peu de la triche de prendre un noeud comme ça
        nodeToVisit.deliver(new JoinMessage(null, node, node.getId())); // TODO : on met null ou node en source ???
    }

    public void leave(Node node) {
        // TODO : retirer le noeud de la liste des noeuds après avoir changé les liens de ses voisins
        node.leave();
    }

    @Override
    public String toString() {
        String result = "\u001B[38;5;33m[TIME : " + time + "]\u001B[0m\n";
        for (Node node : nodes) {
            result += node.toString() + "\n";
        }
        return result;
    }
}
