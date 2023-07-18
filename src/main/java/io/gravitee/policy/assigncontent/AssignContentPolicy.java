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

import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.gravitee.common.http.HttpStatusCode;
import io.gravitee.gateway.api.buffer.Buffer;
import io.gravitee.gateway.api.http.HttpHeaderNames;
import io.gravitee.gateway.api.http.HttpHeaders;
import io.gravitee.gateway.reactive.api.ExecutionFailure;
import io.gravitee.gateway.reactive.api.context.HttpExecutionContext;
import io.gravitee.gateway.reactive.api.context.MessageExecutionContext;
import io.gravitee.gateway.reactive.api.el.EvaluableMessage;
import io.gravitee.gateway.reactive.api.el.EvaluableRequest;
import io.gravitee.gateway.reactive.api.el.EvaluableResponse;
import io.gravitee.gateway.reactive.api.message.Message;
import io.gravitee.gateway.reactive.api.policy.Policy;
import io.gravitee.policy.assigncontent.configuration.AssignContentPolicyConfiguration;
import io.gravitee.policy.assigncontent.utils.AttributesBasedExecutionContext;
import io.gravitee.policy.v3.assigncontent.AssignContentPolicyV3;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AssignContentPolicy extends AssignContentPolicyV3 implements Policy {

    public static final String PLUGIN_ID = "policy-assign-content";

    public AssignContentPolicy(AssignContentPolicyConfiguration configuration) {
        super(configuration);
    }

    @Override
    public String id() {
        return PLUGIN_ID;
    }

    @Override
    public Completable onRequest(HttpExecutionContext ctx) {
        return ctx.request().onBody(body -> assignBodyContent(ctx, ctx.request().headers(), body, true));
    }

    @Override
    public Completable onResponse(HttpExecutionContext ctx) {
        return ctx.response().onBody(body -> assignBodyContent(ctx, ctx.response().headers(), body, false));
    }

    private Maybe<Buffer> assignBodyContent(HttpExecutionContext ctx, HttpHeaders httpHeaders, Maybe<Buffer> body, boolean isRequest) {
        return body
            .flatMap(content -> {
                var writer = replaceContent(isRequest, ctx, content.toString());
                return Maybe.just(Buffer.buffer(writer.toString()));
            })
            .switchIfEmpty(
                Maybe.fromCallable(() -> {
                    // For method like GET where body is missing, we have to handle the case where the maybe is empty.
                    // It can make sens if in the Flow we have an override method policy that replace the GET by a POST
                    var writer = replaceContent(isRequest, ctx, "");
                    return Buffer.buffer(writer.toString());
                })
            )
            .doOnSuccess(buffer -> httpHeaders.set(HttpHeaderNames.CONTENT_LENGTH, Integer.toString(buffer.length())))
            .onErrorResumeNext(ioe -> {
                log.debug("Unable to assign body content", ioe);
                return ctx.interruptBodyWith(
                    new ExecutionFailure(HttpStatusCode.INTERNAL_SERVER_ERROR_500)
                        .message("Unable to assign body content: " + ioe.getMessage())
                );
            });
    }

    private StringWriter replaceContent(boolean isRequest, HttpExecutionContext ctx, String content) throws IOException, TemplateException {
        Template template = getTemplate(configuration.getBody());
        StringWriter writer = new StringWriter();
        Map<String, Object> model = new HashMap<>();
        if (isRequest) {
            model.put("request", new EvaluableRequest(ctx.request(), content));
        } else {
            model.put("request", new EvaluableRequest(ctx.request()));
            model.put("response", new EvaluableResponse(ctx.response(), content));
        }
        model.put("context", new AttributesBasedExecutionContext(ctx));
        template.process(model, writer);
        return writer;
    }

    @Override
    public Completable onMessageRequest(MessageExecutionContext ctx) {
        return ctx.request().onMessage(msg -> assignMessageContent(ctx, msg));
    }

    @Override
    public Completable onMessageResponse(MessageExecutionContext ctx) {
        return ctx.response().onMessage(msg -> assignMessageContent(ctx, msg));
    }

    private Maybe<Message> assignMessageContent(MessageExecutionContext ctx, Message msg) {
        return Maybe
            .fromCallable(() -> {
                Template template = getTemplate(configuration.getBody());
                StringWriter writer = new StringWriter();
                Map<String, Object> model = new HashMap<>();
                model.put("message", new EvaluableMessage(msg));
                model.put("context", new AttributesBasedExecutionContext(ctx));
                template.process(model, writer);
                return msg.content(Buffer.buffer(writer.toString()));
            })
            .onErrorResumeNext(err -> {
                log.debug("Unable to assign message content", err);
                return ctx.interruptMessageWith(
                    new ExecutionFailure(HttpStatusCode.INTERNAL_SERVER_ERROR_500)
                        .message("Unable to assign message content: " + err.getMessage())
                );
            });
    }
}
