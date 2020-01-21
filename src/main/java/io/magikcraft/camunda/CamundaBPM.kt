package io.magikcraft.camunda

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import org.camunda.bpm.engine.*
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.model.bpmn.Bpmn
import java.io.ByteArrayInputStream
import java.io.File
import javax.script.ScriptContext
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager


@Suppress("Unused")
class CamundaBPM() : JavaPlugin() {
    companion object {
        var engine: ScriptEngine? = ScriptEngineManager().getEngineByName("nashorn")
    }

    private val handlerMap = "__camundaHandlers"

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
        if (engine == null) {
            throw Error("No Nashorn Engine found. The Camunda plugin cannot load.")
        }
        initialiseExecutionEngine(engine!!)
        loadBpmnAndHandlers()
        logger.info("Camunda plugin ready for action")
    }

    /**
     * Call setExecutionEngine and pass in a custom execution engine, for example, one
     * with the Scriptcraft / Magikcraft API loaded in it.
     */
    @Suppress("Unused")
    fun setExecutionEngine(executionEngine: ScriptEngine) {
        engine = executionEngine
        initialiseExecutionEngine(engine!!)
    }

    private fun initialiseExecutionEngine(executionEngine: ScriptEngine) {
        executionEngine.eval("$handlerMap = {};")
    }

    override fun onDisable() {
        logger.info("onDisable is called!")
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (command.name == "bpmn") {
            val subCommand: String
            if (args.isNotEmpty()) {
                subCommand = args[0];
                if (subCommand == "reload") {
                    loadBpmnAndHandlers()
                    return true
                }
                if (subCommand == "start") {
                    if (args.size < 2) {
                        return false
                    }
                    val processKey = args[1]
                    startInstance(processKey, mutableMapOf())
                    return true
                }
            }
            return false
        }
        return true
    }

    private fun loadBpmnAndHandlers() {
        val cfgFolder = this.dataFolder
        val files = cfgFolder.listFiles()
        val jsFiles = files?.filter { it.extension == "js" }
        val bpmnFiles = files?.filter { it.extension == "bpmn" }
        jsFiles?.forEach { loadJSHandlersFromModuleFile(it)  }
        bpmnFiles?.forEach { deployBpmn(it.name, it.readText()) }
    }

    private fun loadJSHandlersFromModuleFile(moduleFile: File) {
        logger.info("Loading " + moduleFile.name)
        val module = moduleFile.readText()
        try {
            engine?.eval("""
            (function(module) {
                $module
                for (x in module.exports) {
                    $handlerMap[x] = module.exports[x];
                };
            })({}) 
            """.trimIndent())
        } catch (ex: ScriptEvaluationException) {
            logger.info(ex.toString())
        }
    }

    @Suppress("Unused")
    fun deployBpmn(name: String, bpmnModel: String) {
        logger.info("Loading $name")
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

    @Suppress("Unused")
    fun registerHandler(name: String, code: String): Boolean {
        return engine?.eval("$handlerMap.$name = $code; true") as Boolean
    }

    init {
        val context = engine!!.context
        context.setAttribute("__plugin", this, ScriptContext.ENGINE_SCOPE)
    }
}