package be.cytomine.appengine.models.task.formats;

import java.awt.Dimension;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;

import be.cytomine.appengine.dto.responses.errors.ErrorCode;
import be.cytomine.appengine.exceptions.TypeValidationException;

public class DicomFormat implements FileFormat {

    public static final byte[] SIGNATURE = { (byte) 0x44, (byte) 0x49, (byte) 0x43, (byte) 0x4D };
    private static final int DICOM_MAGIC_BYTES_LENGTH = 4;
    private static final int DICOM_MAGIC_BYTE_OFFSET = 128;

    @Override
    public boolean checkSignature(File file) {

        if (file.length() < SIGNATURE.length) {
            return false;
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] fileSignature = new byte[DICOM_MAGIC_BYTE_OFFSET + SIGNATURE.length];
            int bytesRead = fis.read(fileSignature);
            byte[] magicBytes = Arrays.copyOfRange(fileSignature,
                fileSignature.length - DICOM_MAGIC_BYTES_LENGTH, fileSignature.length);
            return bytesRead == DICOM_MAGIC_BYTE_OFFSET
                + SIGNATURE.length && Arrays.equals(magicBytes, SIGNATURE);
        } catch (IOException e) {
            return false;
        }
    }

    public boolean checkSignature(byte[] magicBytes) {
        if (magicBytes.length < SIGNATURE.length) {
            return false;
        }
        return magicBytes.length == SIGNATURE.length && Arrays.equals(magicBytes, SIGNATURE);
    }

    @Override
    public boolean validate(File file) {
        return true;
    }

    public void validateDicomHeader(ZipFile zipFile, ZipEntry zipEntry)
        throws IOException, TypeValidationException {
        try (InputStream inputStream = new BufferedInputStream(zipFile.getInputStream(zipEntry))) {
            byte[] headerBytes = inputStream.readNBytes(
                DICOM_MAGIC_BYTE_OFFSET + DICOM_MAGIC_BYTES_LENGTH);
            byte[] magicBytes = Arrays.copyOfRange(headerBytes,
                headerBytes.length - DICOM_MAGIC_BYTES_LENGTH, headerBytes.length);

            DicomFormat dicomFormat = new DicomFormat();
            if (!dicomFormat.checkSignature(magicBytes)) {
                throw new TypeValidationException(
                    ErrorCode.INTERNAL_PARAMETER_INVALID_IMAGE_FORMAT);
            }
        }
    }

    @Override
    public Dimension getDimensions(File file) {
        try (DicomInputStream dis = new DicomInputStream(file)) {
            Attributes attributes = dis.readDataset();

            int width = attributes.getInt(Tag.Columns, -1);
            int height = attributes.getInt(Tag.Rows, -1);
            if (width == -1 || height == -1) {
                return null;
            }

            return new Dimension(width, height);
        } catch (IOException e) {
            return null;
        }
    }
}
