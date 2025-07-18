package evrete.${RULE_SET_PACKAGE};

import org.evrete.api.RhsContext;
import org.evrete.dsl.annotation.Rule;
import org.evrete.dsl.annotation.RuleSet;
import org.evrete.dsl.annotation.Where;
${RS_HELPER.writeImports(RULE_SET)}

/**
 * @author Panos Bariamis (pbaris)
 */
@RuleSet(value = "${RULE_SET.name}", defaultSort = RuleSet.Sort.${RULE_SET.defaultSort})
public class ${RULE_SET_CLASS_NAME} {
<% RULES.eachWithIndex { rule, index -> %>
	@Rule(value = "${rule.name}", salience = ${rule.priority})
	${RS_HELPER.writeWhere(rule)}
	public void rule${index}(final RhsContext ctx) {

	}
<% } %>
}
