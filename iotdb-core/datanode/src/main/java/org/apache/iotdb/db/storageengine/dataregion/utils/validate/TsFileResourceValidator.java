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

package org.apache.iotdb.db.storageengine.dataregion.utils.validate;

import org.apache.iotdb.db.storageengine.dataregion.tsfile.TsFileResource;
import org.apache.iotdb.db.storageengine.dataregion.utils.TsFileResourceUtils;

import java.util.List;

@SuppressWarnings("squid:S6548")
public class TsFileResourceValidator implements TsFileValidator {

  private TsFileResourceValidator() {}

  @Override
  public boolean validateTsFile(TsFileResource resource) {
    return TsFileResourceUtils.validateTsFileIsComplete(resource)
        && TsFileResourceUtils.validateTsFileResourceCorrectness(resource);
  }

  @Override
  public boolean validateTsFiles(List<TsFileResource> resourceList) {
    for (TsFileResource resource : resourceList) {
      if (!validateTsFile(resource)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean validateTsFilesIsHasNoOverlap(List<TsFileResource> resourceList) {
    return TsFileResourceUtils.validateTsFileResourcesHasNoOverlap(resourceList);
  }

  public static TsFileResourceValidator getInstance() {
    return ResourceOnlyCompactionValidatorHolder.INSTANCE;
  }

  private static class ResourceOnlyCompactionValidatorHolder {
    private static final TsFileResourceValidator INSTANCE = new TsFileResourceValidator();
  }
}
