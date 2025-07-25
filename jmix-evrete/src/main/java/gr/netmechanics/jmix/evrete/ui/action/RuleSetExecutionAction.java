package gr.netmechanics.jmix.evrete.ui.action;

import gr.netmechanics.jmix.evrete.RuleSetExecutionSupport;
import gr.netmechanics.jmix.evrete.entity.ExecutionType;
import gr.netmechanics.jmix.evrete.entity.RuleSet;
import gr.netmechanics.jmix.evrete.ui.accesscontext.UiRuleSetExecutionContext;
import io.jmix.core.AccessManager;
import io.jmix.core.Messages;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.list.ItemTrackingAction;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

/**
 * @author Panos Bariamis (pbaris)
 */
@ActionType(RuleSetExecutionAction.ID)
public class RuleSetExecutionAction extends ItemTrackingAction<RuleSet> {

    public static final String ID = "evrete_rule_set_execution";

    private boolean visibleBySpecificUiPermission = true;
    private RuleSetExecutionSupport executionServiceSupport;

    public RuleSetExecutionAction() {
        this(ID);
    }

    public RuleSetExecutionAction(final String id) {
        super(id);
    }

    @Override
    public void execute() {
        checkTarget();

        RuleSet item = target.getSingleSelectedItem();
        if (item == null) {
            throw new IllegalStateException(String.format("There is not selected item in %s target", getClass().getSimpleName()));
        }

        executionServiceSupport.execute(item, ExecutionType.NORMAL);
    }

    @Autowired
    @Override
    protected void setAccessManager(@NonNull final AccessManager accessManager) {
        super.setAccessManager(accessManager);

        UiRuleSetExecutionContext context = new UiRuleSetExecutionContext();
        accessManager.applyRegisteredConstraints(context);
        visibleBySpecificUiPermission = context.isPermitted();
    }

    @Override
    protected boolean isApplicable() {
        return super.isApplicable()
               && target.getSelectedItems().size() == 1
               && target.getSingleSelectedItem() != null
               && BooleanUtils.isTrue(target.getSingleSelectedItem().getActive());
    }

    @Override
    public boolean isVisibleByUiPermissions() {
        return visibleBySpecificUiPermission && super.isVisibleByUiPermissions();
    }

    @Autowired
    public void setMessages(final Messages messages) {
        setText(messages.getMessage("gr.netmechanics.jmix.evrete.actions/ruleSetExecution.text"));
    }

    @Autowired
    public void setExecutionServiceSupport(final RuleSetExecutionSupport executionServiceSupport) {
        this.executionServiceSupport = executionServiceSupport;
    }
}
