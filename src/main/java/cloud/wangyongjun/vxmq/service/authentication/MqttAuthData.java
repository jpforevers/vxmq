/*
 * Copyright 2018-present 王用军
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
