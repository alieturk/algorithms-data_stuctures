package models;

/**
 The Train class represents a train of wagons that are linked together using
 doubly linked lists. This class provides methods for manipulating the train,
 such as attaching and detaching wagons, moving wagons between trains, and
 splitting the train at a specific position.

 @author Huseyin Altunbas
 */
public class Train {
    private final String origin;
    private final String destination;
    private final Locomotive engine;
    private Wagon firstWagon;

    /* Representation invariants:
        firstWagon == null || firstWagon.previousWagon == null
        engine != null
     */

    public Train(Locomotive engine, String origin, String destination) {
        this.engine = engine;
        this.destination = destination;
        this.origin = origin;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    /**
     * Indicates whether the train has at least one connected Wagon
     *
     * @return
     */
    public boolean hasWagons() {
        return getFirstWagon() != null;   // replace by proper outcome
    }

    /**
     * A train is a passenger train when its first wagon is a PassengerWagon
     * (we do not worry about the posibility of mixed compositions here)
     *
     * @return
     */
    public boolean isPassengerTrain() {
        return getFirstWagon() instanceof PassengerWagon;   // replace by proper outcome
    }

    /**
     * A train is a freight train when its first wagon is a FreightWagon
     * (we do not worry about the posibility of mixed compositions here)
     *
     * @return
     */
    public boolean isFreightTrain() {
        return getFirstWagon() instanceof FreightWagon;   // replace by proper outcome
    }

    public Locomotive getEngine() {
        return engine;
    }

    public Wagon getFirstWagon() {
        return firstWagon;
    }

    /**
     * Replaces the current sequence of wagons (if any) in the train
     * by the given new sequence of wagons (if any)
     *
     * @param wagon the first wagon of a sequence of wagons to be attached (can be null)
     */
    public void setFirstWagon(Wagon wagon) {
        firstWagon = wagon;
    }

    /**
     * @return the number of Wagons connected to the train
     */
    public int getNumberOfWagons() {
        int numberOfWagons = 0;
        Wagon currentWagon = getFirstWagon();
        while (currentWagon != null) {
            numberOfWagons++;
            currentWagon = currentWagon.getNextWagon();
        }
        return numberOfWagons;
    }


    /**
     * @return the last wagon attached to the train
     */
    public Wagon getLastWagonAttached() {
        Wagon currentWagon = getFirstWagon();
        Wagon lastWagon = null;
        while (currentWagon != null) {
            lastWagon = currentWagon;
            currentWagon = currentWagon.getNextWagon();
        }
        return lastWagon;
    }

    /**
     * @return the total number of seats on a passenger train
     * (return 0 for a freight train)
     */
    public int getTotalNumberOfSeats() {
        int totalNumberOfSeats = 0;
        Wagon currentWagon = getFirstWagon();
        while (currentWagon != null) {
            if (currentWagon instanceof PassengerWagon) {
                totalNumberOfSeats += ((PassengerWagon) currentWagon).getNumberOfSeats();
            }
            currentWagon = currentWagon.getNextWagon();
        }
        return totalNumberOfSeats;
    }

    /**
     * calculates the total maximum weight of a freight train
     *
     * @return the total maximum weight of a freight train
     * (return 0 for a passenger train)
     */
    public int getTotalMaxWeight() {
        Wagon currentWagon = getFirstWagon();
        int totalMaxWeight = 0;
        while (currentWagon != null) {
            if (currentWagon instanceof FreightWagon) {
                totalMaxWeight += ((FreightWagon) currentWagon).getMaxWeight();
            }
            currentWagon = currentWagon.getNextWagon();
        }
        return totalMaxWeight;
    }

    /**
     * Finds the wagon at the given position (starting at 1 for the first wagon of the train)
     *
     * @param position
     * @return the wagon found at the given position
     * (return null if the position is not valid for this train)
     */
    public Wagon findWagonAtPosition(int position) {
        if (position <= 0) {
            return null;
        }
        Wagon currentWagon = getFirstWagon();
        int currentPosition = 1;

        while (currentPosition <= position) {
            if (currentWagon == null) {
                return null;
            }
            if (currentPosition == position) {
                return currentWagon;
            }
            currentWagon = currentWagon.getNextWagon();
            currentPosition++;
        }

        return null;
    }


    /**
     * Finds the wagon with a given wagonId
     *
     * @param wagonId
     * @return the wagon found
     * (return null if no wagon was found with the given wagonId)
     */
    public Wagon findWagonById(int wagonId) {
        Wagon currentWagon = getFirstWagon();
        while (currentWagon != null && currentWagon.getId() != wagonId) {
            currentWagon = currentWagon.getNextWagon();
        }
        return currentWagon;
    }

    /**
     * Determines if the given sequence of wagons can be attached to this train
     * Verifies if the type of wagons match the type of train (Passenger or Freight)
     * Verifies that the capacity of the engine is sufficient to also pull the additional wagons
     * Verifies that the wagon is not part of the train already
     * Ignores the predecessors before the head wagon, if any
     *
     * @param wagon the head wagon of a sequence of wagons to consider for attachment
     * @return whether type and capacity of this train can accommodate attachment of the sequence
     */
    public boolean canAttach(Wagon wagon) {
        // Check whether wagon type matches train type
        if (isPassengerTrain() && !(wagon instanceof PassengerWagon)) {
            return false;
        }
        if (isFreightTrain() && !(wagon instanceof FreightWagon)) {
            return false;
        }

        // Check if wagon is already part of the train
        Wagon currentWagon = getFirstWagon();
        while (currentWagon != null) {
            if (currentWagon == wagon) {
                return false;
            }
            currentWagon = currentWagon.getNextWagon();
        }

        // Check if engine can pull additional wagons
        int additionalWagons = 1;  // start with the wagon being checked
        currentWagon = wagon.getNextWagon();  // set currentWagon to the next wagon in the train
        while (currentWagon != null) {
            additionalWagons++;
            currentWagon = currentWagon.getNextWagon();
        }
        return additionalWagons <= this.getEngine().getMaxWagons() - this.getNumberOfWagons();
    }


    /**
     * Tries to attach the given sequence of wagons to the rear of the train
     * No change is made if the attachment cannot be made.
     * (when the sequence is not compatible or the engine has insufficient capacity)
     * if attachment is possible, the head wagon is first detached from its predecessors, if any
     *
     * @param wagon the head wagon of a sequence of wagons to be attached
     * @return whether the attachment could be completed successfully
     */
    public boolean attachToRear(Wagon wagon) {
        if (!canAttach(wagon)) {
            return false;
        }

        // detach the head wagon from its predecessors, if any
        if (wagon.getPreviousWagon() != null) {
            wagon.getPreviousWagon().setNextWagon(null);
            wagon.setPreviousWagon(null);
        }

        // Attach the given sequence of wagons to the rear of the train
        if (getLastWagonAttached() == null) {
            // If the train has no wagons, set the given wagon as the first wagon
            setFirstWagon(wagon);
        } else {
            // Otherwise, append the given wagon to the end of the train
            Wagon currentWagon = getLastWagonAttached();
            currentWagon.setNextWagon(wagon);
            wagon.setPreviousWagon(currentWagon);
        }

        return true;
    }


    /**
     * Tries to insert the given sequence of wagons at the front of the train
     * (the front is at position one, before the current first wagon, if any)
     * No change is made if the insertion cannot be made.
     * (when the sequence is not compatible or the engine has insufficient capacity)
     * if insertion is possible, the head wagon is first detached from its predecessors, if any
     *
     * @param wagon the head wagon of a sequence of wagons to be inserted
     * @return whether the insertion could be completed successfully
     */
    public boolean insertAtFront(Wagon wagon) {
        if (!canAttach(wagon)) {
            return false;
        }
        wagon.detachFront();

        if (getNumberOfWagons() == 0) {
            setFirstWagon(wagon);
        } else {
            getFirstWagon().reAttachTo(wagon.getLastWagonAttached());
            setFirstWagon(wagon);
        }

        return true;
    }

    /**
     * Tries to insert the given sequence of wagons at/before the given position in the train.
     * (The current wagon at given position including all its successors shall then be reattached
     * after the last wagon of the given sequence.)
     * No change is made if the insertion cannot be made.
     * (when the sequence is not compatible or the engine has insufficient capacity
     * or the given position is not valid for insertion into this train)
     * if insertion is possible, the head wagon of the sequence is first detached from its predecessors, if any
     *
     * @param position the position where the head wagon and its successors shall be inserted
     *                 1 <= position <= numWagons + 1
     *                 (i.e. insertion immediately after the last wagon is also possible)
     * @param wagon    the head wagon of a sequence of wagons to be inserted
     * @return whether the insertion could be completed successfully
     */

    public boolean insertAtPosition(int position, Wagon wagon) {
        if (wagon == null) {
            return false;
        }
        if (position > getNumberOfWagons() + 1) {
            return false;
        }
        Wagon wagonAtPosition = findWagonAtPosition(position);
        if (canAttach(wagon)) {
            // detach the wagon
            wagon.detachFront();
            // train without wagons
            if (firstWagon == null) {
                firstWagon = wagon;
                return true;
            }
            if (position == getNumberOfWagons() + 1) {
                getLastWagonAttached().attachTail(wagon);
                return true;
            }
            // insert in the middle of the train
            if (wagonAtPosition.hasNextWagon()) {
                Wagon previous = wagonAtPosition.getPreviousWagon();
                wagonAtPosition.detachFront();
                previous.attachTail(wagon);
                wagon.getLastWagonAttached().attachTail(wagonAtPosition);
            } else {
                // insert at position one
                if (position == 1) {
                    Wagon oldFirst = firstWagon;
                    firstWagon = wagon;
                    wagon.attachTail(oldFirst);
                    return true;
                }
                // insert immediately before the last wagon
                Wagon attachToHead = wagonAtPosition.getPreviousWagon();
                wagonAtPosition.detachFront();
                attachToHead.attachTail(wagon);
                wagon.attachTail(wagonAtPosition);
            }
            return true;
        }
        return false;
    }

    /**
     * Tries to remove one Wagon with the given wagonId from this train
     * and attach it at the rear of the given toTrain
     * No change is made if the removal or attachment cannot be made
     * (when the wagon cannot be found, or the trains are not compatible
     * or the engine of toTrain has insufficient capacity)
     *
     * @param wagonId the id of the wagon to be removed
     * @param toTrain the train to which the wagon shall be attached
     *                toTrain shall be different from this train
     * @return whether the move could be completed successfully
     */
    public boolean moveOneWagon(int wagonId, Train toTrain) {
        // Find the wagon with the given ID in this train
        Wagon wagonToMove = findWagonById(wagonId);
        if (wagonToMove == null) {
            // Wagon with given ID not found in this train
            return false;
        }

        if (toTrain.isFreightTrain()) {
            return false;
        }
        // Remove the wagon from this train's sequence
        if (!(wagonToMove.hasPreviousWagon())) {
            setFirstWagon(wagonToMove.getNextWagon());
            wagonToMove.removeFromSequence();
        } else if (!(wagonToMove.hasNextWagon())) {
            wagonToMove.detachFront();
        } else {
            Wagon wagonFirst = wagonToMove.getPreviousWagon();
            Wagon wagonTail = wagonToMove.getNextWagon();
            wagonFirst.setNextWagon(wagonTail);
            wagonTail.setPreviousWagon(wagonFirst);
            wagonToMove.setNextWagon(null);
            wagonToMove.setPreviousWagon(null);
        }

        // Attach the removed wagon to the rear of the toTrain
        Wagon firstWagon = toTrain.getFirstWagon();
        if (firstWagon == null) {
            toTrain.insertAtFront(wagonToMove);
        } else {
            toTrain.attachToRear(wagonToMove);
        }
        return true;
    }


    /**
     * Tries to split this train before the wagon at given position and move the complete sequence
     * of wagons from the given position to the rear of toTrain.
     * No change is made if the split or re-attachment cannot be made
     * (when the position is not valid for this train, or the trains are not compatible
     * or the engine of toTrain has insufficient capacity)
     *
     * @param position 1 <= position <= numWagons
     * @param toTrain  the train to which the split sequence shall be attached
     *                 toTrain shall be different from this train
     * @return whether the move could be completed successfully
     */
    public boolean splitAtPosition(int position, Train toTrain) {
        // Check if the specified position is valid
        if (position > getNumberOfWagons()) {
            return false;
        }
        // Check if the receiving train is a freight train, which cannot receive passenger wagons
        if (toTrain.isFreightTrain()) {
            return false;
        }
        // Find the wagon at the specified position
        Wagon wagonAtPosition = findWagonAtPosition(position);
        // Check if the wagon can be attached to the receiving train
        if (!(toTrain.canAttach(wagonAtPosition))) {
            return false;
        }
        // Detach the wagon from the current train and attach it to the receiving train
        if (!(wagonAtPosition.hasNextWagon())) {
            wagonAtPosition.detachFront();
            toTrain.attachToRear(wagonAtPosition);
            // If the wagon is the first wagon in the train
        } else if (wagonAtPosition.equals(getFirstWagon())) {
            toTrain.attachToRear(wagonAtPosition);
            this.setFirstWagon(null);
        } else {
            // If the wagon is not the first or the last wagon in the train
            wagonAtPosition.detachFront();
            toTrain.attachToRear(wagonAtPosition);
        }

        return true;
    }

    /**
     * Reverses the sequence of wagons in this train (if any)
     * i.e. the last wagon becomes the first wagon
     * the previous wagon of the last wagon becomes the second wagon
     * etc.
     * (No change if the train has no wagons or only one wagon)
     */
    public void reverse() {
        if (getNumberOfWagons() <= 1) {
            return;
        }

        Wagon current = getFirstWagon();
        Wagon lastWagon = null;
        while (current != null) {
            Wagon next = current.getNextWagon();
            current.setNextWagon(current.getPreviousWagon());
            current.setPreviousWagon(next);
            lastWagon = current;
            current = next;
        }

        getLastWagonAttached().setNextWagon(null);
        setFirstWagon(lastWagon);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Welcome to the HvA trains configurator\n");
        sb.append("[Loc-").append(getFirstWagon().getId()).append("]");

        Wagon currentWagon = getFirstWagon();
        while (currentWagon != null) {
            sb.append("[").append(currentWagon.getId()).append("]");
            currentWagon = currentWagon.getNextWagon();
        }

        sb.append(" with ").append(getNumberOfWagons()).append(" wagons from ")
                .append(getOrigin()).append(" to ").append(getDestination()).append("\n");

        sb.append("Total number of seats: ").append(getTotalNumberOfSeats()).append("\n");
        return sb.toString();
    }

}
