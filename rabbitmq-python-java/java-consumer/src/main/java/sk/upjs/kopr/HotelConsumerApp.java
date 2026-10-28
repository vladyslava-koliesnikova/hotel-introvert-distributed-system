package sk.upjs.kopr;

import sk.upjs.kopr.service.BillingService;

public class HotelConsumerApp {
    public static void main(String[] args) {
        System.out.println("Java consumer starting");
        try {
            BillingService billingService = new BillingService();
            billingService.start();

            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}