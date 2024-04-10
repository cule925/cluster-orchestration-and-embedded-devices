# Pokretanje mreže lokalno

Tri su sloja komunikacije u ovom sustavu. Na vrhu je server, u sredini su klijent-serveri, a na dnu su klijenti. Svaka od sljedećih skripti pokreće jedan sloj:

```
start_server_layer.sh
```
- skripta koji pokreće gornji sloj: 1 instancu server.jar aplikacije, zadana vrijednost porta na kojem prisluškuje je 8080 

```
start_client_server_layer.sh
```
- skripta koji pokreće srednji sloj: 3 instance client-server.jar aplikacija, zadane vrijednosti portova na kojem prisluškuju su 8081, 8082 i 8083
- ovaj sloj će zapravo pokretati Kubernetes kada u ne-testnom slučaju

```
start_client_layer.sh
```
- skripta koji pokreće donji sloj: 12 instanci client.jar aplikacija

Sve se aplikacije pokreću lokalno, IP adresa je loopback (localhost).
