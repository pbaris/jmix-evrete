package gr.netmechanics.jmix.evrete.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import lombok.Getter;
import org.springframework.lang.Nullable;

/**
 * @author Panos Bariamis (pbaris)
 */
@Getter
public enum RuleSetExecutionType implements EnumClass<String> {

    NORMAL("NORMAL"),
    TEST("TEST");

    private final String id;

    RuleSetExecutionType(final String id) {
        this.id = id;
    }

    @Nullable
    public static RuleSetExecutionType fromId(final String id) {
        for (RuleSetExecutionType at : RuleSetExecutionType.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}