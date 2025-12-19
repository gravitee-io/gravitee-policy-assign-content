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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.gravitee.gateway.reactive.api.context.kafka.KafkaMessageExecutionContext;
import io.gravitee.gateway.reactive.api.message.kafka.KafkaMessage;
import io.gravitee.policy.assigncontent.configuration.AssignContentPolicyConfiguration;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.processors.ReplayProcessor;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import test.stub.KafkaMessageRequestStub;
import test.stub.KafkaMessageResponseStub;
import test.stub.KafkaMessageStub;

/**
 * @author Anthony CALLAERT (anthony.callaert at graviteesource.com)
 * @author GraviteeSource Team
 */
public class AssignContentPolicyTest {

    @Nested
    class OnMessageRequest {

        @ParameterizedTest
        @ValueSource(ints = { 1, 5, 10 })
        void should_assign_content_on_kafka_message(int recordsCount) {
            KafkaMessageExecutionContext ctx = mock(KafkaMessageExecutionContext.class);
            final KafkaMessageRequestStub request = new KafkaMessageRequestStub();
            when(ctx.request()).thenReturn(request);

            List<KafkaMessage> messages = new ArrayList<>();
            for (int i = 0; i < recordsCount; i++) {
                KafkaMessage stubMessage = new KafkaMessageStub("test_" + i);
                messages.add(stubMessage);
            }

            AssignContentPolicyConfiguration assignContentPolicyConfiguration = new AssignContentPolicyConfiguration();
            assignContentPolicyConfiguration.setBody("Override: ${message.content}");
            AssignContentPolicy policy = new AssignContentPolicy(assignContentPolicyConfiguration);

            policy
                .onMessageRequest(ctx)
                .doOnComplete(() -> request.messages(Flowable.fromIterable(messages)))
                .test()
                .awaitDone(3, TimeUnit.SECONDS)
                .assertComplete();

            ReplayProcessor<KafkaMessage> messagesEmittedToBrokerProcessor = ReplayProcessor.create();
            request
                .messages()
                .doOnNext(messagesEmittedToBrokerProcessor::onNext)
                .toList()
                .test()
                .awaitDone(3, TimeUnit.SECONDS)
                .assertComplete()
                .assertValue(messagesList ->
                    messagesList
                        .stream()
                        .allMatch(message -> Objects.requireNonNull(message.content()).toString().startsWith("Override: test_"))
                );
        }
    }

    @Nested
    class OnMessageResponse {

        @ParameterizedTest
        @ValueSource(ints = { 1, 5, 10 })
        void should_assign_content_on_kafka_message(int recordsCount) {
            KafkaMessageExecutionContext ctx = mock(KafkaMessageExecutionContext.class);
            final KafkaMessageResponseStub response = new KafkaMessageResponseStub();
            when(ctx.response()).thenReturn(response);

            List<KafkaMessage> messages = new ArrayList<>();
            for (int i = 0; i < recordsCount; i++) {
                KafkaMessage stubMessage = new KafkaMessageStub("test_" + i);
                messages.add(stubMessage);
            }

            AssignContentPolicyConfiguration assignContentPolicyConfiguration = new AssignContentPolicyConfiguration();
            assignContentPolicyConfiguration.setBody("Override: ${message.content}");
            AssignContentPolicy policy = new AssignContentPolicy(assignContentPolicyConfiguration);

            policy
                .onMessageResponse(ctx)
                .doOnComplete(() -> response.messages(Flowable.fromIterable(messages)))
                .test()
                .awaitDone(3, TimeUnit.SECONDS)
                .assertComplete();

            ReplayProcessor<KafkaMessage> messagesEmittedToBrokerProcessor = ReplayProcessor.create();
            response
                .messages()
                .doOnNext(messagesEmittedToBrokerProcessor::onNext)
                .toList()
                .test()
                .awaitDone(3, TimeUnit.SECONDS)
                .assertComplete()
                .assertValue(messagesList ->
                    messagesList.stream().allMatch(message -> Objects.requireNonNull(message.content()).toString().startsWith("Override:"))
                );
        }
    }
}
