import pika
import json
import time
import threading
import random
from datetime import datetime, timedelta

RABBIT_HOST = 'localhost'
QUEUE_CHECKIN = 'hotel.checkin'
QUEUE_CHECKOUT = 'hotel.checkout'
QUEUE_MEAL = 'hotel.meal'

simulation_time = datetime.now().replace(hour=0, minute=0, second=0, microsecond=0)
time_lock = threading.Lock() 

def get_connection():
    return pika.BlockingConnection(pika.ConnectionParameters(host=RABBIT_HOST))

#odoslanie spravy do konkretnej fronty
def send_message(queue_name, message_dict):
    try:
        connection = get_connection()
        channel = connection.channel()
        channel.queue_declare(queue=queue_name, durable=False)
        
        json_body = json.dumps(message_dict)
        
        channel.basic_publish(exchange='', routing_key=queue_name, body=json_body)
        connection.close()
    except Exception as e:
        print(f"Chyba pri odosielaní do {queue_name}: {e}")

def run_guest_generator():
    global simulation_time
    print("GuestGenerator spustený")
    
    while True:
        with time_lock:
            current_sim_time = simulation_time.strftime("%Y-%m-%d %H:%M:%S")
        
        message = {
            "type": "CHECK_IN",
            "timestamp": current_sim_time
        }
        
        send_message(QUEUE_CHECKIN, message)
        print(f"GuestGen: Odoslaný CHECK_IN v čase {current_sim_time}")
        
        time.sleep(1)

def run_checkout_generator(probability_percent):
    global simulation_time
    print(f"CheckoutGenerator spustený ({probability_percent}%)")
    probability = probability_percent / 100.0
    
    while True:
        with time_lock:
            current_sim_time = simulation_time.strftime("%Y-%m-%d %H:%M:%S")
            
        #nahodne generujeme číslo izby
        #consumer sam skontroluje ci tam niekto byva
        if random.random() < probability:
            random_room = random.randint(0, 9)
            room_name = f"Izba {random_room}"
            
            message = {
                "type": "CHECK_OUT",
                "roomNumber": random_room,
                "guestName": room_name,
                "timestamp": current_sim_time
            }
            
            send_message(QUEUE_CHECKOUT, message)
            print(f"CheckoutGen: Odoslaný CHECK_OUT pre {room_name}")
            
        time.sleep(1)

def run_meal_generator():
    global simulation_time
    print("MealGenerator spustený")
    
    while True:
        with time_lock:
            #aktualna hodina simulacie
            current_hour = simulation_time.hour
            current_sim_time_str = simulation_time.strftime("%Y-%m-%d %H:%M:%S")
        
        meal_type = None
        if current_hour == 7:
            meal_type = "BREAKFAST"
        elif current_hour == 12:
            meal_type = "LUNCH"
        elif current_hour == 18:
            meal_type = "DINNER"
            
        if meal_type:
            message = {
                "type": "MEAL_SERVED",
                "mealType": meal_type,
                "timestamp": current_sim_time_str
            }
            send_message(QUEUE_MEAL, message)
            
            slovak_meal_name = "Jedlo"
            if meal_type == "BREAKFAST": slovak_meal_name = "Raňajky"
            elif meal_type == "LUNCH": slovak_meal_name = "Obed"
            elif meal_type == "DINNER": slovak_meal_name = "Večera"

            print(f"MealGen: Odoslané: {slovak_meal_name} v čase {current_sim_time_str}")
            
        time.sleep(1)

#hodiny
#jeden proces, ktory posuva cas dopredu
def run_time_ticker():
    global simulation_time
    print("Čas beží")
    while True:
        time.sleep(1)
        with time_lock:
            simulation_time += timedelta(hours=1)

if __name__ == "__main__":
    try:
        prob_input = input("Zadajte pravdepodobnosť odchodu hosťa (v %): ")
        prob = float(prob_input)
    except:
        prob = 20.0
        print("Chybný vstup, používam predvolenú hodnotu 20%")
    
    t1 = threading.Thread(target=run_time_ticker)
    t2 = threading.Thread(target=run_guest_generator)
    t3 = threading.Thread(target=run_checkout_generator, args=(prob,))
    
    t4 = threading.Thread(target=run_guest_generator) 
    t5 = threading.Thread(target=run_checkout_generator, args=(prob,))
    
    t6 = threading.Thread(target=run_meal_generator)

    t1.start()
    t2.start()
    t3.start()
    t4.start()
    t5.start()
    t6.start()

    #hlavne vlakno caka aby program neskoncil
    t1.join()