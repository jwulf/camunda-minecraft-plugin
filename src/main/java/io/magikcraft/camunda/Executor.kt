package io.magikcraft.camunda

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.Expression
import org.camunda.bpm.engine.delegate.JavaDelegate

class Executor: JavaDelegate {
    @Suppress("Unused")
    lateinit var delegate: Expression
    override fun execute(execution: DelegateExecution?) {
        // The delegate.expressionText can be the name of a JavaScript function to invoke in a Nashorn Engine
       println(delegate.expressionText)
    }
}