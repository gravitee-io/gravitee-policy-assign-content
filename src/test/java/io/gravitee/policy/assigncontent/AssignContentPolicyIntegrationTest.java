/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.policy.assigncontent;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import io.gravitee.apim.gateway.tests.sdk.AbstractPolicyTest;
import io.gravitee.apim.gateway.tests.sdk.annotations.DeployApi;
import io.gravitee.apim.gateway.tests.sdk.annotations.GatewayTest;
import io.gravitee.policy.assigncontent.configuration.AssignContentPolicyConfiguration;
import io.vertx.core.http.HttpMethod;
import io.vertx.rxjava3.core.http.HttpClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * @author Yann TAVERNIER (yann.tavernier at graviteesource.com)
 * @author GraviteeSource Team
 */
@GatewayTest
@DeployApi("/apis/assign-content.json")
class AssignContentPolicyIntegrationTest extends AbstractPolicyTest<AssignContentPolicy, AssignContentPolicyConfiguration> {

    @Test
    @DisplayName("Should assign content, using Freemarker")
    void shouldAssignContent(HttpClient client) throws Exception {
        wiremock.stubFor(get("/endpoint").willReturn(ok("response from backend").withHeader("responseHeader", "responseHeaderValue")));

        final var obs = client
            .rxRequest(HttpMethod.GET, "/test")
            .flatMap(request -> request.putHeader("requestHeader", "requestHeaderValue").rxSend())
            .flatMapPublisher(response -> {
                assertThat(response.statusCode()).isEqualTo(200);
                return response.toFlowable();
            })
            .test();

        obs.await();
        obs
            .assertComplete()
            .assertValue(response -> {
                assertThat(response.toString()).isEqualTo("Response body built from header 'responseHeader': responseHeaderValue");
                return true;
            })
            .assertNoErrors();

        wiremock.verify(
            getRequestedFor(urlPathEqualTo("/endpoint"))
                .withRequestBody(equalTo("Request body built from header 'requestHeader': requestHeaderValue"))
        );
    }
}
