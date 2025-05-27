// NODE implementaiton stack::

package data_sturcts;
import java.util.logging.Logger;
import main.Parcel;

public class ReturnStack {
    private static final Logger logger = Logger.getLogger(ReturnStack.class.getName());
    private static final int MAX_RETRY_COUNT = 3; //if parcel failed push back 3 times max
    
    //Node class::
    private class Node {
        Parcel parcel;
        Node next;

        Node(Parcel parcel) {
            this.parcel = parcel;
            this.next = null;
        }
    }

    //top to return lifo::
    private Node top;
    private int size;

    public ReturnStack() {
        this.top = null;
        this.size = 0;
    }

    public boolean push(Parcel parcel) {
        if (parcel.getReturnCount() >= MAX_RETRY_COUNT) {
            logger.warning("\u001B[31m" + "Parcel " + parcel.getParcelID() + 
                         " has exceeded maximum retry count. Removing from system." + "\u001B[0m");
            return false;
        }
        parcel.incrementReturnCount();
        parcel.setStatus(Parcel.Status.Returned);
        
        Node newNode = new Node(parcel);
        newNode.next = top;
        top = newNode;
        size++;
        logger.info("\u001B[33m" + "Parcel " + parcel.getParcelID() + 
                   " pushed to return stack (Retry #" + parcel.getReturnCount() + ")" + "\u001B[0m");
        return true;
    }

    //!!pop then returns the top parcel from the stack.

    public Parcel pop() {
        if (isEmpty()) {
            return null;
        }

        Parcel parcel = top.parcel;
        top = top.next;
        size--;

        logger.info("\u001B[36m" + "Parcel " + parcel.getParcelID() + 
                   " popped from return stack for reprocessing" + "\u001B[0m");
        return parcel;
    }


    public Parcel peek() {
        return isEmpty() ? null : top.parcel;
    }
    public boolean isEmpty() {
        return top == null;
    }

    public int size() {
        return size;
    }

    //ansci color code implementaiton
    public void printStack() {
        Node current = top;
        System.out.println("\u001B[35m=== Return Stack Contents ===\u001B[0m");
        while (current != null) {
            Parcel p = current.parcel;
            System.out.println("\u001B[35m" + 
                String.format("Parcel[ID: %s, Dest: %s, RetryCount: %d]",
                    p.getParcelID(), p.getDestinationCity(), p.getReturnCount()) +
                "\u001B[0m");
            current = current.next;
        }
        System.out.println("\u001B[35m=== End of Stack ===\u001B[0m");
    }
}
