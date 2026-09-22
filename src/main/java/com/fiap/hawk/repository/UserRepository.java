package com.fiap.hawk.repository;

import com.fiap.hawk.domain.UserDocument;
import com.fiap.hawk.domain.Role;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<UserDocument, String> {
	Optional<UserDocument> findByEmailIgnoreCase(String email);
	Optional<UserDocument> findByPublicId(Integer publicId);
	boolean existsByEmailIgnoreCase(String email);
	List<UserDocument> findByRoleAndActive(Role role, boolean active);
	List<UserDocument> findByActiveTrue();
}
