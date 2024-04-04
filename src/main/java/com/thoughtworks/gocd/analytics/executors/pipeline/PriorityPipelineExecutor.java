/*
 * Copyright 2024 ThoughtWorks, Inc.
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

package com.thoughtworks.gocd.analytics.executors.pipeline;

import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.gocd.analytics.SessionFactory;
import com.thoughtworks.gocd.analytics.dao.PipelineDAO;
import com.thoughtworks.gocd.analytics.executors.AbstractSessionFactoryAwareExecutor;
import com.thoughtworks.gocd.analytics.models.AnalyticsRequest;
import com.thoughtworks.gocd.analytics.models.AnalyticsResponseBody;
import com.thoughtworks.gocd.analytics.models.PipelineTimeSummary;
import com.thoughtworks.gocd.analytics.models.PipelineTimeSummaryTwo;
import java.util.List;

public class PriorityPipelineExecutor extends AbstractSessionFactoryAwareExecutor {

    private PipelineDAO pipelineDAO;

    public PriorityPipelineExecutor(AnalyticsRequest analyticsRequest, SessionFactory sessionFactory) {
        this(analyticsRequest, new PipelineDAO(), sessionFactory);
    }

    public PriorityPipelineExecutor(AnalyticsRequest analyticsRequest,
        PipelineDAO pipelineDAO, SessionFactory sessionFactory) {
        super(analyticsRequest, sessionFactory);
        this.pipelineDAO = pipelineDAO;
    }

    @Override
    protected GoPluginApiResponse doExecute() {

        final String result = param(PARAM_RESULT);

        List<PipelineTimeSummaryTwo> summary =
            doInTransaction(sqlSession -> pipelineDAO.pipelineSummaryTwo(sqlSession, result));

        AnalyticsResponseBody responseBody = new AnalyticsResponseBody(summary,
            ".html");

        return new DefaultGoPluginApiResponse(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE,
            responseBody.toJson());
    }
}
