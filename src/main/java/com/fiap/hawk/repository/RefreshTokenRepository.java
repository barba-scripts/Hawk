package com.fiap.hawk.repository;

import com.fiap.hawk.domain.RefreshTokenDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends MongoRepository<RefreshTokenDocument, String> {
	Optional<RefreshTokenDocument> findByTokenHash(String tokenHash);
	void deleteByUserPublicId(Integer userPublicId);
}
