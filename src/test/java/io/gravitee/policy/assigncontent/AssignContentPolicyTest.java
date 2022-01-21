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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import io.gravitee.common.http.HttpStatusCode;
import io.gravitee.common.util.ServiceLoaderHelper;
import io.gravitee.gateway.api.ExecutionContext;
import io.gravitee.gateway.api.Request;
import io.gravitee.gateway.api.Response;
import io.gravitee.gateway.api.buffer.Buffer;
import io.gravitee.gateway.api.buffer.BufferFactory;
import io.gravitee.gateway.api.http.HttpHeaders;
import io.gravitee.gateway.api.stream.ReadWriteStream;
import io.gravitee.policy.api.PolicyChain;
import io.gravitee.policy.api.PolicyResult;
import io.gravitee.policy.assigncontent.configuration.AssignContentPolicyConfiguration;
import io.gravitee.policy.assigncontent.configuration.PolicyScope;
import io.gravitee.reporter.api.http.Metrics;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
@RunWith(MockitoJUnitRunner.class)
public class AssignContentPolicyTest {

    @Mock
    private Request request;

    @Mock
    private Response response;

    @Mock
    private PolicyChain chain;

    @Mock
    private ExecutionContext context;

    @Mock
    private AssignContentPolicyConfiguration configuration;

    private BufferFactory factory = ServiceLoaderHelper.loadFactory(BufferFactory.class);

    private Metrics metrics = Metrics.on(System.currentTimeMillis()).build();

    @Before
    public void beforeAll() {
        when(request.headers()).thenReturn(HttpHeaders.create());
        when(request.metrics()).thenReturn(metrics);
    }

    @Test
    public void shouldContinueRequestStreaming_templateHeaders() {
        HttpHeaders headers = HttpHeaders.create().set("my-header", "header-value");

        when(request.headers()).thenReturn(headers);

        when(configuration.getBody()).thenReturn("${request.headers['my-header']}");
        when(configuration.getScope()).thenReturn(PolicyScope.REQUEST);

        Buffer buffer = factory.buffer("{\"name\":1}");
        ReadWriteStream<Buffer> stream = new AssignContentPolicy(configuration).onRequestContent(request, context, chain);
        stream.bodyHandler(buffer1 -> Assert.assertEquals("header-value", buffer1.toString()));

        stream.end(buffer);

        verify(chain, times(1)).streamFailWith(any(PolicyResult.class));
    }

    @Test
    public void shouldContinueRequestStreaming_templateHeadersIndexed() {
        HttpHeaders headers = HttpHeaders.create().set("my-header", "header-value");

        when(request.headers()).thenReturn(headers);

        when(configuration.getBody()).thenReturn("${request.headers['my-header'][0]}");
        when(configuration.getScope()).thenReturn(PolicyScope.REQUEST);

        Buffer buffer = factory.buffer("{\"name\":1}");
        ReadWriteStream<Buffer> stream = new AssignContentPolicy(configuration).onRequestContent(request, context, chain);
        stream.bodyHandler(buffer1 -> Assert.assertEquals("header-value", buffer1.toString()));

        stream.end(buffer);

        verify(chain, never()).streamFailWith(any(PolicyResult.class));
    }

    @Test
    public void shouldContinueRequestStreaming_validTemplate() {
        when(configuration.getBody()).thenReturn("root { ${request.content} }");
        when(configuration.getScope()).thenReturn(PolicyScope.REQUEST);

        Buffer buffer = factory.buffer("{\"name\":1}");
        ReadWriteStream<Buffer> stream = new AssignContentPolicy(configuration).onRequestContent(request, context, chain);
        stream.bodyHandler(buffer1 -> Assert.assertEquals("root { {\"name\":1} }", buffer1.toString()));

        stream.end(buffer);

        verify(chain, never()).streamFailWith(any(PolicyResult.class));
    }

    @Test
    public void shouldNotContinueRequestStreaming_unsafeTemplate() {
        when(configuration.getBody()).thenReturn("${request.getClass().getProtectionDomain()}");
        when(configuration.getScope()).thenReturn(PolicyScope.REQUEST);

        Buffer buffer = factory.buffer("{\"name\":1}");
        ReadWriteStream<Buffer> stream = new AssignContentPolicy(configuration).onRequestContent(request, context, chain);
        stream.end(buffer);

        ArgumentCaptor<PolicyResult> policyResult = ArgumentCaptor.forClass(PolicyResult.class);
        verify(chain, times(1)).streamFailWith(policyResult.capture());
        PolicyResult value = policyResult.getValue();
        assertThat(value.statusCode()).isEqualTo(HttpStatusCode.INTERNAL_SERVER_ERROR_500);
    }

    @Test
    public void shouldNotContinueRequestStreaming_couldNotCreateInstance() {
        when(configuration.getBody()).thenReturn("<#assign foo = \"com.example.FooDirective\"?new()>");
        when(configuration.getScope()).thenReturn(PolicyScope.REQUEST);

        Buffer buffer = factory.buffer("{\"name\":1}");
        ReadWriteStream<Buffer> stream = new AssignContentPolicy(configuration).onRequestContent(request, context, chain);
        stream.end(buffer);

        ArgumentCaptor<PolicyResult> policyResult = ArgumentCaptor.forClass(PolicyResult.class);
        verify(chain, times(1)).streamFailWith(policyResult.capture());
        PolicyResult value = policyResult.getValue();
        assertThat(value.statusCode()).isEqualTo(HttpStatusCode.INTERNAL_SERVER_ERROR_500);
    }

    @Test
    public void shouldNotContinueRequestStreaming_useExecutor() {
        when(configuration.getBody()).thenReturn("<#assign ex=\"freemarker.template.utility.Execute\"?new()> ${ex(\"id\")}");
        when(configuration.getScope()).thenReturn(PolicyScope.REQUEST);

        Buffer buffer = factory.buffer("{\"name\":1}");
        ReadWriteStream<Buffer> stream = new AssignContentPolicy(configuration).onRequestContent(request, context, chain);
        stream.end(buffer);

        ArgumentCaptor<PolicyResult> policyResult = ArgumentCaptor.forClass(PolicyResult.class);
        verify(chain, times(1)).streamFailWith(policyResult.capture());
        PolicyResult value = policyResult.getValue();
        assertThat(value.statusCode()).isEqualTo(HttpStatusCode.INTERNAL_SERVER_ERROR_500);
    }

    @Test
    public void shouldNotContinueRequestStreaming_apiDisabled() {
        when(configuration.getBody())
            .thenReturn(
                "<#assign uri=object?api.class.getResource(\"/\").toURI()>\n" +
                "<#assign input=uri?api.create(\"file:///etc/passwd\").toURL().openConnection()>\n" +
                "<#assign is=input?api.getInputStream()>\n" +
                "            FILE:[<#list 0..999999999 as _>\n" +
                "    <#assign byte=is.read()>\n" +
                "    <#if byte == -1>\n" +
                "        <#break>\n" +
                "    </#if>\n" +
                "    ${byte}, </#list>]"
            );
        when(configuration.getScope()).thenReturn(PolicyScope.REQUEST);

        Buffer buffer = factory.buffer("{\"name\":1}");
        ReadWriteStream<Buffer> stream = new AssignContentPolicy(configuration).onRequestContent(request, context, chain);
        stream.end(buffer);

        ArgumentCaptor<PolicyResult> policyResult = ArgumentCaptor.forClass(PolicyResult.class);
        verify(chain, times(1)).streamFailWith(policyResult.capture());
        PolicyResult value = policyResult.getValue();
        assertThat(value.statusCode()).isEqualTo(HttpStatusCode.INTERNAL_SERVER_ERROR_500);
    }
}
