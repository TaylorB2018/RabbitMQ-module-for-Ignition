package org.changchun.rabbitmq.designer;

import com.inductiveautomation.ignition.common.licensing.LicenseState;
import com.inductiveautomation.ignition.common.script.ScriptManager;
import com.inductiveautomation.ignition.designer.model.AbstractDesignerModuleHook;
import com.inductiveautomation.ignition.designer.model.DesignerContext;
import org.changchun.rabbitmq.common.RabbitmqModule;
import org.changchun.rabbitmq.gateway.RabbitMQModule;

import java.util.logging.Logger;


/**
 * This is the Designer-scope module hook.  The minimal implementation contains a startup method.
 */
public class RabbitmqDesignerHook extends AbstractDesignerModuleHook {

    // override additonal methods as requried
    private static final Logger logger = Logger.getLogger(RabbitmqDesignerHook.class.getName());
    private RabbitMQModule rabbitMQModule;

    @Override
    public void startup(DesignerContext context, LicenseState activationState) throws Exception {
        // implelement functionality as required
        super.startup(context, activationState);
    }
    @Override
    public void shutdown() {
        logger.info("DesignerRabbitMQModule shutting down.");
        if (rabbitMQModule != null) {
            rabbitMQModule.shutdown();
        }
    }


    @Override
    public void initializeScriptManager(ScriptManager manager) {
        manager.addScriptModule("system.rabbitmqD", RabbitmqModule.class);
    }
}
