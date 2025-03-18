public class ResourceMessage extends Message{
    private Resource resource;
    private boolean center;
    private boolean forwardingResource;
    
    // Constructeur pour simplement envoyer une ressource (après un get)
    public ResourceMessage(Node source, Resource resource) {
        super(source);
        this.resource = resource;
        this.forwardingResource = true;
    }

    // Constructeur pour placer la ressource dans la DHT (avec le flag "center" pour savoir si le noeud est le PLUS proche de la ressource ou si c'est juste un voisin)
    public ResourceMessage(Node source, Resource resource, boolean center) {
        super(source);
        this.resource = resource;
        this.center = center;
        this.forwardingResource = false;
    }

    public Resource getResource() {
        return this.resource;
    }

    public boolean isCenter() {
        return this.center;
    }

    boolean isForwardingAResource() {
        return this.forwardingResource;
    }

    public String toString() {
        return "resourceMessage from " + this.getSource().getId();
    }

}