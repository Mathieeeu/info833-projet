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
        Resource resource1 = new Resource(12, "Resource1");
        des.addResource(resource1);
        Resource resource2 = new Resource(130, "Resource2");
        des.addResource(resource2);
        Resource resource3 = new Resource(410, "Resource3");
        des.addResource(resource3);
        Resource resource4 = new Resource(876, "Resource4");
        des.addResource(resource4);
        
        // Scénario de la simulation
        des.join(node5, 0);
        des.join(node6, 0);
        des.join(node7, 1);
        des.join(node8, 0);
        des.leave(node5, 45); // ce message là doit partir bien plus tard, après tous les join !!!
        des.join(node9, 50);
        des.put(resource1, 60);
        des.put(resource2, 63);
        des.put(resource3, 70);
        des.put(resource4, 59);
        des.get(node6, 130, 80); // TODO : Si la ressource n'est pas dans la DHT, ça ne plante pas mais ça tourne en boucle, il faudrait un moyen de savoir si on a déjà visité un noeud ou pas et de dire "ressource indisponible" au noeud qui l'a demandée si besoin
        des.get(node7, 876, 90);
        //des.leave(node7, 120);
        
        // Etat final
        des.startSimulation(500);
        des.displayDES();
    }
}
