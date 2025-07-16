package gr.netmechanics.jmix.evrete.entity.data;

import gr.netmechanics.jmix.evrete.entity.RuleCondition;
import gr.netmechanics.jmix.evrete.util.JsonUtil;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.lang.Nullable;

/**
 * @author Panos Bariamis (pbaris)
 */
@Converter(autoApply = true)
public class RuleConditionConverter implements AttributeConverter<RuleCondition, String> {

    @Nullable
    @Override
    public String convertToDatabaseColumn(final RuleCondition rc) {
        return JsonUtil.toJson(rc).orElse(null);
    }

    @Nullable
    @Override
    public RuleCondition convertToEntityAttribute(final String dbData) {
        return JsonUtil.fromJson(dbData, RuleCondition.class).orElse(null);
    }
}