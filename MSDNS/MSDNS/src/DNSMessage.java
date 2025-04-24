import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class DNSMessage {
    private DNSHeader header_;
    private DNSQuestion[] questions_;
    private DNSRecord[] answers_;
    private DNSRecord[] authorityRecords_;
    private DNSRecord[] additionalRecords_;
    private byte[] rawMessage_;


    static DNSMessage decodeMessage(byte[] bytes) throws IOException {
        if (bytes == null || bytes.length < 12) {
            throw new IOException("Invalid DNS message: too short or null");
        }

        DNSMessage msg = new DNSMessage();
        msg.rawMessage_ = bytes;

        ByteArrayInputStream inStream = new ByteArrayInputStream(bytes);

        // Decode header
        msg.header_ = DNSHeader.decodeHeader(inStream);

        // Read the questions
        int questionCount = msg.header_.getQDCount_();
        msg.questions_ = new DNSQuestion[questionCount];
        for (int i = 0; i < questionCount; i++) {
            msg.questions_[i] = DNSQuestion.decodeQuestion(inStream, msg);
        }

        // Read the answers
        int answerCount = msg.header_.getANCount_();
        msg.answers_ = new DNSRecord[answerCount];
        for (int i = 0; i < answerCount; i++) {
            msg.answers_[i] = DNSRecord.decodeRecord(inStream, msg);
        }

        // Read the authority records
        int authorityCount = msg.header_.getNSCount_();
        msg.authorityRecords_ = new DNSRecord[authorityCount];
        for (int i = 0; i < authorityCount; i++) {
            msg.authorityRecords_[i] = DNSRecord.decodeRecord(inStream, msg);
        }

        // Read the additional records
        int additionalCount = msg.header_.getARCount_();
        msg.additionalRecords_ = new DNSRecord[additionalCount];
        for (int i = 0; i < additionalCount; i++) {
            msg.additionalRecords_[i] = DNSRecord.decodeRecord(inStream, msg);
        }

        return msg;
    }


    String[] readDomainName(InputStream is) throws IOException {
        List<String> labels = new ArrayList<>();
        int length;

        while ((length = is.read()) > 0) {
            if ((length & 0xC0) == 0xC0) { //Check for name compression
                int secondbyte = is.read();
                int offset = ((length & 0x3F) << 8) | secondbyte;
                return readDomainName(offset);
            } else {
                byte[] labelBytes = new byte[length];
                is.read(labelBytes);
                labels.add(new String(labelBytes));
            }
        }
        return labels.toArray(new String[0]);
    }


    String[] readDomainName(int firstByte) throws IOException {
        ByteArrayInputStream inStream = new ByteArrayInputStream(rawMessage_, firstByte, rawMessage_.length-firstByte);
        return readDomainName(inStream);
    }


    static DNSMessage buildResponse(DNSMessage request, DNSRecord[] answers) throws IOException {
        DNSMessage response = new DNSMessage();

        response.header_ = DNSHeader.buildHeaderForResponse(request, response);
        response.questions_ = request.questions_;
        response.answers_ = answers;
        response.authorityRecords_ = new DNSRecord[0];
        response.additionalRecords_ = new DNSRecord[0];

        return response;
    }


    byte[] toBytes() throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        //Write the header
        header_.writeBytes(outStream);

        //Write the questions
        HashMap<String, Integer> domainLocations = new HashMap<>();
        for (DNSQuestion question : questions_) {
            question.writeBytes(outStream, domainLocations);
        }

        //Write the answers
        for (DNSRecord answer : answers_) {
            answer.writeBytes(outStream, domainLocations);
        }

        //Write the authority records
        for (DNSRecord authority : authorityRecords_) {
            authority.writeBytes(outStream, domainLocations);
        }

        //Write the additional records
        for (DNSRecord additional : additionalRecords_) {
            additional.writeBytes(outStream, domainLocations);
        }

        return outStream.toByteArray();
    }


    static void writeDomainName(ByteArrayOutputStream os, HashMap<String,Integer> domainLocations, String[] domainPieces) throws IOException {
        String domainName = String.join(".", domainPieces);

        if (domainLocations.containsKey(domainName)){
            int offset = domainLocations.get(domainName);
            os.write((offset >> 8) | 0xC0); //If the domain name has been written before, the second time it will be compressed with a pointer (0xC0 followed by the offset)
            os.write(offset & 0xFF);
        } else {
            domainLocations.put(domainName, os.size());
            for (String piece : domainPieces){
                os.write(piece.length());
                os.write(piece.getBytes());
            }
            os.write(0); //End of domain name
        }
    }


    String joinDomainName(String[] pieces){
        return String.join(".", pieces);
    }


    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(header_).append("\n");

        sb.append("Questions:\n");
        for (DNSQuestion question : questions_) {
            sb.append(question).append("\n");
        }

        sb.append("Answers:\n");
        for (DNSRecord answer : answers_) {
            sb.append(answer).append("\n");
        }

        return sb.toString();
    }


    public DNSHeader getHeader(){
        return header_;
    }


    public DNSQuestion getQuestions(){
        return questions_[0];
    }


    public DNSRecord getAnswers(){
        return answers_[0];
    }


    public boolean isResponse(){
        return (header_.getQr() == 0);
    }


    public void setResponseCode(int rcode){
        header_.setResponseCode(rcode);
    }
}