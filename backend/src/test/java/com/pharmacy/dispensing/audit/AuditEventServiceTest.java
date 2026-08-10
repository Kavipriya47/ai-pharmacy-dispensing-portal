package com.pharmacy.dispensing.audit;

import com.pharmacy.dispensing.audit.entity.AuditEvent;
import com.pharmacy.dispensing.audit.repository.AuditEventRepository;
import com.pharmacy.dispensing.audit.service.AuditEventService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditEventServiceTest {

    @Mock
    private AuditEventRepository auditEventRepository;

    @InjectMocks
    private AuditEventService auditEventService;

    @Test
    void shouldLogBusinessAuditEvent() {
        auditEventService.logEvent("USER_LOGIN", "pharmacist", "User logged in", "{}", "127.0.0.1");

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditEventRepository).save(captor.capture());

        AuditEvent saved = captor.getValue();
        assertEquals("USER_LOGIN", saved.getEventType());
        assertEquals("pharmacist", saved.getPerformedBy());
        assertEquals("User logged in", saved.getDescription());
        assertEquals("127.0.0.1", saved.getIpAddress());
    }
}
