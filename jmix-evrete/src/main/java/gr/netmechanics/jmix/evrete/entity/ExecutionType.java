package gr.netmechanics.jmix.evrete.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import lombok.Getter;
import org.springframework.lang.Nullable;

/**
 * @author Panos Bariamis (pbaris)
 */
@Getter
public enum ExecutionType implements EnumClass<String> {

    NORMAL("NORMAL"),
    TEST("TEST");

    private final String id;

    ExecutionType(final String id) {
        this.id = id;
    }

    @Nullable
    public static ExecutionType fromId(final String id) {
        for (ExecutionType at : ExecutionType.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}