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
