import GET_STACKED_BAR_TEMPLATE from "./stacked-bar";
import {getBarSeries, getLineSeries, getPlainBarSeries} from "../template";
import {getDateFromTimestampString, secondsToHms} from "../utils";

/**
 * @class
 * @interface {ChartInterface}
 */
class StageRerunsInstances {

    data = null;
    c = null;

    constructor(settings) {
        console.log('✅ stage-reruns-instances')
        this.settings = settings;

        console.log('stage-reruns-instances.js settings', settings);
    }

    isPipelineSelected() {
        console.log('this.settings.selectedPipeline = ', this.settings.selectedPipeline);
        console.log('isPipelineSelected = ', this.settings.selectedPipeline !== '' || this.settings.selectedPipeline !== '*** All ***');
        return this.settings.selectedPipeline !== '*** All ***';
    }

    draw(data, c) {

        this.data = data;
        this.c = c;

        console.log('stage-reruns.js this.data = ', this.data);

        const info = this.prepareData(this.data);

        const option = {
            tooltip: {
                trigger: "axis",
                axisPointer: {
                    type: "cross",
                    crossStyle: {
                        color: "#999",
                    },
                },
            },
            toolbox: {
                feature: {
                    dataView: {show: true, readOnly: false},
                    magicType: {show: true, type: ["line", "bar"]},
                    restore: {show: true},
                    saveAsImage: {show: true},
                },
            },
            legend: {
                data: info.legends,
            },
            xAxis: [
                {
                    name: "Stage Counter",
                    type: "category",
                    data: info.xData,
                    axisPointer: {
                        type: "shadow",
                    },
                },
            ],
            yAxis: [
                {
                    name: "Time",
                    axisLabel: {
                        formatter: function (value) {
                            return secondsToHms(value);
                        },
                    }
                },
            ],

            series: info.series,
        };

        // option.tooltip.formatter = this.tooltipFormatter();
        // option.tooltip.valueFormatter = (value) => secondsToHms(value);

        return option;
    }

    tooltipFormatter() {
        return function (params) {
            this.c.log(params);

            let result = null;

            if (params.length > 0) {
                result = params[0].name + '<br>';
            }

            params.forEach(param => {
                result += param.marker + param.seriesName + ': ' + param.data + '<br>';
            });

            result += '<hr style="border-top: dotted 1px;">Click on the bar for more info';

            return result;
        };
    }

    prepareData(data) {
        const legend = ["Waiting Time", "Building Time", "Total Time"];
        const xData = [];
        const data1 = [];
        const data2 = [];
        const data3 = [];
        const colorData = [];

        data.forEach((m) => {
            // xData.push(timestampToWords(m.scheduled_at));
            xData.push(m.stage_counter);
            data1.push(m.time_waiting_secs);
            data2.push(m.total_time_secs - m.time_waiting_secs);
            data3.push(m.total_time_secs);
            colorData.push(m.result);
        });

        function getSeries() {
            const wt = getPlainBarSeries("Waiting Time", data1);
            wt.tooltip = {
                valueFormatter: function (value) {
                    return value + " s";
                },
            };

            const bt = getPlainBarSeries("Building Time", data2, colorData);
            bt.tooltip = {
                valueFormatter: function (value) {
                    return value + " s";
                },
            };

            const monitor = getLineSeries("Total Time", data3);

            return [wt, bt, monitor];
        }

        return {
            legends: legend,
            xData: xData,
            series: getSeries(),
        };
    }


    get_requestParamsPoint(index) {
        console.log('stage-reruns.js get_requestParamsPoint ', this.data[index]);
        return {
            "pipeline_name": this.data.pipeline_name,
            "stage_name": this.data.stage_name
        }
    }

    getNextGraphName() {
        return null;
    }

    insertBreadcrumb() {
        return false;
    }

    breadcrumbCaption() {
        return "Instance";
    }

    breadcrumbTooltip() {
        return "List all Pipelines";
    }

    getSeriesIndex() {
        return 0;
    }

    nativeClickHandler2remove(transport, params) {
        console.log('nativeClickHandler params', params);

        // const selectedDate = params.name.toString();
        // console.log("Searching for data on ", selectedDate);
        //
        // const filteredData =
        //     this.data.filter(item => selectedDate === getDateFromTimestampString(item.scheduled_at));
        //
        // console.log(filteredData);
        //
        // return filteredData;
    }
}

export default StageRerunsInstances;