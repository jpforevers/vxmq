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

package cloud.wangyongjun.vxmq.service.authentication;

import cloud.wangyongjun.vxmq.assist.ModelConstants;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.nio.charset.StandardCharsets;

@DataObject
public class MqttAuthData {

  private final int protocolLevel;
  private final String clientId;
  private final String username;
  private final byte[] password;
  private final byte[] cert;
  private final String remoteAddr;
  private final Integer remotePort;

  private MqttAuthData(Builder builder){
    this.protocolLevel = builder.protocolLevel;
    this.clientId = builder.clientId;
    this.username = builder.username;
    this.password = builder.password;
    this.cert = builder.cert;
    this.remoteAddr = builder.remoteAddr;
    this.remotePort = builder.remotePort;
  }

  public static MqttAuthData.Builder builder(){
    return new MqttAuthData.Builder();
  }

  public MqttAuthData(JsonObject jsonObject) {
    this.protocolLevel = jsonObject.getInteger(ModelConstants.FIELD_NAME_PROTOCOL_LEVEL);
    this.clientId = jsonObject.getString(ModelConstants.FIELD_NAME_CLIENT_ID);
    this.username = jsonObject.getString(ModelConstants.FIELD_NAME_USERNAME);
    this.password = jsonObject.getString(ModelConstants.FIELD_NAME_PASSWORD) == null ? null : jsonObject.getString(ModelConstants.FIELD_NAME_PASSWORD).getBytes(StandardCharsets.UTF_8);
    this.cert = jsonObject.getBinary(ModelConstants.FIELD_NAME_CERT);
    this.remoteAddr = jsonObject.getString(ModelConstants.FIELD_NAME_REMOTE_ADDR);
    this.remotePort = jsonObject.getInteger(ModelConstants.FIELD_NAME_REMOTE_PORT);
  }

  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put(ModelConstants.FIELD_NAME_PROTOCOL_LEVEL, protocolLevel);
    jsonObject.put(ModelConstants.FIELD_NAME_CLIENT_ID, clientId);
    jsonObject.put(ModelConstants.FIELD_NAME_USERNAME, username);
    jsonObject.put(ModelConstants.FIELD_NAME_PASSWORD, password == null ? null : new String(password, StandardCharsets.UTF_8));
    jsonObject.put(ModelConstants.FIELD_NAME_CERT, cert);
    jsonObject.put(ModelConstants.FIELD_NAME_REMOTE_ADDR, remoteAddr);
    jsonObject.put(ModelConstants.FIELD_NAME_REMOTE_PORT, remotePort);
    return jsonObject;
  }

  public int getProtocolLevel() {
    return protocolLevel;
  }

  public String getClientId() {
    return clientId;
  }

  public String getUsername() {
    return username;
  }

  public byte[] getPassword() {
    return password;
  }

  public byte[] getCert() {
    return cert;
  }

  public String getRemoteAddr() {
    return remoteAddr;
  }

  public Integer getRemotePort() {
    return remotePort;
  }

  public static class Builder{
    private int protocolLevel;
    private String clientId;
    private String username;
    private byte[] password;
    private byte[] cert;
    private String remoteAddr;
    private Integer remotePort;

    private Builder(){

    }

    public Builder protocolLevel(int protocolLevel) {
      this.protocolLevel = protocolLevel;
      return this;
    }

    public Builder clientId(String clientId) {
      this.clientId = clientId;
      return this;
    }

    public Builder username(String username) {
      this.username = username;
      return this;
    }

    public Builder password(byte[] password) {
      this.password = password;
      return this;
    }

    public Builder cert(byte[] cert) {
      this.cert = cert;
      return this;
    }

    public Builder remoteAddr(String remoteAddr) {
      this.remoteAddr = remoteAddr;
      return this;
    }

    public Builder remotePort(Integer remotePort) {
      this.remotePort = remotePort;
      return this;
    }

    public MqttAuthData build(){
      return new MqttAuthData(this);
    }

  }

}
