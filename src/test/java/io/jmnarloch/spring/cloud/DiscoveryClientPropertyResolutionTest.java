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
package io.jmnarloch.spring.cloud;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the registered service url expansion.
 *
 * @author Jakub Narloch
 */
@WebAppConfiguration
@IntegrationTest
@SpringApplicationConfiguration(classes = {DiscoveryClientPropertyResolutionTest.Application.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class DiscoveryClientPropertyResolutionTest {

    @Autowired
    private TestProperties properties;

    @Test
    public void shouldExpandServiceUrl() {

        assertEquals("http://127.0.0.1:9300/", properties.getUrl());
    }

    @EnableConfigurationProperties
    @EnableAutoConfiguration
    public static class Application {

        @Bean
        @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
        public DiscoveryClient discoveryClient() {
            final ServiceInstance instance = new DefaultServiceInstance("elasticsearch", "127.0.0.1", 9300, false);
            final DiscoveryClient discoveryClient = mock(DiscoveryClient.class);
            when(discoveryClient.getInstances(eq("elasticsearch"))).thenReturn(Arrays.asList(instance));
            return discoveryClient;
        }

        @Bean
        public TestProperties testProperties() {
            return new TestProperties();
        }
    }
}
