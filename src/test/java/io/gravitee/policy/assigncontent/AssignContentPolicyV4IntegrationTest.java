/*
 * Copyright © 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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

import io.gravitee.apim.gateway.tests.sdk.annotations.DeployApi;
import io.gravitee.apim.gateway.tests.sdk.connector.fakes.MessageStorage;
import io.gravitee.common.http.MediaType;
import io.gravitee.gateway.api.http.HttpHeaderNames;
import io.vertx.core.http.HttpMethod;
import io.vertx.rxjava3.core.buffer.Buffer;
import io.vertx.rxjava3.core.http.HttpClient;
import io.vertx.rxjava3.core.http.HttpClientResponse;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
public class AssignContentPolicyV4IntegrationTest {

    @Nested
    @DeployApi("/apis/v4/assign-content-proxy.json")
    class HttpProxy extends V4EngineTest {

        @Test
        @DisplayName("Should assign content on HTTP Request & Response body, using Freemarker")
        void shouldAssignContentOnBody(HttpClient client) throws Exception {
            final String responseFromBackend = "response from backend";
            wiremock.stubFor(get("/endpoint").willReturn(ok(responseFromBackend).withHeader("responseHeader", "responseHeaderValue")));

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
                    assertThat(response)
                        .hasToString(
                            "Response body built from header 'responseHeader' and content: responseHeaderValue / " + responseFromBackend
                        );
                    return true;
                })
                .assertNoErrors();

            wiremock.verify(
                getRequestedFor(urlPathEqualTo("/endpoint"))
                    .withRequestBody(equalTo("Request body built from header 'requestHeader' and content: requestHeaderValue"))
            );
        }
    }

    @Nested
    @DeployApi("/apis/v4/assign-content-message-subscription.json")
    class OnMessageResponse extends V4EngineTest {

        @Test
        @DisplayName("Should assign content on message body, using Freemarker")
        void shouldAssignContentOnMessage(HttpClient client) {
            client
                .rxRequest(HttpMethod.GET, "/subscribe-assign-content")
                .flatMap(request -> {
                    request.putHeader(HttpHeaderNames.ACCEPT.toString(), MediaType.TEXT_EVENT_STREAM);
                    return request.rxSend();
                })
                .flatMapPublisher(HttpClientResponse::toFlowable)
                .map(Buffer::toString)
                .filter(content -> !content.startsWith("retry")) // ignore retry
                .filter(content -> !content.equals(":\n\n")) // ignore heartbeat
                .test()
                .awaitCount(1)
                .assertValue(content -> {
                    assertThat(content)
                        .contains("event: message")
                        .contains(
                            "data: " +
                            "Body built from message header 'msgHeader' and content: messageHeaderValue / Content Sent by Mock policy"
                        );
                    return true;
                });
        }
    }

    @Nested
    @DeployApi("/apis/v4/assign-content-message-publish.json")
    class OnMessageRequest extends V4EngineTest {

        private MessageStorage messageStorage;

        @BeforeEach
        void setUp() {
            messageStorage = getBean(MessageStorage.class);
        }

        @Test
        @DisplayName("Should assign content on message body, using Freemarker")
        void shouldAssignContentOnMessage(HttpClient client) throws Exception {
            final var obs = client
                .rxRequest(HttpMethod.POST, "/publish-assign-content")
                .flatMap(request -> request.putHeader("msgHeader", "headerValue").rxSend("Content Sent to Mock policy"))
                .flatMapPublisher(response -> {
                    assertThat(response.statusCode()).isEqualTo(202);
                    return response.toFlowable();
                })
                .test();

            obs.await(5, TimeUnit.SECONDS);

            messageStorage
                .subject()
                .test()
                .assertValue(message -> {
                    assertThat(message.content())
                        .hasToString("Body built from message header 'msgHeader' and content: headerValue / Content Sent to Mock policy");
                    return true;
                })
                .dispose();
        }
    }
}
