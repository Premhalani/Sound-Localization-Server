package cse.a605.com.audio_locator;

class AudioDataObject {
    private String timestamp;
    private String amplitude;
    private String frequency;
    private String intensity;
    private int sequenceNumber;
    private String deviceId;
    private int id;

    public AudioDataObject(String _deviceId, String _timestamp, int _sequenceNumber, int _id) {
        this.id = _id;
        this.timestamp = _timestamp;
        this.sequenceNumber = _sequenceNumber;
        this.deviceId = _deviceId;
    }

    public AudioDataObject(String _amplitude, String _frequency, String _intensity, String _deviceId) {
        this.amplitude = _amplitude;
        this.frequency = _frequency;
        this.intensity = _intensity;
        this.deviceId = _deviceId;
    }
    public String getTimestamp() { return timestamp;}

    public int getId() {
        return id;
    }

    public String getAmplitude() {
        return amplitude;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getFrequency() {
        return frequency;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public String getIntensity() {
        return intensity;
    }
}
