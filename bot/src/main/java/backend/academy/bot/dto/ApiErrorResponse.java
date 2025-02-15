package backend.academy.bot.dto;

public class ApiErrorResponse {
    private String description;
    private String code;
    private String exceptionName;
    private String exceptionMessage;
    private String stacktrace;

    public ApiErrorResponse(String description, String code, String exceptionName,
                            String exceptionMessage, String stacktrace) {
        this.description = description;
        this.code = code;
        this.exceptionName = exceptionName;
        this.exceptionMessage = exceptionMessage;
        this.stacktrace = stacktrace;
    }

    public String getDescription() {
        return description;
    }

    public String getCode() {
        return code;
    }

    public String getExceptionName() {
        return exceptionName;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public String getStacktrace() {
        return stacktrace;
    }
}
