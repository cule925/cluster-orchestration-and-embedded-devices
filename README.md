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

Uzmimo tri Raspberry Pi mikroračunala: dva Raspberry Pi Model 4B mikroračunala (2 GB i 8 GB inačice) i jedan Raspberry Pi 3 Model B i instalirajmo Raspberry Pi OS Lite na sva tri mikroračunala. Za svaki Raspberry Pi potrebno je na njegovu odgovarajuću SD karticu zapisati sliku Raspberry Pi OS Lite operacijskog sustava. Dakle potrebno je uzeti čitač mikro SD kartice, ubaciti mikro SD karticu u čitač i spojiti čitač na računalo i u terminalu izvršiti naredbu (**oprez kod odabira datoteke uređaja**):
```
make push-img
```
Za inicijalno postavljanje Raspberry Pi OS Lite operacijskog sustava bit će potrebna jedna USB tipkovnica i jedan monitor koji ima potporu za HDMI. Nakon pisanja slike na sve tri kartice za svaki Raspberry Pi i ubacivanje istih kartica u utore istih potrebno je konfigurirati raspored tipkovnice (hrvatski ili engleski), korisničko ime, zaporku, ime mikroračunala, po mogućnosti spojiti se na bežičnu mrežu, ažurirati repozitorije i sustav te omogućiti SSH servis. Za ovaj primjer je preporučeno koristiti korisničko ime ```user``` i zaporku ```password```, a ime mikroračunala neka bude za Raspberry Pi 3 Model B 'control-node', za prvi Raspberry Pi Model 4B 'worker-node-1', a za drugi Raspberry Pi Model 4B 'worker-node-2'. Dakle, nakon namještanja rasporeda tipkovnice, korisničkog imena i zaporke potrebno je ulogirati se i izvršiti niz naredbi:
```
sudo systemctl enable NetworkManager					# Omogući rad mrežnih servisa
sudo systemctl start NetworkManager					# Započni rad mrežnih servisa
sudo systemctl enable ssh						# Omogući rad SSH servisa
sudo systemctl start ssh						# Započni rad SSH servisa
nmcli dev wifi list							# Izlistaj dostupne mreže
nmcli dev wifi connect [ime mreže] password [zaporka mreže]		# Spoji se na jednu od dostupnih mreža
sudo apt update								# Ažuriraj popis paketa
sudo apt full-upgrade -y						# Ažuriraj sve pakete
sudo raspi-config							# Namjesti postavke Raspberry Pi-a (1->S4, 5->L1, 5->L2, 5->L3)
sudo dpkg-reconfigure console-setup					# Promjena fonta tipkovnice
sudo reboot								# Ponovno pokreni Raspberry Pi
```
Nakon ponovnog pokretanja potrebno je ulogirati se i provjeriti rade li servisi za SSH povezanost i upravljanje mrežom (```systemctl status [ime servisa]```).

### Spajanje s radnog računala na mikroračunala pomoću SSH protokola

Na radnom računalu korišten je Arch Linux operacijski sustav. Preduvjet je da su sva Raspberry Pi mikroračunala i računalo s Arch Linux operacijskim sustavom spojeno na istu lokalnu mrežu. Za početak je potrebno generirati jedan par kriptografskih asimetričnih ključeva u terminalu naredbom:
```
ssh-keygen -t ed25519
```
Zatim je potrebno otvoriti još tri terminala namijenjena za svaki Raspberry Pi i prenijeti prethodno generirani javni ključ na Raspberry Pi mikroračunala:
```
ssh-copy-id user@[IP adresa Raspberry Pi-a]
```
Tek onda je moguće ulogirati se na svaki Raspberry Pi naredbom:
```
ssh user@[IP adresa Raspberry Pi-a]
```

### Instalacija K3s Kubernetes distribucije

Potrebno je u ```/etc/hosts``` datotekama na svim Raspberry Pi mikroračunalima zapisati IP adrese i njihova imena kako bi se mogli međusobno adresirati po imenima umjesto IP adresama (ne dodajemo ime vlastitog čvora čiju datoteku uređujemo). Dakle, potrebno je urediti datoteku ```/etc/hosts``` na svim troma mikroračunalima:
```
echo -e "[IP adresa drugog čvora]\t[ime drugog čvora]" | sudo tee -a /etc/hosts
echo -e "[IP adresa trećeg čvora]\t[ime trećeg čvora]" | sudo tee -a /etc/hosts
```

Također, potrebno je urediti datoteku ```/boot/firmware/cmdline.txt``` na svim mikroračunalima kako bi K3s distribucija mogla koristiti kontrolne grupe (*eng. cgroups*) i nakon toga ponovno pokrenuti sva mikroračunala. Na kraj datoteke dodaje se ```cgroup_memory=1 cgroup_enable=memory``` (potrebne su *sudo* ovlasti), dakle:
```
sudo nano /boot/firmware/cmdline.txt
sudo reboot
```
U terminalu gdje smo ulogirani na *control-node* (Raspberry Pi Model 3B) potrebno je instalirati Kubernetes upravljački sloj koristeći [K3s distribuciju v1.29.3+k3s1](https://github.com/k3s-io/k3s/releases/tag/v1.29.3%2Bk3s1):
```
curl -sfL https://get.k3s.io | INSTALL_K3S_VERSION=v1.29.3+k3s1 sh -
```
Potrebno je provjeriti jeli K3s servis aktivan naredbom ```systemctl status k3s```. Za instalaciju Kubernetes radnih čvorova i istovremeno pridruživanje tih čvorova u grozd *control-node* mikroračunala, potrebno je prvo ispisati token za pridruživanje *control-node*-a naredbom ```sudo cat /var/lib/rancher/k3s/server/node-token``` te taj ispis iskoristiti u naredbi instalacije i pridruživanja koje se izvršavaju na *worker-node-1* (Raspberry Pi Model 4B 8G) i *worker-node-2* (Raspberry Pi Model 4B 2G) mikroračunalima (također je potrebno navesti verziju K3s distribucije):
```
curl -sfL https://get.k3s.io | INSTALL_K3S_VERSION=v1.29.3+k3s1 K3S_URL=https://control-node:6443 K3S_TOKEN=[token] sh -
```
Stanje K3s agent servisa može se vidjeti naredbom ```systemctl status k3s-agent```.

