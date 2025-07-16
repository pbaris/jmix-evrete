package gr.netmechanics.jmix.evrete.entity;

import java.io.Serializable;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Panos Bariamis (pbaris)
 */
@Getter
@Setter
@EqualsAndHashCode
public class RuleCondition implements Serializable {

    private List<RuleConditionParameter> parameters;
}