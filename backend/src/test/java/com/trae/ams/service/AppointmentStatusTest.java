package com.trae.ams.service;

import com.trae.ams.entity.AmsAppointment;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AppointmentStatusTest {

    @Test
    public void testVerificationCodeGeneration() {
        AmsAppointment appt = new AmsAppointment();
        appt.setId(12345L);
        appt.setStoreId(101L);

        // Case 1: Pending Payment (1) - No Code
        appt.setStatus(AmsAppointment.STATUS_PENDING_PAYMENT);
        assertNull(appt.getVerificationCode(), "Should be null when pending payment");

        // Case 2: Pending Consumption (5) - Has Code
        appt.setStatus(AmsAppointment.STATUS_PENDING_CONSUMPTION);
        String code = appt.getVerificationCode();
        assertNotNull(code);
        // Format: V + StoreId(3) + Id(8)
        assertEquals("V10100012345", code);

        // Case 3: Completed (2) - No Code
        appt.setStatus(AmsAppointment.STATUS_COMPLETED);
        assertNull(appt.getVerificationCode(), "Should be null when completed");
        
        // Case 4: Null Store ID
        appt.setStoreId(null);
        appt.setStatus(AmsAppointment.STATUS_PENDING_CONSUMPTION);
        String codeNoStore = appt.getVerificationCode();
        assertEquals("V00000012345", codeNoStore);
    }

    @Test
    public void testStatusTransitionLogic() {
        // Simulate the logic in AppointmentServiceImpl.markAsPaid
        AmsAppointment appt = new AmsAppointment();
        appt.setStatus(AmsAppointment.STATUS_PENDING_PAYMENT);
        
        // Action: Mark as Paid
        appt.setPaymentStatus("PAID");
        if (appt.getStatus() == AmsAppointment.STATUS_PENDING_PAYMENT) {
            appt.setStatus(AmsAppointment.STATUS_PENDING_CONSUMPTION);
        }

        // Verify
        assertEquals(AmsAppointment.STATUS_PENDING_CONSUMPTION, appt.getStatus());
    }
}
