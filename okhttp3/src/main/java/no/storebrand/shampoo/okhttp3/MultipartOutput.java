package no.storebrand.shampoo.okhttp3;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class MultipartOutput {

    private String boundary;
    private OutputStream outputStream;

    public MultipartOutput(String boundary, OutputStream outputStream) {
        this.boundary = boundary;
        this.outputStream = outputStream;
    }

    public void write(List<Attachment> attachments) throws IOException {
        writeBoundary(false);
        int last = attachments.size() - 1;
        for (int i = 0; i < attachments.size(); i++) {
            Attachment attachment = attachments.get(i);
            writeHeader("Content-Type", attachment.contentType.toString());
            writeHeader("Content-ID", String.format("<%s>", attachment.id.value));
            writeCRLF();
            //writeHeader("Content-Transfer-Encoding", String.format("<%s>", attachment.id.value));
            outputStream.write(attachment.data.toByteArray());
            writeBoundary(last == i);
            outputStream.flush();
        }
    }

    private void writeBoundary(boolean last) throws IOException {
        writeCRLF();
        writeString(String.format("--%s", boundary));
        if (!last) {
            writeCRLF();
            writeCRLF();
        } else {
            writeString("--");
        }
    }

    private void writeHeader(String name, String value) throws IOException {
        writeString(String.format("%s: %s", name, value));
        writeCRLF();
    }

    private void writeNewline() throws IOException {
        writeString("\n");
    }

    private void writeCRLF() throws IOException {
        writeString("\r\n");
    }

    private void writeString(String s) throws IOException {
        outputStream.write(s.getBytes(StandardCharsets.UTF_8));
    }
}
