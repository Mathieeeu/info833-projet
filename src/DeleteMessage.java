public class DeleteMessage extends Message {
    
    private int idRessourceToDelete;
    
    public DeleteMessage(Node source, int idRessourceToDelete) {
        super(source);
        this.idRessourceToDelete = idRessourceToDelete;
    }

    public int getIdRessourceToDelete() {
        return this.idRessourceToDelete;
    }

    public String toString() {
        return "deleteMessage from " + this.getSource().getId() + " (nodeToDelete : " + this.idRessourceToDelete + ")";
    }
}