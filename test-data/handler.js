module.exports = {
    someFunction:  function(execution) {
        var process = execution.getProcessInstanceId();
        __plugin.getLogger().info('Yay!!! The job handler was called from process instance ' + process )
    }
}