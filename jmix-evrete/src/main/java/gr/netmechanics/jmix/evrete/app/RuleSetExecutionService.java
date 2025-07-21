package gr.netmechanics.jmix.evrete.app;

import java.time.LocalDateTime;

import gr.netmechanics.jmix.evrete.entity.RuleSet;
import gr.netmechanics.jmix.evrete.entity.RuleSetExecutionType;
import lombok.RequiredArgsConstructor;
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

    public void execute(final RuleSet ruleSet, final RuleSetExecutionType executionType) {
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
//            session.insert(sessionData); //TODO how/what ?????????
//            session.fire();

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
}
