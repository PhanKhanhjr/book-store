package phankhanh.book_store.util.error;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = InvalidException.class)
    public ResponseEntity<String> handleInvalidException(InvalidException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
