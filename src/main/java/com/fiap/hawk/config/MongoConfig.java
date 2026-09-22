package com.fiap.hawk.config;

import com.fiap.hawk.domain.IdeaImpact;
import com.fiap.hawk.domain.IdeaStatus;
import com.fiap.hawk.domain.ProjectStage;
import com.fiap.hawk.domain.ProjectStatus;
import com.fiap.hawk.domain.Role;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.util.List;

@Configuration
public class MongoConfig {

	@Bean
	MongoCustomConversions mongoCustomConversions() {
		return new MongoCustomConversions(List.of(
				new RoleWriteConverter(), new RoleReadConverter(),
				new IdeaImpactWriteConverter(), new IdeaImpactReadConverter(),
				new IdeaStatusWriteConverter(), new IdeaStatusReadConverter(),
				new ProjectStageWriteConverter(), new ProjectStageReadConverter(),
				new ProjectStatusWriteConverter(), new ProjectStatusReadConverter()
		));
	}

	@WritingConverter
	static class RoleWriteConverter implements Converter<Role, String> {
		@Override
		public String convert(Role source) {
			return source.getValue();
		}
	}

	@ReadingConverter
	static class RoleReadConverter implements Converter<String, Role> {
		@Override
		public Role convert(String source) {
			return Role.fromValue(source);
		}
	}

	@WritingConverter
	static class IdeaImpactWriteConverter implements Converter<IdeaImpact, String> {
		@Override
		public String convert(IdeaImpact source) {
			return source.getValue();
		}
	}

	@ReadingConverter
	static class IdeaImpactReadConverter implements Converter<String, IdeaImpact> {
		@Override
		public IdeaImpact convert(String source) {
			return IdeaImpact.fromValue(source);
		}
	}

	@WritingConverter
	static class IdeaStatusWriteConverter implements Converter<IdeaStatus, String> {
		@Override
		public String convert(IdeaStatus source) {
			return source.getValue();
		}
	}

	@ReadingConverter
	static class IdeaStatusReadConverter implements Converter<String, IdeaStatus> {
		@Override
		public IdeaStatus convert(String source) {
			return IdeaStatus.fromValue(source);
		}
	}

	@WritingConverter
	static class ProjectStageWriteConverter implements Converter<ProjectStage, String> {
		@Override
		public String convert(ProjectStage source) {
			return source.getValue();
		}
	}

	@ReadingConverter
	static class ProjectStageReadConverter implements Converter<String, ProjectStage> {
		@Override
		public ProjectStage convert(String source) {
			return ProjectStage.fromValue(source);
		}
	}

	@WritingConverter
	static class ProjectStatusWriteConverter implements Converter<ProjectStatus, String> {
		@Override
		public String convert(ProjectStatus source) {
			return source.getValue();
		}
	}

	@ReadingConverter
	static class ProjectStatusReadConverter implements Converter<String, ProjectStatus> {
		@Override
		public ProjectStatus convert(String source) {
			return ProjectStatus.fromValue(source);
		}
	}
}
