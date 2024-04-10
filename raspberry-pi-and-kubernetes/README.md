# Raspberry Pi i Kubernetes

## Raspberry Pi

[Raspberry Pi](https://www.raspberrypi.com/) je serija mikroračunala razvijena od zaklade Raspberry Pi Foundation. Niske su potrošnje, a razlikuju se po sustavu na čipu (*eng. System on Chip - SoC*) kojeg koriste, količini radne memorije, koja sučelja za komunikaciju koriste, podršci za pohranu podataka, veličini pločice i slično. Mogu se koristiti za razne projekte, desktop računala, Internet stvari pa i u industriji. Neki od poznatijih [modela](https://www.raspberrypi.com/products/) su: Raspberry Pi 5, Raspberry Pi 4 Model B, Raspberry Pi 3 Model B, Raspberry Pi Zero, Raspberry Pi Pico.

### Raspberry Pi OS Lite

[Raspberry Pi OS Lite](https://www.raspberrypi.com/documentation/computers/os.html#introduction) je minimalna instalacija Raspberry Pi OS-a. Temelji se na Debian Linux distribuciji te je optimiziran za hardver Raspberry Pi mikroračunala. Instalacija paketa vrši se APT (eng. Advanced Package Tool) upraviteljem paketa. Ažuriranje liste dostupnih paketa vrši se naredbom ```sudo apt update```, ažuriranje cijelog sustava naredbom ```sudo apt full-upgrade```, a ažuriranje same distribucije naredbom ```sudo apt dist-upgrade```. Pretraga dostupnih paketa radi se naredbom ```apt-cache search [ime paketa]```, a izlistavanje informacija o paketu radi se naredbom ```apt-cache show```. Instalacija paketa radi se naredbom ```sudo apt install [ime paketa]```, deinstalacija ```sudo apt remove [ime paketa]```, a deinstalacija s uklanjanjem konfiguracijskih datoteka ```sudo apt purge [ime paketa]```.

#### Preuzimanje 64 bitne Raspberry Pi OS Lite slike sustava i pisanje slike na SD karticu

Većina novijih Raspberry Pi modela podržavaju ovu verziju operacijskog sustava ([lista operacijskih sustava i njihova podrška za određene Raspberry Pi modele](https://www.raspberrypi.com/software/operating-systems/)). Preuzimanje i otpakiranje 64 bitnog Raspberry Pi OS Lite (baziran na Debian 12 - bookworm) sustava izvršava se naredbama u terminalu:
```
curl https://downloads.raspberrypi.com/raspios_lite_arm64/images/raspios_lite_arm64-2024-03-15/2024-03-15-raspios-bookworm-arm64-lite.img.xz -o raspios-bookworm-arm64-lite.img.xz
xz -dk raspios-bookworm-arm64-lite.img.xz
```
Nakon toga je potrebno priključiti SD karticu na računalo (pomoću čitača SD kartice), odabrati datoteku uređaja koja predstavlja SD karticu i odmontirati njezine automatski montirane particije te joj zapisati sliku Raspberry Pi OS Lite sustava:
```
umount /dev/[datoteka uređaja]*
sudo dd raspios-bookworm-arm64-lite.img of=/dev/[datoteka uređaja] bs=4M conv=fsync status=progress
```

Za bežično povezivanje na mrežu koristi se servis NetworkManager. Potrebno je provjeriti status servisa te omogućiti i pokrenuti ga ako nije:
```
sudo systemctl status NetworkManager
sudo systemctl enable NetworkManager
sudo systemctl start NetworkManager
```
Zatim je potrebno ispisati dostupne mreže i povezati se na jednu od ponuđenih:
```
nmcli dev wifi list
nmcli dev wifi connect [ime mreže] password [zaporka mreže]
```
Ako WiFi modul nije bio uključen, potrebno je izvršiti ```nmcli radio wifi on```.
Nakon povezivanja na mrežu (ili ako smo već bili povezani žičano), potrebno je ažurirati repozitorij i pakete te je potrebno namjestiti lokalizaciju, vrijeme i tipkovnicu te promijeniti korisničko ime pokretanjem:
```
sudo apt update
sudo apt full-upgrade
sudo raspi-config
```

#### Ažuriranje firmware-a

Ažuriranje firmware-a obavlja se naredbama:
```
sudo rpi-update
sudo reboot
```
U slučaju da stvari nakon ažuriranja firmware-a ne rade kako trebaju, ako je sustav funkcionalan (moguće je pokrenuti Raspberry Pi OS Lite), moguće je vratiti firmware na prethodno stabilno stanje naredbama:
```
sudo apt-get update
sudo apt install --reinstall raspi-firmware
```

#### SSH povezanost

[SSH (*eng. Secure Shell*)](https://www.ssh.com/academy/ssh) je kriptografski mrežni protokol koji omogućuje enkriptiranu komunikaciju i upravljanje udaljenim računalima. SSH protokolom moguće je izvršavati naredbe na udaljenom računalu te prenositi datoteke obostrano. SSH se temelji na klijent-poslužitelj modelu što znači da se SSH konekcija otvara kada se SSH klijent javi SSH poslužitelju. [Postupak autentifikacije](https://www.ssh.com/academy/ssh/public-key-authentication) i otvaranje SSH tunela je sljedeći:

* SSH klijent generira jedan par asimetričnih ključeva (javni i privatni ključ algoritmom RSA, DSA, ECDSA, EDD25519), najčešće naredbom (```ssh-keygen```) koja će stvoriti par u (```.ssh/```) direktoriju u korisničkom direktoriju na klijentu
* SSH klijent kopira svoj javni ključ na SSH poslužitelj (```ssh-copy-id```), SSH poslužitelj sprema ključ i označava ga [autoriziranim](https://www.ssh.com/academy/ssh/authorized-key) (u datoteku ```.ssh/authorized_keys```) u korisničkom direktoriju na poslužitelju dok SSH klijent dodaje poslužitelja u datoteku ```.ssh/known_hosts``` u klijentovom korisničkom direktoriju
* SSH klijent se spaja na SSH poslužitelj pomoću naredbe (```ssh```) te se autentificira pomoću privatnog ključa
* nakon autentifikacije SSH klijent i SSH poslužitelj se dogovore za ključ sesije odnosno jedan simetrični ključ s kojim će enkriptirati i dekriptirati podatke koji će se slati i primati

[OpenSSH](https://www.ssh.com/academy/ssh/openssh) je skup alata otvorenog koda koji sadržavaju implementaciju SSH klijenta i SSH poslužitelja. Raspberry Pi OS Lite obično već uključuje OpenSSH, ali ako nije dostupan može ga se instalirati naredbom:
```
sudo apt install openssh
```
Raspberry Pi mikroračunalo će u ovom slučaju biti SSH poslužitelj, dakle na računalu koje će biti SSH klijent je također potrebno instalirati OpenSSH. Nakon instalacije paketa, potrebno je na Raspberry Pi-u provjeriti jeli omogućen i pokrenut *sshd* servis i ako nije omogućiti ga i pokrenuti:
```
sudo systemctl status sshd
sudo systemctl enable sshd
sudo systemctl start sshd
```
Generiranje primjerice ED25519 par ključa na SSH klijentu radi se naredbom:
```
ssh-keygen -t ed25519
```
Kopiranje javnog ključa sa SSH klijenta na SSH poslužitelj radi se naredbom:
```
ssh-copy-id [korisničko ime SSH poslužitelja]@[IP adresa SSH poslužitelja]
```
Autorizacija i otvaranje sesije sa SSH klijenta na SSH poslužitelj može se napraviti naredbom:
```
ssh [korisničko ime SSH poslužitelja]@[IP adresa SSH poslužitelja]
```
U slučaju da se želi prekinuti sesija dovoljno je upisati ```exit``` ili ```logout```.

## Kubernetes

[Kubernetes](https://kubernetes.io/) (skraćenica K8s) je otvorena platforma za automatizaciju, upravljanje i skaliranje kontejneriziranih aplikacija. Razvio ga je Google, ali ga se trenutno održava kao projekt otvorenog koda. Postoje razne distribucije Kubernetes platforme, primjerice MicroK8s tvrtke Canonical, Amazon Elastic Kubernetes Service (EKS), Google Kubernetes Engine (GKE), Microsoft Kubernetes Service (AKS), Red Hat OpenShift i itd. Svaka od implementacija ima neke dodatke i nedostatke. Različite implementacije su tu kako bi ih se prilagodilo specifičnim namjenama.

### Osnovni koncepti Kubernetes platforme

Implementacijom Kubenetes platforme nad skupom računala dobije se Kubernetes grozd (*eng. Kubernetes Cluster*). [Kubernetes grozd](https://kubernetes.io/docs/concepts/overview/components/) se sastoji od barem jednog radnog čvora. Kubernetes radni čvor (*eng. Kubernetes Worker Node*) pokreće Kubernetes kontejnere (*eng. Kubernetes Pod*) odnosno apstrakcije nad jednim ili više Docker kontejnera. Za orkestraciju rada grozda odgovoran je Kubernetes upravljački sloj (*eng. Kubernetes Control Plane*). U manjim lokalnim sustavima obično se koristi jedno računalo za upravljački sloj koji upravlja cijelim grozdom, ali u slučaju da je potrebna visoka otpornost na kvarove koristi se više računala za upravljački sloj.

#### Upravljački sloj

Upravljački sloj čini jedno ili više računala koja upravljaju radom grozda. Uloga im je održati stanje korisnika koje je zadao administrator grozda. Bitne komponente upravljačkog sloja su:

- [kube-apiserver](https://kubernetes.io/docs/reference/command-line-tools-reference/kube-apiserver/)
	- pruža Kubernetes API (običan HTTP poslužitelj koji poslužuje REST zahtjeve)
	- radni čvorovi i administrator sustava komuniciraju pomoću ovog aplikacijskog sučelja
	- administrator komunicira s API-je preko alata naredbenog retka [kubectl](https://kubernetes.io/docs/reference/kubectl/) pomoću kojeg namješta konfiguraciju sustava
	- ostale komponente iz upravljačkog čvora također komuniciraju preko Kubernetes API poslužitelja prema radnim čvorovima

- [etcd](https://kubernetes.io/docs/tasks/administer-cluster/configure-upgrade-etcd/)
	- distribuirana baza podataka koja pohranjuje kritične podatke o grozdu: konfiguraciju grozda, stanje grozda, metapodatke i slično

- [kube-scheduler](https://kubernetes.io/docs/reference/command-line-tools-reference/kube-scheduler/)
	- servis koji raspoređuje kontejnere po čvorovima
	- koristi informacije o dostupnim resursima na radnim čvorovima i pokušava što bolje rasporediti kontejnere

- [kube-controller-manager](https://kubernetes.io/docs/reference/command-line-tools-reference/kube-controller-manager/)
	- skupina procesa koji nadgledaju rad grozda
	- u slučaju pada čvora odnosno promjene stanja grozda pokušava vratiti stanje grozda u stanje zadano konfiguracijom
	- osiguravaju lako skaliranje

#### Radni čvor

[Radni čvorovi](https://kubernetes.io/docs/concepts/architecture/nodes/) pokreću Kubernetes kontejnere koji su apstrakcija jednog ili više Docker kontejnera. Glavne komponente jednog radnog čvora su:

- [kubelet](https://kubernetes.io/docs/reference/command-line-tools-reference/kubelet/)
	- servis koji stvara i uništava instance Kubernetes kontejnera ovisno o uputama koje mu pošalje upravljački sloj pošalje
	- održava stanje radnog čvora koje je zadano, u slučaju pada kontejnera pokušava ga obnoviti tako da održi stanje grozda
	- upravljački sloj šalje upute o stvaranju (*eng. PodSpec*) kontejnera kroz Kubernetes API poslužitelj najčešće u obliku JSON ili YAML objekta
	
- [kube-proxy](https://kubernetes.io/docs/reference/command-line-tools-reference/kube-proxy/)
	- mrežni proxy
	- rutira mrežni promet prema Kubernetes kontejnerima
	- nudi i uravnotežavanje opterećenja osiguravanjem da se mrežni promet distribuira jednako prema svim ostalim Kubernetes kontejnerima istog tipa

- [container runtime](https://kubernetes.io/docs/setup/production-environment/container-runtimes/)
	- stvara i uništava, pokreće i zaustavlja Docker kontejnere unutar Kubernetes kontejnera
	- povlači Docker slike iz repozitorija

### Lightweight Kubernetes

[Lightweight Kubernetes](https://k3s.io/) (skraćenica K3s) je distribucija Kubernetes platforme namijenjena za Internet stvari (*eng. Internet of things - IoT*). Cijela distribucija je pakirana u binarnu datoteku veličine manje od 70 MB. Nudi potporu za x86_64, ARMv7 i ARMv8 (ARM64) arhitekture. Međutim, postoje neke sitne [razlike](https://docs.k3s.io/) koje su napravljene tako da bi distribucija bila što lakša za uređaje Internet stvari:

* upravljački sloj ne koristi etcd kao distribuiranu bazu podataka već [sqlite3](https://www.sqlite.org/)
* sam upravljački sloj je pakiran u jednu binarnu datoteku i vanjske ovisnosti su minimizirane
* kompleksne operacije poput distribucije certifikatima su automatizirane radi jednostavnosti i olakšanog upravljanja

#### Prije instalacije na Raspberry Pi OS

Standardne Raspberry Pi OS instalacije nemaju omogućenu značajku *cgroups*. [Prije instalacije K3s distribucije](https://docs.k3s.io/installation/requirements?os=pi), u datoteku ```/boot/cmdline.txt``` je potrebno dodati u prvom redu značajke ```cgroup_memory=1 cgroup_enable=memory```. Općenito, minimalne zahtjevi za K3s distribuciju su:

* procesor s jednom jezgrom

* 512 MB RAM-a

#### Instalacija upravljačkog sloja i radnog čvora

Upravljački sloj uz radni radni čvor koji dolazi u ovom paketu se u ovoj distribuciji naziva *k3s server* i njegova instalacija na Raspberry Pi OS sustavima je vrlo jednostavna. Potrebno je izvršiti sljedeću naredbu:
```
curl -sfL https://get.k3s.io | sh -
```
Naredba će preuzeti binarnu datoteku koja sadrži upravljački sloj i radni čvor. Rad grozda započinje odmah nakon instalacije. Deinstalacija *k3s-server* komponente može se učiniti pokretanjem skripte:
```
/usr/local/bin/k3s-uninstall.sh
```
Također, naredba će stvoriti Kubernetes konfiguracijsku datoteku ```/etc/rancher/k3s/k3s.yaml``` koju će odmah početi koristiti.

#### Instalacija radnog čvora

Radni čvor se u ovoj distribuciji naziva *k3s-agent* i njegova instalacija na Raspberry Pi OS sustavima je skoro pa identična instalaciji *k3s-server* komponente. Naredbi za dohvaćanje *k3s-server* upravljačkog sloja se dodaje parametar ```K3S_URL``` iz [konfiguracijske skripte ljuske](https://get.k3s.io/):
```
curl -sfL https://get.k3s.io | K3S_URL=https://[IP adresa k3s-server komponente]:6443 K3S_TOKEN=[token] sh -
```
Token je potrebno kopirati s *k3s-server* komponente iz datoteke ```/var/lib/rancher/k3s/server/node-token```. Radni čvor će se pridružiti grozdu upravljačkog sloja. Preporučeno je koristiti adresiranje preko imena umjesto IP adrese (potrebno je urediti datoteku ```/etc/hosts```). Deinstalacija *k3s-agent* komponente može se učiniti pokretanjem skripte:
```
/usr/local/bin/k3s-uninstall.sh
```

