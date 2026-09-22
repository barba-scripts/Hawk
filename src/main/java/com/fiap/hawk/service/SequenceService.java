package com.fiap.hawk.service;

import com.fiap.hawk.domain.CounterDocument;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class SequenceService {

	private final MongoTemplate mongoTemplate;

	public SequenceService(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	public long next(String sequenceName) {
		Query query = Query.query(Criteria.where("_id").is(sequenceName));
		Update update = new Update().inc("seq", 1);
		FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true).upsert(true);
		CounterDocument counter = mongoTemplate.findAndModify(query, update, options, CounterDocument.class);
		if (counter == null) {
			throw new IllegalStateException("Failed to allocate sequence: " + sequenceName);
		}
		return counter.getSeq();
	}
}
