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

package io.github.jpforevers.vxmq.http.api;

public class ApiConstants {

  public static final String API_URL_PREFIX_API = "/api";
  public static final String API_URL_PREFIX_VERSION_V1 = "/v1";
  public static final String API_URL_PREFIX_VERSION_V2 = "/v2";
  public static final String API_URL_PREFIX_V1 = API_URL_PREFIX_API + API_URL_PREFIX_VERSION_V1;
  public static final String API_URL_PREFIX_V2 = API_URL_PREFIX_API + API_URL_PREFIX_VERSION_V2;

  public static final String API_PREFIX_TEST = "/test";
  public static final String API_PREFIX_SESSION = "/session";
  public static final String API_PREFIX_SESSIONS = "/sessions";

  public static final String Q_URL_PREFIX = "/q";
  public static final String Q_PREFIX_HEALTH = "/health";
  public static final String Q_PREFIX_PING = "/ping";
  public static final String Q_PREFIX_METRICS = "/metrics";

}
