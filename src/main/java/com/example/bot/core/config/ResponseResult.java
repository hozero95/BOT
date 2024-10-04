package com.example.bot.biz.dto;

import lombok.*;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@RequiredArgsConstructor(staticName = "of")
public class ResponseResult<D> {
    private final HttpStatus status;
    private final String message;
    private final D data;

    /**
     * HTTP 통신 성공
     * @param message
     * @param data
     * @return ResponseResult<D>
     * @param <D>
     */
    public static <D> ResponseResult<D> ofSuccess(String message, D data) {
        return new ResponseResult<>(HttpStatus.OK, message == null ? "success" : message, data);
    }

    /**
     * HTTP 통신 실패
     * @param status
     * @param message
     * @return
     * @param <D>
     */
    public static <D> ResponseResult<D> ofFailure(HttpStatus status, String message) {
        return new ResponseResult<>(status == null ? HttpStatus.BAD_REQUEST : status, message == null ? "failure" : message, null);
    }
}
