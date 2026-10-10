package com.finstream.common.dto;

import java.math.BigDecimal;
import java.time.Instant;

public class FraudAlert {
    private String alertId;
    private String transactionId;
    private String accountId;
    private BigDecimal amount;
    private String ruleTriggered;
    private String severity;
    private Instant detectedAt;

    public FraudAlert() {}

    public FraudAlert(String alertId, String transactionId, String accountId, BigDecimal amount, String ruleTriggered, String severity, Instant detectedAt) {
        this.alertId = alertId;
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.amount = amount;
        this.ruleTriggered = ruleTriggered;
        this.severity = severity;
        this.detectedAt = detectedAt;
    }

    public String getAlertId() { return alertId; }
    public void setAlertId(String alertId) { this.alertId = alertId; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getRuleTriggered() { return ruleTriggered; }
    public void setRuleTriggered(String ruleTriggered) { this.ruleTriggered = ruleTriggered; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public Instant getDetectedAt() { return detectedAt; }
    public void setDetectedAt(Instant detectedAt) { this.detectedAt = detectedAt; }
}
