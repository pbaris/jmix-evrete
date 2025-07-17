package gr.netmechanics.jmix.evrete.entity.data;

import gr.netmechanics.jmix.evrete.entity.RuleMetadata;
import gr.netmechanics.jmix.evrete.util.JsonUtil;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.lang.Nullable;

/**
 * @author Panos Bariamis (pbaris)
 */
@Converter(autoApply = true)
public class RuleMetadataConverter implements AttributeConverter<RuleMetadata, String> {

    @Nullable
    @Override
    public String convertToDatabaseColumn(final RuleMetadata rc) {
        return JsonUtil.toJson(rc).orElse(null);
    }

    @Nullable
    @Override
    public RuleMetadata convertToEntityAttribute(final String dbData) {
        return JsonUtil.fromJson(dbData, RuleMetadata.class).orElse(null);
    }
}