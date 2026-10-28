package sk.upjs.kopr.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class Guest {
    private String name; // Meno hosťa, napr. "Izba 5"
    private int roomNumber; // Číslo izby (0-9)
    private LocalDateTime checkInTime; // Presný čas príchodu
    private LocalDateTime checkOutTime; // Presný čas odchodu (nastaví sa až pri odchode)

    // Počítadlá služieb
    // Používame AtomicInteger pre garantovanú bezpečnosť pri prístupe z viacerých vlákien.
    // Aj keď naša DatabaseService už používa 'synchronized' metódy, je to dobrá prax.
    private AtomicInteger breakfasts = new AtomicInteger(0);
    private AtomicInteger lunches = new AtomicInteger(0);
    private AtomicInteger dinners = new AtomicInteger(0);
    private AtomicInteger nights = new AtomicInteger(0);

    public Guest(int roomNumber, LocalDateTime checkInTime) {
        this.roomNumber = roomNumber;
        // Meno hosťa je priamo odvodené od čísla izby podľa zadania
        this.name = "Izba " + roomNumber;
        this.checkInTime = checkInTime;
    }

    /**
     * Vypočíta počet nocí strávených v hoteli.
     * Metóda sa volá IBA RAZ, tesne pred odchodom hosťa (v DatabaseService.checkOutGuest).
     * Noc sa počíta, ak bol hosť v hoteli aspoň 1 hodinu
     * v "nočnom" intervale [22:00 - 06:00).
     *
     * @param checkOutTime Čas odchodu hosťa
     */
    public void calculateNights(LocalDateTime checkOutTime) {
        this.checkOutTime = checkOutTime;
        nights.set(0); // Resetujeme počítadlo pre istotu

        LocalDateTime start = this.checkInTime; // Čas príchodu
        LocalDateTime end = this.checkOutTime;  // Čas odchodu

        // Použijeme 'Set' (množinu) na uloženie unikátnych dátumov, kedy sa noc *začala*.
        // Týmto zabránime dvojitému započítaniu jednej noci.
        // Príklad: Hosť bol v hoteli o 23:00 (noc X) aj o 01:00 (tiež noc X). Do Setu sa pridá "X" iba raz.
        Set<LocalDate> nightsCounted = new HashSet<>();

        // Vytvoríme "kurzor", ktorým budeme prechádzať čas hodinu po hodine
        LocalDateTime cursor = start;

        // Iterujeme každou hodinou pobytu hosťa (od príchodu až po odchod)
        while (cursor.isBefore(end)) {
            int hour = cursor.getHour(); // Získame aktuálnu hodinu (0-23)

            // Kľúčová podmienka pre "nočnú hodinu"
            // (22:00, 23:00) ALEBO (0:00, 1:00, 2:00, 3:00, 4:00, 5:00)
            if (hour >= 22 || hour < 6) {
                // Toto je "nočná" hodina.
                // Musíme určiť, ku ktorému "dátumu noci" patrí.
                // Noc "patrí" tomu dátumu, kedy sa začala (o 22:00).

                LocalDate nightDate;
                if (hour < 6) {
                    // Ak je teraz 0-5 ráno, táto hodina patrí "včerajšej" noci
                    // Príklad: 17. novembra 04:00 patrí k noci, ktorá sa začala 16. novembra.
                    nightDate = cursor.toLocalDate().minusDays(1);
                } else {
                    // Ak je teraz 22:00 alebo 23:00, táto hodina patrí "dnešnej" noci
                    // Príklad: 17. novembra 23:00 patrí k noci, ktorá sa začala 17. novembra.
                    nightDate = cursor.toLocalDate();
                }
                // Pridáme tento dátum noci do Setu. Ak tam už je, nič sa nestane.
                nightsCounted.add(nightDate);
            }
            // Posunieme sa na ďalšiu hodinu
            cursor = cursor.plusHours(1);
        }

        // Výsledný počet nocí je veľkosť Setu (počet unikátnych nocí)
        nights.set(nightsCounted.size());
    }

    // Metódy na zvýšenie počítadiel jedál (volá ich DatabaseService)
    public void addBreakfast() {
        breakfasts.incrementAndGet();
    }

    public void addLunch() {
        lunches.incrementAndGet();
    }

    public void addDinner() {
        dinners.incrementAndGet();
    }

    // Metóda na výpočet celkovej ceny (volá ju BillingService)
    public int getTotalCost() {
        // Sčítame všetky poskytnuté služby (každá za 10 EUR)
        return (breakfasts.get() + lunches.get() + dinners.get() + nights.get()) * 10;
    }

    // --- Gettery a Settery (prístupové metódy k súkromným premenným) ---
    public String getName() {
        return name;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public LocalDateTime getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(LocalDateTime checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public int getBreakfasts() {
        return breakfasts.get();
    }

    public int getLunches() {
        return lunches.get();
    }

    public int getDinners() {
        return dinners.get();
    }

    public int getNights() {
        return nights.get();
    }
}