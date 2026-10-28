package sk.upjs.kopr.model;

/**
 * Dátová trieda (POJO), ktorá reprezentuje správu posielanú cez RabbitMQ.
 * Musí byť jednoduchá, aby ju Gson vedel ľahko serializovať/deserializovať.
 */
public class HotelMessage {

    // Enum (výčtový typ) pre rôzne typy správ
    public enum MessageType {
        CHECK_IN,           // Požiadavka na ubytovanie nového hosťa
        CHECK_OUT,          // Oznámenie o odchode hosťa
        MEAL_SERVED,        // Oznámenie o podávaní jedla
        //ROOM_STATUS_REQUEST // (Nepoužité, ale užitočné pre prípadné rozšírenie)
    }

    private MessageType type;    // Typ tejto správy
    private String guestName;    // Meno hosťa (používané pri CHECK_OUT)
    private int roomNumber;      // Číslo izby (používané pri CHECK_OUT)
    private String mealType;     // Typ jedla: "BREAKFAST", "LUNCH", "DINNER"
    private String timestamp;    // Časová známka udalosti (vo formáte "yyyy-MM-dd HH:mm:ss")
    //private double probability;  // (Nepoužité)

    // Prázdny konštruktor je potrebný pre knižnicu Gson
    public HotelMessage() {
    }

    // Konštruktor pre jednoduchšie vytváranie správy
    public HotelMessage(MessageType type) {
        this.type = type;
    }

    // --- Gettery a Settery ---
    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(int roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getMealType() {
        return mealType;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
/**
    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }
 **/
}