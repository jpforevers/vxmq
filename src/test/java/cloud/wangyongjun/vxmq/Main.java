package cloud.wangyongjun.vxmq;

import cloud.wangyongjun.vxmq.assist.ConsumerUtil;

public class Main {

  public static void main(String[] args) {
    VxmqLauncher vxmqLauncher = new VxmqLauncher();
    vxmqLauncher.start().subscribe().with(ConsumerUtil.nothingToDo(), ConsumerUtil.nothingToDo());
  }
}
