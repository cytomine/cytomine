package be.cytomine.exceptions.handler;

public record ErrorResponseDto(ErrorMessage errors) {
    public record ErrorMessage(String message) {}

    public static ErrorResponseDto of(String message) {
        return new ErrorResponseDto(new ErrorMessage(message));
    }
}
