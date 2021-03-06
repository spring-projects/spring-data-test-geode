/*
 *  Copyright 2019 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 */
package org.springframework.data.gemfire.tests.integration;

import static org.springframework.data.gemfire.util.RuntimeExceptionFactory.newIllegalStateException;

import java.util.Optional;

import org.junit.After;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.gemfire.util.ArrayUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * The {@link SpringApplicationContextIntegrationTestsSupport} class is an extension of {@link IntegrationTestsSupport}
 * for writing Integration Tests involving a Spring {@link ApplicationContext}.
 *
 * This class contains functionality common to all Integration Tests involving a Spring {@link ApplicationContext}
 * and can be extended to create, acquire a reference and close the {@link ApplicationContext}
 * on test class completion, properly.
 *
 * @author John Blum
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.context.ApplicationEventPublisher
 * @see org.springframework.context.ApplicationEventPublisherAware
 * @see org.springframework.context.ConfigurableApplicationContext
 * @see org.springframework.context.annotation.AnnotationConfigApplicationContext
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public abstract class SpringApplicationContextIntegrationTestsSupport extends IntegrationTestsSupport
		implements ApplicationEventPublisherAware {

	private volatile ConfigurableApplicationContext applicationContext;

	@After
	public void closeApplicationContext() {
		getOptionalApplicationContext().ifPresent(ConfigurableApplicationContext::close);
	}

	protected ConfigurableApplicationContext newApplicationContext(Class<?>... annotatedClasses) {

		AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();

		applicationContext.register(ArrayUtils.nullSafeArray(annotatedClasses, Class.class));
		applicationContext.registerShutdownHook();
		processBeforeRefresh(applicationContext);
		applicationContext.refresh();

		return setApplicationContext(applicationContext);
	}

	protected @NonNull ConfigurableApplicationContext processBeforeRefresh(
			@NonNull ConfigurableApplicationContext applicationContext) {

		setApplicationEventPublisher(applicationContext);

		return applicationContext;
	}

	protected @Nullable <T extends ConfigurableApplicationContext> T setApplicationContext(
			@Nullable T applicationContext) {

		this.applicationContext = applicationContext;

		return applicationContext;
	}

	@SuppressWarnings("unchecked")
	protected @Nullable <T extends ConfigurableApplicationContext> T getApplicationContext() {
		return (T) this.applicationContext;
	}

	protected <T extends ConfigurableApplicationContext> Optional<T> getOptionalApplicationContext() {
		return Optional.ofNullable(getApplicationContext());
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void setApplicationEventPublisher(@NonNull ApplicationEventPublisher applicationEventPublisher) {

		if (applicationEventPublisher != null) {
			TestContextCacheLifecycleListenerAdapter.getInstance()
				.setApplicationEventPublisher(applicationEventPublisher);
		}
	}

	protected <T> T getBean(Class<T> requiredType) {

		return getOptionalApplicationContext()
			.map(applicationContext -> applicationContext.getBean(requiredType))
			.orElseThrow(() -> newIllegalStateException("An ApplicationContext was not initialized"));
	}

	protected <T> T getBean(String beanName, Class<T> requiredType) {

		return getOptionalApplicationContext()
			.map(applicationContext -> applicationContext.getBean(beanName, requiredType))
			.orElseThrow(() -> newIllegalStateException("An ApplicationContext was not initialized"));
	}
}
