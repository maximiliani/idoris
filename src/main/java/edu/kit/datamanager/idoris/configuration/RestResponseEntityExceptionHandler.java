/*
 * Copyright (c) 2024 Karlsruhe Institute of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.kit.datamanager.idoris.configuration;

import lombok.extern.java.Log;
import org.springframework.data.rest.core.RepositoryConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.method.MethodValidationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Log
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
//            String errors = ex.getAllErrors()
//                    .stream()
//                    .map(ObjectError::toString)
//                    .collect(Collectors.joining("\n"));

//            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(ex.getAllErrors(), HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleMethodValidationException(MethodValidationException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
//            String errors = ex.getAllValidationResults()
//                    .stream()
//                    .map(ParameterValidationResult::toString)
//                    .collect(Collectors.joining("\n"));
//
//            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>(ex.getAllErrors(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({RepositoryConstraintViolationException.class})
    public ResponseEntity<Object> handleAccessDeniedException(Exception ex) {
        RepositoryConstraintViolationException nevEx = (RepositoryConstraintViolationException) ex;
        return new ResponseEntity<>(nevEx.getErrors().getAllErrors(), new HttpHeaders(), HttpStatus.NOT_ACCEPTABLE);
    }
}
