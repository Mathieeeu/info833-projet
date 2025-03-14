public class Event {
    private Node target;
    private int time;
    private Message message;

    public Event(Node target, int time, Message message) {
        this.target = target;
        this.time = time;
        this.message = message;
    }

    public int getTime() {
        return time;
    }

    public Node getTarget(){
        return this.target;
    }

    public Message getMessage(){
        return this.message;
    }

    public void decreaseTime(){
        this.time--;
    }
    
    public String toString() {
        return "Event [time=" + time + ", target=" + target + ", message=" + message + "]";
    }
}
