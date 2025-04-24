import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class DNSHeader {
    private static int ID_, QDCount_, ANCount_, NSCount_, ARCount_;
    private static byte QR_, AA_, TC_, RD_, RA_, Z_, AD_, CD_, OpCode_, RCode_;
    private byte[] rawHeader_;


    public DNSHeader(byte[] header) {
        rawHeader_ = header;
    }


    private static void parseHeader(byte[] header){
        ID_ = ((header[0] & 0xFF) << 8) | (header[1] & 0xFF);

        QR_ = (byte) ((header[2] >> 7) & 1);
        OpCode_ = (byte) ((header[2] >> 3) & 0x0F);
        AA_ = (byte) ((header[2] >> 2) & 1);
        TC_ = (byte) ((header[2] >> 1) & 1);
        RD_ = (byte) (header[2] & 1);

        RA_ = (byte) ((header[3] >> 7) & 1);
        Z_ = (byte) ((header[3] >> 6) & 1);
        AD_ = (byte) ((header[3] >> 5) & 1);
        CD_ = (byte) ((header[3] >> 4) & 1);
        RCode_ = (byte) (header[3] & 0x0F);

        QDCount_ = combineBytes(header[4], header[5]);
        ANCount_ = combineBytes(header[6], header[7]);
        NSCount_ = combineBytes(header[8], header[9]);
        ARCount_ = combineBytes(header[10], header[11]);
    }


    private static int combineBytes(byte a, byte b){
        int mask = 0;
        mask |= (a << 8);
        mask |= (b);
        return mask;
    }

//             *                                  1  1  1  1  1  1
//            *    0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5
//            *   +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
//            *   |                      ID                       |
//            *   +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
//            *   |QR|   OpCode  |AA|TC|RD|RA| Z|AD|CD|   RCODE   |
//            *   +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
//            *   |                QDCOUNT/ZOCOUNT                |
//            *   +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
//            *   |                ANCOUNT/PRCOUNT                |
//            *   +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
//            *   |                NSCOUNT/UPCOUNT                |
//            *   +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
//            *   |                    ARCOUNT                    |
//            *   +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+

    public static DNSHeader decodeHeader(InputStream in) throws IOException {
        byte[] header = in.readNBytes(12);
        parseHeader(header);
        return new DNSHeader(header);
    }


    public static DNSHeader buildHeaderForResponse(DNSMessage request, DNSMessage response) throws IOException {
        byte[] responseHeader = new byte[12];

        // Copy 12 bytes of a DNS header from a request object into the header array
        System.arraycopy(request.getHeader().rawHeader_, 0, responseHeader, 0, 12);
        // Set the QR to 1 indicating that it is a response
        responseHeader[2] |= (1 << 7);
        // Clear the RCODE
        responseHeader[3] &= 0xF0;

        int answerCount = DNSHeader.getANCount_();
        responseHeader[6] = (byte) ((answerCount >> 8) & 0xFF);
        responseHeader[7] = (byte) (answerCount & 0xFF);

        return new DNSHeader(responseHeader);
    }


    public void writeBytes(OutputStream os) throws IOException {
        os.write(rawHeader_);
    }


    public String toString(){
        return String.format("DNSHeader { ID=%d, QR=%d, OpCode=%d, AA=%d, TC=%d, RD=%d, RA=%d, Z=%d, AD=%d, CD=%d, RCode=%d, QDCount=%d, ANCount=%d, NSCount=%d, ARCount=%d }",
                ID_, QR_, OpCode_, AA_, TC_, RD_, RA_, Z_, AD_, CD_, RCode_, QDCount_, ANCount_, NSCount_, ARCount_);
    }


    public static int getId() {
        return ID_;
    }


    public static int getQr() {
        return QR_;
    }


    public static int getOpCode_(){
        return OpCode_;
    }


    public static int getAa() {
        return AA_;
    }


    public static int getTc() {
        return TC_;
    }


    public static int getRd() {
        return RD_;
    }


    public static int getRa() {
        return RA_;
    }


    public static int getZ() {
        return Z_;
    }


    public static int getAd() {
        return AD_;
    }


    public static int getCd() {
        return CD_;
    }


    public static int getRCode_() {
        return RCode_;
    }


    public static int getANCount_() {
        return ANCount_;
    }


    public static int getQDCount_() {
        return QDCount_;
    }


    public static int getNSCount_() {
        return NSCount_;
    }


    public static int getARCount_() {
        return ARCount_;
    }


    public void setResponseCode(int rcode){
        if (rcode < 0 || rcode > 15){
            throw new IllegalArgumentException("Invalid response code: " + rcode);
        }

        int RC = getRCode_();
        RC = (RCode_ & 0xFFF0) | rcode;
    }
}