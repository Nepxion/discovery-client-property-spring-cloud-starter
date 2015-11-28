/**
 * Copyright (c) 2015 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
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
 * @author Jakub Narloch
 */
public class DiscoveryPropertySource extends PropertySource<DiscoveryClient> {

    private final Logger logger = LoggerFactory.getLogger(DiscoveryPropertySource.class);

    public static final String PREFIX = "discovery.";

    public DiscoveryPropertySource(String name) {
        super(name, new DelegatingDiscoveryClient());
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ((DelegatingDiscoveryClient) getSource()).setApplicationContext(applicationContext);
    }

    @Override
    public Object getProperty(String name) {
        if (!name.startsWith(PREFIX)) {
            return null;
        }
        return handleDiscoveryOperation(name.substring(PREFIX.length()));
    }

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

    private URI findOne(String serviceName) {
        final ServiceInstance serviceInstance = findOneService(serviceName);
        return serviceInstance != null ? serviceInstance.getUri() : null;
    }

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
            throw new RuntimeException("Could not parse URI value: " + uri, e);
        }
    }

    private ServiceInstance findOneService(String serviceName) {
        final List<ServiceInstance> instances = getSource().getInstances(serviceName);
        return !instances.isEmpty() ? instances.get(0) : null;
    }

    private String getArgument(String operation, String input) {
        final int index = operation.length() + 1;
        if (index >= input.length()) {
            return null;
        }
        return input.substring(index, input.length() - 1);
    }

    private static class DelegatingDiscoveryClient implements DiscoveryClient {

        private ApplicationContext applicationContext;

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
