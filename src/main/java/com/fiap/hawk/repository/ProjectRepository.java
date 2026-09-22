package com.fiap.hawk.repository;

import com.fiap.hawk.domain.ProjectDocument;
import com.fiap.hawk.domain.ProjectStage;
import com.fiap.hawk.domain.ProjectStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends MongoRepository<ProjectDocument, String> {
	Optional<ProjectDocument> findByPublicId(Integer publicId);
	Optional<ProjectDocument> findByIdeaId(Integer ideaId);
	boolean existsByIdeaId(Integer ideaId);
	List<ProjectDocument> findAllByOrderByCreatedAtDesc();
	long countByStatus(ProjectStatus status);
	long countByStage(ProjectStage stage);
}
