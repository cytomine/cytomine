package io.github.ya_b.registry.client.file;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Sha256HashOutputStream extends OutputStream {
    OutputStream os;
    MessageDigest sha256Digest;

    public Sha256HashOutputStream(OutputStream os) {
        this.os = os;
        try {
            sha256Digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(int b) throws IOException {
        sha256Digest.update((byte) b);
        os.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        sha256Digest.update(b, off, len);
        os.write(b, off, len);
    }

    @Override
    public void close() throws IOException {
        super.close();
        os.close();
    }

    public String hash() {
        BigInteger number = new BigInteger(1, sha256Digest.digest());
        StringBuilder hexString = new StringBuilder(number.toString(16));
        while (hexString.length() < 64) {
            hexString.insert(0, '0');
        }
        return hexString.toString();
    }
}
