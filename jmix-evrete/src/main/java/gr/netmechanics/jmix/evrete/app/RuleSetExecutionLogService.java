package gr.netmechanics.jmix.evrete.app;

import gr.netmechanics.jmix.evrete.entity.RuleSetExecutionLog;
import io.jmix.core.DataManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author Panos Bariamis (pbaris)
 */
@RequiredArgsConstructor
@Service("evrete_RuleSetExecutionLogService")
public class RuleSetExecutionLogService {

    private final DataManager dataManager;

    public RuleSetExecutionLog create() {
        return dataManager.create(RuleSetExecutionLog.class);
    }

    public RuleSetExecutionLog save(RuleSetExecutionLog log) {
        return dataManager.save(log);
    }
}
