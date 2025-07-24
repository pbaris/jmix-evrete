package gr.netmechanics.jmix.evrete.role;

import gr.netmechanics.jmix.evrete.entity.Rule;
import gr.netmechanics.jmix.evrete.entity.RuleActionDefinition;
import gr.netmechanics.jmix.evrete.entity.RulePropertyCondition;
import gr.netmechanics.jmix.evrete.entity.RuleSet;
import gr.netmechanics.jmix.evrete.entity.RuleSetExecutionLog;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

/**
 * @author Panos Bariamis (pbaris)
 */
@SuppressWarnings("checkstyle:LineLength")
@ResourceRole(name = "Evrete: Administrator", code = EvreteAdministratorRole.CODE, scope = "UI")
public interface EvreteAdministratorRole {

    String CODE = "evrete-admin";

    @MenuPolicy(menuIds = {"evrete_RuleSet.list", "evrete_RuleSetExecutionLog.list"})
    @ViewPolicy(viewIds = {"evrete_RuleSet.list", "evrete_RuleSet.detail", "RuleDetailFragment", "RuleListFragmentRenderer", "evrete_Rule.detail", "evrete_RuleSetExecutionLog.detail", "evrete_RuleSetExecutionLog.list"})
    void screens();

    @EntityAttributePolicy(entityClass = Rule.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = Rule.class, actions = EntityPolicyAction.ALL)
    void rule();

    @EntityAttributePolicy(entityClass = RuleSet.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = RuleSet.class, actions = EntityPolicyAction.ALL)
    void ruleSet();

    @EntityAttributePolicy(entityClass = RulePropertyCondition.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = RulePropertyCondition.class, actions = EntityPolicyAction.ALL)
    void rulePropertyCondition();

    @EntityAttributePolicy(entityClass = RuleSetExecutionLog.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = RuleSetExecutionLog.class, actions = EntityPolicyAction.ALL)
    void ruleSetExecutionLog();

    @EntityAttributePolicy(entityClass = RuleActionDefinition.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = RuleActionDefinition.class, actions = EntityPolicyAction.ALL)
    void ruleActionDefinition();
}
