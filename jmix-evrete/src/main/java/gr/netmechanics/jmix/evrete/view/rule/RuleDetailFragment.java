package gr.netmechanics.jmix.evrete.view.rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import gr.netmechanics.jmix.evrete.entity.Rule;
import gr.netmechanics.jmix.evrete.entity.RuleMetadata;
import gr.netmechanics.jmix.evrete.entity.RulePropertyCondition;
import io.jmix.core.DataManager;
import io.jmix.flowui.Fragments;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.StandardDetailView.ValidationEvent;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.Target;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewValidation;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Panos Bariamis (pbaris)
 */
@FragmentDescriptor("rule-detail-fragment.xml")
public class RuleDetailFragment extends Fragment<VerticalLayout> {
    @ViewComponent private InstanceContainer<Rule> ruleDc;
    @ViewComponent private VerticalLayout propertyConditionsContainer;

    @Autowired private Fragments fragments;
    @Autowired private DataManager dataManager;
    @Autowired private ViewValidation viewValidation;

    private final Map<UUID, RulePropertyCondition> propertyConditions = new HashMap<>();

    @Subscribe(id = "ruleDc", target = Target.DATA_CONTAINER)
    public void onRuleDcItemChange(final InstanceContainer.ItemChangeEvent<Rule> event) {
        Rule rule = event.getItem();
        if (rule == null) {
            return;
        }

        initRulePropertyConditions();
    }

    @Subscribe(id = "addPropertyConditionButton", subject = "clickListener")
    public void onAddPropertyConditionButtonClick(final ClickEvent<JmixButton> event) {
        buildRulePropertyConditionRow(dataManager.create(RulePropertyCondition.class));
        displayRulePropertyConditionFragmentLabels();
    }

    public void onValidation(final ValidationEvent event) {
        UiComponentUtils.getOwnComponents(propertyConditionsContainer).forEach(cmp -> {
            RulePropertyConditionFragment fragment = (RulePropertyConditionFragment) cmp;
            ValidationErrors validationErrors = viewValidation.validateUiComponents(fragment.getContent());
            if (!validationErrors.isEmpty()) {
                event.addErrors(validationErrors);
            }
        });
    }

    private void adjustRuleMetadata() {
        RuleMetadata ruleMetadata = new RuleMetadata();
        ruleMetadata.setPropertyConditions(new ArrayList<>(propertyConditions.values()));
        ruleDc.getItem().setRuleMetadata(ruleMetadata);
    }

    private void initRulePropertyConditions() {
        propertyConditions.clear();
        propertyConditionsContainer.removeAll();

        RuleMetadata ruleCondition = ruleDc.getItem().getRuleMetadata();
        if (ruleCondition != null && CollectionUtils.isNotEmpty(ruleCondition.getPropertyConditions())) {
            ruleCondition.getPropertyConditions().forEach(this::buildRulePropertyConditionRow);
        }
        displayRulePropertyConditionFragmentLabels();
    }

    private void buildRulePropertyConditionRow(final RulePropertyCondition rpc) {
        propertyConditions.put(rpc.getId(), rpc);
        adjustRuleMetadata();

        RulePropertyConditionFragment fragment = fragments.create(this, RulePropertyConditionFragment.class);
        fragment.setItem(rpc);
        fragment.setRemoveDelegate(this::rulePropertyConditionRemoveDelegate);
        fragment.setItemChangeDelegate(this::rulePropertyConditionItemChangeDelegate);
        propertyConditionsContainer.add(fragment);
    }

    private void rulePropertyConditionRemoveDelegate(final RulePropertyConditionFragment fragment, final RulePropertyCondition rpc) {
        propertyConditionsContainer.remove(fragment);
        propertyConditions.remove(rpc.getId());
        adjustRuleMetadata();
        displayRulePropertyConditionFragmentLabels();
    }

    private void rulePropertyConditionItemChangeDelegate(final RulePropertyCondition rpc) {
        propertyConditions.put(rpc.getId(), rpc);
        adjustRuleMetadata();
    }

    private void displayRulePropertyConditionFragmentLabels() {
        if (propertyConditionsContainer.getComponentCount() > 0) {
            ((RulePropertyConditionFragment) propertyConditionsContainer.getComponentAt(0)).displayLabels();
        }
    }
}