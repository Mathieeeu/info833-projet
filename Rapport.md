# INFO833 - Rapport de Project DHT

_Charlotte - Louna - Mathieu_

---

## État initial

Pour ce projet, nous avons décidé de créer la DHT par nous-même sans utiliser de bibliothèques déjà existantes. Nous avons fait le choix de coder en Java pour ce projet. Pour ce faire nous avons décidé d'initialiser la DHT avec quatre noeuds qui sont déjà bien reliés entre eux, afin d'éviter de devoir gérer les premières connexions. Nous avons également choisi de créer et d'ajouter tous les noeuds, ressources et évènements qui se produisent au cours de la simulation dès le début, en précisant à chaque fois le moment où ils s'éxecuteront. Cela nous permet de décider précisément le moment où chaque événement se produit et de pouvoir ainsi les gérer de manière plus précise. Tout cela est fait dans la classe `App` qui est notre classe principale.

## Structure du projet

### Les noeuds 

Tous les noeuds sont créés à partir de la classe `Node`. Ainsi, ils connaissent leurs voisins (à gauche et à droite) et peuvent communiquer entre eux. Ils ont également une liste de ressources qu'ils possèdent, un attribut `locked` qui permet de verrouiller le noeud quand un message est en cours de traitement et une liste d'évenements reçus pendant qu'il était bloqué et qu'il devra traiter plus tard (la `queue`).

Les noeuds ont la possibilité de rejoindre ou de quitter la `DHT` grace aux méthodes `join` et `leave`. Celles-ci envoient des messages aux noeuds présents dans la DHT. 

### Les messages

Les messages sont créés à partir de la classe abstraite `Message`. Par défaut, un message est initialisé avec seulement une `source` et les attributs optionnels `path` et `forward` qui permettent de différencier un message envoyé directement d'un message qui a été transféré par un autre noeud.

L'envoi d'un message se fait par la méthode `deliver` de `Node`. Cette méthode vérifie si le noeud est verrouillé, si c'est le cas, le message est ajouté à la `queue` du noeud. Sinon, le message est traité directement.

Pour une meilleure gestion des messages, nous avons créé des protocoles de communication utilisant des types de messages différents. Ainsi, nous avons les classes `JoinMessage`, `InsertMessage`, `LeaveMessage`, `AckMessage`, `PutMessage`, `GetMessage`, `RessourceMessage` et `DeleteMessage` qui héritent toutes de la classe `Message`. La méthode `deliver` agit différemment selon le type de message. Chaque type de message a des attributs spécifiques qui permettent de transmettre des informations supplémentaires. Le tableau suivant résume les attributs de chaque message :

| Message | Attributs | Description | Fonctionnement |
| --- | --- | --- | --- |
| `JoinMessage` | `Node nodeToInsert`<br>`int idNodeToInsert` | Permet à un noeud de rejoindre la DHT | Le noeud compare son id avec celui du noeud à insérer et transfère le message au noeud suivant jusqu'à trouver le bon emplacement du noeud à insérer. |
| `InsertMessage` | `Node left`<br>`Node right` | Permet de mettre à jour les voisins d'un noeud | Une fois que le noeud a trouvé sa place, il envoie un message à ses nouveaux voisins pour qu'ils se mettent à jour. Les ressources sont redistribuées pour maintenir le bon degré de réplication. |
| `LeaveMessage` | `Node node`<br>`String nodeSide` | Permet à un noeud de quitter (proprement) la DHT | Le noeud communique à ses voisins qu'il va partir et leur indique qui sera le nouveau voisin (et de quel côté). Les voisins envoient un `AckMessage` pour confirmer qu'ils ont bien effectué les changements. Une fois que le noeud a reçu les deux `AckMessage`, il redistribue ses ressources et part. |
| `AckMessage` | `String type` | Permet d'envoyer une confirmation | Le noeud envoie un `AckMessage` pour confirmer qu'il a bien effectué les opérations demandées. Le type permet de savoir à quel message il répond. |
| `PutMessage` | `ressource` | Permet d'ajouter une ressource à la DHT | Le noeud compare l'id de la ressource avec le sien et celui de ses voisins pour placer la ressource correctement. Si la position est trouvée, il envoie un `RessourceMessage` à ses voisins pour qu'ils ajoutent également la ressource. |
| `GetMessage` | `Node requestingNode`<br>`int idRessource` | Permet de récupérer une ressource | Le noeud compare l'id de la ressource avec le sien et celui de ses voisins jusqu'à trouver la ressource. Dès qu'elle est trouvée, il envoie un `RessourceMessage` au noeud demandeur. |
| `RessourceMessage` | `Ressource ressource`<br>`boolean center`<br>`boolean forwardingResource` | Permet de transmettre une ressource | ce message a deux fonctions : soit il transmet une ressource à un noeud demandeur (`forwardingResource = true`), soit il informe les voisins que sa source est le noeud le plus proche de la ressource (`center = true`) et qu'ils doivent ajouter une ressource à leur liste. |
| `DeleteMessage` | `int idRsourceToDelete` | Permet de supprimer une ressource | La ressource est supprimée de la liste du noeud destinataire s'il la possède. Ce message est utilisé lorsqu'un noeud rejoint (ou quitte) la DHT lors de la redistribution des ressources (pour maintenir le bon degré de réplication). |

### Les ressources

Une ressource est identifiée par son `id` et contient des `data` (une chaine de caractère). Elle peut être ajoutée à la DHT via la méthode _put_. Cette méthode assure un degré de réplication de 3 avec comme centre le noeud dont l'id est le plus proche de celui de la ressource. Cela permet une plus grande tolérance aux erreurs et évite de perdre une ressource en cas de crash. Un noeud peut également demander à obtenir une ressource avec la méthode _get_.

## Fonctionnalités de la DHT

Nous avons réalisé les étapes 1, 2 et 3 du projet.

### Evènements et simulation

La simulation est "gérée" majoritairement par la classe `DES`. Cette classe contient une liste de tous les noeuds créés (qu'ils soient présent ou non dans la DHT), la liste de toutes les ressources ainsi qu'une liste d'évènements. Un évènement possède un message, une cible à qui délivrer le message et un délai d'exécution. Le message sera délivré une fois son délai d'exécution à 0. 
A chaque tick d'horloge, la classe parcourt toute la liste des évènements et regarde si leur temps d'exécution est venu. Si c'est le cas, elle les sort de la liste et les exécute à l'aide de la méthode _deliver_ sinon elle diminue leur délai de 1. Une fois toute la liste parcourue, le temps de la simulation avance de 1. Quand la liste d'évènement est vide, la classe regarde si des messages sont en queue sur des noeuds et si ce n'est pas le cas, la simulation s'arrête.
Pour ajouter un côté aléatoire à notre simulation, la méthode _deliver_ possède 2 arguments : `minTimeToDeliver` et `maxTimeToDeliver`. Ils servent à donner une fourchette dans laquelle le message sera envoyé permettant de changer le scénario à chaque fois et donc de se rapprocher un peu plus d'un scénario réaliste.

### Ajout d'un noeud

Pour pouvoir ajouter un noeud à la DHT, on crée un `JoinMessage` qui est stocké dans la liste des évènements jusqu'à ce que son temps d'exécution arrive. A ce moment là, on transfère le message au premier noeud de la DHT. Le noeud compare alors son id avec celui du noeud à inserer :
- Si l'id est supérieur au sien et inférieur à celui de son voisin de droite, le noeud se verouille et transfère le message à son voisin de droite
- Si le noeud possède un id supérieur à l'id du noeud à insérer, il se bloque et envoie un `InsertMessage` au noeud en lui précisant ses voisins
- Si l'id du noeud courant est inférieur à celui du noeud à insérer et supérieur à celui de son voisin de gauche (insertion entre le premier et le dernier noeud de la DHT), alors le noeud courant se bloque et envoie un `InsertMessage` au noeud voulant rejoindre la communauté de l'anneau en lui précisant ses voisins
- Si aucun de ces cas n'est validé, on transfère le message au noeud suivant (le voisin de doite) dans la DHT

Quand le noeud à insérer reçoit le `InsertMessage`, il met à jour ses voisins et leur envoie un autre `InsertMessage` en leur précisant qu'il est leur voisin de gauche (resp de droite). Les noeuds mettent alors à jour leur voisin en gardant en mémoire leur ancien voisin en cas de problème et redistribue leur ressource correctement pour maintenir le bon degré de réplication et la "chaîne". Pour cela, il compare l'id du nouveau noeud avec l'id des ressources, s'il est plus grand (pour le voisin de gauche) ou plus petit (pour le voisin de droite), il informe le nouveau noeud qu'il doit ajouter cette ressource à sa liste de ressources. Il envoie également un `DeleteMessage` à son ancien voisin de droite/gauche afin qu'il supprime cette ressource pour garder un degré de réplication de 3. Si le noeud possède la ressource, il la supprime sinon il renvoie le `DeleteMessage` au noeud source pour qu'il supprime la ressource. Ce dernier cas correspond à l'insertion du nouveau noeud en bout de chaine de replication.
Une fois tout cela fait, les noeuds concernés par l'insertion du nouveau noeud se déverouille.

### Suppression d'un noeud

Lorsqu'un noeud veut quitter la DHT, il se bloque et envoie un message à ses deux voisins. Le message contient le noeud qui deviendra le nouveau voisin ainsi qu'une chaîne de caractère indiquant le côté du noeud qui va changer (droite ou gauche). Quand un noeud reçoit un `LeaveMessage`, il met à jour ses voisins selon l'indication puis il envoie un `AckMessage` au noeud qui part pour lui signifier qu'il a bien effectué les changements.

Dès que la queue du noeud contient deux `AckMessage` (celui du voisin de droite et celui de gauche), le noeud répartit ses ressources à ses voisins. Pour ce faire, il parcourt la liste de ses ressources et pour chaque il envoie à son voisin avec l'id le plus faible (celui de gauche) un message pour placer la ressource puis il supprime cette dernière. Le fait que ce soit le noeud avec le plus petit id qui s'en occupe garantit que la ressource soit placée correctement (dans notre configuration). Une fois la liste des ressources vide, le noeud passe ses voisins à _null_, se déverrouille et vérifie qu'il n'a pas de message en attente (il les livre s'il en a) puis il part de la DHT.

### Ajout d'une ressource

Lorsqu'une ressource veut être stockée dans la DHT, on crée un évènement qui va être stocké dans la liste des évenements du simulateur. Quand arrive son temps d'éxecution, il délivre un `PutMessage` au premier noeud de la DHT. Le noeud qui reçoit ce message va alors comparer l'id de la ressource avec : 
- son propre id, s'ils sont égaux, il ajoute la ressource à sa liste de ressources (si elle ne la contient pas déjà) et envoie un `RessourceMessage` à ses voisins pour qu'ils ajoutent également la ressource. Nous avons un degré de réplication de 3, donc chaque ressource doit être présente sur trois noeuds différents.
- l'id de son voisin de droite. S'il est compris entre l'id du noeud et celui de son voisin de droite, on calcule pour savoir quel noeud est le plus près pour qu'il puisse l'ajouter à sa liste de ressources et en informer ses voisins.
- l'id de son voisin de gauche (surtout dans le cas du premier noeud de l'anneau) afin d'éviter de faire tout le tour de la DHT alors qu'on pouvait placer la ressource beaucoup plus rapidement. Si l'id de la ressource est supérieure à l'id du noeud gauche et l'id du noeud courant, on ajoute la ressource sur le noeud gauche qui informe ses voisins.

Dans le cas où l'id de la ressource est plus grand que celui du voisin de droite, on transfère le `PutMessage` au voisin de droite du noeud courant.

Le `RessourceMessage` contient un paramètre `center` qui permet d'indiquer au noeud qui reçoit le message s'il est le plus proche de la position de la ressource (dans ce cas `center = true`) et qu'il doit envoyer un `RessourceMessage` à chacun de ses voisins pour les informer qu'ils doivent ajouter la ressource. Si l'attribut `center` est à `false`, le noeud ajoute simplement la ressource à sa liste.


### Récupération d'une ressource

Si un noeud souhaite récupérer une ressource, on crée un évènement que l'on stocke jusqu'à ce que son temps d'activation arrive. Dès lors, on transmet un `GetMessage` au premier noeud de la DHT. Le noeud regarde alors s'il a la ressource. Si c'est le cas, il envoie un `RessourceMessage` (contenant la ressource) au noeud demandeur. Si ce n'est pas le cas, il compare l'id de la ressource souhaité à celui du noeud courant. S'il est supérieur, on transmet le `GetMessage` au voisin de droite et inversement.

## Améliorations possibles

### Protection contre les crashs et réparation de la DHT

À ce jour, notre DHT ne gère pas les crashs de noeuds. Si un noeud crash et quitte la DHT de manière non-contrôlée, la DHT ne sera plus cohérente. Pour palier ce problème, nous avons ajouté stocké dans chaque noeud ses anciens voisins (`old_left` et `old_right`) pour pouvoir restaurer un lien si l'un des voisins du noeud ne répond plus. Ainsi, si un noeud ne reçoit pas de réponse d'un de ses voisins, il envoie un message à son ancien voisin pour essayer de rétablir la connexion. 

### Routage amélioré

Le routage de proche en proche fonctionne bien dans notre DHT, cependant il peut être amélioré. En effet, actuellement, le transfert de la plupart des messages se fait de la gauche vers la droite par soucis de simplicité. Cependant, il serait intéressant de mettre en place un routage plus intelligent qui permettrait de réduire le nombre de sauts nécessaires pour atteindre un noeud :

- Soit en déterminant la direction à prendre en fonction de l'id du noeud destinataire (sens horaire ou anti-horaire).
- Soit en créant des raccourcis entre certains noeuds stratégiques (liens longs).

## Conclusion

Ce projet a été très intéressant à réaliser. Il nous a permi de bien comprendre le fonctionnement d'une DHT et de voir l'entendu des possibilités qui en découle. En effet commme dit précédemment, nous pourrions ajouter encore beaucoup de fonctionnalités aux projets et en améliorer des existantes sans avoir l'impression de tourner en rond.
Nous sommes tous content de ne pas avoir utilisé peersim ou des bibliothèques pré-existantes. Cela nous a permi de comprendre de A à Z ce que nous faisions et comment cela marchait, ce qui n'est pas toujours le cas avec de fonctionnalités déjà codées.

