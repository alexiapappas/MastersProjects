import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Objects;


public class DNSQuestion {
    private String questionName_;
    private int questionType_;
    private int questionClass_;


    public DNSQuestion(String qName, int qType, int qClass) {
        this.questionName_ = qName;
        this.questionType_ = qType;
        this.questionClass_ = qClass;
    }


    public static DNSQuestion decodeQuestion(InputStream is, DNSMessage message) throws IOException {
        String[] domainPieces = message.readDomainName(is);
        String domainName= message.joinDomainName(domainPieces);

        // Read question type & class - 2 bytes each. is.read() reads one byte at a time
        int qType = (is.read() << 8) | is.read();
        int qClass = (is.read() << 8) | is.read();

        return new DNSQuestion(domainName, qType, qClass);
    }


    public void writeBytes(ByteArrayOutputStream os, HashMap<String,Integer> domainNameLocations){
        try {
            // Write domain name
            DNSMessage.writeDomainName(os, domainNameLocations, questionName_.split("\\."));

            // Write question type - 2 bytes
            os.write((questionType_ >> 8) & 0xFF);
            os.write(questionType_ & 0xFF);

            // Write question class
            os.write((questionClass_ >> 8) & 0xFF);
            os.write(questionClass_ & 0xFF);
        } catch (IOException e) {
            e.getMessage();
            throw new RuntimeException(e);
        }
    }


    public String toString(){
        return "DNSQuestion {" +
                "Question Name = " + questionName_ +
                ", Question Type = " + questionType_ +
                ", Question Class = " + questionClass_ +
                "}";
    }


    @Override
    public boolean equals(Object o){
        if (o == null || getClass() != o.getClass())
            return false;
        DNSQuestion q = (DNSQuestion) o;
        return questionName_ == q.questionName_ &&
                questionType_ == q.questionType_ &&
                questionClass_ == q.questionClass_;
    }


    public int hashCode(){
        return Objects.hash(questionClass_, questionName_, questionType_);
    }
}