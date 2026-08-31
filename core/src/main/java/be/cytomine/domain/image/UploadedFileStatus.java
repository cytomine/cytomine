package be.cytomine.domain.image;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum UploadedFileStatus {
    /**
     * Even codes lower than 100 => information Even codes greater or equal to 100 => success Odd codes => error
     */
    UPLOADED(0),

    DETECTING_FORMAT(10),
    ERROR_FORMAT(11), // 3

    EXTRACTING_DATA(20),
    ERROR_EXTRACTION(21),

    CONVERTING(30),
    ERROR_CONVERSION(31), // 4

    DEPLOYING(40),
    ERROR_DEPLOYMENT(41), // 8

    UNPACKING(50),
    ERROR_UNPACKING(51),

    CHECKING_INTEGRITY(60),
    ERROR_INTEGRITY(61),
    DEPLOYED(100),
    EXTRACTED(102),
    CONVERTED(104),

    UNPACKED(106);

    private final int code;
}
