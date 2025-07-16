package gr.netmechanics.jmix.evrete.view.ruleset;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.router.Route;
import gr.netmechanics.jmix.evrete.entity.Rule;
import gr.netmechanics.jmix.evrete.entity.RuleSet;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.listbox.JmixListBox;
import io.jmix.flowui.component.tabsheet.JmixTabSheet;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.util.RemoveOperation;
import io.jmix.flowui.view.DefaultMainViewParent;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.Install;
import io.jmix.flowui.view.PrimaryDetailView;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;


/**
 * @author Panos Bariamis (pbaris)
 */
@Route(value = "rule-sets2/:id", layout = DefaultMainViewParent.class)
@ViewController(id = "evrete_RuleSet.detail2")
@ViewDescriptor(path = "rule-set-detail-view.xml")
@EditedEntityContainer("ruleSetDc")
@PrimaryDetailView(RuleSet.class)
public class RuleSetDetailView extends StandardDetailView<RuleSet> {
    @ViewComponent private DataGrid<Rule> rulesDataGrid;
    @ViewComponent private JmixTabSheet ruleSetTabSheet;
    @ViewComponent private InstanceContainer<Rule> ruleDc;

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        adjustRuleEditorTab();
    }

    @Subscribe("rulesListBox")
    public void onRulesListBoxComponentValueChange(final AbstractField.ComponentValueChangeEvent<JmixListBox<Rule>, Rule> event) {
        Rule rule = event.getValue();
        if (rule == null) {
            rulesDataGrid.deselectAll();
            adjustRuleEditorTab();

        } else {
            rulesDataGrid.select(rule);
            ruleDc.setItem(rule);
            adjustRuleEditorTab();
            ruleSetTabSheet.setSelectedIndex(1);
        }
    }

    @Install(to = "rulesDataGrid.removeAction", subject = "afterActionPerformedHandler")
    private void rulesDataGridRemoveActionAfterActionPerformedHandler(final RemoveOperation.AfterActionPerformedEvent<Rule> event) {
        ruleDc.setItem(null);
        ruleSetTabSheet.setSelectedIndex(0);
        adjustRuleEditorTab();
    }

    private void adjustRuleEditorTab() {
        boolean hasSelectedRule = !rulesDataGrid.getSelectedItems().isEmpty();
        ruleSetTabSheet.getTabAt(1).setEnabled(hasSelectedRule);
    }
}