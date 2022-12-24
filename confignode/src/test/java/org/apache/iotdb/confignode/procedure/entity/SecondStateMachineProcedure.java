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

package org.apache.iotdb.confignode.procedure.entity;

import org.apache.iotdb.confignode.procedure.env.TestProcEnv;
import org.apache.iotdb.confignode.procedure.exception.ProcedureException;
import org.apache.iotdb.confignode.procedure.impl.statemachine.StateMachineProcedure;
import org.apache.iotdb.confignode.procedure.state.ProcedureLockState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class SecondStateMachineProcedure extends StateMachineProcedure<TestProcEnv, TestState> {

  public static final Logger LOGGER = LoggerFactory.getLogger(SecondStateMachineProcedure.class);

  public int throwAtIndex = -1;

  @Override
  protected Flow executeFromState(TestProcEnv testProcEnv, TestState testState) {
    AtomicInteger acc = testProcEnv.getAcc();
    try {
      switch (testState) {
        case STEP_1:
          LOGGER.info("Execute SecondStateMachineProcedure {} STEP_1", getProcId());
          acc.getAndAdd(1);
          setNextState(TestState.STEP_2);
          break;
        case STEP_2:
          LOGGER.info("Execute SecondStateMachineProcedure {} STEP_2", getProcId());
          if (throwAtIndex > 0) {
            throw new RuntimeException("throw EXCEPTION in SecondStateMachineProcedure");
          }
          setNextState(TestState.STEP_3);
          break;
        case STEP_3:
          LOGGER.info("Execute SecondStateMachineProcedure {} STEP_3", getProcId());
          acc.getAndAdd(-1);
          return Flow.NO_MORE_STATE;
      }
    } catch (Exception e) {
      if (isRollbackSupported(testState)) {
        setFailure("SecondStateMachineProcedure failed", new ProcedureException(e));
      }
    }
    return Flow.HAS_MORE_STATE;
  }

  @Override
  protected boolean isRollbackSupported(TestState testState) {
    return true;
  }

  @Override
  protected void rollbackState(TestProcEnv testProcEnv, TestState testState) {
    LOGGER.info(
        "Execute rollback in SecondStateMachineProcedure {}, testState: {}",
        getProcId(),
        testState);
  }

  @Override
  protected TestState getState(int stateId) {
    return TestState.values()[stateId];
  }

  @Override
  protected int getStateId(TestState testState) {
    return testState.ordinal();
  }

  @Override
  protected TestState getInitialState() {
    return TestState.STEP_1;
  }

  @Override
  protected ProcedureLockState acquireLock(TestProcEnv testProcEnv) {
    testProcEnv.getEnvLock().lock();
    try {
      if (testProcEnv.getExecuteLock().tryLock(this)) {
        LOGGER.info("Second {} acquire lock.", getProcId());
        return ProcedureLockState.LOCK_ACQUIRED;
      }
      testProcEnv.getExecuteLock().waitProcedure(this);

      LOGGER.info("Second {} wait for lock.", getProcId());
      return ProcedureLockState.LOCK_EVENT_WAIT;
    } finally {
      testProcEnv.getEnvLock().unlock();
    }
  }

  @Override
  protected void releaseLock(TestProcEnv testProcEnv) {
    testProcEnv.getEnvLock().lock();
    try {
      LOGGER.info("Second {} release lock.", getProcId());
      if (testProcEnv.getExecuteLock().releaseLock(this)) {
        testProcEnv.getExecuteLock().wakeWaitingProcedures(testProcEnv.getScheduler());
      }
    } finally {
      testProcEnv.getEnvLock().unlock();
    }
  }
}
