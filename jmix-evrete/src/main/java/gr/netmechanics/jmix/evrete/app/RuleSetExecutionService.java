package gr.netmechanics.jmix.evrete.app;

import java.time.LocalDateTime;

import gr.netmechanics.jmix.evrete.entity.RuleSet;
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
    private final RuleSetMarshaller ruleSetMarshaller;

    public void execute(final RuleSet ruleSet) {
        var executionLog = executionLogService.create();
        executionLog.setRuleSet(ruleSet);
        executionLog.setExecutionStartAt(LocalDateTime.now());

        try {
            String ruleSetSource = ruleSetMarshaller.marshal(ruleSet);
            System.out.println(ruleSetSource);

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
            executionLog.setExecutionEndAt(LocalDateTime.now());
            executionLogService.save(executionLog);
        }
    }
}
