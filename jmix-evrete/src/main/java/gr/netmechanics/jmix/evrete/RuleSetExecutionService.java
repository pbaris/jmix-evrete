package gr.netmechanics.jmix.evrete;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

import gr.netmechanics.jmix.evrete.data.RuleSetDataProvider;
import gr.netmechanics.jmix.evrete.entity.ExecutionType;
import gr.netmechanics.jmix.evrete.entity.Rule;
import gr.netmechanics.jmix.evrete.entity.RuleSet;
import gr.netmechanics.jmix.evrete.entity.RuleSetExecutionLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.evrete.KnowledgeService;
import org.evrete.api.StatelessSession;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * @author Panos Bariamis (pbaris)
 */
@Slf4j
@RequiredArgsConstructor
@Service("evrete_RuleSetExecutionService")
public class RuleSetExecutionService {

    private final KnowledgeService knowledgeService;
    private final RuleSetExecutionLogService executionLogService;
    private final RuleSetGenerator ruleSetGenerator;
    private final RuleSetGeneratorHelper ruleSetGeneratorHelper;
    private final ObjectProvider<RuleSetDataProvider> dataProviders;

    public RuleSetExecutionLog executeTest(final RuleSet ruleSet) {
        return execute(ruleSet, ExecutionType.TEST);
    }

    public RuleSetExecutionLog execute(final RuleSet ruleSet) {
        return execute(ruleSet, ExecutionType.NORMAL);
    }

    private RuleSetExecutionLog execute(final RuleSet ruleSet, final ExecutionType executionType) {
        var executionLog = executionLogService.create();
        executionLog.setRuleSet(ruleSet);
        executionLog.setExecutionStartAt(LocalDateTime.now());
        executionLog.setExecutionType(executionType);

        String ruleSetSource = null;

        try {
            ruleSetSource = ruleSetGenerator.generate(ruleSet);

            var knowledge = knowledgeService.newKnowledge()
                .importRules("JAVA-SOURCE", ruleSetSource);

            StatelessSession session = knowledge.newStatelessSession();
            populateRuleActionData(ruleSet, session);

            dataProviders.orderedStream()
                .filter(dp -> dp.isApplicable(ruleSet, session))
                .forEach(dataProviders -> session.insert(dataProviders.getData(ruleSet, session)));

            session.fire();

            executionLog.setSuccess(true);

        } catch (Exception e) {
            log.error("Failed to execute rule set: {}", ruleSet, e);
            executionLog.setSuccess(false);
            executionLog.setErrorMessage(e.getMessage());

        } finally {
            executionLog.setCode(ruleSetSource);
            executionLog.setExecutionEndAt(LocalDateTime.now());
            executionLog = executionLogService.save(executionLog);
        }

        return executionLog;
    }

    private void populateRuleActionData(final RuleSet ruleSet, final StatelessSession session) {
        Optional.ofNullable(ruleSet.getRules())
            .orElse(Collections.emptyList())
            .stream()
            .filter(Rule::isApplicable)
            .map(r -> ruleSetGeneratorHelper.getRuleAction(r.getRuleMetadata())
                .map(Pair::getRight).orElse(null))
            .filter(Objects::nonNull)
            .distinct()
            .forEach(session::insert);
    }
}
