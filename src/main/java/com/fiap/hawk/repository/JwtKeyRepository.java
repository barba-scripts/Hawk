package com.fiap.hawk.repository;

import com.fiap.hawk.domain.JwtKeyDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface JwtKeyRepository extends MongoRepository<JwtKeyDocument, String> {
	Optional<JwtKeyDocument> findByActiveTrue();
}
