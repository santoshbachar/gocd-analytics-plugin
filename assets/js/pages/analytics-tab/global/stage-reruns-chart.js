/*
 * Copyright 2020 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import "css/global";

import AnalyticsEndpoint from "gocd-server-comms";
import $ from "jquery";

import GraphManager from "../../../santosh/GraphManager";
import Header from "../../../santosh/defination/stage-timeline/header";
import RequestMaster from "../../../RequestMaster";
import Footer from "../../../santosh/defination/stage-timeline/footer";
import Console from "../../../santosh/Console";

let graphManager = null;
let requestMaster = null;

let header = null;
let footer = null;

const c = new Console('stage-reruns-chart.js', 'dev');

AnalyticsEndpoint.onInit(function (initialData, transport) {

    const data = JSON.parse(initialData);
    console.log('stage-reruns data', data);


    requestMaster = new RequestMaster(transport);
    header = new Header(requestMaster);
    footer = new Footer();

    graphManager = new GraphManager("series", transport, informSeriesMovement, footer);

    (async () => {
        const defaultSettings = await header.getStageRerunsHeader(changeHandler);

        await doStageReruns(data, defaultSettings);
    })();

});

async function informSeriesMovement(graphName) {
    c.logs('📞 I am informed of graphName ', graphName, ' changing the header now.');

    // return header.switchHeader(graphName);

    if (graphName === 'JobsTimeline') {
        return await header.getJobsTimelineHeader(jobChangeHandler);
    } else if (graphName === 'stage-timeline') {
        c.logs("I have to send stage-timeline header");
    }

    footer.clear();

}

async function doStageReruns(data, settings) {
    footer.clear();

    if (data.length === 0) {
        footer.showMessage("No data for selected option, can't draw a graph.", "Error", true, 10);
        graphManager.clear();
        return;
    }

    graphManager.initSeries("StageReruns", data, settings);
}

async function changeHandler(settings) {
    c.log('changeHandler settings', settings);

    const sr = await requestMaster.getStageReruns(settings);
    doStageReruns(sr, settings);
}

function jobChangeHandler(settings) {
    doJob(settings);
}

async function doJob(settings) {
    c.logs('🛜 requesting job timeline with the settings', settings);

    // caching for now
    // const stageTimeline = await requestMaster.getJobTimeline(settings.selectedStage, settings.pipeline_counter_start, settings.pipeline_counter_end);

    // graphManager.initSeries("JobsTimeline", stageTimeline, settings);
    graphManager.call_initSeriesWithNewSettings(settings);
}


AnalyticsEndpoint.ensure("v1");
