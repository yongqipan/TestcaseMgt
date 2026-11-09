package com.pyq.tcm.ai.client;

import com.pyq.tcm.ai.model.AiCallContext;
import com.pyq.tcm.ai.model.AiModelResolver;
import com.pyq.tcm.ai.model.ResolvedModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiConnectionTester {

    private final OpenAiChatClient chatClient;
    private final AiModelResolver modelResolver;

    public AiConnectionTester(OpenAiChatClient chatClient, AiModelResolver modelResolver) {
        this.chatClient = chatClient;
        this.modelResolver = modelResolver;
    }

    public ProbeResult test(Long modelId) {
        long start = System.currentTimeMillis();
        ResolvedModel model = modelResolver.resolve(modelId, null);
        AiCallContext context = new AiCallContext("CONNECTION_TEST", "MODEL", model.getModelId(), null, null, null);
        try {
            AiChatResult result = chatClient.chat(model,
                    List.of(ChatMessage.user("ping, reply with pong")), context);
            return new ProbeResult(true, (int) (System.currentTimeMillis() - start),
                    result.getContent(), null, model.getModelName());
        } catch (AiException ex) {
            return new ProbeResult(false, (int) (System.currentTimeMillis() - start),
                    null, ex.getErrorCode() + ": " + ex.getMessage(), model.getModelName());
        }
    }

    public record ProbeResult(boolean success, int latencyMs, String reply, String error, String modelName) {
    }
}
