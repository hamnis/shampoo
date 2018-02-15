package no.storebrand.shampoo.okhttp3;

import no.storebrand.shampoo.Result;
import no.storebrand.shampoo.SoapDocument;
import no.storebrand.shampoo.SoapFault;
import okio.ByteString;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class MTOM {
    public static final MIMEType XOP = new MIMEType("application", "xop+xml");

    public final SoapDocument document;
    public final List<Attachment> attachments;

    MTOM(SoapDocument document, List<Attachment> attachments) {
        this.document = document;
        this.attachments = Collections.unmodifiableList(attachments);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MTOM mtom = (MTOM) o;
        return Objects.equals(document.toCompactString(), mtom.document.toCompactString()) &&
                Objects.equals(attachments, mtom.attachments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(document, attachments);
    }

    @Override
    public String toString() {
        return "MTOM{" +
                "document=" + document +
                ", attachments=" + attachments +
                '}';
    }

    public Optional<Attachment> attachmentById(ContentId id) {
        return attachments.stream().filter(p -> p.id.equals(id)).findFirst();
    }

    public MTOM addAttachment(Attachment attachment) {
        ArrayList<Attachment> copy = new ArrayList<>(this.attachments);
        copy.add(attachment);
        return new MTOM(document, copy);
    }

    List<Attachment> getWriteableAttachments() {
        ArrayList<Attachment> copy = new ArrayList<>(this.attachments);
        copy.add(0, toXOPAttachment());
        return copy;
    }

    private Attachment toXOPAttachment() {
        return new Attachment(
                new ContentId("mymessage.xml@example.org"),
                XOP.addParameter("charset", "UTF-8").addParameter("type", "application/soap+xml"),
                ByteString.encodeUtf8(document.toCompactString().trim())
        );
    }

    public static Result<SoapFault, MTOM> fromInputStream(MIMEType contentType, InputStream inputStream) throws IOException {
        Map<String, String> parameters = contentType.getParameters();
        MultipartInput stream = new MultipartInput(inputStream, parameters.get("boundary").getBytes(StandardCharsets.UTF_8), MultipartInput.DEFAULT_BUFSIZE);

        Result<SoapFault, SoapDocument> parsed = Result.failure(SoapFault.parse("No XOP data found"));

        ArrayList<Attachment> list = new ArrayList<>();

        boolean nextPart = stream.skipPreamble();
        while (nextPart) {
            Map<String, List<String>> headers = parseHeaders(stream.readHeaders());
            String ctHeader = headers.get("content-type").get(0);
            String idHeader = headers.get("content-id").get(0);
            MIMEType type = MIMEType.valueOf(ctHeader);
            // create some output stream
            ByteString data = stream.readBody().readByteString();

            if (XOP.includes(type)) {
                parsed = SoapDocument.fromString(data.utf8());
            } else {
                list.add(new Attachment(new ContentId(idHeader.replace("<", "").replace(">", "")), type, data));
            }
            nextPart = stream.readBoundary();
        }

        return parsed.map(doc -> new MTOM(doc, list));
    }

    private static Map<String, List<String>> parseHeaders(String headers) {
        Map<String, List<String>> map = new HashMap<>();
        String[] split = headers.split("\\r\\n");
        for (String headerString : split) {
            String[] parts = headerString.trim().split(":", 2);
            if (parts.length == 2) {
                List<String> list = map.getOrDefault(parts[0].trim(), new ArrayList<>());
                list.add(parts[1].trim());
                map.put(parts[0].trim().toLowerCase(), list);
            }
        }
        return map;
    }
}
