package cloud.wangyongjun.vxmq.assist;

public enum EBServices {

  NOTHING_SERVICE(EBAddress.SERVICE_NOTHING_SERVICE),
  SUB_SERVICE(EBAddress.SERVICE_SUB_SERVICE),
  CLIENT_LOCK_SERVICE(EBAddress.SERVICE_CLIENT_LOCK_SERVICE);

  private final String ebAddress;

  EBServices(String ebAddress) {
    this.ebAddress = ebAddress;
  }

  public String getEbAddress() {
    return ebAddress;
  }

}
