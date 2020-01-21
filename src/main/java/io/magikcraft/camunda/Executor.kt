package io.magikcraft.camunda

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.Expression
import org.camunda.bpm.engine.delegate.JavaDelegate
import java.util.*
import javax.script.ScriptContext

class Executor: JavaDelegate {
    lateinit var delegate: Expression

    override fun execute(execution: DelegateExecution?) {
        // Set a field injection on the service task with the key "delegate" and the name of
        // the function to invoke.
        val handler = delegate.expressionText
        val variableName = "__" + UUID.randomUUID().toString().replace("-", "_")
        val engine = io.magikcraft.camunda.CamundaBPM.engine
        val context = CamundaBPM.engine?.context
        context?.setAttribute(variableName, execution, ScriptContext.ENGINE_SCOPE)
        engine?.eval("__camundaHandlers.$handler($variableName)")
        context?.setAttribute(variableName, null, ScriptContext.ENGINE_SCOPE)
    }
}