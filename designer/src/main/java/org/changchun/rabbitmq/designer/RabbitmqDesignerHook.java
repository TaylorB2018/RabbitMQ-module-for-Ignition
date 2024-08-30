package org.changchun.rabbitmq.designer;

import com.inductiveautomation.ignition.common.licensing.LicenseState;
import com.inductiveautomation.ignition.common.script.ScriptManager;
import com.inductiveautomation.ignition.designer.model.AbstractDesignerModuleHook;
import com.inductiveautomation.ignition.designer.model.DesignerContext;
import org.changchun.rabbitmq.common.RabbitmqModule;


/**
 * This is the Designer-scope module hook.  The minimal implementation contains a startup method.
 */
public class RabbitmqDesignerHook extends AbstractDesignerModuleHook {

    // override additonal methods as requried

    @Override
    public void startup(DesignerContext context, LicenseState activationState) throws Exception {
        // implelement functionality as required
    }


    @Override
    public void initializeScriptManager(ScriptManager manager) {
        manager.addScriptModule("system.rabbitmq", RabbitmqModule.class);
    }
}
