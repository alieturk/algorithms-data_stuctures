package models;
public class PassengerWagon extends Wagon{

    protected int numberOfSeats;

    public PassengerWagon(int wagonId, int numberOfSeats) {
        super(wagonId);
        this.numberOfSeats = numberOfSeats;
    }

    public int getNumberOfSeats() {
        return numberOfSeats;
    }
}
