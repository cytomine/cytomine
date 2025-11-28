package be.cytomine.appengine.models.task.formats;

import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

public class ZipFormat implements FileFormat {

    public static final byte[] SIGNATURE = { (byte) 0x50, (byte) 0x4B, (byte) 0x03, (byte) 0x04};

    @Override
    public boolean checkSignature(File file) {
        if (file.length() < SIGNATURE.length) {
            return false;
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] fileSignature = new byte[SIGNATURE.length];
            int bytesRead = fis.read(fileSignature);

            return bytesRead == SIGNATURE.length && Arrays.equals(fileSignature, SIGNATURE);
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public Dimension getDimensions(File file) {
        return null;
    }

    @Override
    public boolean validate(File file) {
        return true;
    }
}
