package sk.upjs.kopr.model;

public class HotelMessage {
    public enum MessageType {
        CHECK_IN,
        CHECK_OUT,
        MEAL_SERVED,
    }

    private MessageType type;
    private String guestName;
    private int roomNumber;
    private String mealType;
    private String timestamp;

    public HotelMessage() {
    }

    public HotelMessage(MessageType type) {
        this.type = type;
    }

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

}