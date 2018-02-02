package cse.a605.com.audio_locator;

public class SyncDataObject {
    String messageId;
    String senderTimestamp;
    String receiverTimestamp;
    String senderReceivedTimestamp;

    SyncDataObject(String messageId) {
        this.messageId = messageId;
    }
}
