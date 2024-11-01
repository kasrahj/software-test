package mizdooni.response;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import mizdooni.response.serializer.HttpStatusSerializer;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    @JsonSerialize(using = HttpStatusSerializer.class)
    @Getter
    private HttpStatus status;
    @Getter
    private boolean success;
    @Getter
    private String error;
    @Getter
    private String message;
    @Getter
    private Object data;

    public Response(HttpStatus status, String message, boolean success, String error, Object data) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.message = message;
        this.success = success;
        this.error = error;
        this.data = data;
    }

    public static Response ok(String message) {
        return new Response(HttpStatus.OK, message, true, null, null);
    }

    public static Response ok(String message, Object data) {
        return new Response(HttpStatus.OK, message, true, null, data);
    }
}
