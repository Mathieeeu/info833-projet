public class App {
    public static void main(String[] args) throws Exception {
        System.out.println("\u001B[38;5;198mHello, World!\u001B[0m");

        // Etat initial
        DES des = new DES();
        Node node5 = new Node(123);
        des.addNode(node5);
        Node node6 = new Node(10010);
        des.addNode(node6);
        Node node7 = new Node(122);
        des.addNode(node7);
        Node node8 = new Node(998);
        des.addNode(node8);
        System.out.println(des);
        
        // Simulation
        des.join(node5);
        des.join(node6);
        des.join(node7);
        des.join(node8);
        des.leave(node5);
        System.out.println(des);
    }
}
