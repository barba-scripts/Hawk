package com.fiap.hawk.repository;

import com.fiap.hawk.domain.StrategyDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface StrategyRepository extends MongoRepository<StrategyDocument, String> {
	Optional<StrategyDocument> findByPublicId(Integer publicId);
	List<StrategyDocument> findByActiveTrueOrderByCreatedAtDesc();
	Optional<StrategyDocument> findFirstByActiveTrueOrderByCreatedAtDesc();
}
