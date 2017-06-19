/**
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
package com.streamsets.datacollector.definition;

import com.streamsets.datacollector.config.LineagePublisherDefinition;
import com.streamsets.datacollector.config.StageLibraryDefinition;
import com.streamsets.pipeline.api.lineage.LineagePublisher;
import com.streamsets.pipeline.api.lineage.LineagePublisherDef;

/**
 * Extracts Model object for Lineage annotation.
 */
public abstract class LineagePublisherDefinitionExtractor {

  private static final LineagePublisherDefinitionExtractor EXTRACTOR = new LineagePublisherDefinitionExtractor() {};

  public static LineagePublisherDefinitionExtractor get() {
    return EXTRACTOR;
  }

  public LineagePublisherDefinition extract(
    StageLibraryDefinition libraryDef,
    Class<? extends LineagePublisher> klass
  ) {
    LineagePublisherDef sDef = klass.getAnnotation(LineagePublisherDef.class);
    String name = StageDefinitionExtractor.getStageName(klass);
    String label = sDef.label();
    String description = sDef.description();
    String prefix = sDef.configurationPrefix();

    return new LineagePublisherDefinition(
      libraryDef,
      libraryDef.getClassLoader(),
      klass,
      name,
      label,
      description,
      prefix
    );
  }

}
