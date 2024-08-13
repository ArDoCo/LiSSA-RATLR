package edu.kit.kastel.sdq.lissa.ratlr.classifier;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import edu.kit.kastel.sdq.lissa.ratlr.Configuration;
import edu.kit.kastel.sdq.lissa.ratlr.cache.Cache;
import edu.kit.kastel.sdq.lissa.ratlr.cache.CacheManager;
import edu.kit.kastel.sdq.lissa.ratlr.knowledge.Element;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReasoningClassifier extends Classifier {
    private final Cache cache;
    private final ChatLanguageModelProvider provider;

    private final ChatLanguageModel llm;
    private final String prompt;
    private final boolean useOriginalArtifacts;
    private final boolean useSystemMessage;

    public ReasoningClassifier(Configuration.ModuleConfiguration configuration) {
        this.provider = new ChatLanguageModelProvider(configuration);
        this.cache = CacheManager.getDefaultInstance().getCache(this.getClass().getSimpleName() + "_" + provider.modelName());
        this.prompt = Prompt.values()[configuration.argumentAsInt("prompt_id", 0)].prompt;
        this.useOriginalArtifacts = configuration.argumentAsBoolean("use_original_artifacts", false);
        this.useSystemMessage = configuration.argumentAsBoolean("use_system_message", true);
        this.llm = this.provider.createChatModel();
    }

    private ReasoningClassifier(Cache cache, ChatLanguageModelProvider provider, String prompt, boolean useOriginalArtifacts, boolean useSystemMessage) {
        this.cache = cache;
        this.provider = provider;
        this.prompt = prompt;
        this.useOriginalArtifacts = useOriginalArtifacts;
        this.useSystemMessage = useSystemMessage;
        this.llm = this.provider.createChatModel();
    }

    @Override
    protected final Classifier copyOf() {
        return new ReasoningClassifier(cache, provider, prompt, useOriginalArtifacts, useSystemMessage);
    }

    @Override
    protected final List<ClassificationResult> classify(Element source, List<Element> targets) {
        List<Element> relatedTargets = new ArrayList<>();

        var targetsToConsider = targets;
        if (useOriginalArtifacts) {
            targetsToConsider = new ArrayList<>();
            for (var target : targets) {
                Element artifact = target;
                while (artifact.getParent() != null) {
                    artifact = artifact.getParent();
                }
                // Now we have the artifact
                targetsToConsider.add(new Element(artifact.getIdentifier(), artifact.getType(), artifact.getContent(), 0, null, true));
            }
        }

        var sourceToConsider = source;
        /* TODO Maybe reactivate the sourceToConsider in the future ..
        if(useOriginalArtifacts){
            while (sourceToConsider.getParent() != null) {
                sourceToConsider = sourceToConsider.getParent();
            }
        }
        */

        for (var target : targetsToConsider) {
            String llmResponse = classify(sourceToConsider, target);
            boolean isRelated = isRelated(llmResponse);
            if (isRelated) {
                relatedTargets.add(target);
            }
        }
        return relatedTargets.stream().map(relatedTarget -> ClassificationResult.of(source, relatedTarget)).toList();
    }

    private boolean isRelated(String llmResponse) {
        Pattern pattern = Pattern.compile("<trace>(.*?)</trace>", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(llmResponse);
        boolean related = false;
        if (matcher.find()) {
            related = matcher.group().toLowerCase().contains("yes");
        }
        return related;
    }

    private String classify(Element source, Element target) {
        List<ChatMessage> messages = new ArrayList<>();
        if (useSystemMessage)
            messages.add(new SystemMessage("Your job is to determine if there is a traceability link between two artifacts of a system."));

        String request = prompt.replace("{source_type}", source.getType())
                .replace("{source_content}", source.getContent())
                .replace("{target_type}", target.getType())
                .replace("{target_content}", target.getContent());
        messages.add(new UserMessage(request));

        String key = UUID.nameUUIDFromBytes(messages.toString().getBytes(StandardCharsets.UTF_8)).toString();
        String cachedResponse = cache.get(key, String.class);
        if (cachedResponse != null) {
            return cachedResponse;
        } else {
            logger.info("Classifying: {} and {}", source.getIdentifier(), target.getIdentifier());
            Response<AiMessage> response = llm.generate(messages);
            String responseText = response.content().text();
            cache.put(key, responseText);
            return responseText;
        }
    }

    private enum Prompt {
        REASON_WITH_NAME(
                "Below are two artifacts from the same software system. Is there a traceability link between (1) and (2)? Give your reasoning and then answer with 'yes' or 'no' enclosed in <trace> </trace>.\n (1) {source_type}: '''{source_content}''' \n (2) {target_type}: '''{target_content}''' "),

        REASON_WITH_NAME_CONCEIVABLE(
                "Below are two artifacts from the same software system. Is there a conceivable traceability link between (1) and (2)? Give your reasoning and then answer with 'yes' or 'no' enclosed in <trace> </trace>.\n (1) {source_type}: '''{source_content}''' \n (2) {target_type}: '''{target_content}''' "),

        REASON_WITH_NAME_YES_IF_CERTAIN(
                "Below are two artifacts from the same software system.\n Is there a traceability link between (1) and (2)? Give your reasoning and then answer with 'yes' or 'no' enclosed in <trace> </trace>. Only answer yes if you are absolutely certain.\n (1) {source_type}: '''{source_content}''' \n (2) {target_type}: '''{target_content}''' ");

        private final String prompt;

        Prompt(String prompt) {
            this.prompt = prompt;
        }
    }
}
