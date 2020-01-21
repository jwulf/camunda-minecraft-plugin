package io.magikcraft.camunda

import org.bukkit.plugin.java.JavaPlugin
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.ProcessEngineConfiguration
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.model.bpmn.Bpmn
import java.io.ByteArrayInputStream

@Suppress("Unused")
class CamundaBPM : JavaPlugin() {

    @Suppress("Unused")
    val processEngine: ProcessEngine = ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration()
        .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_CREATE_DROP)
        .setJdbcUrl("jdbc:h2:mem:camunda-db;DB_CLOSE_DELAY=1000")
        .setJobExecutorActivate(true)
        .buildProcessEngine()
    private val repositoryService: RepositoryService = processEngine.repositoryService
    @Suppress("Unused")
    val runtimeService: RuntimeService = processEngine.runtimeService

    override fun onEnable() {
        logger.info("onEnable is called!")
    }

    override fun onDisable() {
        logger.info("onDisable is called!")
    }

    @Suppress("Unused")
    fun deployBpmn(name: String, bpmnModel: String) {
        val byteArray = bpmnModel.toByteArray()
        val stream = ByteArrayInputStream(byteArray)
        val model = Bpmn.readModelFromStream(stream)
        val res = repositoryService.createDeployment()
            .addModelInstance(name, model)
            .deploy()
        logger.info("Deployed " + res.id)
    }

    @Suppress("Unused")
    fun startInstance(key: String, variables: Map<String, Any>): ProcessInstance? {
        val res = runtimeService.createProcessInstanceByKey(key)
                .setVariables(variables)
                .execute()
        val id = res.processInstanceId
        logger.info("Started instance of $key: $id")
        return res
    }

    @Suppress("Unused")
    fun cancelInstance(processId: String) {
        runtimeService.signalEventReceived("CANCEL", processId)
    }
}