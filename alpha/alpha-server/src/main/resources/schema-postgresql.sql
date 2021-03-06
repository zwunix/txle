/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

CREATE TABLE IF NOT EXISTS TxEvent (
  surrogateId BIGSERIAL PRIMARY KEY,
  serviceName varchar(100) NOT NULL,
  instanceId varchar(100) NOT NULL,
  creationTime timestamp(6) NOT NULL DEFAULT CURRENT_DATE,
  globalTxId varchar(36) NOT NULL,
  localTxId varchar(36) NOT NULL,
  parentTxId varchar(36) DEFAULT NULL,
  type varchar(50) NOT NULL,
  compensationMethod varchar(256) NOT NULL,
  expiryTime timestamp(6) NOT NULL,
  retryMethod varchar(256) NOT NULL,
  retries int NOT NULL DEFAULT 0,
  category varchar(36) NOT NULL,
  payloads bytea
);

CREATE INDEX IF NOT EXISTS saga_events_index ON TxEvent (surrogateId, globalTxId, localTxId, type, expiryTime);
CREATE INDEX IF NOT EXISTS saga_global_tx_index ON TxEvent (globalTxId);
CREATE INDEX IF NOT EXISTS saga_surrogateId_index ON TxEvent (surrogateId);
CREATE INDEX IF NOT EXISTS saga_tx_type_index ON TxEvent (type);


CREATE TABLE IF NOT EXISTS Command (
  surrogateId BIGSERIAL PRIMARY KEY,
  eventId bigint NOT NULL UNIQUE,
  serviceName varchar(100) NOT NULL,
  instanceId varchar(100) NOT NULL,
  globalTxId varchar(36) NOT NULL,
  localTxId varchar(36) NOT NULL,
  parentTxId varchar(36) DEFAULT NULL,
  compensationMethod varchar(256) NOT NULL,
  payloads bytea,
  status varchar(12),
  lastModified timestamp(6) NOT NULL DEFAULT CURRENT_DATE,
  version bigint NOT NULL,
  category varchar(36) NOT NULL
);

CREATE INDEX IF NOT EXISTS saga_commands_index ON Command (surrogateId, eventId, globalTxId, localTxId, status);


CREATE TABLE IF NOT EXISTS TxTimeout (
  surrogateId BIGSERIAL PRIMARY KEY,
  eventId bigint NOT NULL UNIQUE,
  serviceName varchar(100) NOT NULL,
  instanceId varchar(100) NOT NULL,
  globalTxId varchar(36) NOT NULL,
  localTxId varchar(36) NOT NULL,
  parentTxId varchar(36) DEFAULT NULL,
  type varchar(50) NOT NULL,
  expiryTime TIMESTAMP NOT NULL,
  status varchar(12),
  version bigint NOT NULL,
  category varchar(36) NOT NULL
);

CREATE INDEX IF NOT EXISTS saga_timeouts_index ON TxTimeout (surrogateId, expiryTime, globalTxId, localTxId, status);

/*
 * *********************** It is necessary to execute following sqls before online. **********************
 */
CREATE TABLE IF NOT EXISTS Message (
  id BIGSERIAL PRIMARY KEY,
  globaltxid varchar(36) NOT NULL,
  localtxid varchar(36) NOT NULL,
  status int(1) NOT NULL DEFAULT 0 COMMENT '0-init, 1-sending, 2-success, 3-fail',
  version int(2) NOT NULL DEFAULT 1,
  dbdrivername varchar(50),
  dburl varchar(100),
  dbusername varchar(20),
  tablename varchar(255),
  operation varchar(20) DEFAULT 'update',
  ids bytea,
  createtime TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS txle_globalTxId_index ON Message(globaltxid);


CREATE TABLE IF NOT EXISTS Config (
  id BIGSERIAL PRIMARY KEY,
  servicename varchar(100),
  instanceid varchar(100),
  instanceid varchar(100),
  type int(2) NOT NULL DEFAULT 0 COMMENT '1-globaltx, 2-compensation, 3-autocompensation, 4-bizinfotokafka, 5-txmonitor, 6-alert, 7-schedule, 8-globaltxfaulttolerant, 9-compensationfaulttolerant, 10-autocompensationfaulttolerant, 50-accidentreport, 51-sqlmonitor  if values are less than 50, then configs for server, otherwise configs for client.',
  status int(1) NOT NULL DEFAULT 0 COMMENT '0-normal, 1-historical, 2-dumped',
  ability int(1) NOT NULL DEFAULT 1 COMMENT '0-do not provide ability, 1-provide ability     ps: the client''s ability inherits the global ability.',
  value varchar(100) NOT NULL,
  remark varchar(500),
  updatetime TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS index_type ON Config(type);