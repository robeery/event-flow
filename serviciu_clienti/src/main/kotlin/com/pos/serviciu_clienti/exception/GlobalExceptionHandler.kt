package com.pos.serviciu_clienti.exception



import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    // 404 - Not Found
    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(ex: NoSuchElementException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(
                status = 404,
                error = "Not Found",
                message = ex.message ?: "Resursa nu a fost gasita"
            ))
    }

    // 409 - Conflict (email duplicat)
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleConflict(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ErrorResponse(
                status = 409,
                error = "Conflict",
                message = ex.message ?: "Conflict la salvarea datelor"
            ))
    }

    // 422 - Unprocessable Entity (stare invalida)
    @ExceptionHandler(IllegalStateException::class)
    fun handleUnprocessable(ex: IllegalStateException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(ErrorResponse(
                status = 422,
                error = "Unprocessable Entity",
                message = ex.message ?: "Operatia nu poate fi procesata"
            ))
    }

    // 400 - Bad Request (validari @NotBlank, @Email, etc.)
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult.fieldErrors
            .map { "${it.field}: ${it.defaultMessage}" }

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(
                status = 400,
                error = "Bad Request",
                message = "Validare esuata",
                details = errors
            ))
    }

    // 400 - Bad Request (body lipsa sau JSON invalid)
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleMissingBody(ex: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(
                status = 400,
                error = "Bad Request",
                message = "Request body lipseste sau JSON-ul este invalid"
            ))
    }
}

// DTO pentru raspunsuri de eroare
data class ErrorResponse(
    val status: Int,
    val error: String,
    val message: String,
    val details: List<String>? = null
)