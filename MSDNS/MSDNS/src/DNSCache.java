import java.util.HashMap;


public class DNSCache {
    HashMap<DNSQuestion, DNSRecord> cache = new HashMap<>();


    public void add(DNSQuestion question, DNSRecord answer) {
        if (cache.containsKey(question)) {
            if (answer.isExpired()) {
                cache.remove(question);
            }
        } else {
            cache.put(question, answer);
        }
    }


    public DNSRecord getQuestion(DNSQuestion question) {
        DNSRecord record = cache.get(question);
        if (record == null) {
            cache.remove(question);
            return null;
        }
        return record;
    }
}