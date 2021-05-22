# Lab 2 homework

### Historyjka
Organizacja zimowej wyprawy górskiej lub wysokogórskiej to trudne zadanie, w szczególności w zakresie zebrania odpowiedniego sprzętu. Niektóre ekipy biorą ze sobą tlen, inne nie dopuszczają takiej możliwości; jedni turyści korzystają z zestawu puchowej odzieży, inni wolą morsować. Niezależnie od potrzeb, obsługa zleceń na sprzęt do wypraw górskich to niełatwe zadanie, które wymaga zapewnienia odpowiedniej komunikacji.

---
### Wersja podstawowa
Zaimplementuj, z użyciem RabbitMQ, system pośredniczący pomiędzy ekipami zmierzającymi na wyprawę górską (Ekipa), a dostawcami sprzętu górskiego (Dostawca). Ekipy mogą zamawiać różne typy sprzętu, natomiast każdy Dostawca posiada swoją listę dostępnych u niego typów sprzętu.


Dostawcy w wyniku porozumienia, określili następujące zasady sprzedaży:

- ceny poszczególnych typów sprzętów są takie same u wszystkich Dostawców (w związku z czym nie są uwzględniane w systemie rozdzielania zamówień)

- zlecenia powinny być rozkładane pomiędzy Dostawców w sposób zrównoważony

- dane zlecenie nie może trafić do więcej niż jednego Dostawcy

- zlecenia identyfikowane są przez nazwę Ekipy oraz wewnętrzny numer zlecenia nadawany przez Dostawcę

- po wykonaniu zlecenia Dostawca wysyła potwierdzenie do Ekipy

----
### Wersja premium
W wersji premium tworzonego systemu dostępny jest dodatkowy moduł administracyjny. Administrator dostaje kopię wszystkich wiadomości przesyłanych w systemie oraz ma możliwość wysłania wiadomości w trzech trybach:

- do wszystkich Ekip

- do wszystkich Dostawców

- do wszystkich Ekip oraz Dostawców

---
### Schemat
Dostosowanie się do unijnych wymagań w zakresie oprogramowania wymaga, aby do projektu załączona została dokumentacja w postaci schematu działania systemu. Schemat powinien uwzględniać:

- użytkowników, exchange'e, kolejki, klucze użyte przy wiązaniach

- schemat musi mieć postać elektroniczną, nie może to być skan odręcznego rysunku

---
### Omówienie zadania
Zadanie należy zaprezentować w następującym scenariuszu:

- omówić schemat

- uruchomić 2 ekipy (nadając im przy tym wybrane nazwy)

- uruchomić 2 dostawców (nadając im przy tym wybrane nazwy), z których:

	pierwszy obsługuje 2 typy sprzętu: tlen i buty; drugi obsługuje dwa typy sprzętu: tlen i plecak;

- (wersja premium) uruchomić 1 Administratora 

- przesłać z Ekipy 1 następującą serię zleceń: 

	'tlen', 'tlen', 'buty', 'buty', 'plecak', 'plecak'

- (wersja premium) wysłać od Administratora 3 wiadomości - do wszystkich Ekip, do wszystkich Dostawców, do wszystkich Ekip i Dostawców

- zaprezentować po kolei, co wypisali: Ekipa 1, Ekipa 2, Dostawca 1, Dostawca 2, Administrator

- w ramach testów systemu przyjmujemy, że zlecenia obsługiwane są natychmiast

- każda operacja (wysłanie i odebranie zlecenia) powinna być wypisana


---
### Punktacja:

- schemat: 2 punkty

- obsługa Ekip oraz Dostawców: 5 punktów

- moduł do administracji: 3 punkty

---
Zadanie można zrealizować w dowolnym języku programowania wspieranym przez RabbitMQ.


Zadanie należy przesłać przez platformę UPEL do godziny 8:00 dnia 22 marca (poniedziałek). Każdy tydzień opóźnienia w przesłaniu zadania obniża punktację o 2 punkty.