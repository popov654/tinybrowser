package render;

import java.util.Vector;

/**
 *
 * @author Alex
 */
public class TableLayout {

    TableLayout(Block b) {
        this.block = b;
        b.v.clear();
        this.cellspacing = !b.border_collapse ? b.border_spacing : 0;
        this.border_collapse = b.border_collapse;
        Vector<Block> els = b.getChildren();
        for (int i = 0; i < els.size(); i++) {
            if (border_collapse) {
                Block row = els.get(i);
                row.borderWidth[1] = 0;
                row.borderWidth[2] = 0;
                row.borderWidth[3] = 0;
            }
            rows.add(new Row(els.get(i)));
            b.v.add(els.get(i));
        }
        pendingCells.clear();
        col_widths = new int[cols_n];
        row_heights = new int[rows_n];
        doLayout();
    }

    class Row {

        Row(Block b) {
            this.block = b;
            b.display_type = Block.Display.TABLE_ROW;
            b.v.clear();
            int x = 0;
            this.cy = rows.size();
            Vector<Block> els = b.getChildren();
            for (int i = 0; i < els.size(); i++) {
                Cell pc;
                while ((pc = getPendingCellAt(x, this.y)) != null) {
                    x += pc.colspan;
                    if (pc.cy + pc.rowspan - 1 == this.y) {
                        pendingCells.remove(pc);
                    }
                    continue;
                }

                Cell c = new Cell(els.get(i));
                c.cx = x;
                c.cy = this.cy;
                x += els.get(i).colspan;
                cells.add(c);
                b.v.add(els.get(i));

                if (border_collapse) {
                    c.block.borderWidth[1] = 0;
                    c.block.borderWidth[2] = 0;
                }

                if (c.rowspan > 1) pendingCells.add(c);
            }
            if (cells.size() > cols_n) cols_n = cells.size();
            rows_n++;
        }

        Block block;
        Vector<Cell> cells = new Vector<Cell>();
        int y;
        int cy;
        int height;
    }

    class Cell {

        Cell(Block block) {
            block.display_type = Block.Display.TABLE_CELL;
            this.block = block;
            this.colspan = block.colspan;
            this.rowspan = block.rowspan;
        }

        int width;
        int height;
        int cx = 0;
        int cy = 0;
        int x = 0;
        int y = 0;
        int colspan = 1;
        int rowspan = 1;
        Block block;
    }

    Block block;

    int cols_n = 0;
    int rows_n = 0;

    int[] col_widths;
    int[] row_heights;

    private void calculateWidths() {
        int min_w = 0;
        int[] free = new int[cols_n];

        for (int i = 0; i < cols_n; i++) {
            free[i] = -1;
        }
        
        boolean[] auto_w = new boolean[cols_n];
        for (int i = 0; i < cols_n; i++) {
            auto_w[i] = true;
        }

        for (int i = 0; i < rows_n; i++) {
            int min = 0;
            int j = 0;
            while (j < cols_n) {
                Cell pc;
                while ((pc = getPendingCellAt(j, i)) != null) {
                    j += pc.colspan;
                    if (pc.cy + pc.rowspan - 1 == i) {
                        pendingCells.remove(pc);
                    }
                }
                if (j >= cols_n) break;

                Cell cell = rows.get(i).cells.get(j);
                cell.block.performLayout();

                if (cell.rowspan > 1) pendingCells.add(cell);

                if (cell.block.min_size > col_widths[j]) {
                    col_widths[j] = cell.block.pref_size;
                }

                min += cell.block.min_size;

                if (cell.block.width > col_widths[j] && cell.colspan == 1) {
                    col_widths[j] = cell.block.width;
                    auto_w[j] = false;
                }
                if (cell.colspan == 1 && col_widths[j] > cell.block.min_size && (col_widths[j] - cell.block.min_size < free[j] || free[j] < 0)) {
                    free[j] = col_widths[j] - cell.block.min_size;
                }
                if (cell.colspan > 1) {
                    int s = 0, f = 0;
                    for (int k = j; k < j + cell.colspan; k++) {
                        s += col_widths[k];
                        f += Math.max(free[k], 0);
                    }
                    if (s > cell.block.min_size && s - cell.block.min_size < f) {
                        int d = Math.round((s - cell.block.min_size - f) / cell.colspan);
                        for (int k = j; k < j + cell.colspan; k++) {
                            if (free[k] < 0) free[k] = 0;
                            if (k < j + cell.colspan - 1) free[k] += d;
                            else free[k] += (s - cell.block.min_size - f) - d * (cell.colspan - 1);
                        }
                    } else if (cell.block.min_size > s) {
                        if (cell.block.pref_size > s) {
                            if (cell.block.min_size - s > f) {
                                int d = Math.round((cell.block.min_size - s - f) / cell.colspan);
                                for (int k = j; k < j + cell.colspan; k++) {
                                    if (free[k] < 0) free[k] = 0;
                                    if (k < j + cell.colspan - 1) free[k] += d;
                                    else free[k] += (cell.block.min_size - s - f) - d * (cell.colspan - 1);
                                }
                            }
                            int d = Math.round((cell.block.pref_size - s) / cell.colspan);
                            for (int k = j; k < j + cell.colspan; k++) {
                                if (k < j + cell.colspan - 1) col_widths[k] += d;
                                else col_widths[k] += (cell.block.pref_size - s) - d * (cell.colspan - 1);
                            }
                        }
                    }
                }
                j += cell.colspan;
                if (cell == rows.get(i).cells.lastElement()) break;
            }
            min_w = Math.max(min, min_w);
        }

        int sum_w = 0;
        for (int i = 0; i < col_widths.length; i++) {
            sum_w += col_widths[i];
        }
        if (sum_w + this.cellspacing * (cols_n+1) + block.borderWidth[3] + block.borderWidth[1] > block.width && !block.auto_width) {
            if (min_w + this.cellspacing * (cols_n+1) + block.borderWidth[3] + block.borderWidth[1] < block.width) {
                while (true) {
                    boolean has_free = false;
                    for (int i = 0; i < cols_n; i++) {
                        if (free[i] > 0) {
                            has_free = true;
                            col_widths[i]--;
                            free[i]--;
                            sum_w--;
                        }
                    }
                    if (!has_free) break;
                }
            }
        } else if (sum_w + this.cellspacing * (cols_n+1) + block.borderWidth[3] + block.borderWidth[1] < block.width) {
            double[] weights = new double[cols_n];
            int s = 0;
            double r = 1.0;
            int n = cols_n;
            int w = block.width - this.cellspacing * (cols_n+1) - block.borderWidth[3] - block.borderWidth[1];
            for (int i = 0; i < cols_n; i++) {
                if (!auto_w[i]) {
                    weights[i] = (double)col_widths[i] / w;
                    r -= weights[i];
                    n--;
                }
            }
            for (int i = 0; i < cols_n; i++) {
                if (auto_w[i]) {
                    weights[i] = r / n;
                }
            }
            for (int i = 0; i < cols_n; i++) {
                //weights[i] = (double)col_widths[i] / sum_w;
                //weights[i] = 1f / cols_n;
                col_widths[i] = i < cols_n-1 ? (int)Math.round(w * weights[i]) : w - s;
                s += col_widths[i];
            }
            sum_w = s;
        }
        pendingCells.clear();
        block.width = sum_w + this.cellspacing * (cols_n+1) + block.borderWidth[3] + block.borderWidth[1];
        block.viewport_width = block.width;
        
        System.out.println();
        System.out.print("Column widths: ");
        for (int i = 0; i < cols_n; i++) {
            System.out.print(col_widths[i] + " ");
        }
        System.out.println();
    }

    private void calculateHeights() {
        int sum_h = 0;
        for (int i = 0; i < rows_n; i++) {
            int j = 0;
            int max = 0;
            Row row = rows.get(i);
            while (j < cols_n) {
                Cell pc;
                while ((pc = getPendingCellAt(j, i)) != null && pc.cy + pc.rowspan - 1 == i) {

                    int h = pc.block.height / pc.rowspan;
                    for (int k = i; k < pc.cy + pc.rowspan; k++) {
                        int r = pc.block.height - h * pc.rowspan;
                        if (k < pc.cy + pc.rowspan - 1 - r) row_heights[k] = Math.max(row_heights[k], h);
                        else row_heights[k] = Math.max(row_heights[k], h+1);
                        //else row_heights[k] = Math.max(row_heights[k], pc.block.height - h * (pc.rowspan - 1));
                    }

                    j += pc.colspan;
                    if (pc.cy + pc.rowspan - 1 == i) {
                        pendingCells.remove(pc);
                        if (row_heights[i] > max) max = row_heights[i];
                    }
                }
                if (j >= cols_n) break;

                Cell cell = rows.get(i).cells.get(j);
                if (cell.rowspan > 1) pendingCells.add(cell);

                if (cell.block.height / cell.rowspan > max) {
                    max = cell.block.height / cell.rowspan;
                }

                j += cell.colspan;

                if (cell == rows.get(i).cells.lastElement()) break;
            }
            row_heights[i] = max;
            System.out.println("Row " + (i+1) + " height: " + row_heights[i]);

            row.y = (!border_collapse ? block.borderWidth[0] : 0) + this.cellspacing * (i + 1) + sum_h;
            row.height = row_heights[i];

            row.block._x_ = block._x_ + block.borderWidth[3] + cellspacing;
            row.block._y_ = block._y_ + row.y;
            row.block.width = row.block.viewport_width = block.width - block.borderWidth[3] - block.borderWidth[1] - cellspacing * 2;
            row.block.height = row.block.viewport_height = row.height;
            
            sum_h += max;
        }
        pendingCells.clear();
        block.height = sum_h + this.cellspacing * (rows_n+1) + (!border_collapse ? block.borderWidth[0] + block.borderWidth[2] : 0);
        block.viewport_height = block.height;
    }

    private void applyCellSizes() {
        int x = !border_collapse ? block.borderWidth[3] + cellspacing : 0;
        int y = !border_collapse ? block.borderWidth[0] + cellspacing : 0;
        for (int i = 0; i < rows_n; i++) {
            x = !border_collapse ? block.borderWidth[3] + cellspacing : 0;
            int j = 0;
            while (j < cols_n) {
                Cell pc;
                Cell cell = null;
                while ((pc = getPendingCellAt(j, i)) != null) {
                    if (i == pc.cy) {
                        cell = pc;
                    } else if (pc.cy + pc.rowspan - 1 == i) {
                        cell = pc;
                        pendingCells.remove(pc);
                    } else {
                        j += pc.colspan;
                    }
                }
                if (j >= cols_n) break;

                if (cell == null) cell = rows.get(i).cells.get(j);

                //cell.cx = j;
                //cell.cy = rows.get(i).cy;

                if (i == cell.cy) {
                    cell.x = cell.block._x_ = block._x_ + x;
                    cell.y = cell.block._y_ = block._y_ + y;
                }

                System.out.println("(" + cell.x + ", " + cell.y + ")");

                cell.width = col_widths[j];
                for (int k = j + 1; k < j + cell.colspan; k++) {
                    cell.width += cellspacing + col_widths[k];
                }
                cell.block.width = cell.block.viewport_width = cell.width;

                
                if (i == cell.cy) cell.height = row_heights[i];
                
                for (int k = i + 1; k < cell.cy + cell.rowspan; k++) {
                    cell.height += cellspacing + row_heights[k];
                }
                cell.block.height = cell.block.viewport_height = cell.height;

                x += cell.width + cellspacing;

                if (cell.rowspan > 1) pendingCells.add(cell);

                j += cell.colspan;

                if (cell == rows.get(i).cells.lastElement()) break;
            }

            y += (!border_collapse ? cellspacing : 0) + rows.get(i).height;
        }
        pendingCells.clear();
    }

    private void doLayout() {
        calculateWidths();
        calculateHeights();
        applyCellSizes();
    }

    private boolean isPendingCellAt(int x, int y) {
        for (int i = 0; i < pendingCells.size(); i++) {
            Cell c = pendingCells.get(i);
            if (x >= c.x && x < c.x + c.colspan && y >= c.y && y < c.y + c.rowspan) {
                return true;
            }
        }
        return false;
    }

    private Cell getPendingCellAt(int x, int y) {
        for (int i = 0; i < pendingCells.size(); i++) {
            Cell c = pendingCells.get(i);
            if (x >= c.cx && x < c.cx + c.colspan && y >= c.cy && y < c.cy + c.rowspan) {
                return c;
            }
        }
        return null;
    }

    Vector<Cell> pendingCells = new Vector<Cell>();

    Vector<Row> rows = new Vector<Row>();
    
    public Vector<Vector<Block>> table_data;

    public int cellpadding = 1;
    public int cellspacing = 2;

    public boolean border_collapse = false;

    public void init(int w, int h) {
        
    }
    
}
