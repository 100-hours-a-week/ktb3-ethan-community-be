package org.restapi.springrestapi.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class SeedLoader {
	private final ObjectMapper mapper;

	@Value("${app.data.dir}")
	private String dataDir;

	public SeedLoader(ObjectMapper mapper) {
		// Use Spring-configured ObjectMapper so global settings (e.g., SNAKE_CASE) apply
		this.mapper = mapper.findAndRegisterModules();
	}

	public <T> List<T> load(String name, Class<T> type) {
		Path path = Paths.get(dataDir, name + ".json");
		if (!Files.exists(path)) {
			return List.of();
		}
		try (BufferedReader in = Files.newBufferedReader(path)) {
			JavaType listType = mapper.getTypeFactory().constructCollectionType(List.class, type);
			return mapper.readValue(in, listType);
		} catch (IOException e) {
			return List.of();
		}
	}
}

