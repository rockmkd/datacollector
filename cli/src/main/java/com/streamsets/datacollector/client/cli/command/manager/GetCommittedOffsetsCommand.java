/*
 * Copyright 2017 StreamSets Inc.
 *
 * Licensed under the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.streamsets.datacollector.client.cli.command.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.streamsets.datacollector.client.api.ManagerApi;
import com.streamsets.datacollector.client.cli.command.BaseCommand;
import io.airlift.airline.Command;
import io.airlift.airline.Option;

@Command(name = "get-committed-offsets", description = "Get Pipeline Committed Offsets")
public class GetCommittedOffsetsCommand extends BaseCommand {
  @Option(
    name = {"-n", "--name"},
    description = "Pipeline ID",
    required = true
  )
  public String pipelineId;

  @Option(
    name = {"-r", "--revision"},
    description = "Pipeline Revision",
    required = false
  )
  public String pipelineRev;

  @Override
  public void run() {
    if(pipelineRev == null) {
      pipelineRev = "0";
    }
    try {
      ManagerApi managerApi = new ManagerApi(getApiClient());
      ObjectMapper mapper = new ObjectMapper();
      mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
      System.out.println(mapper.writeValueAsString(managerApi.getCommittedOffsets(pipelineId, pipelineRev)));
    } catch (Exception ex) {
      if(printStackTrace) {
        ex.printStackTrace();
      } else {
        System.out.println(ex.getMessage());
      }
    }
  }

  public static void main(String [] args) {
    GetCommittedOffsetsCommand committedOffsetsCommand = new GetCommittedOffsetsCommand();
    committedOffsetsCommand.pipelineId = "Samplecopydc30c633-6e46-4b3e-8dac-48e001841186";
    committedOffsetsCommand.sdcURL = "http://localhost:18630";
    committedOffsetsCommand.printStackTrace = true;
    committedOffsetsCommand.run();
  }

}
