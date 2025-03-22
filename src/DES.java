import java.util.ArrayList;
import java.util.Random;

// DES = Discrete-Event Simulation
public class DES {
    private int time;
    private ArrayList<Node> nodes;
    private ArrayList<Resource> resources;
    private ArrayList<Event> queue;

    private Random rand = new Random();

    public static final int DEFAULT_MIN_TIME_TO_DELIVER = 3;
    public static final int DEFAULT_MAX_TIME_TO_DELIVER = 5;

    private boolean endOfSimulation = false;

    public DES() {
        this.time = 0;
        this.nodes = new ArrayList<Node>();
        this.resources = new ArrayList<Resource>();
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

        while (!this.queue.isEmpty() && this.time < timeLimit || !allQueuesEmpty()) {
        // while (this.time < timeLimit) {
            for (int i = 0; i < this.queue.size(); i++) {
                Event event = this.queue.get(i);
                event.decreaseTime();
                if (event.getTimeToDeliver() == 0) {
                    switch (event.getMessage()) {
                        case JoinMessage joinMessage -> {
                            System.out.println("\u001B[38;5;150m[EVENT] " + event.getMessage().getClass().getSimpleName() + " delivered to " + event.getTarget().toString() + " from " + joinMessage.getIdNodeToInsert() + "\u001B[0m");
                        }
                        case InsertMessage insertMessage -> {
                            System.out.println("\u001B[38;5;150m[EVENT] " + event.getMessage().getClass().getSimpleName() + " delivered to " + event.getTarget().toString() + " from " + insertMessage.getSource().getId() + "\u001B[0m");
                        }
                        case LeaveMessage leaveMessage ->  {
                            if (leaveMessage.getSource() == null) {
                                System.out.println("\u001B[38;5;150m[EVENT] " + event.getMessage().getClass().getSimpleName() + " delivered to " + event.getTarget().toString() + "\u001B[0m");
                                leaveMessage.getNode().leave();
                            }
                        }
                        case PutMessage putMessage ->{
                            System.out.println("\u001B[38;5;150m[EVENT] " + event.getMessage().getClass().getSimpleName() + " delivered to " + event.getTarget().toString() + "\u001B[0m");
                        }
                        default -> System.out.println("\u001B[38;5;150m[EVENT] " + event.getMessage().getClass().getSimpleName() + " delivered to " + event.getTarget().toString() + "\u001B[0m");
                    }
                    event.getTarget().deliver(event.getMessage());
                    this.queue.remove(i);
                    i--;
                }
            }
            if (this.queue.isEmpty() && !this.endOfSimulation) {
                this.endOfSimulation = true;
                System.out.println("\u001B[38;5;150m[INFO] Events queue is empty\u001B[0m");
            }
            this.time++;
            System.out.println("\n" + this);
        }
    }

    public void addNode(Node node) {
        this.nodes.add(node);
    }
    
    public void addResource(Resource resource) {
        this.resources.add(resource);
    }

    public void addEvent(Event event) {
        this.queue.add(event);
    }

    public void removeNode(Node node) {
        this.nodes.remove(node);
    }

    public void removeResource(Resource resource) {
        this.resources.remove(resource);
    }

    public void removeEvent(Event event) {
        this.queue.remove(event);
    }

    public void join(Node node, int timeToDeliver) {
        Node nodeToVisit = this.nodes.get(0); // Peut etre à changer parce que c'est un peu de la triche de prendre un noeud comme ça
        this.deliver(nodeToVisit, new JoinMessage(null, node, node.getId()), timeToDeliver); // TODO : on met null ou node en source ???
    }

    public void leave(Node node, int timeToDeliver) {
        this.deliver(node, new LeaveMessage(null, node, null), timeToDeliver);
    }

    public void put(Resource resource, int timeToDeliver) {
        System.out.println("\u001B[38;5;200m[MESSAGE] " + resource.getData() + " wants to be storaged" + "\u001B[0m");
        Node nodeToVisit = this.nodes.get(0);
        this.addEvent(new Event(nodeToVisit, rand.nextInt(DEFAULT_MAX_TIME_TO_DELIVER) + 1 + timeToDeliver, new PutMessage(null, resource)));
    }

    public void get(Node requestingNode, int id, int timeToDeliver) {
        // TODO : requestingNode cherche la ressource parmi ses voisins (dans un sens qui est logique par contre, genre si on cherche un id plus faible que le node (noeud courant) on va à gauche, sinon à droite)
        System.out.println("\u001B[38;5;200m[MESSAGE] " + requestingNode.getId() + " wants to get resource " + id + "\u001B[0m");
        this.addEvent(new Event(requestingNode, rand.nextInt(DEFAULT_MAX_TIME_TO_DELIVER) + 1 + timeToDeliver, new GetMessage(requestingNode, requestingNode, id)));
    }

    public void deliver(Node target, Message message) {
        deliver(target, message, 0);
    }

    public void deliver(Node target, Message message, int timeToDeliver) {
        switch (message) {
            case JoinMessage joinMessage -> {
                System.out.println("\u001B[38;5;200m[MESSAGE] " + message.getClass().getSimpleName() + " sent to " + target.toString() + " from " + joinMessage.getIdNodeToInsert() + "\u001B[0m");
            }
            case InsertMessage insertMessage -> {
                System.out.println("\u001B[38;5;200m[MESSAGE] " + message.getClass().getSimpleName() + " sent to " + target.toString() + " from " + insertMessage.getSource().getId() + "\u001B[0m");
            }
            default -> System.out.println("\u001B[38;5;200m[MESSAGE] " + message.getClass().getSimpleName() + " sent to " + target.toString() + "\u001B[0m");
        }
        this.addEvent(new Event(target, rand.nextInt(DEFAULT_MAX_TIME_TO_DELIVER) + 1 + timeToDeliver, message));
    }
    
    public void deliver(Node target, int minTimeToDeliver, int maxTimeToDeliver, Message message) {
        
        switch (message) {
            case JoinMessage joinMessage -> {
                System.out.println("\u001B[38;5;200m[MESSAGE] " + message.getClass().getSimpleName() + " sent to " + target.toString() + " from " + joinMessage.getIdNodeToInsert() + "\u001B[0m");
            }
            case InsertMessage insertMessage -> {
                System.out.println("\u001B[38;5;200m[MESSAGE] " + message.getClass().getSimpleName() + " sent to " + target.toString() + " from " + insertMessage.getSource().getId() + "\u001B[0m");
            }
            default -> System.out.println("\u001B[38;5;200m[MESSAGE] " + message.getClass().getSimpleName() + " sent to " + target.toString() + "\u001B[0m");
        }
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

    private boolean allQueuesEmpty() {
        for (Node node : this.nodes) {
            if (!node.getQueue().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public void displayDES() {
        System.out.println("Final state of the network :");
        ArrayList<Node> nodesToVisit = new ArrayList<Node>();
        for (Node node : this.nodes) {
            nodesToVisit.add(node);
        }
        Node currentNode = nodesToVisit.get(0);
        
        while(!nodesToVisit.isEmpty()) {
            System.out.print("\u001B[38;5;33m" + currentNode.getId() + "\u001B[38;5;150m" + currentNode.printResources() + "\u001B[0m -> ");
            nodesToVisit.remove(currentNode);
            currentNode = currentNode.getRight();
            if (!nodesToVisit.contains(currentNode) && !nodesToVisit.isEmpty()) {
                System.out.print("\n");
                currentNode = nodesToVisit.get(0);
            }
        }
    }
}
