Yksinkertainen java springboot sivu, jossa käyttäjä voi lisäillä tehtäviä tietokantaan.
Sivu käynnistetään CalendarDatabaseApplicationista.
Sivu vaatii kirjautumisen. Tunnuksia on kaksi valmiina ja niitä voi tehdä lisää käyttämällä tiedostoa DataInitializer(kommentoi component takaisin).

Tunnus 1
login: testuser 1
salasana: password1

Tunnus 2
login: testuser 2
salasan: password2

H2 konsoliin tulisi päästä kirjautumatta, jos tunnuksissa esiintyy ongelmia.

Sivulla on kaikki vaaditut ominaisuudet, paitsi Event-luokkasta sulatin date ja time sarakkeet yhdeksi sarakkeeksi nimeltä timestamp,
koska päivämäärä ja aika voidaan joka tapauksessa pilkkoa tuosta sarakkeesta, joka localDateTime muuttujan avulla tallentaa päivämäärän ja ajan yhteen paikkaan.

Lisäominaisuuksia en tehnyt, paitsi päivämäärän syötteen validoinnin, koska sen mukaan tehtävät tulee järjestellä. Lisäksi etusivulla näytetään onko tehtävä myöhässä,
vertaamalla tehtävän määräpäivää toiseen muuttujaan, joka saa päivämäärän laitteesta LocalDate.now(); komennolla.
