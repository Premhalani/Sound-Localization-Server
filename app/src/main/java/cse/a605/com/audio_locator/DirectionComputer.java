package cse.a605.com.audio_locator;

import android.content.Context;
import android.util.Log;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Queue;

// This class is used to compute the direction of the sound source using data
// from all the 4 devices. It maintains a 4 priority queues of all the sequence numbers
// of all the AudioDataObject and uses the first value from all the queues to compute result.

public class DirectionComputer {
    private static final float SPEED_OF_SOUND = 330.0F;
    private static final float DISTANCE_BETWEEN_RECIEVERS = 1F;
    public static ArrayList<Queue<AudioDataObject>> priorityQueueArrayList;			//ArrayList of all the priority Queues.
    private Context context;
    private int numberOfListeningDevice;
	
	//Default constructor to take number of android client devices to create that many queues.
    public DirectionComputer(Context _context, int _numberOfListeningDevice){
        this.context = _context;
        this.numberOfListeningDevice = _numberOfListeningDevice;
        priorityQueueArrayList = new ArrayList<>();
        init();
    }
	
	// Helper function to initialize all the queues in the arraylist.
    private void init(){
        for(int i=0;i<numberOfListeningDevice;i++){
            Queue<AudioDataObject> queue = new PriorityQueue<>(100,new AudioObjectComparator());
            priorityQueueArrayList.add(queue);
        }
    }
	
	// Takes in audioDataObject and adds the audioDataObject of the respective priority queue.
    public void addToQueue(AudioDataObject audioDataObject){
        int id = audioDataObject.getId();
        Queue<AudioDataObject> queue = priorityQueueArrayList.get(id-1);
        queue.add(audioDataObject);
    }
	
	// Checks all the queues whether they have the same id/sequence number. If all queues have same id then 
	// return true to stop the input and compute the direction. Else return false and keep accepting from client devices.
    public boolean checkQueueForSameSequenceNumber(){
        int prev_seq = -1;          //Initialize to -1
        for(Queue<AudioDataObject> queue : priorityQueueArrayList){
            if(queue.peek() == null) return false;
            if(prev_seq == -1)
                prev_seq = queue.peek().getSequenceNumber();            //get first sequenceNumber to check if all are same
            else if(queue.peek().getSequenceNumber() != prev_seq)
                return false;           //SequenceNumber not same in any of the priority Queue
        }
        return true;
    }
	
	// Get the maximum sequence number and its index from a given set of sequenceNumbers. Returns a int array with 
	// the sequence number and index.
    private int[] findMaxSequenceNumber(int[] sequenceNumbers){
        int[] result = new int[2];
        int maxSequenceNumber = Integer.MIN_VALUE;
        int index = -1;
        for(int i = 0;i<sequenceNumbers.length;i++)
        {
            int seq = sequenceNumbers[i];
            if(seq > maxSequenceNumber) {
                maxSequenceNumber = seq;
                index  = i;
            }
        }
        result[0] = maxSequenceNumber;
        result[1] = index;
        return result;
    }
	
	// Calculate the direction of the sound using the first values of all the queues. This is called only after checkQueueForSameSequenceNumber() returns true.
	// X_CORDINATE and Y_CORDINATE are set to 5 as to 5 meters for this experiment. It gets theta from findAngleOfArrival() and uses the theta to calculate the
	// X and Y coordinates of the sound source. 
    public void calculateDirection(){
        final int X_CORDINATE = 5;
        final int Y_CORDINATE = 5;
        ArrayList<Float> thetaAngles = new ArrayList<>();
        double finalCordinates[] = new double[2];
        for(int i = 0; i < numberOfListeningDevice; i = i + 2)
        {
            AudioDataObject a1 = priorityQueueArrayList.get(i).poll();
            AudioDataObject a2 = priorityQueueArrayList.get(i+1).poll();
            thetaAngles.add(findAngleOfArrival(a1,a2));
        }
        if(thetaAngles.get(0) == -1.0 || thetaAngles.get(1) == -1.0) return;        //Ignore false data
        finalCordinates[0] = ( X_CORDINATE * ( Math.tan(thetaAngles.get(0)) + 1 ) ) / ( Math.tan(thetaAngles.get(0)) - Math.tan(thetaAngles.get(1)) );
        finalCordinates[1] = ( Y_CORDINATE * Math.tan(thetaAngles.get(0)) * (1 + Math.tan(thetaAngles.get(1))) ) / ( Math.tan(thetaAngles.get(0)) - Math.tan(thetaAngles.get(1)) );
        
		//Log the result if valid result found
		Log.d("XCoordinates :", finalCordinates[0]);
        Log.d("YCoordinates :", finalCordinates[1]);
    }

	// Function returns the theta angle between two client devices which will be used to determine the X and Y coordinates of the sound source.
	// This also adds the offset to the current timestamp. This offset is calculated using Christian's algorithm and thus corrects the timestamps.
    public Float findAngleOfArrival(AudioDataObject audioDataObject1, AudioDataObject audioDataObject2){
		final int THRESHOLD = 3; 			// Threshold for this experiment is 3. If Difference between the two timestamp is greater than 3 then angle cannot be calculated.
        long timeStamp1 = Long.parseLong(audioDataObject1.getTimestamp());
        long timeStamp2 = Long.parseLong(audioDataObject2.getTimestamp());
		
		// get Offsets calculated using christians algorithm for the following device Ids.
        long offset1 = MainActivity.offsets.get(audioDataObject1.getDeviceId());
        long offset2 = MainActivity.offsets.get(audioDataObject2.getDeviceId());
        
		// add offsets to the timestamps
		long modT1 = timeStamp1 + offset1;
        long modT2 = timeStamp2 + offset2;
        long diff = Math.abs(modT1 - modT2);

        if(diff > THRESHOLD){
            return -1.0F;       //return -1 since difference is greater than threshold
        }

        double calculation = ( diff * SPEED_OF_SOUND / ( DISTANCE_BETWEEN_RECIEVERS * 1000) );
        double angle = Math.toDegrees(Math.acos(calculation));
        return (float)angle;
    }
}
