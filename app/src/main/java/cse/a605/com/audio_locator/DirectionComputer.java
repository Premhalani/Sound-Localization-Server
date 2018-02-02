package cse.a605.com.audio_locator;

import android.content.Context;
import android.util.Log;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Queue;


public class DirectionComputer {
    private static final float SPEED_OF_SOUND = 330.0F;
    private static final float DISTANCE_BETWEEN_RECIEVERS = 1F;
    public static ArrayList<Queue<AudioDataObject>> priorityQueueArrayList;
    private Context context;
    private int numberOfListeningDevice;
    public DirectionComputer(Context _context, int _numberOfListeningDevice){
        this.context = _context;
        this.numberOfListeningDevice = _numberOfListeningDevice;
        priorityQueueArrayList = new ArrayList<>();
        init();
    }
    private void init(){
        for(int i=0;i<numberOfListeningDevice;i++){
            Queue<AudioDataObject> queue = new PriorityQueue<>(100,new AudioObjectComparator());
            priorityQueueArrayList.add(queue);
        }
    }

    public void addToQueue(AudioDataObject audioDataObject){
        int id = audioDataObject.getId();
        Queue<AudioDataObject> queue = priorityQueueArrayList.get(id-1);
        queue.add(audioDataObject);
    }

    public boolean checkQueueForSameSequenceNumber(){
        int prev_seq = -1;          //Init
        for(Queue<AudioDataObject> queue : priorityQueueArrayList){
            if(queue.peek() == null) return false;
            if(prev_seq == -1)
                prev_seq = queue.peek().getSequenceNumber();            //First sequenceNumber
            else if(queue.peek().getSequenceNumber() != prev_seq)
                return false;           //SequenceNumber not same in any of the priority Queue
        }
        return true;
    }

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
        finalCordinates[0] = ( Y_CORDINATE * Math.tan(thetaAngles.get(0)) * (1 + Math.tan(thetaAngles.get(1))) ) / ( Math.tan(thetaAngles.get(0)) - Math.tan(thetaAngles.get(1)) );
        Log.d("XCoordinates", finalCordinates[0] + "");
        Log.d("YCoordinates", finalCordinates[1] + "");
    }

    public Float findAngleOfArrival(AudioDataObject audioDataObject1, AudioDataObject audioDataObject2){

        long t1 = Long.parseLong(audioDataObject1.getTimestamp());
        long t2 = Long.parseLong(audioDataObject2.getTimestamp());
        long offset1 = MainActivity.offsets.get(audioDataObject1.getDeviceId());
        long offset2 = MainActivity.offsets.get(audioDataObject2.getDeviceId());
        long modT1 = t1 + offset1;
        long modT2 = t2 + offset2;
        long diff = Math.abs(modT1-modT2);

        Log.d("Inputs", audioDataObject1.getSequenceNumber() + "|" + audioDataObject1.getDeviceId() + "|" + modT1);
        Log.d("Inputs", audioDataObject2.getSequenceNumber() + "|" + audioDataObject2.getDeviceId() + "|" + modT2);

        if(diff > 3){
            Log.d("SHIT"," :SN: " + audioDataObject1.getSequenceNumber() + " Devices " +
                    audioDataObject1.getId() + " and " + audioDataObject2.getId() + " :TD: " + (t1-t2));
            return -1.0F;       //return if greater than threshold
        }

        double cal = (diff*SPEED_OF_SOUND/(DISTANCE_BETWEEN_RECIEVERS*1000));
        double angle = Math.toDegrees(Math.acos(cal));

        Log.d("Results"," :SN: " + audioDataObject1.getSequenceNumber() + " Devices " +
                audioDataObject1.getId() + " and " + audioDataObject2.getId() + " :TD: " + diff
                + " :Angle: " + angle);
        return (float)angle;
    }
}