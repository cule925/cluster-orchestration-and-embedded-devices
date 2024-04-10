# Java Spring i Docker

## Java Spring radni okvir

[Java Spring](https://spring.io/projects/spring-framework) je radni okvir koji nudi podršku za razvoj Java aplikacija. Nudi značajke poput:

* injekcija ovisnosti (*eng. Dependency Injection*)
* Spring Security radni okvir za autentifikaciju i autorizaciju
* Spring MVC (*eng. Model View Controller*) radni okvir 
* AOP (*eng. Aspect Oriented Programmimg*) 
* Spring Data radni okvir koji pruža apstrakcije i alate nad upravljanjem bazama podataka
* ...

Odabiranje modula koje želimo uključiti u projekt i preuzimanje ZIP datoteke radi uz pomoć [Spring Initializer](https://start.spring.io/). Preporučuje se koristiti [IntelliJ](https://www.jetbrains.com/idea/) kao razvojno okruženje za pisanje programa.

### Aplikacije i kontekstu ovog projekta

Potrebno je stvoriti tri radna okruženja i preuzeti 3 projekta sa sljedećim specifikacijama:

- projekt klijent:
	- Project: Maven
	- Language: Java
	- Spring Boot: 3.2.3
	- Packaging: Jar
	- Java: 17
	- Group: com.example
	- Artifact: client
	- Name: client
	- Description: Client application for simulating an ESP32 node
	- Package name: com.example.client
	- Dependencies: Spring Reactive Web

- projekt klijent poslužitelj:
	- Project: Maven
	- Language: Java
	- Spring Boot: 3.2.3
	- Packaging: Jar
	- Java: 17
	- Group: com.example
	- Artifact: client-server
	- Name: client-server
	- Description: Client and server application for Raspberry Pi Kubernetes node
	- Package name: com.example.client-server
	- Dependencies: Spring Reactive Web, Spring Web, Spring Session

- projekt poslužitelj:
	- Project: Maven
	- Language: Java
	- Spring Boot: 3.2.3
	- Packaging: Jar
	- Java: 17
	- Group: com.example
	- Artifact: server
	- Name: server
	- Description: Server application acting as the top server
	- Package name: com.example.server
	- Dependencies: Spring Web, Spring Session, Thymeleaf

#### Uvoz aplikacije u IntelliJ

Prije uvoza aplikacija u IntelliJ potrebno je otpakirati sve tri ZIP datoteke. Zatim je potrebno pokrenuti IntelliJ i uvesti sva tri projekta. Za sva tri projekta je potrebno namjestiti SDK IntelliJ-a koji će se koristi za pokretanje programa, mogu se koristiti Java 17 IntelliJ inačice ili inačice OpenJDK-a koje se nalaze na putanji ```/usr/lib/jvm/*```.

#### Pakiranje aplikacija u JAR datoteke uz pomoć Maven-a u IntelliJ-u

[Maven](https://maven.apache.org/) je alat za automatizaciju izgradnje, upravljanje ovisnostima i izvođenje testova. Maven koristi POM (*eng. Project Object Model*) datoteku koja opisuje konfiguraciju projekta i proces izgradnje. [Izgradnja artefakta u IntelliJ radnom okruženju](https://www.jetbrains.com/guide/java/tutorials/hello-world/packaging-the-application/) koji definira kako izgraditi JAR datoteku može se napraviti prateći upute:

* File -> Project Structure -> Artifacts -> + -> JAR -> From modules and dependencies... -> Odabrati Main klasu i opciju "extract to the target JAR" -> OK

Datoteka će se nalaziti na putanji: ```%lokacija_projekta%/out/artifacts/[ime JAR datoteke]_jar/[ime JAR datoteke].jar```. JAR datoteka se izgrađuje pomoću naredbe:

* Build -> Build Artifacts ... -> [označiti artifact]

Pokretanje JAR datoteke radi se u terminalu naredbom ```java -jar [ime JAR datoteke] [argumenti programa]```.

## Docker

[Docker](https://docs.docker.com/get-started/overview/) je platforma za razvoj, implementaciju i izvršavanje aplikacija u kontejnerima. Kontejnerizacija je tehnologija koja omogućuje pakiranje aplikacija i njezinih ovisnosti u izolirane kontejnere, čime se osigurava dosljedno okruženje izvršavanja neovisno o okruženju na kojem se aplikacija pokreće. Prednosti uključuju laku prenosivost, izolaciju, efikasnost resursa te brzinu i skalabilnost, što ga čini popularnim alatom u svijetu softverskog razvoja.

### Arhitektura Docker platforme

Docker platforma koristi klijent-poslužitelj model. Docker klijent šalje REST zahtjeve API-ju Docker poslužitelju. Docker klijent se instancira [naredbom preko terminala](https://docs.docker.com/reference/cli/docker/) ```docker [argumenti]```, a Docker poslužitelj je zapravo servis *dockerd*. Ovo omogućuje da se Docker poslužitelj pokreće na nekom drugom računalu pa je moguće slati REST zahtjeve preko mreže Docker poslužitelju.

#### Docker kontejner

[Docker kontejner](https://docs.docker.com/guides/docker-concepts/the-basics/what-is-a-container/) (Docker container) je skup procesa koji se izvode u izoliranom okruženju. Za razliku od virtualnih strojeva gdje svaki virtualni stroj ima vlastiti operacijski sustav i gdje su virtualni strojevi upravljani hipervizorom, Docker kontejneri dijele jezgru Linux-a s računalom domaćinom što im daje manji *overhead*. Docker kontejneri koriste mehanizme poput *cgroups* i *namespaces* uz pomoć kojih im se dodjeljuje određena količina resursa i izolira ih se od ostatka sustava. Pokretanje kontejnera vrši se naredbom 

##### Kontrolne grupe

[Kontrolne grupe](https://wiki.archlinux.org/title/cgroups) (*eng. control groups - cgroups*) je mehanizam Linux jezgre koji omogućuje dodjeljivanje određene količine dostupnih resursa nekoj skupini procesa. Skupini procesa se dodjeljuje određena količina resursa, primjerice vrijeme izvođenja na procesoru, količina dostupne radne memorije, propusnost mreže i slično. Postoje dvije verzije ovog mehanizma, novija [kontrolna grupa v2](https://docs.kernel.org/admin-guide/cgroup-v2.html) i starija [kontrolna grupa v1](https://docs.kernel.org/admin-guide/cgroup-v1/cgroups.html). Dodjeljivanje resursa kontrolnim grupama rade podsustavi odnosno kontroleri kao što su cpu, cpuset, memory, io, pids, perf_event, rdma, hugetlb i freezer.

###### Kontrolne grupe verzije 2 - cgroups v2

Kontrolne grupe verzije 2 (cgroups-v2) je novija verzija mehanizma kontrolnih grupa koja dolazi integrirana s Linux jezgrom inačice 4.5. Mehanizam koristi virtualni datotečni sustav počevši od direktorija ```/sys/fs/cgroup/``` koji se još naziva i korijenska kontrolna grupa (njemu pripadaju svi procesi kojem nisu dodijeljene specifične kontrolne grupe, osim init procesa). U tom direktoriju se nalazi sljedeće:

* datoteke sučelja oblika ```cgroup.*```
* datoteke specifične kontrolerima cpu, cpuset, memory, io i slično, primjerice ```cpu.*```, ```cpuset.*```, ```memory.*```, ```io.*```...
* direktoriji koji predstavljaju druge kontrolne grupe procesa

Stvaranjem direktorija u ```/sys/fs/cgroup/``` (obično ovo radi već neki servis primjerice *systemd* koji pruža sučelje za ovakve operacije) automatski će stvoriti datoteke sučelja i datoteke specifične kontrolerima (za ove operacije je potrebno biti *root* korisnik - ```sudo -s```):
```
mkdir /sys/fs/cgroup/[ime željene kontrolne grupe]
```
Možemo ograničiti veličinu memorije koju ta kontrolna grupa može koristiti:
```
echo [količina memorije] > /sys/fs/cgroup/[ime željene kontrolne grupe]/memory.max
```
Ako se želi dodati neki proces u tu kontrolnu grupu, potrebno je upisati u datoteku sučelja ```cgroup.procs``` PID procesa:
```
echo [PID procesa] >> /sys/fs/cgroup/[ime željene kontrolne grupe]/cgroup.procs
```
Svi procesi djeca će također biti u ovoj kontrolnoj grupi (sistemski poziv ```fork()```). Ako se želi ukloniti kontrolna grupa, potrebno je prvo ubiti sve procese koji pripadaju kontrolnoj grupi te ne smiju postojati *zombie* procesi koji pripadaju ovoj kontrolnoj grupi. Tek je tada moguće izvršiti:
```
rmdir /sys/fs/cgroup/[ime željene kontrolne grupe]
```
Ako se stvori nova kontrolna grupa dijete unutar ove kontrolne grupe roditelj, dijete nasljeđuje postavke roditelja za kontrolere koji su dodani u datoteci ```cgroup.subtree_control``` kontrolne grupe roditelja.

###### Pokretanje kontejnera s ograničenjem na resurse sustava

Recimo da želimo pokrenuti Docker kontejner i da mu želimo dozvoliti korištenje maksimalno 256 MB radne memorije. To bi učinili naredbom
```
sudo docker run --memory=256m [ime kontejnera]
```
Izlistavanjem svih aktivnih kontejenera naredbom ```sudo docker ps``` može se dobiti skraćeni ID kontejnera što je zapravo samo prefiks pravog ID-a. Za ovaj Docker kontejner stvorila se kontrolna grupa na ```/sys/fs/cgroup/system.slice/docker-[pravi ID kontejnera]```. Ovdje se može vidjeti da je kontrolna grupa ```docker-[pravi ID kontejnera]``` dijete kontrolne grupe ```system.slice``` koji je pak dijete korijenske kontrolne grupe. Postavku ograničenja maksimalne veličine od 256 MB može se vidjeti ispisivanjem datoteke memorijskog kontrolera za tu grupu naredbom:
```
cat /sys/fs/cgroup/system.slice/docker-[pravi ID kontejnera]/memory.max
```
Ova putanja vrijedi za operacijske sustave koje koriste kontrolne grupe verzije 2 i *init system systemd* (vidi [Docker i kontrolne grupe](https://docs.docker.com/config/containers/runmetrics/)).

##### Imenski prostori

[Imenski prostori](https://www.nginx.com/blog/what-are-namespaces-cgroups-how-do-they-work/) je značajka Linux jezgre da razdvaja resurse koji procesi mogu koristiti. Ako kontrolne grupe pružaju raspodjelu resursa, imenski prostori ih izoliraju. Postoje nekoliko vrsta imenskih prostora:

- [korisnički imenski prostor](https://man7.org/linux/man-pages/man7/user_namespaces.7.html)
	- nudi izolaciju korisnika
	- korisnici u nekom imenskom prostoru mogu imati root ovlaštenja nad skupom procesa u tom imenskom prostoru (UID=0), 
	- svaki proces može biti dio samo jednog korisničkog imenskog prostora
	- svi korisnički imenski prostori imaju svog roditelja osim root korisničkog imenskog prostora (imenski prostor u kojem su inicijalno svi procesi)
- [imenski prostor procesnih identifikatora (PID)](https://man7.org/linux/man-pages/man7/pid_namespaces.7.html)
	- nudi izolaciju procesa
	- procesi koji se nalaze u imenskom prostoru procesnih identifikatora imaju vlastite procesne identifikatora (vlastite PID-ove)
	- procesi različitih imenskih prostora se međusobno ne vide
- [mrežni imenski prostor](https://man7.org/linux/man-pages/man7/network_namespaces.7.html)
	- omogućuje izolaciju mrežnih sučelja između procesa koji pripadaju različitim mrežnim imenskim prostorima
	- važno za Docker kontejnere, procesi koji se pokreću unutar kontejnera ne vide sva mrežna sučelja sustava
		- za komunikaciju s vanjskom mrežom Docker koristi virtualni par Ethernet priključaka *veth* i virtualni mrežni most koji se najčešće naziva *docker0* koji se nalazi u root mrežnom imenskom prostoru (*veth* par se ponaša kao kabel između dva Ethernet sučelja)
		- jedan *veth* se spaja na virtualni mrežni most *docker0* u root mrežnom imenskom prostoru dok se drugi umeće u mrežni imenski prostor Docker kontejnera, time efektivno imamo vezu između dva mrežna imenska prostora: mrežni imenski prostor Docker kontejnera i root mrežni imenski prostor
		- kako bismo omogućili i pristup vanjskoj mreži te Internetu na virtualni mrežni most *docker0* se također spaja Ethernet fizički priključak *eth* (priključak koji je spojen na mrežu) koji se također nalazi u root imenskom prostoru
- [imenski prostor montiranja](https://man7.org/linux/man-pages/man7/mount_namespaces.7.html)
	- izolacija točaka montiranja, procesi u određenom imenskom prostoru montiranja mogu vidjeti i pristupiti nekim točkama montiranja za koje drugi procesi u drugom imenskom prostoru ne mogu 
- [imenski prostor interprocesne komunikacije (*eng. IPC*)](https://man7.org/linux/man-pages/man7/ipc_namespaces.7.html)
	- izolacija IPC resursa, procesi u određenom imenskom prostoru interprocesne komunikacije mogu vidjeti i pristupiti resursima IPC-a poglavito redovima poruka (*eng. Message Queue*) za koje drugi procesi u drugom imenskom prostoru ne mogu
- [imenski prostor UNIX domena (*eng. UNIX Time Sharing - UTS*)](https://man7.org/linux/man-pages/man7/uts_namespaces.7.html)
	- izolacija imena računala i domena (*eng. hostname and domain*), procesi u imenskom prostoru imena računala i domena koriste drugačija imena računala i domena od procesa u drugom imenskom prostoru imena računala i domena 
- [vremenski imenski prostor](https://man7.org/linux/man-pages/man7/time_namespaces.7.html)
	- izolacija sistemskih satova za grupe procesa u drugim imenskim prostorima
- [imenski prostor kontrolnih grupa](https://man7.org/linux/man-pages/man7/cgroup_namespaces.7.html)
	- procesi mogu stvarati vlastitu hijerarhiju imenskih prostora preko virtualnih datotečnih sustava, demonstrirano gore u poglavlju kontrolnih grupa

#### Docker slika

[Docker slika](https://docs.docker.com/guides/docker-concepts/the-basics/what-is-an-image/) (*eng. Docker image*) je uputa za izgradnju kontejnera. Sadrži datoteke i konfiguraciju potrebnu za instanciranje kontejnera. Sadrži sve datoteke, biblioteke i konfiguracije za pokretanje kontejnera. Obično se Docker slika bazira na nekoj drugoj Docker slici koja se pritom modificira za potrbe korisnika. Slike se mogu preuzeti s Docker Hub registra u lokalni registar ili ih je moguće izgraditi pisanjem Dockerfile datoteke. Docker slike koriste slojeve (*eng. layers*) za izgradnju. Svaki sloj je neka modifikacija datotečnog sustava i jednom kad se ta modifikacija dogodi ona je nepromjenjiva. Bilo koja Docker naredba u Dockerfile datoteci koja modificira datotečni sustav smatra se jednim slojem dok ostale Docker naredbe najčešće uređuju podatke metapodatke same Docker slike. Docker može koristiti te slojeve za izgradnju drugih slika umjesto da ih ponovno izgrađuje. Docker slika se izgrađuje tako da se referencira direktorij gdje se nalazi Dockerfile datoteka uz pomoć naredbe:
```
sudo docker build [putanja do Dockerfile datoteke] -t [ime slike]:[oznaka slike]
```
Izlistavanje slika u lokalnom registru može se učiniti naredbom:
```
sudo docker images
```
Brisanje slike u lokalnom registru može se učiniti naredbom:
```
sudo docker rmi [ime slike]:[oznaka slike]
```

#### Docker registry

[Docker registar](https://docs.docker.com/guides/docker-concepts/the-basics/what-is-a-registry/) (*eng. Docker registry*) je servis koji pohranjuje Docker slike. Docker registar može biti lokalan gdje se slike pohranjuju na lokalnom računalu (primjerice korištenjem naredbe ```docker build [argumenti]```) ili javan kao primjerice Docker Hub (kojem se pristupa naredbama kao ```docker pull [argumenti]``` ili ```docker push [argumenti]```).
