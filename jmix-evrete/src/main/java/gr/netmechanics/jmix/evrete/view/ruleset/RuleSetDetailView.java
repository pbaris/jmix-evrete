package gr.netmechanics.jmix.evrete.view.ruleset;

import com.vaadin.flow.router.Route;
import gr.netmechanics.jmix.evrete.entity.RuleSet;
import io.jmix.flowui.view.DefaultMainViewParent;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;


/**
 * @author Panos Bariamis (pbaris)
 */
@Route(value = "rule-sets/:id", layout = DefaultMainViewParent.class)
@ViewController(id = "evrete_RuleSet.detail")
@ViewDescriptor(path = "rule-set-detail-view.xml")
@EditedEntityContainer("ruleSetDc")
public class RuleSetDetailView extends StandardDetailView<RuleSet> {
}