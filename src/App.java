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
        Node node9 = new Node(555);
        des.addNode(node9);
        
        // Simulation
        des.join(node5, 0);
        des.join(node6, 0);
        des.join(node7, 1);
        des.join(node8, 0);
        des.leave(node5, 45); // TODO : en fait ce message là doit partir bien plus tard, après tous les join !!!
        des.join(node9, 50);
        des.leave(node6, 100);
        des.startSimulation(500);

        // Etat final
        des.displayDES();
    }
}
