package cloud.wangyongjun.vxmq.assist;

import com.fasterxml.uuid.EthernetAddress;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedGenerator;

import java.util.UUID;

public class UUIDUtil {

  private static final TimeBasedGenerator TIME_BASED_GENERATOR = Generators.timeBasedGenerator(EthernetAddress.fromInterface());

  public static UUID timeBasedUuid() {
    return TIME_BASED_GENERATOR.generate();
  }

}
