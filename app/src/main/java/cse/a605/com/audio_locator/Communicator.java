package cse.a605.com.audio_locator;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Communicator extends Thread {
    private String LOG_TAG = "Communicator";
    private String receiverIP = "";
    private MainActivity _mainActivity;
    private final int PORT = 5000;
    private SyncDataObject syncDataObject;

    public Communicator(MainActivity clientActivity, SyncDataObject syncDataObject, String receiverIP) {
        this._mainActivity = clientActivity;
        this.syncDataObject = syncDataObject;
        this.receiverIP = receiverIP;
    }

    @Override
    public void run() {
        Socket clientSocket = null;
        PrintWriter printWriter = null;
        BufferedReader bufferedReader = null;

        try {
            // Create socket for the destination port.
            clientSocket = new Socket(receiverIP, PORT);

            printWriter = new PrintWriter(clientSocket.getOutputStream(), true);
            syncDataObject.senderTimestamp = String.valueOf(System.currentTimeMillis());
            printWriter.println("1");

            bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String timeStamp = bufferedReader.readLine();
            syncDataObject.receiverTimestamp = timeStamp;
            syncDataObject.senderReceivedTimestamp = String.valueOf(System.currentTimeMillis());

            SyncDataCompute computation = new SyncDataCompute(syncDataObject);
            long offset = computation.computeOffset();
            Log.d("Offset", String.valueOf(offset)+" ID = "+syncDataObject.messageId);
            MainActivity.offsets.put(syncDataObject.messageId, offset);

        } catch (UnknownHostException e) {
            Log.e(LOG_TAG, "ClientTask Send UnknownHostException.");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(LOG_TAG, "ClientTask Send IOException");
            e.printStackTrace();
        } finally {
            cleanUp(printWriter, bufferedReader, clientSocket);
        }
    }

    public void cleanUp(PrintWriter writer, BufferedReader reader, Socket socket){
        // Close output stream
        if (writer != null) {
            writer.close();
        } // Close input stream.
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                Log.i(LOG_TAG, "CleanUp of ObjectInputStream");
            }
        } // Close socket
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                Log.i(LOG_TAG, "CleanUp of Socket");
            }
        }
    }
}
