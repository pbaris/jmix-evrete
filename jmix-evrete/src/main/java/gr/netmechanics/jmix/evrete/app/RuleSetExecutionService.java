package gr.netmechanics.jmix.evrete.app;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

import gr.netmechanics.jmix.evrete.entity.Rule;
import gr.netmechanics.jmix.evrete.entity.RuleSet;
import gr.netmechanics.jmix.evrete.entity.RuleSetExecutionType;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.evrete.KnowledgeService;
import org.evrete.api.StatelessSession;
import org.springframework.stereotype.Service;

/**
 * @author Panos Bariamis (pbaris)
 */
@RequiredArgsConstructor
@Service("evrete_RuleSetExecutionService")
public class RuleSetExecutionService {

    private final KnowledgeService knowledgeService;
    private final RuleSetExecutionLogService executionLogService;
    private final RuleSetGenerator ruleSetGenerator;
    private final RuleSetGeneratorHelper ruleSetGeneratorHelper;

    public void execute(final RuleSet ruleSet, Iterable<?> data) {
        execute(ruleSet, RuleSetExecutionType.NORMAL, data);
    }

    public void executeTest(final RuleSet ruleSet, Iterable<?> data) {
        execute(ruleSet, RuleSetExecutionType.TEST, data);
    }

    private void execute(final RuleSet ruleSet, final RuleSetExecutionType executionType, Iterable<?> data) {
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
            populateSessionContext(ruleSet, session);
//            session.insert(sessionData); //TODO how/what ?????????
            session.insert(data);
            session.fire();

            executionLog.setSuccess(true);

        } catch (Exception e) {
            executionLog.setSuccess(false);
            executionLog.setErrorMessage(e.getMessage());

        } finally {
            executionLog.setCode(ruleSetSource);
            executionLog.setExecutionEndAt(LocalDateTime.now());
            executionLogService.save(executionLog);
        }
    }

    private void populateSessionContext(final RuleSet ruleSet, final StatelessSession session) {
        Optional.ofNullable(ruleSet.getRules())
            .orElse(Collections.emptyList())
            .stream()
            .filter(Rule::isValidToProcess)
            .map(r -> ruleSetGeneratorHelper.getRuleAction(r.getRuleMetadata())
                .map(Pair::getRight).orElse(null))
            .filter(Objects::nonNull)
            .distinct()
            .forEach(action -> session.insert(ruleSetGeneratorHelper.getRuleActionParameterName(action), action));
    }
}
