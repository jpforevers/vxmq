package cloud.wangyongjun.vxmq.assist;

import java.util.function.Consumer;

public class ConsumerUtil {

  private static final Consumer NOTHING_TO_DO = o -> {
  };

  public static <T> Consumer<T> nothingToDo() {
    return (Consumer<T>) NOTHING_TO_DO;
  }

}
