// application/port/out/PaymentPort.java
package com.serviloc.mission.application.port.out;

public interface PaymentPort {
    void releaseTransaction(String transactionId);
}