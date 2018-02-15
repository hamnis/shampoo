package no.storebrand.shampoo.okhttp3;

import okio.ByteString;

import java.util.Objects;

public final class Attachment {
    public final ContentId id;
    public final MIMEType contentType;
    public final ByteString data;

    public Attachment(ContentId id, MIMEType contentType, ByteString data) {
        this.id = id;
        this.contentType = contentType;
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Attachment that = (Attachment) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(contentType, that.contentType) &&
                Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, contentType, data);
    }

    @Override
    public String toString() {
        return "Attachment{" +
                "id=" + id +
                ", contentType=" + contentType +
                ", data=" + data +
                '}';
    }
}
