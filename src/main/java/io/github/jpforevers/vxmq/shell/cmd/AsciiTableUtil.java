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

package io.github.jpforevers.vxmq.shell.cmd;

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
