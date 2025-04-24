import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

class Test_DNSQuestion {

    @Test
    public void testParseHeader() throws IOException {

        byte[] inputHeader = new byte[] {
                (byte) 0x12, (byte) 0x34,   // ID = 18 52
                (byte) 0xB5,                // QR = 1, OP = 6 , AA = 1, TC = 0, RD = 1,
                (byte) 0x8F,                // RA = 1, Z = 0, AD = 0, CD = 0, RCode = 15
                (byte) 0x55, (byte) 0x1F,   // QDCount = 85 31
                (byte) 0x45, (byte) 0x45,   // ANCount = 69 69
                (byte) 0x11, (byte) 0x2B,   // NSCount 17 43
                (byte) 0x2F, (byte) 0x5     // ARCount 47 5
        };

        DNSHeader header = new DNSHeader(inputHeader);
        ByteArrayInputStream bais = new ByteArrayInputStream(inputHeader);
        header.decodeHeader(bais);
        assert(header.getId() == 0x1234);
        assert(header.getQr() == 0x1);
        assert(header.getOpCode_() == 0x6);
        assert(header.getAa() == 0x1);
        assert(header.getTc() == 0x0);
        assert(header.getRd() == 0x1);
        assert(header.getRa() == 0x1);
        assert(header.getZ() == 0x0);
        assert(header.getAd() == 0x0);
        assert(header.getCd() == 0x0);
        assert(header.getRCode_() == 0xF);
        assert(header.getQDCount_() == 0x551F);
        assert(header.getANCount_() == 0x4545);
        assert(header.getNSCount_() == 0x112B);
        assert(header.getARCount_() == 0x2F05);
    }

}