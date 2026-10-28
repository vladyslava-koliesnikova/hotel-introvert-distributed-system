package sk.upjs.kopr.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class Guest {
    private String name;
    private int roomNumber;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;

    //pocitadla sluzieb
    private AtomicInteger breakfasts = new AtomicInteger(0);
    private AtomicInteger lunches = new AtomicInteger(0);
    private AtomicInteger dinners = new AtomicInteger(0);
    private AtomicInteger nights = new AtomicInteger(0);

    public Guest(int roomNumber, LocalDateTime checkInTime) {
        this.roomNumber = roomNumber;
        this.name = "Izba " + roomNumber;
        this.checkInTime = checkInTime;
    }

    public void calculateNights(LocalDateTime checkOutTime) {
        this.checkOutTime = checkOutTime;
        nights.set(0);

        LocalDateTime start = this.checkInTime;
        LocalDateTime end = this.checkOutTime;

        Set<LocalDate> nightsCounted = new HashSet<>();

        //prechadzame cas hodinu po hodine
        LocalDateTime cursor = start;

        while (cursor.isBefore(end)) {
            int hour = cursor.getHour();

            if (hour >= 22 || hour < 6) {
                LocalDate nightDate;
                if (hour < 6) {
                    nightDate = cursor.toLocalDate().minusDays(1);
                } else {
                    nightDate = cursor.toLocalDate();
                }
                nightsCounted.add(nightDate);
            }
            cursor = cursor.plusHours(1);
        }
        nights.set(nightsCounted.size());
    }

    public void addBreakfast() {
        breakfasts.incrementAndGet();
    }

    public void addLunch() {
        lunches.incrementAndGet();
    }

    public void addDinner() {
        dinners.incrementAndGet();
    }

    public int getTotalCost() {
        return (breakfasts.get() + lunches.get() + dinners.get() + nights.get()) * 10;
    }

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