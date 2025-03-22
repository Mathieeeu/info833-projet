# INFO833 - Rapport de Project DHT

_Charlotte - Louna - Mathieu_

---

## État initial

Pour ce projet, nous avons décidé de créer l'entièrté de la DHT par nous-même et de ne pas utiliser de bibliothèques déjà existantes. Nous avons fait le choix de coder en Java pour ce projet. Pour ce faire nous avons décidé d'initialiser la DHT avec quatre noeuds qui sont déjà bien reliés entre eux, afin d'éviter de devoir gérer les premières connexions. Nous avons également choisi de créer et d'ajouter tous les noeuds, ressources et évènements qui se produisent au cours de la simulation dès le début, en précisant à chaque fois le moment où ils s'éxecuteront. Cela nous permet de décider précisément le moment où chaque événement se produit et de pouvoir ainsi les gérer de manière plus précise. Tout cela est fait dans la classe `App` qui est notre classe principale.

## Structure du projet

### Les noeuds 

Tous les noeuds sont créés à partir de la classe `Node`. Ainsi, ils connaissent leurs voisins (à gauche et à droite) et peuvent communiquer entre eux. Ils ont également une liste de ressources qu'ils possèdent, un attribut `locked` qui permet de verrouiller le noeud quand un message est en cours de traitement et une liste d'évenements reçus pendant qu'il était bloqué et qu'il devra traiter plus tard (la `queue`).

Les noeuds ont la possibilité de rejoindre ou de quitter la `DHT` grace aux méthodes `join` et `leave`. Celles-ci envoient des messages aux noeuds présents dans la DHT 

### Les messages

(détailler le fonctionnement des msg)

### Les ressources

(détailler le fonctionnement des ressources)


## Fonctionnalités de la DHT

Nous avons réalisé les étapes 1, 2 et 3 du projet.

### Envoi d'un message

(parler des délais aléatoires)

### Ajout d'un noeud

(parler de "comment maintenir le degré de réplication de chaque donnée" parce qu'on l'a fait et c'est assez avancé je crois)


### Suppression d'un noeud

(parler de "comment maintenir le degré de réplication de chaque donnée" parce qu'on l'a fait et c'est assez avancé je crois)

Lorsqu'un noeud veut quitter la DHT, il se bloque et envoie un message à ses deux voisins. Le message contient le noeud qui deviendra le nouveau voisin ainsi qu'une chaîne de caractère indiquant le côté du noeud qui va changer (droite ou gauche). Quand un noeud reçoit un `LeaveMessage`, il met à jour ses voisins selon l'indication puis il envoie un `AckMessage` au noeud qui part pour lui signifier qu'il a bien effectué les changements.
Dès que la queue du noeud contient 2 `AckMessage` (celui du voisin de droite et celui de gauche), le noeud répartit ses ressources à ses voisins. Pour ce faire, il parcourt la liste de ses ressources et pour chaque il envoie à son voisin avec l'id le plus faible (celui de gauche) un message pour placer la ressource puis il supprime cette dernière. Le fait que ce soit le noeud avec le plus petit id qui s'en occupe garantie que la ressource soit placée correctement. Une fois la liste des ressources vides, il initialise ses voisins à _null_, se déverrouille et il vérifie qu'il n'a pas de message en attende (il les livre s'il en a) puis il part.

### Ajout d'une ressource

Lorsqu'une ressource veut être stockée dans la DHT, on crée un évènement qui va se stocker dans la liste des events. Quand arrive son temps d'éxecution, il délivre un `PutMessage` au premier noeud de la DHT. Le noeud qui reçoit ce message va alors comparer l'id de la ressource avec 
- son id. S'ils sont égaux, il ajoute la ressource à sa liste de ressources (si elle ne la contient pas déjà) et envoie un `RessourceMessage` à ses voisins pour qu'ils ajoutent également la ressource. Nous avons un degré de réplication de 3.
- l'id de son voisin de droite. S'il est compris entre l'id du noeud et celui de son voisin de droite, on calcule pour savoir quel noeud est le plus près pour qu'il puisse l'ajouter à sa liste de ressources et en informer ses voisins.
- l'id de son voisin de gauche (surtout dans le cas du premier noeud de l'anneau) afin d'éviter de faire tout le tour de la DHT alors qu'on pouvait placer la ressource beaucoup plus rapidement. Si l'id de la ressource est supérieure à l'id du noeud gauche et l'id du noeud courant, on ajoute la ressource sur le noeud gauche qui informe ses voisins.

Si l'id de la ressource est plus grand que celui du voisin de droite, on transfère le `PutMessage` au voisin de droite du noeud courant.

Le `RessourceMessage` contient un paramètre _center_ qui permet d'indiquer au noeud qui reçoit le message s'il est le plus proche de la position de la ressource (dans ce cas _center = true_) et qu'il doit envoyer un `RessourceMessage` à chacun de ses voisins pour les informer qu'ils doivent ajouter la ressource. Si _center_ est à _false_, le noeud ajoute juste la ressource à sa liste.


### Récupération d'une ressource

Si un noeud souhaite récupérer une ressource, on crée un évènement que l'on stocke jusqu'à ce que son temps d'activation arrive. Dès lors, on transmet un `GetMessage` au premier noeud de la DHT. Le noeud regarde alors s'il a la ressource. Si c'est le cas, il envoie un `RessourceMessage` (contenant la ressource) au noeud demandeur. Si ce n'est pas le cas, il compare l'id de la ressource souhaité à celui du noeud courant. S'il est supérieur, on transmet le `GetMessage` au voisin de droite et inversement.


## Améliorations possibles

### Protection contre les crashs et réparation de la DHT

À ce jour, notre DHT ne gère pas les crashs de noeuds. Si un noeud crash et quitte la DHT de manière non-contrôlée, la DHT ne sera plus cohérente. Pour palier ce problème, nous avons ajouté stocké dans chaque noeud ses anciens voisins (`old_left` et `old_right`) pour pouvoir restaurer un lien si l'un des voisins du noeud ne répond plus. Ainsi, si un noeud ne reçoit pas de réponse d'un de ses voisins, il envoie un message à son ancien voisin pour essayer de rétablir la connexion. 

### Routage amélioré

Le routage de proche en proche fonctionne bien dans notre DHT, cependant il peut être amélioré. En effet, actuellement, le transfert de la plupart des messages se fait de la gauche vers la droite par soucis de simplicité. Cependant, il serait intéressant de mettre en place un routage plus intelligent qui permettrait de réduire le nombre de sauts nécessaires pour atteindre un noeud :

- Soit en déterminant la direction à prendre en fonction de l'id du noeud destinataire (sens horaire ou anti-horaire).
- Soit en créant des raccourcis entre certains noeuds stratégiques.

## Conclusion