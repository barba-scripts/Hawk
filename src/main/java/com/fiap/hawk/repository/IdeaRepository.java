package com.fiap.hawk.repository;

import com.fiap.hawk.domain.IdeaDocument;
import com.fiap.hawk.domain.IdeaStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface IdeaRepository extends MongoRepository<IdeaDocument, String> {
	Optional<IdeaDocument> findByPublicId(Integer publicId);
	List<IdeaDocument> findByAuthorUserIdOrderByCreatedAtDesc(Integer authorUserId);
	List<IdeaDocument> findAllByOrderByCreatedAtDesc();
	long countByStatus(IdeaStatus status);
}
