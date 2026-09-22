package com.fiap.hawk.repository;

import com.fiap.hawk.domain.AuditLogDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AuditLogRepository extends MongoRepository<AuditLogDocument, String> {
}
