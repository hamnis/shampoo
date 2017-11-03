package no.storebrand.shampoo;

import okhttp3.MediaType;
import okio.ByteString;

import java.util.Objects;

public final class Attachment {
    public final MediaType contentType;
    public final ByteString data;

    public Attachment(MediaType contentType, ByteString data) {
        this.contentType = contentType;
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Attachment that = (Attachment) o;
        return Objects.equals(contentType, that.contentType) &&
                Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contentType, data);
    }
}
