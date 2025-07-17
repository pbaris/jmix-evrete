package gr.netmechanics.jmix.evrete.entity.data;

import java.text.ParseException;
import java.util.Locale;

import gr.netmechanics.jmix.evrete.entity.RuleMetadata;
import gr.netmechanics.jmix.evrete.util.JsonUtil;
import io.jmix.core.metamodel.annotation.DatatypeDef;
import io.jmix.core.metamodel.annotation.Ddl;
import io.jmix.core.metamodel.datatype.Datatype;
import org.springframework.lang.Nullable;

/**
 * @author Panos Bariamis (pbaris)
 */
@DatatypeDef(id = "ruleMetadata", javaClass = RuleMetadata.class, defaultForClass = true)
@Ddl("CLOB")
public class RuleMetadataDatatype implements Datatype<RuleMetadata> {

    @Override
    public String format(@Nullable final Object value) {
        return JsonUtil.toJson(value).orElse("");
    }

    @Override
    public String format(@Nullable final Object value, final Locale locale) {
        return format(value);
    }

    @Nullable
    @Override
    public RuleMetadata parse(@Nullable final String value) throws ParseException {
        return JsonUtil.fromJson(value, RuleMetadata.class).orElse(null);
    }

    @Nullable
    @Override
    public RuleMetadata parse(@Nullable final String value, final Locale locale) throws ParseException {
        return parse(value);
    }
}
