package sk.upjs.kopr.service;

import sk.upjs.kopr.model.Guest;

import java.time.LocalDateTime;
import java.util.*;


public class DatabaseService {
    private static DatabaseService instance;
    private final Map<Integer, Guest> guests;
    private static final int TOTAL_ROOMS = 10;

    private final Random random = new Random();

    private DatabaseService() {
        guests = new HashMap<>();
    }


    public static synchronized DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }


    public synchronized Guest checkInGuest(LocalDateTime checkInTime) {
        for (int roomNumber = 0; roomNumber < TOTAL_ROOMS; roomNumber++) {
            if (!guests.containsKey(roomNumber)) {
                Guest guest = new Guest(roomNumber, checkInTime);
                guests.put(roomNumber, guest);
                return guest;
            }
        }
        return null;
    }

    public synchronized Guest checkOutGuest(int roomNumber, LocalDateTime checkOutTime) {
        Guest guest = guests.remove(roomNumber);
        if (guest != null) {
            guest.calculateNights(checkOutTime);
        }
        return guest;
    }


    public synchronized Guest getRandomGuest() {
        if (guests.isEmpty()) return null;
        List<Integer> roomNumbers = new ArrayList<>(guests.keySet());
        //Random random = new Random();
        int randomRoom = roomNumbers.get(random.nextInt(roomNumbers.size()));
        return guests.get(randomRoom);
    }

    public synchronized void serveMealToAll(String mealType) {
        for (Guest guest : guests.values()) {
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

    public synchronized int getOccupiedRoomsCount() {
        return guests.size();
    }

    public synchronized List<Integer> getFreeRooms() {
        List<Integer> freeRooms = new ArrayList<>();
        for (int i = 0; i < TOTAL_ROOMS; i++) {
            if (!guests.containsKey(i)) {
                freeRooms.add(i);
            }
        }
        return freeRooms;
    }
}