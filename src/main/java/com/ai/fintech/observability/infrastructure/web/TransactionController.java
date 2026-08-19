package com.ai.fintech.observability.infrastructure.web;

import com.ai.fintech.observability.application.dtos.TransactionRequestDTO;
import com.ai.fintech.observability.application.dtos.TransactionResponseDTO;
import com.ai.fintech.observability.application.dtos.FraudResultDTO;
import com.ai.fintech.observability.application.dtos.FraudMetricsDTO;
import com.ai.fintech.observability.application.useCase.RegisterTransaction;
import com.ai.fintech.observability.application.useCase.GetTransactionById;
import com.ai.fintech.observability.application.useCase.ListCustomerTransactions;
import com.ai.fintech.observability.application.useCase.GetFraudMetrics;
import com.ai.fintech.observability.domain.model.Transaction;
import com.ai.fintech.observability.domain.model.CustomerId;
import com.ai.fintech.observability.domain.model.Money;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controlador REST que expone los endpoints para la gestión de transacciones y detección de fraude.
 * Anotado con OpenAPI/Swagger para generación automática de documentación de API.
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Transaction Management", description = "Endpoints for managing financial transactions and fraud detection")
public class TransactionController {

    private final RegisterTransaction registerTransaction;
    private final GetTransactionById getTransactionById;
    private final ListCustomerTransactions listCustomerTransactions;
    private final GetFraudMetrics getFraudMetrics;

    @Autowired
    public TransactionController(
            RegisterTransaction registerTransaction,
            GetTransactionById getTransactionById,
            ListCustomerTransactions listCustomerTransactions,
            GetFraudMetrics getFraudMetrics) {
        this.registerTransaction = registerTransaction;
        this.getTransactionById = getTransactionById;
        this.listCustomerTransactions = listCustomerTransactions;
        this.getFraudMetrics = getFraudMetrics;
    }

    /**
     * Registra una nueva transacción y la evalúa para fraude.
     *
     * @param request DTO con los datos de la transacción
     * @return resultado de la evaluación de fraude
     */
    @Operation(
        summary = "Register a new transaction and evaluate for fraud",
        description = "Creates a new financial transaction and evaluates it for potential fraud using rule-based detection"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transaction processed successfully",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = FraudResultDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/transactions")
    public ResponseEntity<FraudResultDTO> registerTransaction(@Valid @RequestBody TransactionRequestDTO request) {
        // Convertir DTO a objeto de dominio
        Transaction transaction = new Transaction(
            UUID.randomUUID(),
            new CustomerId(UUID.fromString(request.getCustomerId())),
            new Money(request.getAmount(), request.getCurrency()),
            request.getCurrency(),
            request.getTimestamp(),
            request.getMerchantCode()
        );

        // Ejecutar caso de uso
        com.ai.fintech.observability.domain.model.FraudResult fraudResult = registerTransaction.execute(transaction);

        // Convertir resultado a DTO
        FraudResultDTO response = new FraudResultDTO();
        response.setTransactionId(fraudResult.transactionId());
        response.setRiskScore(fraudResult.riskScore());
        response.setStatus(fraudResult.status());

        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene los detalles de una transacción por su ID.
     *
     * @param id ID de la transacción
     * @return detalles de la transacción
     */
    @Operation(
        summary = "Get transaction by ID",
        description = "Retrieves the details of a specific transaction by its unique identifier"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transaction found",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = TransactionResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Transaction not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/transactions/{id}")
    public ResponseEntity<TransactionResponseDTO> getTransactionById(@PathVariable String id) {
        Transaction transaction = getTransactionById.execute(id);
        if (transaction == null) {
            return ResponseEntity.notFound().build();
        }

        TransactionResponseDTO response = new TransactionResponseDTO();
        response.setId(transaction.id());
        response.setCustomerId(transaction.customerId().value().toString());
        response.setAmount(transaction.amount().amount());
        response.setCurrency(transaction.amount().currency());
        response.setTimestamp(transaction.timestamp().toString());
        response.setMerchantCode(transaction.merchantCode());

        return ResponseEntity.ok(response);
    }

    /**
     * Lista todas las transacciones de un cliente.
     *
     * @param customerId ID del cliente
     * @return lista de transacciones del cliente
     */
    @Operation(
        summary = "List customer transactions",
        description = "Retrieves all transactions for a specific customer"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = TransactionResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Customer not found or no transactions"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/customers/{customerId}/transactions")
    public ResponseEntity<List<TransactionResponseDTO>> listCustomerTransactions(@PathVariable String customerId) {
        List<Transaction> transactions = listCustomerTransactions.execute(customerId);
        
        List<TransactionResponseDTO> dtos = transactions.stream()
                .map(t -> {
                    TransactionResponseDTO dto = new TransactionResponseDTO();
                    dto.setId(t.id());
                    dto.setCustomerId(t.customerId().value().toString());
                    dto.setAmount(t.amount().amount());
                    dto.setCurrency(t.amount().currency());
                    dto.setTimestamp(t.timestamp().toString());
                    dto.setMerchantCode(t.merchantCode());
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Obtiene métricas de fraude.
     *
     * @return métricas de fraude
     */
    @Operation(
        summary = "Get fraud metrics",
        description = "Retrieves aggregated metrics about fraud detection performance"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Fraud metrics retrieved successfully",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = FraudMetricsDTO.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/metrics/fraud")
    public ResponseEntity<FraudMetricsDTO> getFraudMetrics() {
        com.ai.fintech.observability.application.port.FraudMetrics metrics = getFraudMetrics.execute();
        
        FraudMetricsDTO response = new FraudMetricsDTO();
        response.setTotalTransactions(metrics.getTotalTransactions());
        response.setApprovedTransactions(metrics.getApprovedTransactions());
        response.setReviewTransactions(metrics.getReviewTransactions());
        response.setAverageRiskScore(metrics.getAverageRiskScore());

        return ResponseEntity.ok(response);
    }
}