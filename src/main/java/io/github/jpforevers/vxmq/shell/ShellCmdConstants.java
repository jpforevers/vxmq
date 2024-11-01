/*
 * Copyright (C) 2023-2024 王用军
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.jpforevers.vxmq.shell;

public class ShellCmdConstants {

  public static final String COMMAND_OPTION_HELP_SHORT_NAME = "h";
  public static final String COMMAND_OPTION_HELP_LONG_NAME = "help";
  public static final String COMMAND_OPTION_LIST_SHORT_NAME = "l";
  public static final String COMMAND_OPTION_LIST_LONG_NAME = "list";
  public static final String COMMAND_OPTION_COUNT_SHORT_NAME = "c";
  public static final String COMMAND_OPTION_COUNT_LONG_NAME = "count";
  public static final String COMMAND_OPTION_DELETE_SHORT_NAME = "d";
  public static final String COMMAND_OPTION_DELETE_LONG_NAME = "delete";
  public static final String COMMAND_OPTION_GET_SHORT_NAME = "g";
  public static final String COMMAND_OPTION_GET_LONG_NAME = "get";
  public static final String COMMAND_OPTION_SET_SHORT_NAME = "s";
  public static final String COMMAND_OPTION_SET_LONG_NAME = "set";
  public static final String COMMAND_OPTION_FOLLOW_SHORT_NAME = "f";
  public static final String COMMAND_OPTION_FOLLOW_LONG_NAME = "follow";
  public static final String COMMAND_OPTION_EXACT_LONG_NAME = "exact";
  public static final String COMMAND_OPTION_WILDCARD_LONG_NAME = "wildcard";
  public static final String COMMAND_OPTION_MATCH_LONG_NAME = "match";
  public static final String COMMAND_OPTION_CLIENT_LONG_NAME = "client";
  public static final String COMMAND_OPTION_NODE_LONG_NAME = "node";

  public static final String COMMAND_SESSIONS = "sessions";
  public static final String COMMAND_WILLS = "wills";
  public static final String COMMAND_RETAINS = "retains";
  public static final String COMMAND_SUBS = "subs";
  public static final String COMMAND_INBOUNDQOS2PUBS = "inboundQos2Pubs";
  public static final String COMMAND_OUTBOUNDQOS1PUBS = "outboundQos1Pubs";
  public static final String COMMAND_OUTBOUNDQOS2PUBS = "outboundQos2Pubs";
  public static final String COMMAND_OUTBOUNDQOS2RELS = "outboundQos2Rels";
  public static final String COMMAND_OFFLINEMSG = "offlineMsg";

  public static final String COMMAND_LOGGER = "logger";
  public static final String COMMAND_LOGS = "logs";
  public static final String COMMAND_CLIENT_VERTICLE = "client-verticle";
  public static final String COMMAND_CLUSTER = "cluster";

  public static final String COMMAND_ARG_NAME_CLIENT_ID = "clientId";
  public static final String COMMAND_ARG_NAME_TOPIC_NAME = "topicName";
  public static final String COMMAND_ARG_NAME_NODE_ID = "nodeId";

}
