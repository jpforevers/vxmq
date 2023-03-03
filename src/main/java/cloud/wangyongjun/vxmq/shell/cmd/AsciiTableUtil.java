package cloud.wangyongjun.vxmq.shell.cmd;

import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_LongestWordMax;
import de.vandermeer.asciithemes.a7.A7_Grids;

import java.util.List;

class AsciiTableUtil {

  public static String format(List<String> headers, List<List<String>> rows) {
    AsciiTable at = new AsciiTable();
    at.addRule();
    at.addRow(headers);
    at.addRule();
    for (List<String> row : rows) {
      at.addRow(row.stream().map(s -> s == null ? "null" : s).toList());
      at.addRule();
    }
    at.getRenderer().setCWC(new CWC_LongestWordMax(24));
    at.getContext().setGrid(A7_Grids.minusBarPlusEquals());
    return at.render();
  }

}
