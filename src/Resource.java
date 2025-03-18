public class Resource {
    private int id;
    private String data;

    public Resource(int id, String data) {
        this.id = id;
        this.data = data;
    }

    public int getId() {
        return id;
    }

    public String getData() {
        return data;
    }

    public String toString() {
        return "Resource" + id + " : " + data;
    }
}    