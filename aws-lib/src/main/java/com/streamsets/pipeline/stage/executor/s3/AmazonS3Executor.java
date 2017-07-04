/**
 * Copyright 2017 StreamSets Inc.
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
package com.streamsets.pipeline.stage.executor.s3;

import com.amazonaws.services.s3.model.ObjectTagging;
import com.amazonaws.services.s3.model.SetObjectTaggingRequest;
import com.amazonaws.services.s3.model.Tag;
import com.streamsets.pipeline.api.Batch;
import com.streamsets.pipeline.api.Record;
import com.streamsets.pipeline.api.StageException;
import com.streamsets.pipeline.api.base.BaseExecutor;
import com.streamsets.pipeline.api.base.OnRecordErrorException;
import com.streamsets.pipeline.api.el.ELEval;
import com.streamsets.pipeline.api.el.ELEvalException;
import com.streamsets.pipeline.api.el.ELVars;
import com.streamsets.pipeline.lib.el.RecordEL;
import com.streamsets.pipeline.stage.common.DefaultErrorRecordHandler;
import com.streamsets.pipeline.stage.common.ErrorRecordHandler;
import com.streamsets.pipeline.stage.executor.s3.config.AmazonS3ExecutorConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AmazonS3Executor extends BaseExecutor {

  private static final Logger LOG = LoggerFactory.getLogger(AmazonS3Executor.class);

  private final AmazonS3ExecutorConfig config;
  private ErrorRecordHandler errorRecordHandler;
  private Map<String, ELEval> evals;

  public AmazonS3Executor(AmazonS3ExecutorConfig config) {
    this.config = config;
  }

  @Override
  protected List<ConfigIssue> init() {
    List<ConfigIssue> issues = super.init();
    config.init(getContext(), issues);
    errorRecordHandler = new DefaultErrorRecordHandler(getContext());
    evals = new HashMap<>();

    // Initialize ELs
    validateEL("bucketTemplate", config.s3Config.bucketTemplate, issues);
    validateEL("objectPath", config.taskConfig.objectPath, issues);
    validateEL("tags", null, issues);

    return issues;
  }

  private void validateEL(String configName, String el, List<ConfigIssue> issues) {
     try {
      evals.put(configName, getContext().createELEval(configName));
      if(el != null) {
        getContext().parseEL(el);
      }
    } catch (ELEvalException e) {
      issues.add(getContext().createConfigIssue(Groups.TASKS.name(), configName, Errors.S3_EXECUTOR_0001, e.getMessage()));
    }
  }

  @Override
  public void write(Batch batch) throws StageException {
    Iterator<Record> it = batch.getRecords();
    while(it.hasNext()) {
      Record record = it.next();

      try {
        switch (config.taskConfig.taskType) {
          case CHANGE_EXISTING_OBJECT:
            changeExistingObject(record);
            break;
          default:
            throw new StageException(Errors.S3_EXECUTOR_0000, "Unknown task type: " + config.taskConfig.taskType);
        }
      } catch (OnRecordErrorException e) {
        errorRecordHandler.onError(e);
      } catch (Exception e) {
        LOG.error("Can't execute S3 operation", e);
        errorRecordHandler.onError(new OnRecordErrorException(record, Errors.S3_EXECUTOR_0000, e.toString()));
      }
    }
  }

  private void changeExistingObject(Record record) throws OnRecordErrorException {
    ELVars variables = getContext().createELVars();
    RecordEL.setRecordInContext(variables, record);

    // Working file
    String bucket = evaluate(record, "bucketTemplate", variables, config.s3Config.bucketTemplate);
    String objectPath = evaluate(record, "objectPath", variables, config.taskConfig.objectPath);
    if(bucket.isEmpty()) {
      throw new OnRecordErrorException(record, Errors.S3_EXECUTOR_0003);
    }
    if(objectPath.isEmpty()) {
      throw new OnRecordErrorException(record, Errors.S3_EXECUTOR_0004);
    }
    LOG.debug("Working on {}:{}", bucket, objectPath);

    // Tag application
    if(!config.taskConfig.tags.isEmpty()) {
      List<Tag> newTags = new ArrayList<>();

      // Evaluate each tag separately
      for (Map.Entry<String, String> entry : config.taskConfig.tags.entrySet()) {
        newTags.add(new Tag(
          evaluate(record, "tags", variables, entry.getKey()),
          evaluate(record, "tags", variables, entry.getValue())
        ));
      }

      // Apply all tags at once
      config.s3Config.getS3Client().setObjectTagging(new SetObjectTaggingRequest(
        bucket,
        objectPath,
        new ObjectTagging(newTags)
      ));
    }
  }

  private String evaluate(Record record, String name, ELVars vars, String expression) throws OnRecordErrorException {
    try {
      return evals.get(name).eval(vars, expression, String.class);
    } catch (ELEvalException e) {
      throw new OnRecordErrorException(record, Errors.S3_EXECUTOR_0002, e.toString(), e);
    }
  }

  @Override
  public void destroy() {
    config.destroy();
  }
}
