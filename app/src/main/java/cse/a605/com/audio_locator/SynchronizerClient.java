package cse.a605.com.audio_locator;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class SynchronizerClient {
    private HashMap<String, String> idToIP = new HashMap<>();
    MainActivity mainActivity;
    Timer t = new Timer();

    SynchronizerClient(MainActivity mainActivity)
    {
        this.mainActivity = mainActivity;
        setIdToIP();
    }

    public void setIdToIP() {
        idToIP.put(MainActivity.ids[0], "192.168.1.187");
        idToIP.put(MainActivity.ids[1], "192.168.1.186");
        idToIP.put(MainActivity.ids[2], "192.168.1.188");
        idToIP.put(MainActivity.ids[3], "192.168.1.177");
    }

    public void sendMessage()
    {
        t.scheduleAtFixedRate(
                new TimerTask() {
                    @Override
                    public void run() {
                        Log.d("Christians", "Performing Synchronization");
                        synchronize();
                    }
                },
        0, 15000);
    }

    private void synchronize()
    {
        for(Map.Entry<String, String> iterator : idToIP.entrySet())
        {
            String receiverIP = iterator.getValue();

            SyncDataObject messageObj = new SyncDataObject(iterator.getKey());

            Communicator communicator = new Communicator(mainActivity, messageObj, receiverIP);
            communicator.start();
        }
    }
}
