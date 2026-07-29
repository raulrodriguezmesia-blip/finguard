package com.example.finguard.exception;

/**
 * Excepción lanzada cuando una transacción no supera las validaciones de negocio.
 */
public class InvalidTransactionException extends RuntimeException {
    public InvalidTransactionException(String message) {
        super(message);
    }
}
