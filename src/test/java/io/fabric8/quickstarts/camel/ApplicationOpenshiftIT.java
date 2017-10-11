/*
 * Copyright 2005-2015 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */
package io.fabric8.quickstarts.camel;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.openshift.client.OpenShiftClient;
import org.assertj.core.api.Assertions;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static io.fabric8.kubernetes.assertions.Assertions.assertThat;

@RunWith(Arquillian.class)
public class ApplicationOpenshiftIT {

    @ArquillianResource
    private KubernetesClient client;

    @Test
    @RunAsClient
    @InSequence(1)
    public void testAppProvisionsRunningPods() {
        assertThat(client).deployments().pods().isPodReadyForPeriod();
    }

    @Test
    @RunAsClient
    @InSequence(2)
    public void booksTest() {
        TestRestTemplate restTemplate = new TestRestTemplate();
        ResponseEntity<List<Book>> response = restTemplate.exchange("http://" + client.adapt(OpenShiftClient.class).routes().withName("camel-rest-jpa").get().getSpec().getHost() + "/camel-rest-jpa/books",
            HttpMethod.GET, null, new ParameterizedTypeReference<List<Book>>(){});
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<Book> books = response.getBody();
        Assertions.assertThat(books).hasSize(2);
        Assertions.assertThat(books.get(0))
            .hasFieldOrPropertyWithValue("item", "Camel")
            .hasFieldOrPropertyWithValue("description", "Camel in Action");
        Assertions.assertThat(books.get(1))
            .hasFieldOrPropertyWithValue("item", "ActiveMQ")
            .hasFieldOrPropertyWithValue("description", "ActiveMQ in Action");
    }
}