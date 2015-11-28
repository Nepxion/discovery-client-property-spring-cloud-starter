/**
 * Copyright (c) 2015 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jmnarloch.spring.cloud.discovery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.PropertySource;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Resolves a SpEL expressions for the ${discoveryClient.*} operations.
 *
 * @author Jakub Narloch
 */
public class DiscoveryClientPropertySource extends PropertySource<DiscoveryClient> {

    /**
     * Logger instance used by this class.
     */
    private final Logger logger = LoggerFactory.getLogger(DiscoveryClientPropertySource.class);

    /**
     * The default SpEL prefix.
     */
    public static final String PREFIX = "discoveryClient.";

    /**
     * Creates new instance of {@link DiscoveryClientPropertySource} with specific name.
     *
     * @param name the property source name
     */
    public DiscoveryClientPropertySource(String name) {
        super(name, new DelegatingDiscoveryClient());
    }

    /**
     * Sets the application context.
     *
     * @param applicationContext the application context
     * @throws BeansException if any error occurs
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ((DelegatingDiscoveryClient) getSource()).setApplicationContext(applicationContext);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getProperty(String name) {
        if (!name.startsWith(PREFIX)) {
            return null;
        }
        return handleDiscoveryOperation(name.substring(PREFIX.length()));
    }

    /**
     * Processes the operation declared through SPEL expression.
     *
     * @param input the input
     * @return the resolved expression value
     */
    private Object handleDiscoveryOperation(String input) {
        if (input.startsWith("service")) {
            final String serviceName = getArgument("service", input);
            return findOne(serviceName);
        } else if (input.startsWith("url")) {
            final String argument = getArgument("url", input);
            return expandUri(argument);
        }
        return null;
    }

    /**
     * Finds single service URI from the discovery service.
     *
     * @param serviceName the service name
     * @return the service url, or {@code null} if no service has been found
     */
    private URI findOne(String serviceName) {
        final ServiceInstance serviceInstance = findOneService(serviceName);
        return serviceInstance != null ? serviceInstance.getUri() : null;
    }

    /**
     * Expands the service URI.
     *
     * @param uri the input uri
     * @return the result uri
     */
    private URI expandUri(String uri) {
        try {
            final URI inputUri = URI.create(uri);
            final ServiceInstance serviceInstance = findOneService(inputUri.getHost());
            if (serviceInstance == null) {
                return null;
            }
            return new URI((serviceInstance.isSecure() ? "https" : "http"),
                    inputUri.getUserInfo(),
                    serviceInstance.getHost(),
                    serviceInstance.getPort(),
                    inputUri.getPath(),
                    inputUri.getQuery(),
                    inputUri.getFragment());
        } catch (URISyntaxException e) {
            logger.error("Unexpected error occurred when expanding the property URI", e);
            throw new RuntimeException("Could not parse URI value: " + uri, e);
        }
    }

    /**
     * Retrieves single service instance.
     *
     * @param serviceName the service name
     * @return the service instance
     */
    private ServiceInstance findOneService(String serviceName) {
        final List<ServiceInstance> instances = getSource().getInstances(serviceName);
        if(instances.isEmpty()) {
            logger.error("No service found matching name: {}", serviceName);
            return null;
        }
        return instances.get((int) (Math.random() * instances.size()));
    }

    /**
     * Matches the operation argument.
     *
     * @param operation the operation name
     * @param input     the input string
     * @return the argument value
     */
    private String getArgument(String operation, String input) {
        final int index = operation.length() + 1;
        if (index >= input.length()) {
            return null;
        }
        return input.substring(index, input.length() - 1);
    }

    /**
     * Simple implementation that delegates to the {@link DiscoveryClient} registered in application context.
     *
     * @author Jakub Narloch
     */
    private static class DelegatingDiscoveryClient implements DiscoveryClient {

        /**
         * The instance of {@link ApplicationContext}.
         */
        private ApplicationContext applicationContext;

        /**
         * Sets the application context.
         *
         * @param applicationContext the applciation context
         */
        public void setApplicationContext(ApplicationContext applicationContext) {
            this.applicationContext = applicationContext;
        }

        @Override
        public String description() {
            return getDiscoveryClient().description();
        }

        @Override
        public ServiceInstance getLocalServiceInstance() {
            return getDiscoveryClient().getLocalServiceInstance();
        }

        @Override
        public List<ServiceInstance> getInstances(String serviceId) {
            return getDiscoveryClient().getInstances(serviceId);
        }

        @Override
        public List<String> getServices() {
            return getDiscoveryClient().getServices();
        }

        private DiscoveryClient getDiscoveryClient() {
            return applicationContext.getBean(DiscoveryClient.class);
        }
    }
}
