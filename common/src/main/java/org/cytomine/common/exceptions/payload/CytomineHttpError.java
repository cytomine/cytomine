package org.cytomine.common.exceptions.payload;

public record CytomineHttpError(HttpErrorMessage errors) {

    public static CytomineHttpError of(String message) {
        return new CytomineHttpError(new HttpErrorMessage(message));
    }

}
