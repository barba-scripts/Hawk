package com.fiap.hawk.service;

import com.fiap.hawk.domain.AuditLogDocument;
import com.fiap.hawk.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
public class AuditService {

	private final AuditLogRepository auditLogRepository;

	public AuditService(AuditLogRepository auditLogRepository) {
		this.auditLogRepository = auditLogRepository;
	}

	public void log(Integer actorUserId, String action, String entityType, Integer entityId,
			Map<String, Object> before, Map<String, Object> after) {
		AuditLogDocument log = new AuditLogDocument();
		log.setActorUserId(actorUserId);
		log.setAction(action);
		log.setEntityType(entityType);
		log.setEntityId(entityId);
		log.setBefore(before);
		log.setAfter(after);
		log.setAt(Instant.now());
		auditLogRepository.save(log);
	}
}
