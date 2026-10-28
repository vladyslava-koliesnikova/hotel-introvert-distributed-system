package sk.upjs.kopr.service;

import sk.upjs.kopr.model.Guest;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Naša "databáza" v pamäti.
 * Je implementovaná ako Singleton (vzor Orol), aby sa zabezpečilo,
 * že všetky vlákna (generátory aj služby) pristupujú k JEDNEJ A TEJ ISTEJ
 * inštancii a teda k tým istým dátam.
 * <p>
 * Bezpečnosť prístupu z viacerých vlákien (thread-safety) je riešená
 * kľúčovým slovom 'synchronized' pri každej verejnej metóde.
 */
public class DatabaseService {
    // Statická premenná, ktorá drží jedinú inštanciu tejto triedy
    private static DatabaseService instance;

    // Naša "databáza" v pamäti. Je to obyčajný, ne-synchronizovaný HashMap.
    // Kľúč (Integer) je číslo izby (0-9).
    // Hodnota (Guest) je objekt hosťa.
    private final Map<Integer, Guest> guests;

    // Pevne stanovená kapacita hotela
    private static final int TOTAL_ROOMS = 10;

    // Random vytvorený iba raz, nie pri každom volaní getRandomGuest()
    private final Random random = new Random();

    // Súkromný konštruktor, aby nikto nemohol vytvoriť inštanciu zvonku
    // pomocou 'new DatabaseService()'
    private DatabaseService() {
        guests = new HashMap<>();
    }

    /**
     * Statická 'synchronized' metóda na získanie JEDINEJ inštancie triedy.
     * 'synchronized' tu zaručuje, že aj keby dve vlákna zavolali túto metódu
     * v presne rovnaký čas, inštancia sa vytvorí iba raz.
     *
     * @return Jediná inštancia DatabaseService
     */
    public static synchronized DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }

    /**
     * Metóda na ubytovanie hosťa.
     * 'synchronized' zamyká CELÝ objekt 'instance'. Kým táto metóda beží,
     * žiadna iná 'synchronized' metóda (napr. checkOutGuest) nemôže začať.
     *
     * @param checkInTime Čas, kedy sa hosť pokúša ubytovať
     * @return Objekt Guest, ak sa podarilo ubytovať; null, ak je hotel plný
     */
    public synchronized Guest checkInGuest(LocalDateTime checkInTime) {
        // Hľadáme prvé voľné číslo izby od 0 do 9
        for (int roomNumber = 0; roomNumber < TOTAL_ROOMS; roomNumber++) {
            // Ak mapa neobsahuje kľúč (číslo izby), znamená to, že izba je voľná
            if (!guests.containsKey(roomNumber)) {
                Guest guest = new Guest(roomNumber, checkInTime); // Vytvoríme nového hosťa
                guests.put(roomNumber, guest); // Vložíme ho do mapy (ubytujeme)
                return guest; // Vrátime hosťa (úspech)
            }
        }
        // Ak slučka prešla a nenašla voľnú izbu, hotel je plný
        return null;
    }

    /**
     * Metóda na odhlásenie hosťa. Taktiež 'synchronized'.
     *
     * @param roomNumber   Číslo izby hosťa, ktorý odchádza
     * @param checkOutTime Aktuálny čas odchodu
     * @return Objekt hosťa, ktorý bol odhlásený (pre účely fakturácie)
     */
    public synchronized Guest checkOutGuest(int roomNumber, LocalDateTime checkOutTime) {
        // 'remove' vyberie hosťa z mapy a zároveň ho zmaže. Vráti null, ak tam nebol.
        Guest guest = guests.remove(roomNumber);
        if (guest != null) { // Ak tam hosť skutočne bol
            // AŽ TU, pri odchode, finálne vypočítame počet nocí
            // Metóda 'calculateNights' uloží výsledok priamo do objektu 'guest'
            guest.calculateNights(checkOutTime);
        }
        return guest; // Vrátime odhláseného hosťa
    }

    /**
     * Získanie náhodného hosťa (pre CheckoutGenerator). Taktiež 'synchronized'.
     *
     * @return Náhodný objekt Guest, alebo null, ak je hotel prázdny
     */
    public synchronized Guest getRandomGuest() {
        if (guests.isEmpty()) return null; // Ak je hotel prázdny, vrátime null

        // Získame všetky obsadené čísla izieb (kľúče mapy)
        List<Integer> roomNumbers = new ArrayList<>(guests.keySet());
        //Random random = new Random();
        // Vyberieme náhodný index zo zoznamu obsadených izieb
        int randomRoom = roomNumbers.get(random.nextInt(roomNumbers.size()));
        // Vrátime hosťa z tejto náhodnej izby
        return guests.get(randomRoom);
    }

    /**
     * Pridanie jedla všetkým aktuálne ubytovaným hosťom. Taktiež 'synchronized'.
     *
     * @param mealType Typ jedla ("BREAKFAST", "LUNCH", "DINNER")
     */
    public synchronized void serveMealToAll(String mealType) {
        // Prejdeme všetkých hostí, ktorí sú aktuálne v mape (guests.values())
        for (Guest guest : guests.values()) {
            // A každému z nich zvýšime príslušné počítadlo
            switch (mealType) {
                case "BREAKFAST":
                    guest.addBreakfast();
                    break;
                case "LUNCH":
                    guest.addLunch();
                    break;
                case "DINNER":
                    guest.addDinner();
                    break;
                default:
                    throw new IllegalArgumentException("Unknown meal type: " + mealType);
            }
        }
    }

    /**
     * Získanie aktuálneho počtu obsadených izieb. Taktiež 'synchronized'.
     *
     * @return Počet hostí v hoteli
     */
    public synchronized int getOccupiedRoomsCount() {
        // Počet hostí je jednoducho veľkosť mapy
        return guests.size();
    }

    /**
     * Získanie zoznamu čísel voľných izieb. Taktiež 'synchronized'.
     *
     * @return Zoznam (List) čísel voľných izieb
     */
    public synchronized List<Integer> getFreeRooms() {
        List<Integer> freeRooms = new ArrayList<>();
        // Prejdeme všetky izby od 0 do 9
        for (int i = 0; i < TOTAL_ROOMS; i++) {
            if (!guests.containsKey(i)) { // Ak izba nie je v mape, je voľná
                freeRooms.add(i);
            }
        }
        return freeRooms;
    }
}