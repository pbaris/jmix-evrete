package evrete.${RULE_SET_PACKAGE};

import org.evrete.api.RhsContext;
import org.evrete.api.events.SessionFireEvent;
import org.evrete.dsl.annotation.EventSubscription;
import org.evrete.dsl.annotation.Rule;
import org.evrete.dsl.annotation.RuleSet;
import org.evrete.dsl.annotation.Where;
${RS_HELPER.writeImports(RULE_SET)}

@RuleSet(value = "${RS_HELPER.writeName(RULE_SET.name)}", defaultSort = RuleSet.Sort.${RULE_SET.defaultSort ?: 'BY_NAME'})
public class ${RULE_SET_CLASS_NAME} {
	${RS_HELPER.writeClassProperties(RULE_SET)}

	@EventSubscription
	public void sessionFireObserver(SessionFireEvent event) {
		${RS_HELPER.loadClassProperties(RULE_SET)}
	}

<% RULES.eachWithIndex { rule, index -> %>
	@Rule(value = "${RS_HELPER.writeName(rule.name)}", salience = ${(rule.priority ?: 0) * -1})
	${RS_HELPER.writeWhere(rule)}
	public void rule${index + 1}(${RS_HELPER.writeParameters(rule)}) {
		${RS_HELPER.writeMethodBody(rule)}
	}

<% } %>
}
