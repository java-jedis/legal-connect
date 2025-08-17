package com.javajedis.legalconnect.scheduling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ScheduleTypeTest {

    @Test
    void testAllEnumValues() {
        ScheduleType[] values = ScheduleType.values();
        
        assertEquals(17, values.length);
        
        // Verify all expected values exist
        assertTrue(contains(values, ScheduleType.MEETING));
        assertTrue(contains(values, ScheduleType.COURT_HEARING));
        assertTrue(contains(values, ScheduleType.DOCUMENT_SUBMISSION_DEADLINE));
        assertTrue(contains(values, ScheduleType.PAYMENT_DUE_DATE));
        assertTrue(contains(values, ScheduleType.FOLLOW_UP_CALL));
        assertTrue(contains(values, ScheduleType.MEDIATION_SESSION));
        assertTrue(contains(values, ScheduleType.ARBITRATION_SESSION));
        assertTrue(contains(values, ScheduleType.LEGAL_NOTICE_RESPONSE_DEADLINE));
        assertTrue(contains(values, ScheduleType.CONTRACT_SIGNING));
        assertTrue(contains(values, ScheduleType.DISCOVERY_DATE));
        assertTrue(contains(values, ScheduleType.DEPOSITION_DATE));
        assertTrue(contains(values, ScheduleType.EVIDENCE_COLLECTION_REMINDER));
        assertTrue(contains(values, ScheduleType.LEGAL_ADVICE_SESSION));
        assertTrue(contains(values, ScheduleType.BAIL_HEARING));
        assertTrue(contains(values, ScheduleType.PROBATION_MEETING));
        assertTrue(contains(values, ScheduleType.PAROLE_MEETING));
        assertTrue(contains(values, ScheduleType.COMPLIANCE_DEADLINE));
    }

    @Test
    void testEnumValueOf() {
        assertEquals(ScheduleType.MEETING, ScheduleType.valueOf("MEETING"));
        assertEquals(ScheduleType.COURT_HEARING, ScheduleType.valueOf("COURT_HEARING"));
        assertEquals(ScheduleType.DOCUMENT_SUBMISSION_DEADLINE, ScheduleType.valueOf("DOCUMENT_SUBMISSION_DEADLINE"));
        assertEquals(ScheduleType.PAYMENT_DUE_DATE, ScheduleType.valueOf("PAYMENT_DUE_DATE"));
        assertEquals(ScheduleType.FOLLOW_UP_CALL, ScheduleType.valueOf("FOLLOW_UP_CALL"));
        assertEquals(ScheduleType.MEDIATION_SESSION, ScheduleType.valueOf("MEDIATION_SESSION"));
        assertEquals(ScheduleType.ARBITRATION_SESSION, ScheduleType.valueOf("ARBITRATION_SESSION"));
        assertEquals(ScheduleType.LEGAL_NOTICE_RESPONSE_DEADLINE, ScheduleType.valueOf("LEGAL_NOTICE_RESPONSE_DEADLINE"));
        assertEquals(ScheduleType.CONTRACT_SIGNING, ScheduleType.valueOf("CONTRACT_SIGNING"));
        assertEquals(ScheduleType.DISCOVERY_DATE, ScheduleType.valueOf("DISCOVERY_DATE"));
        assertEquals(ScheduleType.DEPOSITION_DATE, ScheduleType.valueOf("DEPOSITION_DATE"));
        assertEquals(ScheduleType.EVIDENCE_COLLECTION_REMINDER, ScheduleType.valueOf("EVIDENCE_COLLECTION_REMINDER"));
        assertEquals(ScheduleType.LEGAL_ADVICE_SESSION, ScheduleType.valueOf("LEGAL_ADVICE_SESSION"));
        assertEquals(ScheduleType.BAIL_HEARING, ScheduleType.valueOf("BAIL_HEARING"));
        assertEquals(ScheduleType.PROBATION_MEETING, ScheduleType.valueOf("PROBATION_MEETING"));
        assertEquals(ScheduleType.PAROLE_MEETING, ScheduleType.valueOf("PAROLE_MEETING"));
        assertEquals(ScheduleType.COMPLIANCE_DEADLINE, ScheduleType.valueOf("COMPLIANCE_DEADLINE"));
    }

    @Test
    void testEnumName() {
        assertEquals("MEETING", ScheduleType.MEETING.name());
        assertEquals("COURT_HEARING", ScheduleType.COURT_HEARING.name());
        assertEquals("DOCUMENT_SUBMISSION_DEADLINE", ScheduleType.DOCUMENT_SUBMISSION_DEADLINE.name());
        assertEquals("PAYMENT_DUE_DATE", ScheduleType.PAYMENT_DUE_DATE.name());
        assertEquals("FOLLOW_UP_CALL", ScheduleType.FOLLOW_UP_CALL.name());
        assertEquals("MEDIATION_SESSION", ScheduleType.MEDIATION_SESSION.name());
        assertEquals("ARBITRATION_SESSION", ScheduleType.ARBITRATION_SESSION.name());
        assertEquals("LEGAL_NOTICE_RESPONSE_DEADLINE", ScheduleType.LEGAL_NOTICE_RESPONSE_DEADLINE.name());
        assertEquals("CONTRACT_SIGNING", ScheduleType.CONTRACT_SIGNING.name());
        assertEquals("DISCOVERY_DATE", ScheduleType.DISCOVERY_DATE.name());
        assertEquals("DEPOSITION_DATE", ScheduleType.DEPOSITION_DATE.name());
        assertEquals("EVIDENCE_COLLECTION_REMINDER", ScheduleType.EVIDENCE_COLLECTION_REMINDER.name());
        assertEquals("LEGAL_ADVICE_SESSION", ScheduleType.LEGAL_ADVICE_SESSION.name());
        assertEquals("BAIL_HEARING", ScheduleType.BAIL_HEARING.name());
        assertEquals("PROBATION_MEETING", ScheduleType.PROBATION_MEETING.name());
        assertEquals("PAROLE_MEETING", ScheduleType.PAROLE_MEETING.name());
        assertEquals("COMPLIANCE_DEADLINE", ScheduleType.COMPLIANCE_DEADLINE.name());
    }

    @Test
    void testEnumToString() {
        for (ScheduleType scheduleType : ScheduleType.values()) {
            String toString = scheduleType.toString();
            assertNotNull(toString);
            assertEquals(scheduleType.name(), toString);
        }
    }

    @Test
    void testEnumOrdinal() {
        ScheduleType[] values = ScheduleType.values();
        for (int i = 0; i < values.length; i++) {
            assertEquals(i, values[i].ordinal());
        }
    }

    private boolean contains(ScheduleType[] values, ScheduleType target) {
        for (ScheduleType value : values) {
            if (value == target) {
                return true;
            }
        }
        return false;
    }
} 