package no.storebrand.shampoo.okhttp3;

import no.storebrand.shampoo.Result;
import no.storebrand.shampoo.SoapBody;
import no.storebrand.shampoo.SoapDocument;
import no.storebrand.shampoo.SoapFault;
import okio.ByteString;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;

import static no.storebrand.shampoo.JDOM2Utils.elem;
import static org.junit.Assert.*;

public class MTOMTest {

    @Test
    public void parseOutput() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        MultipartOutput mo = new MultipartOutput("boundary", bos);

        SoapDocument document = SoapDocument.soap12(Collections.emptyList(), new SoapBody(elem("Hello", "world")));
        MTOM mtom = new MTOM(document, Collections.singletonList(
                new Attachment(
                        new ContentId("id"),
                        MIMEType.valueOf("text/plain"),
                        ByteString.encodeUtf8("Hello world")
                )
        ));

        mo.write(mtom.getWriteableAttachments());

        //MultipartInput input = new MultipartInput(new ByteArrayInputStream(bos.toByteArray()), "boundary".getBytes(StandardCharsets.UTF_8), MultipartInput.DEFAULT_BUFSIZE);
        Result<SoapFault, MTOM> maybeparse = MTOM.fromInputStream(MIMEType.valueOf("multipart/mixed; boundary=boundary"), new ByteArrayInputStream(bos.toByteArray()));
        assertTrue(maybeparse.isSuccess());
        assertEquals(mtom, maybeparse.toOptional().get());

    }
}
