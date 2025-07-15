package gr.netmechanics.jmix.evrete.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import lombok.Getter;
import org.springframework.lang.Nullable;

/**
 * @author Panos Bariamis (pbaris)
 */
@Getter
public enum RuleSort implements EnumClass<String> {

    BY_NAME("BY_NAME"),
    BY_NAME_INVERSE("BY_NAME_INVERSE");

    private final String id;

    RuleSort(final String id) {
        this.id = id;
    }

    @Nullable
    public static RuleSort fromId(final String id) {
        for (RuleSort at : RuleSort.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}