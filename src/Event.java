public class Event {
    private Node target;
    private int timeToDeliver;
    private Message message;

    public Event(Node target, int timeToDeliver, Message message) {
        this.target = target;
        this.timeToDeliver = timeToDeliver;
        this.message = message;
    }

    public int getTimeToDeliver() {
        return timeToDeliver;
    }

    public Node getTarget(){
        return this.target;
    }

    public Message getMessage(){
        return this.message;
    }

    public void decreaseTime(){
        this.timeToDeliver--;
    }
    
    @Override
    public String toString() {
        return "Event [time=" + timeToDeliver + ", target=" + target + ", message=" + message + "]";
    }
}
