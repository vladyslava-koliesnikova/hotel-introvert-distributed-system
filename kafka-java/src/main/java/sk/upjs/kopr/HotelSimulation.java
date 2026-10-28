package sk.upjs.kopr;

import sk.upjs.kopr.generator.CheckoutGenerator;
import sk.upjs.kopr.generator.GuestGenerator;
import sk.upjs.kopr.generator.MealGenerator;
import sk.upjs.kopr.service.BillingService;

import java.util.Scanner;

public class HotelSimulation {
    public static void main(String[] args) {
        System.out.println("HOTEL INTROVERT SIMULATION:");
        Scanner scanner = new Scanner(System.in);
        System.out.print("Zadajte pravdepodobnosť odchodu hosťa (v %): ");
        double checkoutProbability = scanner.nextDouble();
        System.out.println("\nSpúšťam simuláciu hotela (Kafka)...\n");

        try {
            Thread billingThread = new Thread(() -> {
                try {
                    BillingService billingService = new BillingService();
                    billingService.start();
                } catch (Exception e) {
                    System.err.println("Chyba BillingService: " + e.getMessage());
                }
            });
            billingThread.start();

            Thread.sleep(2000);


            Thread guestGenerator1 = new Thread(() -> {
                try {
                    GuestGenerator generator = new GuestGenerator();
                    generator.start();
                } catch (Exception e) {
                    System.err.println("Chyba GuestGenerator-1: " + e.getMessage());
                }
            });
            guestGenerator1.start();

            Thread guestGenerator2 = new Thread(() -> {
                try {
                    Thread.sleep(500);
                    GuestGenerator generator = new GuestGenerator();
                    generator.start();
                } catch (Exception e) {
                    System.err.println("Chyba GuestGenerator-2: " + e.getMessage());
                }
            });
            guestGenerator2.start();

            Thread checkoutGenerator1 = new Thread(() -> {
                try {
                    CheckoutGenerator generator = new CheckoutGenerator(checkoutProbability);
                    generator.start();
                } catch (Exception e) {
                    System.err.println("Chyba CheckoutGenerator-1: " + e.getMessage());
                }
            });
            checkoutGenerator1.start();

            Thread checkoutGenerator2 = new Thread(() -> {
                try {
                    Thread.sleep(500);
                    CheckoutGenerator generator = new CheckoutGenerator(checkoutProbability);
                    generator.start();
                } catch (Exception e) {
                    System.err.println("Chyba CheckoutGenerator-2: " + e.getMessage());
                }
            });
            checkoutGenerator2.start();

            Thread mealGenerator = new Thread(() -> {
                try {
                    MealGenerator generator = new MealGenerator();
                    generator.start();
                } catch (Exception e) {
                    System.err.println("Chyba MealGenerator: " + e.getMessage());
                }
            });
            mealGenerator.start();

            System.out.println("Všetky komponenty spustené!");
            System.out.println("Simulácia beží...\n");

            billingThread.join();

        } catch (Exception e) {
            System.err.println("Kritická chyba: " + e.getMessage());
            e.printStackTrace();
        }

        scanner.close();
    }
}