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
    }

    public void startSimulation() {
        while (events.size() > 0) {
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
}
