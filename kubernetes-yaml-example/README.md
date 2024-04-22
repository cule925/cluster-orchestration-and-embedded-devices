# Primjer Kubernetes YAML konfiguracijskih datoteka

Tri su YAML konfiguracijske datoteke i jedan direktorij:

- ```kubernetes-daemonset-v1.yaml```
	- stvara, ažurira ili briše objekt tipa DaemonSet koji na svakom čvoru stvara jedan Kubernetes kontejner omotavajući Docker kontejner ```cule925/client-server-arm64:v1```

- ```kubernetes-daemonset-v1.yaml```
	- stvara, ažurira ili briše objekt tipa DaemonSet koji na svakom čvoru stvara jedan Kubernetes kontejner omotavajući Docker kontejner ```cule925/client-server-arm64:v2```
	- ova konfiguracija služi kao primjer automatskog ažuriranja kontejnera odnosno što bi se dogodilo kad bismo proslijedili konfiguraciju v1 pa v2 alatom *kubectl*

- ```kubernetes-service-nodeport.yaml```
	- stvara, ažurira ili briše objekt tipa Service podtipa NodePort koji svaki Kubernetes kontejner u ```kubernetes-daemonset-v1.yaml``` i ```kubernetes-daemonset-v2.yaml``` konfiguraciji izlaže vanjskom svijetu

- ```dashboard-config```
	- konfiguracija za Kubernetes dashboard web grafičko sučelje

## Prijenos na upravljački čvor

Datoteke i direktorij se prenose na upravljački čvor pomoću SSH protokola naredbom:

```
scp -r kubernetes-* dashboard-config/ user@192.168.7.24:~/
```

## Prosljeđivanje konfiguracije za aplikaciju

Za početak je potrebno ulogirati se na upravljački čvor pomoću SSH protokola naredbom:

```
ssh user@[IP adresa upravljačkog čvora]
```

Zatim je potrebno proslijediti konfiguraciju koja stvara objekt tipa Service kako bi se Kubernetes kontejnerima moglo pristupiti iz vanjskog svijeta:

```
k3s kubectl apply -f kubernetes-service-nodeport.yaml
```

Nakon toga je potrebno proslijediti konfiguraciju koja će stvoriti Kubernetes kontejnere prve slike:

```
k3s kubectl apply -f kubernetes-daemonset-v1.yaml
```

Stanje kontejnera može se vidjeti naredbom ```k3s kubectl get pods```, stanje čvorova naredbom ```k3s kubectl get nodes```, a stanje servisa naredbom ```k3s kubectl get service```. Detaljan opis Kubernetes kontejnera može se dobiti naredbom ```k3s kubectl describe pods```. Ako se žele ažurirati kontejneri na novu verziju potrebno je proslijediti konfiguraciju druge slike:

```
k3s kubectl apply -f kubernetes-daemonset-v2.yaml
```

Brisanje konfiguracija odnosno brisanje DaemonSet objekta može se napraviti naredbom ```k3s kubectl delete -f kubernetes-daemonset-v1.yaml``` ili ```k3s kubectl delete -f kubernetes-daemonset-v2.yaml``` jer se obe konfiguracije referiraju na istu instancu objekta. Objekt tipa Service se može obrisati naredbom ```k3s kubectl delete -f kubernetes-service-nodeport.yaml```.

