package be.cytomine.common.repository.utils;

import java.util.HashMap;
import java.util.Map;

public enum Language {
    // see https://fr.wikipedia.org/wiki/Liste_des_codes_ISO_639-1
    ARABIC("AR"),
    GERMAN("DE"),
    GREEK("EL"),
    ENGLISH("EN"),
    FRENCH("FR"),
    DUTCH("NL"),
    SPANISH("ES");

    private final String code;

    Language(String code) {
        this.code = code;
    }

    private static final Map<String, Language> map;

    static {
        map = new HashMap<>();
        for (Language l : Language.values()) {
            map.put(l.code, l);
        }
    }

    public static Language findByCode(String c) {
        return map.get(c);
    }

    public static Language findByOrdinal(Integer ordinal) {
        if (ordinal == null || ordinal < 0 || ordinal >= values().length) {
            return ENGLISH;
        }
        return values()[ordinal];
    }

    @Override
    public String toString() {
        return this.code;
    }
}
