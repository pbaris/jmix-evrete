package gr.netmechanics.jmix.evrete.entity;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Panos Bariamis (pbaris)
 */
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class RuleMetadata implements Serializable {

    private UUID id;

    private RuleActionDefinition action;

    private List<RulePropertyCondition> propertyConditions;

    public RuleMetadata() {
        this.id = UUID.randomUUID();
    }
}