package be.cytomine.appengine.models.task.formats;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import be.cytomine.appengine.dto.responses.errors.ErrorCode;
import be.cytomine.appengine.exceptions.TypeValidationException;

public class WSIDicomFormat implements FileFormat {

    public final DicomFormat dicomFormat = new DicomFormat();

    @Override
    public boolean checkSignature(File directory) {
        File[] dicoms = directory.listFiles();
        if (Objects.isNull(dicoms) || dicoms.length == 0) {
            return false;
        }
        for (File dicom : dicoms) {
            if (dicom.isDirectory() || !dicomFormat.checkSignature(dicom)) {
                return false;
            }
        }
        return true;
    }


    @Override
    public boolean validate(File file) {
        return true;
    }

    public void validateZippedWSIDicom(File file) throws TypeValidationException {
        try (ZipFile zipFile = new ZipFile(file)) {
            Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
            while (zipEntries.hasMoreElements()) {
                ZipEntry zipEntry = zipEntries.nextElement();
                dicomFormat.validateDicomHeader(zipFile, zipEntry);
            }
        } catch (IOException e) {
            throw new TypeValidationException(ErrorCode.INTERNAL_PARAMETER_TYPE_ERROR);
        }
    }


    @Override
    public Dimension getDimensions(File file) {
        return dicomFormat.getDimensions(file);
    }
}
