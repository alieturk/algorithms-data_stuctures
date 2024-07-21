package models;
/**
 Represents a wagon in a train.
 A wagon has a unique ID, a type, and can be attached to other wagons.
 Wagon objects are designed to be used in a doubly linked list representation of a train.
 @author Huseyin Altunbas
 */
public abstract class Wagon {
    protected int id;               // some unique ID of a Wagon
    private Wagon nextWagon;        // another wagon that is appended at the tail of this wagon
    // a.k.a. the successor of this wagon in a sequence// set to null if no successor is connected
    private Wagon previousWagon;    // another wagon that is prepended at the front of this wagon
    // a.k.a. the predecessor of this wagon in a sequence
    // set to null if no predecessor is connected

    // representation invariant propositions:
    // tail-connection-invariant:   wagon.nextWagon == null or wagon == wagon.nextWagon.previousWagon
    // front-connection-invariant:  wagon.previousWagon == null or wagon = wagon.previousWagon.nextWagon

    public Wagon(int wagonId) {
        this.id = wagonId;
    }

    public int getId() {
        return id;
    }

    public Wagon getNextWagon() {
        return nextWagon;
    }

    public Wagon getPreviousWagon() {
        return previousWagon;
    }


    public void setNextWagon(Wagon nextWagon) {
        this.nextWagon = nextWagon;
    }

    public void setPreviousWagon(Wagon previousWagon) {
        this.previousWagon = previousWagon;
    }

    /**
     * @return whether this wagon has a wagon appended at the tail
     */
    public boolean hasNextWagon() {
        return this.getNextWagon() != null;
    }

    /**
     * @return whether this wagon has a wagon prepended at the front
     */
    public boolean hasPreviousWagon() {
        return this.getPreviousWagon() != null;
    }

    /**
     * Returns the last wagon attached to it,
     * if there are no wagons attached to it then this wagon is the last wagon.
     *
     * @return the last wagon
     */
    public Wagon getLastWagonAttached() {
        // Set the last wagon as the current wagon
        Wagon last = this;
        // Traverse the linked list of wagons until the last wagon is found
        while (last.getNextWagon() != null) {
            last = last.getNextWagon();
        }
        // Return the last wagon in the train
        return last;
    }


    /**
     * @return the length of the sequence of wagons towards the end of its tail
     * including this wagon itself.
     */
    public int getSequenceLength() {
        // Initialize current wagon to be this wagon and length to be 1.
        Wagon current = this;
        int length = 1;
        // Traverse the wagon sequence until the end and increment length for each wagon visited.
        while (current.getNextWagon() != null) {
            current = current.getNextWagon();
            length++;
        }
        // Return the length of the wagon sequence.
        return length;
    }


    /**
     * Attaches the tail wagon and its connected successors behind this wagon,
     * if and only if this wagon has no wagon attached at its tail
     * and if the tail wagon has no wagon attached in front of it.
     *
     * @param tail the wagon to attach behind this wagon.
     * @throws IllegalStateException if this wagon already has a wagon appended to it.
     * @throws IllegalStateException if tail is already attached to a wagon in front of it.
     *                               The exception should include a message that reports the conflicting connection,
     *                               e.g.: "%s is already pulling %s"
     *                               or:   "%s has already been attached to %s"
     */
    public void attachTail(Wagon tail) throws IllegalStateException {
        Wagon currentWagon = this;

        // Check if current wagon is already pulling another wagon
        if (currentWagon.hasNextWagon()) {
            throw new IllegalStateException(String.format("%s is already pulling %s", currentWagon, currentWagon.getNextWagon()));
        }
        // Check if tail is already attached to another wagon
        if (tail.hasPreviousWagon()) {
            throw new IllegalStateException(String.format("%s has already been attached to %s", tail.getPreviousWagon(), tail));
        }
        // Attach tail to the end of the train
        tail.setPreviousWagon(currentWagon);
        while (currentWagon.hasNextWagon()) {
            currentWagon = currentWagon.getNextWagon();
        }
        currentWagon.setNextWagon(tail);

    }

    /**
     * Detaches the tail from this wagon and returns the first wagon of this tail.
     *
     * @return the first wagon of the tail that has been detached
     * or <code>null</code> if it had no wagons attached to its tail.
     */
    public Wagon detachTail() {
        // wagon should have no wagons attached to its tail
        Wagon detachedWagon = this.nextWagon;
        if (detachedWagon == null) {
            return null;
        }
        // detach tail from this wagon
        this.nextWagon = null;
        detachedWagon.previousWagon = null;
        //return the first wagon of this tail
        return detachedWagon;
    }


    /**
     * Detaches this wagon from the wagon in front of it.
     * No action if this wagon has no previous wagon attached.
     *
     * @return the former previousWagon that has been detached from,
     * or <code>null</code> if it had no previousWagon.
     */
    public Wagon detachFront() {
        Wagon previousWagon = this.getPreviousWagon();
        if (previousWagon != null) {
            // If the current wagon has a previous wagon, set the previous wagon's next wagon to null and the current wagon's previous wagon to null
            this.setPreviousWagon(null);
            previousWagon.setNextWagon(null);
        }
        return previousWagon;
    }

    /**
     * Replaces the tail of the <code>front</code> wagon by this wagon and its connected successors
     * Before such reconfiguration can be made,
     * the method first disconnects this wagon form its predecessor,
     * and the <code>front</code> wagon from its current tail.
     *
     * @param front the wagon to which this wagon must be attached to.
     */
    public void reAttachTo(Wagon front) {
        if (front == null) {return;}
        Wagon tailOfFront = front.getLastWagonAttached();
        // disconnect this wagon from its predecessor
        detachFront();
        // disconnect the front wagon from its current tail
        front.detachTail();
        // tail of the front wagon if this wagon
        tailOfFront.attachTail(this);
    }

    /**
     * Removes this wagon from the sequence that it is part of,
     * and reconnects its tail to the wagon in front of it, if any.
     */
    public void removeFromSequence() {
        // Get references to the previous and next wagons
        Wagon previousWagon = this.getPreviousWagon();
        Wagon nextWagon = this.getNextWagon();
        // If there is a previous wagon, set its next wagon to the next wagon of this wagon
        if (previousWagon != null) {
            previousWagon.setNextWagon(nextWagon);
        }
        // If there is a next wagon, set its previous wagon to the previous wagon of this wagon
        if (nextWagon != null) {
            nextWagon.setPreviousWagon(previousWagon);
        }
        // Set this wagon's previous and next wagons to null, effectively removing it from the sequence
        this.setPreviousWagon(null);
        this.setNextWagon(null);
    }


    /**
     * Reverses the order in the sequence of wagons from this Wagon until its final successor.
     * The reversed sequence is attached again to the wagon in front of this Wagon, if any.
     * No action if this Wagon has no succeeding next wagon attached.
     *
     * @return the new start Wagon of the reversed sequence (with is the former last Wagon of the original sequence)
     */
    public Wagon reverseSequence() {
        Wagon currentWagon = this;
        Wagon previousWagon = null;
        // While there are still wagons in the sequence
        while (currentWagon != null) {
            // Get the next Wagon in the sequence
            Wagon nextWagon = currentWagon.getNextWagon();
            // Detach the current Wagon from the front of the sequence
            currentWagon.detachFront();
            // Reattach the current Wagon to the previous Wagon (null if this is the first iteration)
            currentWagon.reAttachTo(previousWagon);
            // Update the previous Wagon to the current Wagon
            previousWagon = currentWagon;
            // Move to the next Wagon in the sequence
            currentWagon = nextWagon;
        }
        // Return the new start Wagon of the reversed sequence (which is the former last Wagon of the original sequence)
        return previousWagon;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[Wagon-");
        sb.append(this.getId());
        sb.append("]");
        return sb.toString();
    }
}

