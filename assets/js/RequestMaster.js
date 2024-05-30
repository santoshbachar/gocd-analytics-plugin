import Console from "./santosh/Console";

const c = new Console('RequestMaster.js', 'dev');

class RequestMaster {
    constructor(transport) {
        this.transport = transport;
        c.log('1. RequestMaster constructor');
    }

    async getPipelineList() {
        c.log('2. RequestMaster getPipelineList() called');
        const requestParams = {
            metric: "pipeline_list",
        };
        const pipelines = await this.asyncRequest(requestParams);
        return pipelines;
    }

    async getStageTimeline(pipelineName, requestResult, requestOrder, requestLimit) {
        const requestParams = {
            metric: "stage_timeline",
            pipeline_name: pipelineName,
            result: requestResult,
            order: requestOrder,
            limit: requestLimit
        };
        const stageTimelines = await this.asyncRequest(requestParams);
        return stageTimelines;
    }

    async getStageStartupTime(pipelineName, requestOrder, requestLimit) {
        const requestParams = {
            metric: "stage_startup_time",
            pipeline_name: pipelineName,
            order: requestOrder,
            limit: requestLimit
        };
        const stageTimelines = await this.asyncRequest(requestParams);
        return stageTimelines;
    }

    async getJobTimeline(stageName, pipelineCounterStart, pipelineCounterEnd) {
        const requestParams = {
            "type": "drilldown",
            metric: "job_timeline",
            stage_name: stageName,
            pipeline_counter_start: pipelineCounterStart,
            pipeline_counter_end: pipelineCounterEnd
        };
        const jobTimelines = await this.asyncRequest(requestParams);
        return jobTimelines;
    }

    async getPriorityPipeline(result) {
        const requestParams = {
            "type": "drilldown",
            metric: "priority_pipeline",
            result: result
        };
        const priorityPipeline = await this.asyncRequest(requestParams);
        return priorityPipeline;
    }

    async getPriorityStage(result) {
        const requestParams = {
            "type": "drilldown",
            metric: "priority_stage",
            result: result
        };
        const priorityStage = await this.asyncRequest(requestParams);
        return priorityStage;
    }

    async getPriorityJob(result) {
        const requestParams = {
            "type": "drilldown",
            metric: "priority_job",
            result: result
        };
        const priorityJob = await this.asyncRequest(requestParams);
        return priorityJob;
    }

    async getStageReruns(settings) {
        const requestParams = {
            "type": "dashboard",
            "metric": "stage_reruns",
            "pipeline_name": settings.selectedPipeline,
            "order": settings.requestOrder,
            "limit": settings.requestLimit
        }
        const stageReruns = await this.asyncRequest(requestParams);
        return stageReruns;
    }


    async asyncRequest(requestParams) {
        c.log('3. RequestMaster asyncRequest');
        c.log('🧩 asyncRequest() this.transport', this.transport);
        c.log('🧩 asyncRequest() requestParams ', requestParams);

        return new Promise((resolve) => {
            this.transport.request("fetch-analytics", requestParams)
                .done((data) => resolve(JSON.parse(data)))
                .fail(console.error.toString());
        });
    }
}

export default RequestMaster