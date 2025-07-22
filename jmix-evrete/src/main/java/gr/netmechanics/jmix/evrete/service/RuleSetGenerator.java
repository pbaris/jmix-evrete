package gr.netmechanics.jmix.evrete.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import gr.netmechanics.jmix.evrete.entity.Rule;
import gr.netmechanics.jmix.evrete.entity.RuleSet;
import gr.netmechanics.jmix.evrete.util.JavaFormatter;
import gr.netmechanics.jmix.evrete.util.JavaNamingUtil;
import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

/**
 * @author Panos Bariamis (pbaris)
 */
@Slf4j
@RequiredArgsConstructor
@Component("evrete_RuleSetMarshaller")
public class RuleSetGenerator {

    private final RuleSetGeneratorHelper generatorHelper;

    private Template ruleSetTemplate;

    @PostConstruct
    @SneakyThrows
    private void init() {
        ruleSetTemplate = new SimpleTemplateEngine().createTemplate(getTemplateContent());
    }

    public String generate(final RuleSet ruleSet) {
        StringWriter writer = new StringWriter();

        try {
            ruleSetTemplate.make(getRuleSetModel(ruleSet)).writeTo(writer);
            writer.close();

        } catch (IOException e) {
            log.error("Failed to marshal rule set: {}", ruleSet, e);
        }

        return JavaFormatter.format(writer.toString());
    }

    private Map<String, Object> getRuleSetModel(final RuleSet ruleSet) {
        String name = ruleSet.getName();

        Map<String, Object> model = new HashMap<>();
        model.put("RS_HELPER", generatorHelper);
        model.put("RULE_SET", ruleSet);
        model.put("RULE_SET_PACKAGE", JavaNamingUtil.getPackageName(name));
        model.put("RULE_SET_CLASS_NAME", JavaNamingUtil.getClassName(name));
        model.put("RULES", Optional.ofNullable(ruleSet.getRules())
            .orElse(Collections.emptyList())
            .stream()
            .filter(Rule::isValidToProcess)
            .sorted(Comparator.comparingInt(Rule::getPriority))
            .toList());
        return model;
    }

    @SneakyThrows
    private String getTemplateContent() {
        ClassPathResource resource = new ClassPathResource("gr/netmechanics/jmix/evrete/tpl/rule_set.groovy.tpl");
        return FileCopyUtils.copyToString(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
    }
}
