package io.github.ya_b.registry.client.http;

import lombok.AllArgsConstructor;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;

@AllArgsConstructor
public class InputStreamRequestBody extends RequestBody {
    private InputStream inputStream;
    private Long length;
    private MediaType mediaType;

    @Nullable
    @Override
    public MediaType contentType() {
        return mediaType;
    }

    @Override
    public void writeTo(BufferedSink bufferedSink) throws IOException {
        long len = length;
        byte[] bytes = new byte[1024];
        while (len > 0) {
            int l = inputStream.read(bytes, 0, (int) Math.min(bytes.length, len));
            if (l < 0) {
                break;
            }
            bufferedSink.write(bytes, 0, l);
            len -= l;
        }
    }
}
