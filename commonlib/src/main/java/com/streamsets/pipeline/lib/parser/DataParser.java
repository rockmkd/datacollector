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
package com.streamsets.pipeline.lib.parser;

import com.streamsets.pipeline.api.Record;

import java.io.Closeable;
import java.io.IOException;

public interface DataParser extends Closeable {

  // returns NULL when reaching EOF
  // throws IOException if closed()
  public Record parse() throws IOException, DataParserException;

  // returns current offset or -1 after reaching OEF
  public String getOffset() throws DataParserException, IOException;

  public void setTruncated();

  @Override
  public void close() throws IOException;

}
