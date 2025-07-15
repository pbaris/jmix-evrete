package gr.netmechanics.jmix.evrete.view.ruleset;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.router.Route;
import gr.netmechanics.jmix.evrete.entity.Rule;
import gr.netmechanics.jmix.evrete.entity.RuleSet;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.listbox.JmixListBox;
import io.jmix.flowui.component.tabsheet.JmixTabSheet;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.DefaultMainViewParent;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
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
    @ViewComponent private DataGrid<Rule> rulesDataGrid;
    @ViewComponent private JmixTabSheet ruleSetTabSheet;
    @ViewComponent private InstanceContainer<Rule> ruleDc;

    @Subscribe("rulesListBox")
    public void onRulesListBoxComponentValueChange(final AbstractField.ComponentValueChangeEvent<JmixListBox<Rule>, Rule> event) {
        Rule value = event.getValue();
        if (value == null) {
            rulesDataGrid.deselectAll();

        } else {
            rulesDataGrid.select(value);
            ruleDc.setItem(value);
            ruleSetTabSheet.setSelectedIndex(1);
        }
    }
}