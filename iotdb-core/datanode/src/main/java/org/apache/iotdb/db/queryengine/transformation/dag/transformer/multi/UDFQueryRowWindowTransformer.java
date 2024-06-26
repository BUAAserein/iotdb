/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.iotdb.db.queryengine.transformation.dag.transformer.multi;

import org.apache.iotdb.db.queryengine.transformation.api.LayerRowWindowReader;
import org.apache.iotdb.db.queryengine.transformation.api.YieldableState;
import org.apache.iotdb.db.queryengine.transformation.dag.udf.UDTFExecutor;
import org.apache.iotdb.db.queryengine.transformation.dag.util.TypeUtils;

import org.apache.tsfile.block.column.ColumnBuilder;
import org.apache.tsfile.read.common.block.column.TimeColumnBuilder;

public class UDFQueryRowWindowTransformer extends UniversalUDFQueryTransformer {

  protected final LayerRowWindowReader layerRowWindowReader;

  public UDFQueryRowWindowTransformer(
      LayerRowWindowReader layerRowWindowReader, UDTFExecutor executor) {
    super(executor);
    this.layerRowWindowReader = layerRowWindowReader;
  }

  @Override
  protected YieldableState tryExecuteUDFOnce() throws Exception {
    final YieldableState yieldableState = layerRowWindowReader.yield();
    if (yieldableState != YieldableState.YIELDABLE) {
      return yieldableState;
    }

    TimeColumnBuilder timeColumnBuilder = new TimeColumnBuilder(null, 1);
    ColumnBuilder valueColumnBuilder = TypeUtils.initColumnBuilder(tsDataType, 1);

    executor.execute(layerRowWindowReader.currentWindow(), timeColumnBuilder, valueColumnBuilder);
    layerRowWindowReader.readyForNext();
    return YieldableState.YIELDABLE;
  }
}
