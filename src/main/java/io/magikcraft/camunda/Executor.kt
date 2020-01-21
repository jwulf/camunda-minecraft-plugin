package io.magikcraft.camunda

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.Expression
import org.camunda.bpm.engine.delegate.JavaDelegate

class Executor: JavaDelegate {
    @Suppress("Unused")
    lateinit var delegate: Expression
    override fun execute(execution: DelegateExecution?) {
       println(delegate.expressionText)
    }
}