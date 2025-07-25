/*
 * Copyright 2018-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.config.server.environment;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import io.micrometer.observation.ObservationRegistry;
import org.eclipse.jgit.transport.HttpTransport;

import org.springframework.cloud.config.server.config.ConfigServerProperties;
import org.springframework.cloud.config.server.support.GitCredentialsProviderFactory;
import org.springframework.cloud.config.server.support.TransportConfigCallbackFactory;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * @author Dylan Roberts
 */
public class MultipleJGitEnvironmentRepositoryFactory
		implements EnvironmentRepositoryFactory<MultipleJGitEnvironmentRepository, MultipleJGitEnvironmentProperties> {

	private ConfigurableEnvironment environment;

	private ConfigServerProperties server;

	private Optional<ConfigurableHttpConnectionFactory> connectionFactory;

	private final TransportConfigCallbackFactory transportConfigCallbackFactory;

	private final GitCredentialsProviderFactory gitCredentialsProviderFactory;

	private final List<HttpClient4BuilderCustomizer> customizers;

	public MultipleJGitEnvironmentRepositoryFactory(ConfigurableEnvironment environment, ConfigServerProperties server,
			Optional<ConfigurableHttpConnectionFactory> connectionFactory,
			TransportConfigCallbackFactory transportConfigCallbackFactory,
			GitCredentialsProviderFactory gitCredentialsProviderFactory) {
		this(environment, server, connectionFactory, transportConfigCallbackFactory, gitCredentialsProviderFactory,
				Collections.EMPTY_LIST);
	}

	public MultipleJGitEnvironmentRepositoryFactory(ConfigurableEnvironment environment, ConfigServerProperties server,
			Optional<ConfigurableHttpConnectionFactory> connectionFactory,
			TransportConfigCallbackFactory transportConfigCallbackFactory,
			GitCredentialsProviderFactory gitCredentialsProviderFactory,
			List<HttpClient4BuilderCustomizer> customizers) {
		this.environment = environment;
		this.server = server;
		this.connectionFactory = connectionFactory;
		this.transportConfigCallbackFactory = transportConfigCallbackFactory;
		this.gitCredentialsProviderFactory = gitCredentialsProviderFactory;
		this.customizers = customizers;
	}

	@Override
	public MultipleJGitEnvironmentRepository build(MultipleJGitEnvironmentProperties environmentProperties)
			throws Exception {
		if (this.connectionFactory.isPresent()) {
			HttpTransport.setConnectionFactory(this.connectionFactory.get());
			this.connectionFactory.get().addConfiguration(environmentProperties, this.customizers);
		}

		MultipleJGitEnvironmentRepository repository = new MultipleJGitEnvironmentRepository(this.environment,
				environmentProperties, ObservationRegistry.NOOP);
		repository.setTransportConfigCallback(transportConfigCallbackFactory.build(environmentProperties));
		if (this.server.getDefaultLabel() != null) {
			repository.setDefaultLabel(this.server.getDefaultLabel());
		}
		repository.setGitCredentialsProviderFactory(gitCredentialsProviderFactory);
		repository.getRepos()
			.forEach((name, repo) -> repo.setGitCredentialsProviderFactory(gitCredentialsProviderFactory));
		return repository;
	}

}
