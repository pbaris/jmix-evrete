package gr.netmechanics.jmix.evrete.role;

import gr.netmechanics.jmix.evrete.entity.Rule;
import gr.netmechanics.jmix.evrete.entity.RuleSet;
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
@ResourceRole(name = "Evrete: Administrator", code = EvreteAdministratorRole.CODE, scope = "UI")
public interface EvreteAdministratorRole {

    String CODE = "evrete-admin";

    @MenuPolicy(menuIds = "evrete_RuleSet.list")
    @ViewPolicy(viewIds = {"evrete_RuleSet.list", "evrete_RuleSet.detail"})
    void screens();

    @EntityAttributePolicy(entityClass = Rule.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = Rule.class, actions = EntityPolicyAction.ALL)
    void rule();

    @EntityAttributePolicy(entityClass = RuleSet.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = RuleSet.class, actions = EntityPolicyAction.ALL)
    void ruleSet();
}
