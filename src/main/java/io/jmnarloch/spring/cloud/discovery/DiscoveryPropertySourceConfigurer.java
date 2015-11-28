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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;

/**
 * @author Jakub Narloch
 */
public class DiscoveryPropertySourceConfigurer implements ApplicationListener<ApplicationEvent>, Ordered {


    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationEnvironmentPreparedEvent) {
            configurePropertySources(((ApplicationEnvironmentPreparedEvent) event).getEnvironment());
        } else if (event instanceof ApplicationPreparedEvent) {
            registerPostProcessors(((ApplicationPreparedEvent) event).getApplicationContext());
        }
    }

    protected void configurePropertySources(ConfigurableEnvironment environment) {
        environment.getPropertySources()
                .addAfter(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                        new DiscoveryPropertySource("discovery"));
    }

    protected void registerPostProcessors(ConfigurableApplicationContext applicationContext) {
        applicationContext.addBeanFactoryPostProcessor(new DiscoveryPropertySourceInitializer(applicationContext));
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE + 100;
    }

    private class DiscoveryPropertySourceInitializer implements BeanFactoryPostProcessor, Ordered {

        private ConfigurableApplicationContext applicationContext;

        public DiscoveryPropertySourceInitializer(ConfigurableApplicationContext applicationContext) {
            this.applicationContext = applicationContext;
        }

        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
            initialize(applicationContext.getEnvironment());
        }

        protected void initialize(ConfigurableEnvironment environment) {
            for (PropertySource<?> propertySource : environment.getPropertySources()) {
                if (propertySource instanceof DiscoveryPropertySource) {
                    ((DiscoveryPropertySource) propertySource).setApplicationContext(applicationContext);
                }
            }
        }

        @Override
        public int getOrder() {
            return HIGHEST_PRECEDENCE;
        }
    }
}
