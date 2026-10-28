package sk.upjs.kopr;

import sk.upjs.kopr.generator.CheckoutGenerator;
import sk.upjs.kopr.generator.GuestGenerator;
import sk.upjs.kopr.generator.MealGenerator;
import sk.upjs.kopr.service.BillingService;

import java.util.Scanner;

/**
 * Hlavná trieda, ktorá spúšťa celú simuláciu.
 * Jej úlohou je načítať vstup od používateľa a spustiť všetky
 * komponenty (generátory a služby) v samostatných vláknach (Threads).
 */
public class HotelSimulation {
    public static void main(String[] args) {
        System.out.println("HOTEL INTROVERT SIMULATION:");


        Scanner scanner = new Scanner(System.in);

        // Načítanie vstupného parametra 'p' (pravdepodobnosť odchodu)
        System.out.print("Zadajte pravdepodobnosť odchodu hosťa (v %): ");
        double checkoutProbability = scanner.nextDouble();
        System.out.println("\nSpúšťam simuláciu hotela...\n");

        try {
            // 1. Spustíme BillingService v samostatnom vlákne
            // BillingService je náš "spotrebiteľ" (consumer) správ
            Thread billingThread = new Thread(() -> { // Používame lambda výraz na definovanie úlohy vlákna
                try {
                    BillingService billingService = new BillingService();
                    billingService.start(); // Spustí počúvanie na RabbitMQ
                } catch (Exception e) {
                    System.err.println("Chyba BillingService: " + e.getMessage());
                }
            });
            billingThread.start(); // Spustíme vlákno

            // Dôležitý krok: Dáme BillingService čas (2 sekundy) na to, aby sa
            // stihol pripojiť k RabbitMQ a spustiť listenery (basicConsume).
            // Inak by mu generátory mohli poslať správy skôr, ako ich stihne začať počúvať.
            Thread.sleep(2000);

            // 2. Spustíme GuestGenerator (prvá inštancia)
            Thread guestGenerator1 = new Thread(() -> {
                try {
                    GuestGenerator generator = new GuestGenerator();
                    generator.start();
                } catch (Exception e) {
                    System.err.println("Chyba GuestGenerator-1: " + e.getMessage());
                }
            });
            guestGenerator1.start();

            // 3. Spustíme GuestGenerator (druhá inštancia) - podľa zadania
            Thread guestGenerator2 = new Thread(() -> {
                try {
                    Thread.sleep(500); // Malé oneskorenie (0.5s) pre desynchronizáciu
                    GuestGenerator generator = new GuestGenerator();
                    generator.start();
                } catch (Exception e) {
                    System.err.println("Chyba GuestGenerator-2: " + e.getMessage());
                }
            });
            guestGenerator2.start();

            // 4. Spustíme CheckoutGenerator (prvá inštancia)
            Thread checkoutGenerator1 = new Thread(() -> {
                try {
                    CheckoutGenerator generator = new CheckoutGenerator(checkoutProbability);
                    generator.start();
                } catch (Exception e) {
                    System.err.println("Chyba CheckoutGenerator-1: " + e.getMessage());
                }
            });
            checkoutGenerator1.start();

            // 5. Spustíme CheckoutGenerator (druhá inštancia) - podľa zadania
            Thread checkoutGenerator2 = new Thread(() -> {
                try {
                    Thread.sleep(500); // Malé oneskorenie
                    CheckoutGenerator generator = new CheckoutGenerator(checkoutProbability);
                    generator.start();
                } catch (Exception e) {
                    System.err.println("Chyba CheckoutGenerator-2: " + e.getMessage());
                }
            });
            checkoutGenerator2.start();

            // 6. Spustíme MealGenerator (jedna inštancia) - podľa zadania
            Thread mealGenerator = new Thread(() -> {
                try {
                    MealGenerator generator = new MealGenerator();
                    generator.start();
                } catch (Exception e) {
                    System.err.println("Chyba MealGenerator: " + e.getMessage());
                }
            });
            mealGenerator.start();

            System.out.println("Všetky komponenty spúšťané");
            System.out.println("Simulácia beží...\n");
            //System.out.println("Stlačte Ctrl+C pre zastavenie\n");

            // Pripojíme hlavné vlákno ('main') k vláknu 'billingThread'.
            // To znamená, že 'main' bude čakať, kým sa 'billingThread' neukončí.
            // Keďže 'billingThread' beží donekonečna (cez Thread.join() v jeho vlastnom main),
            // toto efektívne udrží program nažive.
            billingThread.join();

        } catch (Exception e) {
            System.err.println("Kritická chyba: " + e.getMessage());
            e.printStackTrace();
        }

        scanner.close();
    }
}