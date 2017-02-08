package com.trevorbye.POJO;

import java.util.ArrayList;
import java.util.List;

public class FieldErrorDTOWrapper {
    private List<FieldErrorDTO> fieldErrors = new ArrayList<>();

    public FieldErrorDTOWrapper() {
    }

    public List<FieldErrorDTO> getFieldErrors() {
        return fieldErrors;
    }

    public void addFieldError(String path, String message) {
        FieldErrorDTO error = new FieldErrorDTO(path, message);
        fieldErrors.add(error);
    }
}
