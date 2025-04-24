import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;


public class DNSRecord {
    private static String recordName_;
    private static int recordType_, recordClass_;
    private static long ttl_;
    private static Date recordCreationTime_;
    private static byte[] recordData_;


    public DNSRecord(String name, int rType, int rClass, long timeTL, byte[] data, Date creationTime) {
        recordName_ = name;
        recordType_ = rType;
        recordClass_ = rClass;
        ttl_ = timeTL;
        recordData_ = data;
        recordCreationTime_ = creationTime;
    }


    public static DNSRecord decodeRecord(InputStream instream, DNSMessage message) throws IOException {
        recordName_ = String.join(".", message.readDomainName(instream));

        recordType_ = (instream.read() << 8) | instream.read();
        recordClass_ = (instream.read() << 8) | instream.read();
        ttl_ = ((long) instream.read() << 24) | ((long) instream.read() << 16) | ((long) instream.read() << 8) | instream.read();

        int dataLength = (instream.read() << 8) | instream.read();
        recordData_ = new byte[dataLength];
        instream.read(recordData_);

        recordCreationTime_ = new Date();

        return new DNSRecord(recordName_, recordType_, recordClass_, ttl_, recordData_, recordCreationTime_);
    }


    public void writeBytes(ByteArrayOutputStream os, HashMap<String, Integer> map) throws IOException {
        DNSMessage.writeDomainName(os, map, recordName_.split("\\."));

        os.write((recordType_ >> 8) & 0xFF);
        os.write(recordType_ & 0xFF);

        os.write((recordClass_ >> 8) & 0xFF);
        os.write(recordClass_ & 0xFF);

        os.write((int) ((ttl_ >> 24) & 0xFF));
        os.write((int) ((ttl_ >> 16) & 0xFF));
        os.write((int) ((ttl_ >> 8) & 0xFF));
        os.write((int) (ttl_ & 0xFF));

        os.write((recordData_.length >> 8) & 0xFF);
        os.write(recordData_.length & 0xFF);
        os.write(recordData_);
    }


    public String toString(){
        return "DNS Record{ " +
                "recordName = " + recordName_ + '\'' +
                ", type = " + recordType_ +
                ", class = " + recordClass_ +
                ", TTL = " + ttl_ +
                ", created = " + recordCreationTime_ +
                ", date = " + new String(recordData_) + // assuming that the date is printable
                '}';
    }


    public boolean isExpired() {
        long elapsedTime = (new Date().getTime() - recordCreationTime_.getTime()) / 1000; // divide by 1000 to convert ms to seconds
        return elapsedTime > ttl_;
    }
}