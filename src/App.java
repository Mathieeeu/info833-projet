public class App {

    public static DES des = new DES();
    public static void main(String[] args) throws Exception {

        System.out.println("\u001B[38;5;33mWelcome to the fellowship!\u001B[0m");

        // Etat initial
        Node node5 = new Node(123);
        des.addNode(node5);
        Node node6 = new Node(10010);
        des.addNode(node6);
        Node node7 = new Node(122);
        des.addNode(node7);
        Node node8 = new Node(998);
        des.addNode(node8);
        
        // Simulation
        des.join(node5);
        des.join(node6);
        des.join(node7);
        des.join(node8);
        // des.leave(node5); // TODO : en fait ce message là doit partir bien plus tard, après tous les join !!!
        des.startSimulation(6);
    }
}
