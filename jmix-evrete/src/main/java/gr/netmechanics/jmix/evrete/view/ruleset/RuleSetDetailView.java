package gr.netmechanics.jmix.evrete.view.ruleset;

import java.util.Optional;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.router.Route;
import gr.netmechanics.jmix.evrete.app.RuleSetExecutionService;
import gr.netmechanics.jmix.evrete.app.RuleSetGenerator;
import gr.netmechanics.jmix.evrete.entity.Rule;
import gr.netmechanics.jmix.evrete.entity.RuleSet;
import gr.netmechanics.jmix.evrete.entity.RuleSetSort;
import gr.netmechanics.jmix.evrete.util.JsonUtil;
import gr.netmechanics.jmix.evrete.view.rule.RuleDetailFragment;
import io.jmix.core.DataManager;
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
    @ViewComponent private CodeEditor processDataEditor;
    @ViewComponent private JmixSelect<RuleSetSort> defaultSortField;

    @Autowired private RuleSetExecutionService executionService;
    @Autowired private RuleSetGenerator ruleSetGenerator;
    @Autowired private EntityStates entityStates;
    @Autowired private DataManager dataManager;

    @Subscribe
    public void onInit(final InitEvent event) {
        sourceCodeEditor.setMode(CodeEditorMode.JAVA);
        sourceCodeEditor.setFontSize("0.8rem");
        processDataEditor.setMode(CodeEditorMode.JSON);
        processDataEditor.setFontSize("0.8rem");
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        adjustRuleEditorTab();

        if (entityStates.isNew(getEditedEntity())) {
            defaultSortField.setValue(RuleSetSort.BY_NAME);
        }
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
            ruleSetTabSheet.setSelectedIndex(RULE_EDITOR_TAB_INDEX);
        }
    }

    @Subscribe
    public void onValidation(final ValidationEvent event) {
        ruleSetTabSheet.setSelectedIndex(RULE_EDITOR_TAB_INDEX);
        Tab ruleEditorTab = ruleSetTabSheet.getTabAt(RULE_EDITOR_TAB_INDEX);

        Optional.ofNullable(ruleSetTabSheet.getContentByTab(ruleEditorTab))
            .ifPresent(cmp -> ((RuleDetailFragment) cmp).onValidation(event));
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
        executionService.executeTest(getEditedEntity(), null); //TODO remove second parameter
    }

    @Subscribe("ruleSetTabSheet")
    public void onRuleSetTabSheetSelectedChange(final JmixTabSheet.SelectedChangeEvent event) {
        Optional<Tab> selectedTab = Optional.ofNullable(event.getSelectedTab());

        Tab tab;
        if (selectedTab.isEmpty() || (tab = selectedTab.get()).getId().isEmpty() || !tab.getId().get().equals("previewTab")) {
            return;
        }

        sourceCodeEditor.setValue(ruleSetGenerator.generate(getEditedEntity()));

        JsonUtil.toJsonPretty(getEditedEntity().getProcessData())
            .ifPresentOrElse(metadata -> processDataEditor.setValue(metadata), () -> processDataEditor.clear());
    }

    private void adjustRuleEditorTab() {
        boolean hasSelectedRule = !rulesDataGrid.getSelectedItems().isEmpty();
        ruleSetTabSheet.getTabAt(RULE_EDITOR_TAB_INDEX).setEnabled(hasSelectedRule);
    }
}