/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.Manoj.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class InternalApplicationError extends BaseException {
    public InternalApplicationError() {
        super();
        responseStatus = 500;
    }
    
    public InternalApplicationError(String message) {
        super(message);
        responseStatus = 500;
    }
    
    public InternalApplicationError(String message, Throwable cause) {
        super(message, cause);
        responseStatus = 500;
    }
}
