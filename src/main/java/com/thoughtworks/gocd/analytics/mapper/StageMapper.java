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

package com.thoughtworks.gocd.analytics.mapper;

import com.thoughtworks.gocd.analytics.models.Stage;
import com.thoughtworks.gocd.analytics.models.StageTimeSummary;
import org.apache.ibatis.annotations.*;

import java.time.ZonedDateTime;
import java.util.List;

public interface StageMapper {

    @Insert("INSERT INTO stages(pipeline_name," +
        "pipeline_counter," +
        "stage_name," +
        "stage_counter," +
        "result," +
        "state," +
        "approval_type," +
        "approved_by," +
        "scheduled_at," +
        "completed_at," +
        "duration_secs," +
        "time_waiting_secs) values (#{pipelineName}," +
        "#{pipelineCounter}," +
        "#{stageName}," +
        "#{stageCounter}," +
        "#{result}," +
        "#{state}," +
        "#{approvalType}," +
        "#{approvedBy}," +
        "#{scheduledAt}," +
        "#{completedAt}," +
        "#{totalTimeSecs}," +
        "#{timeWaitingSecs})")
    @SelectKey(statement = "SELECT lastval()", keyProperty = "id", keyColumn = "id", before = false, resultType = long.class)
    void insert(Stage stage);

    @Results(id = "Stage", value = {
        @Result(property = "pipelineName", column = "pipeline_name"),
        @Result(property = "pipelineCounter", column = "pipeline_counter"),
        @Result(property = "stageName", column = "stage_name"),
        @Result(property = "stageCounter", column = "stage_counter"),
        @Result(property = "result", column = "result"),
        @Result(property = "state", column = "state"),
        @Result(property = "approvalType", column = "approval_type"),
        @Result(property = "approvedBy", column = "approved_by"),
        @Result(property = "scheduledAt", column = "scheduled_at"),
        @Result(property = "completedAt", column = "completed_at"),
        @Result(property = "totalTimeSecs", column = "duration_secs"),
        @Result(property = "timeWaitingSecs", column = "time_waiting_secs")
    })
    @Select("SELECT * FROM stages WHERE pipeline_name = #{pipelineName}")
    List<Stage> allStages(@Param("pipelineName") String pipelineName);

    @ResultMap("Stage")
    @Select("SELECT *\n" +
        "  FROM stages\n" +
        " WHERE pipeline_name = #{pipelineName}\n" +
        "   AND (DATE(#{startDate}) IS NULL OR DATE(scheduled_at) >= DATE(#{startDate}))\n" +
        "   AND (DATE(#{endDate}) IS NULL OR DATE(scheduled_at) <= DATE(#{endDate}))\n" +
        "   AND stage_name = #{stageName}")
    List<Stage> stageHistory(@Param("pipelineName") String pipelineName,
        @Param("stageName") String stageName,
        @Param("startDate") ZonedDateTime startDate,
        @Param("endDate") ZonedDateTime endDate);

    @ResultMap("Stage")
    @Select("  SELECT *\n" +
        "    FROM stages\n" +
        "   WHERE pipeline_name = #{pipelineName}\n" +
        "     AND pipeline_counter = #{pipelineCounter}\n" +
        "     AND stage_name = #{stageName}\n" +
        "     AND stage_counter = #{stageCounter}\n" +
        "ORDER BY completed_at\n" +
        "   LIMIT 1")
    Stage One(@Param("pipelineName") String pipelineName,
        @Param("pipelineCounter") int pipelineCounter,
        @Param("stageName") String stageName,
        @Param("stageCounter") int stageCounter);

    @Delete("DELETE FROM stages where scheduled_at AT TIME ZONE 'UTC' < #{scheduled_date} AT TIME ZONE 'UTC';")
    void deleteStageRunsPriorTo(@Param("scheduled_date") ZonedDateTime scheduledDate);

    @ResultMap("Stage")
    @Select("<script>" +
        "  SELECT * FROM stages " +
        "    WHERE (id IN (SELECT stage_id FROM pipeline_workflows WHERE workflow_id=#{workflowId}))"
        +
        "      AND pipeline_name IN " +
        "      <foreach item='item' index='index' collection='pipelines'" +
        "        open='(' separator=',' close=')'>" +
        "        #{item}" +
        "      </foreach>" +
        "</script>")
    List<Stage> allStageInstancesWithWorkflowIdOfPipelines(@Param("workflowId") Long workflowID,
        @Param("pipelines") List<String> pipelines);

    @ResultMap("Stage")
//    @Select("select * from stages where id in (select stage_id from pipeline_workflows pw where "
//        + "pipeline_id in (select id from pipelines p where name = #{pipelineName} limit 10))")
    @Select("<script>"
        + "select * from stages where "
        + "<if test='result != null'>"
        + "result = #{result} and "
        + "</if>"
        + "id in (select stage_id from pipeline_workflows pw where "
        + "pipeline_id in (select id from pipelines p where name = #{pipelineName} order by id "
        + "<if test='order != null'>"
        + "desc "
        + "</if>"
        + "limit #{limit})) "
        + "</script>"
    )
    List<Stage> stageByPipelineNameAndCounter(@Param("pipelineName") String pipelineName, @Param(
        "result") String result, @Param("order") String order, @Param("limit") int limit);

    @ResultMap("Stage")
    @Select("<script>"
        + "SELECT pipeline_name, stage_name, MAX(stage_counter) AS stage_counter\n"
        + "FROM stages s \n"
        + "GROUP BY pipeline_name, stage_name\n"
        + "HAVING MAX(stage_counter) > 1\n"
        + "order by stage_counter ${order} limit #{limit} ;"
        + "</script>"
    )
    List<Stage> stageRerunsForAllPipelines(@Param("result") String result,
        @Param("order") String order, @Param("limit") int limit);

    @ResultMap("Stage")
    @Select("<script>"
        + "SELECT pipeline_name, stage_name, pipeline_counter, MAX(stage_counter) AS stage_counter\n"
        + "FROM stages s \n"
        + "WHERE pipeline_name = #{pipelineName} \n"
        + "GROUP BY pipeline_name, stage_name, pipeline_counter\n"
        + "HAVING MAX(stage_counter) > 1\n"
        + "order by stage_counter ${order} limit #{limit} ;"
        + "</script>"
    )
    List<Stage> stageReruns(@Param("pipelineName") String pipelineName, @Param(
        "result") String result, @Param("order") String order, @Param("limit") int limit);

    @ResultMap("Stage")
    @Select("<script>"
        + "select * from stages \n"
        + "where pipeline_name = #{pipelineName} \n"
        + "and stage_name = #{stageName} \n"
        + "and pipeline_counter = #{pipelineCounter} \n"
        + "order by id asc \n"
        + "</script>"
    )
    List<Stage> stageRerunsForPipelineStageAndCounter(@Param("pipelineName") String pipelineName,
        @Param("stageName") String stageName, @Param("pipelineCounter") int pipelineCounter,
        @Param("result") String result, @Param("order") String order, @Param("limit") int limit);

    @ResultMap("Stage")
    @Select("<script>"
        + "SELECT *\n"
        + "FROM (\n"
        + "  SELECT *, ROW_NUMBER() OVER (PARTITION BY pipeline_counter ORDER BY id ASC) AS row_num\n"
        + "  FROM stages\n"
        + "  WHERE pipeline_name = #{pipelineName}\n"
        + ") AS ranked_data\n"
        + "WHERE row_num = 1;"
        + "</script>"
    )
    List<Stage> stageStartupTime(@Param("pipelineName") String pipelineName, @Param(
        "result") String result, @Param("order") String order, @Param("limit") int limit);

    @ResultMap("Stage")
    @Select("<script>"
        + "select * from stages where pipeline_name = #{pipelineName} \n"
        + "and pipeline_counter = #{pipelineCounter} \n"
        + "order by id;"
        + "</script>"
    )
    List<Stage> stageStartupTimeCompare(@Param("pipelineName") String pipelineName,
        @Param("pipelineCounter") int pipelineCounter);

    @Results(id = "stage_summary", value = {
        @Result(property = "pipelineName", column = "pipeline_name"),
        @Result(property = "stageName", column = "stage_name"),
        @Result(property = "times", column = "times"),
        @Result(property = "sumTotalTimeSecs", column = "sum_total_time_secs"),
        @Result(property = "sumTimeWaitingSecs", column = "sum_time_waiting_secs")
    })
    @Select("SELECT pipeline_name, stage_name,\n"
        + "       SUM(CASE WHEN result = #{result} THEN 1 ELSE 0 END) AS times,\n"
        + "       SUM(CASE WHEN result = #{result} THEN duration_secs ELSE 0 END) AS sum_total_time_secs,\n"
        + "       SUM(CASE WHEN result = #{result} THEN time_waiting_secs ELSE 0 END) AS sum_time_waiting_secs\n"
        + "FROM stages\n"
        + "GROUP BY pipeline_name, stage_name\n"
        + "HAVING SUM(CASE WHEN result = #{result} THEN 1 ELSE 0 END) > 0\n"
        + "order by times desc;")
    List<StageTimeSummary> stageSummary(@Param("result") String result);

    @ResultMap("Stage")
    @Select("SELECT * FROM stages s WHERE s.stage_name = #{stageName} and s.result = #{result} "
        + "ORDER BY s.scheduled_at")
    List<Stage> stageSummaryDetails(@Param("stageName") String stageName,
        @Param("result") String result);
}
