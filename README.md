# Korištenje sustava Kubernetes za upravljanje aplikacijama na raspodijeljenim ugrađenim računalima

## Preduvjeti

Projekt je napravljen u Arch Linux operacijskom sustavu. Pakete koje je potrebno instalirati [make](https://www.gnu.org/software/make/), [openssh](https://wiki.archlinux.org/title/OpenSSH), [jdk17-openjdk](https://wiki.archlinux.org/title/java), [docker, docker-buildx](https://wiki.archlinux.org/title/docker), opcionalno intellij-idea-community-edition. Za početak je potrebno ažurirati cijeli sustav:
```
sudo pacman -Syu
```
Instalacija navedenih paketa:
```
sudo pacman -S make openssh jdk17-openjdk docker docker-buildx intellij-idea-community-edition
```
Za korištenje Docker naredbi potrebno je omogućiti i pokrenuti *dockerd* servis:
```
sudo systemctl enable docker
sudo systemctl start docker
```
Stanje servisa može se vidjeti naredbom ```systemctl status docker```. Potrebno je naglasiti da će se Docker slika pokretati na Raspberry Pi mikroračunalu što znači da sliku treba izgraditi za ARM64 arhitekturu na računalu x86_64 arhitekture, za tu svrhu se koristi plugin *buildx*. Radno okruženje IntelliJ može se pokrenuti naredbom ```idea```.

## Koncept projekta

Projekt se sastoji od tri JAR aplikacije *server.jar*, *client-server.jar* i *client.jar*. Aplikacije se pokreću u tzv. slojevima:

* gornji sloj je jedna jedina aplikacija *server.jar*
* srednji sloj je *N* aplikacija *client-server.jar*
* donji sloj je *M* aplikacija *client.jar*

Donji slojevi šalju podatke srednjem sloju, a srednji sloj prosljeđuje podatke gornjem sloju. Podatke na gornjem sloju moguće je vidjeti pomoću web preglednika adresirajući ```http://[IP adresa server.jar]:[port server.jar]```. Aplikacija server.jar i sve client.jar aplikacije pokreću se lokalno, što znači da je IP adresa server.jar aplikacije *localhost*. Port je zadan u ```config.sh``` datoteci ili ga korisnik može namjestiti pri pokretanju. U testiranju, svi slojevi se pokreću na lokalnom računalu. U stvarnom radu se srednji sloj odnosno aplikacije *client-server.jar* pokreće na Raspberry Pi mikroračunalima u Docker kontejnerima upravljani Kubernetes platformom.

## Struktura projekta

Struktura projekta je sljedeća:

- direktorij ```application/```
	- sadrži IntelliJ Java Spring izvorne kodove, Dockerfile i skripte za izgradnju Docker slike
	- direktorij ```layer-control/``` sadrži skripte za lansiranje skupine aplikacija
	- [više o aplikaciji](application)

- direktorij ```raspberry-pi-and-kubernetes/```
	- sadrži skriptu za pisanje slike na SD karticu
	- sadrži i Kubernetes konfiguracije u direktoriju ```kubernetes-configs```
	- [više o aplikaciji](raspberry-pi-and-kubernetes/)

- datoteka ```config.sh```
	- opisuje zadanu konfiguraciju za slojeve, ako se želi promjeniti zadana konfiguracija potrebno je urediti ovu datoteku

Navedene skripte se izvršavaju neizravno pozivajući ```make``` alat.

## Dostupni ciljevi

Korijen projekta sadrži *Makefile* datoteku gdje je uz pomoć ```make``` alata moguće pokrenuti sljedeće naredbe:

- ```make push-img```
	- zapiši sliku Raspberry Pi OS-a na SD karticu
- ```make test-layers```
	- pokreće sva tri sloja na lokalnom računalu, čita konfiguraciju iz ```config.sh``` datoteke
- ```make start-layers```
	- pokreće gornji sloj *server.jar* i donji sloj *client.jar*, korisnik sam daje informacije o konfiguraciji ili se čitaju podatci iz ```config.sh```
	- srednji sloj se pokreće na Raspberry Pi mikroračunalima i tim pokretanjem upravlja Kubernetes
- ```make kill-layers```
	- mogućnost ubijanja pokrenutih slojeva
- ```make build-docker-client-img```
	- izgradi Docker sliku za *client-server* aplikaciju

## Primjer projekta


