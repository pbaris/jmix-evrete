package gr.netmechanics.jmix.evrete.view.ruleset;

import java.util.Optional;

import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.router.Route;
import gr.netmechanics.jmix.evrete.RuleSetExecutionSupport;
import gr.netmechanics.jmix.evrete.RuleSetGenerator;
import gr.netmechanics.jmix.evrete.entity.ExecutionType;
import gr.netmechanics.jmix.evrete.entity.Rule;
import gr.netmechanics.jmix.evrete.entity.RuleSet;
import gr.netmechanics.jmix.evrete.entity.RuleSetSort;
import gr.netmechanics.jmix.evrete.util.JsonUtil;
import io.jmix.core.EntityStates;
import io.jmix.flowui.component.codeeditor.CodeEditor;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.listbox.JmixListBox;
import io.jmix.flowui.component.select.JmixSelect;
import io.jmix.flowui.component.tabsheet.JmixTabSheet;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.kit.component.codeeditor.CodeEditorMode;
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
import org.springframework.beans.factory.annotation.Autowired;


/**
 * @author Panos Bariamis (pbaris)
 */
@Route(value = "rule-sets/:id", layout = DefaultMainViewParent.class)
@ViewController(id = "evrete_RuleSet.detail")
@ViewDescriptor(path = "rule-set-detail-view.xml")
@EditedEntityContainer("ruleSetDc")
@PrimaryDetailView(RuleSet.class)
public class RuleSetDetailView extends StandardDetailView<RuleSet> {
    private static final int RULE_EDITOR_TAB_INDEX = 1;

    @ViewComponent private DataGrid<Rule> rulesDataGrid;
    @ViewComponent private JmixTabSheet ruleSetTabSheet;
    @ViewComponent private InstanceContainer<Rule> ruleDc;
    @ViewComponent private CodeEditor sourceCodeEditor;
    @ViewComponent private CodeEditor generatorDataEditor;
    @ViewComponent private JmixSelect<RuleSetSort> defaultSortField;
    @ViewComponent private JmixListBox<Rule> rulesListBox;

    @Autowired private RuleSetGenerator ruleSetGenerator;
    @Autowired private EntityStates entityStates;
    @Autowired private RuleSetExecutionSupport ruleSetExecutionSupport;

    @Subscribe
    public void onInit(final InitEvent event) {
        sourceCodeEditor.setMode(CodeEditorMode.JAVA);
        sourceCodeEditor.setFontSize("0.8rem");
        generatorDataEditor.setMode(CodeEditorMode.JSON);
        generatorDataEditor.setFontSize("0.8rem");
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        adjustRuleEditorTab();

        if (entityStates.isNew(getEditedEntity())) {
            defaultSortField.setValue(RuleSetSort.BY_NAME);
        }
    }

    @Subscribe("rulesListBox")
    public void onRulesListBoxComponentValueChange(final ComponentValueChangeEvent<JmixListBox<Rule>, Rule> event) {
        Rule rule = event.getValue();
        if (rule == null) {
            rulesDataGrid.deselectAll();
            ruleDc.setItem(null);
            adjustRuleEditorTab();

        } else {
            rulesDataGrid.select(rule);
            ruleDc.setItem(rule);
            adjustRuleEditorTab();
            ruleSetTabSheet.setSelectedIndex(RULE_EDITOR_TAB_INDEX);
        }
    }

    // ttfm: ignore inspection
    @Install(to = "rulesDataGrid.removeAction", subject = "afterActionPerformedHandler")
    private void rulesDataGridRemoveActionAfterActionPerformedHandler(final RemoveOperation.AfterActionPerformedEvent<Rule> event) {
        ruleDc.setItem(null);
        ruleSetTabSheet.setSelectedIndex(0);
        adjustRuleEditorTab();
    }

    @Subscribe(id = "testButton", subject = "clickListener")
    public void onTestButtonClick(final ClickEvent<JmixButton> event) {
        ruleSetExecutionSupport.execute(getEditedEntity(), ExecutionType.TEST);
    }

    @Subscribe("ruleSetTabSheet")
    public void onRuleSetTabSheetSelectedChange(final JmixTabSheet.SelectedChangeEvent event) {
        Optional<Tab> selectedTab = Optional.ofNullable(event.getSelectedTab());
        if (selectedTab.isEmpty()) {
            return;
        }

        String tabId = selectedTab.get().getId().orElse("");

        if (!tabId.equals("ruleEditorTab")) {
            rulesListBox.clear();
        }

        if (tabId.equals("previewTab")) {
            sourceCodeEditor.setValue(ruleSetGenerator.generate(getEditedEntity()));
            JsonUtil.toJsonPretty(getEditedEntity().getGeneratorData())
                .ifPresentOrElse(metadata -> generatorDataEditor.setValue(metadata), () -> generatorDataEditor.clear());
        }
    }

    private void adjustRuleEditorTab() {
        boolean hasSelectedRule = !rulesDataGrid.getSelectedItems().isEmpty();
        ruleSetTabSheet.getTabAt(RULE_EDITOR_TAB_INDEX).setEnabled(hasSelectedRule);
    }
}