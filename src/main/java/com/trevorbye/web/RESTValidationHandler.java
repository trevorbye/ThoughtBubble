package com.trevorbye.web;


import com.trevorbye.POJO.FieldErrorDTOWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;

@ControllerAdvice
@RestController
public class RESTValidationHandler {

    private MessageSource messageSource;

    @Autowired
    public RESTValidationHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    //Payload validation methods triggered by @Valid annotation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public FieldErrorDTOWrapper processValidationErrors(MethodArgumentNotValidException ex) {
        BindingResult result = ex.getBindingResult();
        List<FieldError> fieldErrors = result.getFieldErrors();

        return processFieldErrors(fieldErrors);

    }

    private FieldErrorDTOWrapper processFieldErrors(List<FieldError> fieldErrors) {
        FieldErrorDTOWrapper dtoWrapper = new FieldErrorDTOWrapper();

        for (FieldError fieldError : fieldErrors) {
            String localizedErrorMessage = resolveLocalizedErrorMessage(fieldError);
            dtoWrapper.addFieldError(fieldError.getField(), localizedErrorMessage);
        }

        return dtoWrapper;
    }

    private String resolveLocalizedErrorMessage(FieldError fieldError) {
        Locale currentLocale = LocaleContextHolder.getLocale();
        String localizedErrorMessage = messageSource.getMessage(fieldError,currentLocale);
        return localizedErrorMessage;
    }
}
