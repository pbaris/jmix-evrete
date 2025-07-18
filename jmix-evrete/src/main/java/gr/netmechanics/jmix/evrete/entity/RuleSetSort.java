package gr.netmechanics.jmix.evrete.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import lombok.Getter;
import org.springframework.lang.Nullable;

/**
 * @author Panos Bariamis (pbaris)
 */
@Getter
public enum RuleSetSort implements EnumClass<String> {

    BY_NAME("BY_NAME"),
    BY_NAME_INVERSE("BY_NAME_INVERSE");

    private final String id;

    RuleSetSort(final String id) {
        this.id = id;
    }

    @Nullable
    public static RuleSetSort fromId(final String id) {
        for (RuleSetSort at : RuleSetSort.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}