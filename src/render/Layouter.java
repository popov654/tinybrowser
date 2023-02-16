package render;

import java.awt.Font;
import java.awt.FontMetrics;
import java.util.Vector;
import javax.swing.JLabel;

/**
 *
 * @author Alex
 */
public class Layouter {

    public Layouter(WebDocument document) {
        this.owner = document;
    }

    public void setCurrentBlock(Block d) {
        block = d;
        cur_x = d.borderWidth[3] + d.paddings[3];
        cur_y = d.borderWidth[0] + d.paddings[0];
        float_left_offset = 0;
        float_right_offset = 0;
        float_left_last_el = null;
        float_right_last_el = null;
    }

    public int getX() {
        return cur_x;
    }

    public int getY() {
        return cur_y;
    }

    public void setX(int x) {
        cur_x = x;
    }

    public void setY(int y) {
        cur_y = y;
    }

    public boolean isFloatLeft() {
        return float_right_last_el != null;
    }

    public int getFloatLeftOffset() {
        return float_left_offset;
    }

    public boolean isFloatRight() {
        return float_right_last_el != null;
    }

    public int getFloatRightOffset() {
        return float_right_offset;
    }

    public void clearLeft() {
        float_left_offset = 0;
        float_left_until = 0;
        float_left_last_el = null;
    }

    public void clearRight() {
        float_right_offset = 0;
        float_right_until = 0;
        float_right_last_el = null;
    }

    public void clearBoth() {
        float_left_offset = 0;
        float_right_offset = 0;
        float_left_until = 0;
        float_right_until = 0;
        float_left_last_el = null;
        float_right_last_el = null;
    }

    public void printDetails() {
        Block b = block;
        while (b.parent != null && b.display_type == Block.Display.INLINE) {
            b = b.parent;
        }
        System.out.println();
        printLines(b, 0);
        System.out.println();
    }

    public void printLines(Block b, int level) {
        if (level > 0) {
            for (int j = 0; j < level; j++) System.out.print("  ");
            System.out.println((b._getX() - b.line.getX()) + "x" + (b._getY() - b.line.getY()));
        }
        for (int i = 0; i < b.lines.size(); i++) {
            Line l = b.lines.get(i);
            for (int j = 0; j < level; j++) System.out.print("  ");
            System.out.println("Line " + (i + 1) + ": " + l.width + "x" + l.height);
            for (int k = 0; k < l.elements.size(); k++) {
                if (!(l.elements.get(k) instanceof Block)) continue;
                printLines((Block)l.elements.get(k), level+1);
            }
        }
    }

    public Line startNewLine(int offset, int clear, Block d) {

        Block b = block;
        while (b.parent != null && b.display_type == Block.Display.INLINE) {
            b = b.parent;
        }

        if (b != block && last_line != null) {
            splitInlines();
        }

        if (last_line != null && last_line.elements.size() == 0) {
            int style = (block.text_bold || block.text_italic) ? ((block.text_bold ? Font.BOLD : 0) | (block.text_italic ? Font.ITALIC : 0)) : Font.PLAIN;
            Font font = new Font(block.fontFamily, style, block.fontSize);
            last_line.setHeight(block.getFontMetrics(font).getHeight());
        }

        Line new_line = new Line(block);
        cur_x = block.borderWidth[3] + block.paddings[3];
        cur_y = last_line != null && last_line.parent == new_line.parent ? last_line.getY() + last_line.getHeight() + offset :
                                    block.borderWidth[0] + block.paddings[0] + offset;
        if (float_left_last_el != null && cur_y < float_left_last_el.getOffsetTop() +
                float_left_last_el.height + float_left_last_el.margins[2]) {
            if ((clear & Block.ClearType.LEFT) > 0 || float_left_last_el.getOffsetLeft() +
                   float_left_last_el.width + float_left_last_el.margins[1] + d.margins[3] + d.width  + d.margins[1] > block.width - block.paddings[3] - block.paddings[1]) {
                int m = Math.max(float_left_last_el.margins[2], d.margins[0]);
                cur_y += float_left_last_el.height + m;
            } else {
                cur_x += float_left_offset;
            }
        }
        new_line.setX(cur_x);
        new_line.setY(cur_y);
        int w = block.viewport_width - block.borderWidth[1] - block.paddings[1] - block.borderWidth[3] - block.paddings[3];
        if (float_right_last_el != null && cur_y < float_right_last_el.getOffsetTop() +
                float_right_last_el.height + float_right_last_el.margins[2]) {
            if ((clear & Block.ClearType.RIGHT) > 0) {
                cur_y = float_right_last_el.height + float_right_last_el.margins[2];
            } else {
                w -= float_right_offset;
            }
        }
        new_line.setWidth(w);
        return new_line;
    }

    public void splitInlines() {

        int index = stack.size()-2;
        if (last_word > -1) index++;

        Vector<Block> s = new Vector<Block>();
        Block d = block.original != null ? block.original : block;
        
        while (d != null && d.display_type == Block.Display.INLINE) {
            Block b0 = d.parts.lastElement();
            if (b0.lines.size() > 0) {
                b0.lines.get(0).setWidth(b0.lines.get(0).cur_pos);
                b0.width = b0.borderWidth[3] + b0.paddings[3] + b0.lines.get(0).getWidth();
                b0.viewport_width = b0.width;
                b0.orig_width = (int)Math.floor(b0.width / b0.ratio);
            }
            Block tb = null;
            if (last_word > -1) {
                Block text_block = b0.original.children.get(last_element);
                String[] w = text_block.textContent.split("((?<=\\s+)|(?=\\s+))");
                String w1 = new String();
                for (int i = 0; i < last_word; i++) {
                    w1 += w[i];
                }
                String w2 = new String();
                for (int i = last_word; i < w.length; i++) {
                    w2 += w[i];
                }
                b0.children.get(last_element).textContent = w1;

                tb = text_block.clone();
                tb.textContent = w2;
            }

            Line last = b0.lines.lastElement();
            while (!last.elements.isEmpty() && last.elements.lastElement() instanceof Character &&
                    ((Character)last.elements.lastElement()).getText().matches("\\s+")) {
                last.cur_pos -= ((Character)last.elements.lastElement()).getWidth();
                last.width = last.cur_pos;
                last.elements.remove(last.elements.size()-1);
            }
            b0.width = b0.borderWidth[3] + b0.paddings[3] + last.width;
            b0.viewport_width = b0.width;
            b0.orig_width = (int)Math.floor(b0.width / b0.ratio);
            b0.parent.lines.lastElement().cur_pos = b0._getX() - b0.parent.lines.lastElement().getX() + b0.width;
            b0.parent.lines.lastElement().width = b0.parent.lines.lastElement().cur_pos;

            Block b = b0.original.clone();
            b.children.clear();
                
            for (int i = last_element; i < d.children.size(); i++) {
                b.children.add(d.children.get(i).clone());
            }

            if (b.children.get(0).type == Block.NodeTypes.TEXT) {
                b.children.get(0).textContent = tb.textContent;
            }

            b0.cut = b0.cut | Block.Cut.RIGHT;
            b.cut = Block.Cut.LEFT;

            //b0.width -= b0.borderWidth[1] + b0.paddings[1];
            b0.borderWidth[1] = 0;
            b0.margins[1] = 0;
            b0.paddings[1] = 0;
            b.borderWidth[3] = 0;
            b.paddings[3] = 0;
            b.margins[3] = 0;

            d.parts.add(b);

            for (int i = 0; i < b0.children.size(); i++) {
                if (b0.children.get(i).pos > last_element ||
                        (b0.children.get(i).pos == last_element && last_word < 1)) {
                    b0.children.remove(i);
                    i--;
                }
            }

            if (b0.children.size() == 0) {
                b0.parent.getLayouter().last_line.elements.remove(b0);
            }

            if (index < stack.size() - 1) {
                stack.get(index+1).getLayouter().setCurrentBlock(b);
            } else {
                setCurrentBlock(b);
            }
            index--;
            s.add(d);
            d = d.parent;
        }

        if (index < 0) index = 0;

        if (d == null) d = s.lastElement();

        int to = last_word > -1 ? stack.size()-1 : stack.size()-2;
        index = Math.min(index, to);

        for (int i = s.size()-1; i >= 0; i--) {
            d = s.get(i);
            Block b = (index < stack.size()-1 ? stack.get(index+1) : block.original).parts.lastElement();
            Block p = b.parent;
            p.getLayouter().last_line = p.getLayouter().startNewLine(0, 0, b);
            b.width = p.getLayouter().last_line.getWidth();
            b.viewport_width = b.width;
            b.orig_width = (int)Math.floor(b.width / b.ratio);
            p.getLayouter().last_line.addElement(b);
            index++;
        }
    }

    public int last_element = -1;
    public int last_word = -1;


    private boolean omitWhitespace() {
        if (last_line != block.lines.get(0) || block.display_type != Block.Display.INLINE) return true;
        Block b = block;
        while (b.parent != null) {
            //First child cannot be a character since we don't have blocks with mixed content
            if (b.parent.lines.get(0).elements.size() == 0) return true;
            if (b.parent.lines.get(0).elements.get(0) instanceof Character) {
                Drawable end = b.parent.lines.get(0).elements.lastElement();
                if (end == b) {
                    Line line = b.parent.lines.get(0);
                    if (line.elements.size() == 1) return true;
                    end = line.elements.get(line.elements.size()-2);
                }
                if (end instanceof Block) {
                    return endsWithWhitespace((Block)end);
                } else {
                    return ((Character)end).textContent.matches("\\s+");
                }
            }
            Block d = (Block)b.parent.lines.get(0).elements.get(0);
            if (b == d) return true;

            Block prev = null;
            int i = 0; int j = 0;
            while (i < b.parent.lines.size()) {
                if ((Block)b.parent.lines.get(i).elements.get(j) == block && prev != null) {
                    if (prev.display_type != Block.Display.INLINE) return true;
                    return endsWithWhitespace(prev.parts.size() > 0 ? prev.parts.lastElement() : prev);
                }
                if (j+1 < b.parent.lines.get(i).elements.size()) {
                    prev = (Block)b.parent.lines.get(i).elements.get(j);
                    j++;
                } else {
                    i++;
                    j = 0;
                }
            }

            b = b.parent;
        }
        return false;
    }

    private boolean endsWithWhitespace(Block b) {
        if (!(b.children.size() == 1 && b.children.get(0).type == Block.NodeTypes.TEXT)) {
            if (b.lines.size() == 0) return false;
            Block b0 = (Block)b.lines.lastElement().elements.lastElement();
            if (b0.display_type != Block.Display.INLINE) return false;
            return endsWithWhitespace(b0.parts.size() > 0 ? b0.parts.lastElement() : b0);
        }
        if (b.lines.size() > 0 && b.lines.lastElement().elements.size() > 0 &&
               ((Character)b.lines.lastElement().elements.lastElement()).getText().matches("\\s+")) {
            return true;
        }
        return false;
    }

    public void addWord(String str, Font font) {
        if (last_line == null) {
            last_line = startNewLine(0, 0, null);
        }
        if (str.contains("\n") && block.white_space == Block.WhiteSpace.PRE_WRAP) {
            last_line = startNewLine(0, 0, null);
            return;
        }
        if (str.matches("\\s+") && block.white_space != Block.WhiteSpace.PRE_WRAP) str = " ";
        JLabel label = new JLabel(str);
        label.setFont(font);
        int width = label.getFontMetrics(font).stringWidth(str);
        char[] ch = {};
        if (((last_line.elements.size() == 0 && omitWhitespace() ||
               (last_line.elements.size() > 0 && last_line.elements.lastElement() instanceof Character &&
                ((Character)last_line.elements.lastElement()).getText().matches("\\s+"))) && str.equals(" ")) &&
                block.white_space != Block.WhiteSpace.PRE_WRAP) {
            last_word = -1;
            return;
        }
        int parent_width = block.width - block.borderWidth[3] - block.paddings[3] - block.borderWidth[1] - block.paddings[1];
        if (last_line.cur_pos + width > last_line.getWidth() && !(last_line.cur_pos == 0 && width > parent_width)
                && block.white_space == Block.WhiteSpace.NORMAL) {
            //Remove trailing spaces
//            if (!str.matches("\\s+") && last_line.elements.lastElement() instanceof Character &&
//                    ((Character)last_line.elements.lastElement()).getText().matches("\\s+")) {
//                last_line.cur_pos -= ((Character)last_line.elements.lastElement()).width;
//                last_line.elements.remove(last_line.elements.lastElement());
//            }
            if (!str.equals(" ")) {
                Line new_line = startNewLine(0, 0, null);
                cur_x = new_line.getX();
                cur_y = new_line.getY();
                last_line = new_line;
            }
        }
        int last_pos = last_line.cur_pos;
        ch = str.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            Character c = new Character(last_line, ch[i]);
            c.setColor(block.color);
            c.setFont(block.getFont());
            if (str.matches("\\s+") && block.word_spacing > 0) {
                c.setWidth(c.width + block.word_spacing);
            }
            //block.height = label.getFontMetrics(font).getHeight();
            if (last_line.cur_pos + c.getWidth() > last_line.getWidth() && (block.white_space == Block.WhiteSpace.WORD_BREAK ||
                    block.white_space == Block.WhiteSpace.PRE_WRAP && str.matches("\\s+"))) {
                last_line = startNewLine(0, 0, null);
                c.line = last_line;
            }
            last_line.addElement(c);
            //block.height = last_line.getY() + last_line.getHeight() + block.paddings[2] + block.borderWidth[2];
        }
        block.pref_size += last_line.cur_pos - last_pos;
        if (block.min_size < last_line.cur_pos - last_pos) {
            block.min_size = last_line.cur_pos - last_pos;
        }
        last_word = -1;
    }

    public void addBlock(Block d) {
        if (d.display_type == Block.Display.NONE) return;
        int offset = 0;
        stack.add(d);
        if (d.float_type != Block.FloatType.NONE) {
            positionFloat(d);
            d.performLayout();
            stack.remove(stack.lastIndexOf(d));
            return;
        }
        if (d.display_type == Block.Display.TABLE) {
            layoutTable(d);
        }
        if (d.display_type == Block.Display.TABLE_ROW || d.parent.display_type == Block.Display.TABLE_CELL) {
            return;
        }
        if (d.display_type == Block.Display.BLOCK || d.display_type == Block.Display.TABLE) {
            if (d.auto_y_margin) {
                d.margins[0] = 0;
                d.margins[2] = 0;
            }

            offset = d.margins[0];
            
            if (last_line != null && last_line.elements.size() == 1 && last_line.elements.get(0) instanceof Block &&
                    ((Block)last_line.elements.get(0)).display_type == Block.Display.BLOCK) {
                int m1 = ((Block)last_line.elements.get(0)).margins[2];
                int m2 = d.margins[0];
                offset = m1 * m2 >= 0 ? (m1 >= 0 && m2 >= 0 ? Math.max(m1, m2) : Math.min(m1, m2)) : m1 + m2;
            }

            last_line = startNewLine(offset, d.clear_type, d);

            Line new_line = last_line;
            cur_x = new_line.getX();
            cur_y = new_line.getY();
            d.no_draw = true;
            
            if (d.auto_x_margin) {
                d.margins[3] = Math.max(0, Math.round((new_line.getWidth() - d.viewport_width) / 2));
                d.margins[1] = Math.max(0, new_line.getWidth() - d.viewport_width - d.margins[3]);
            }

            new_line.addElement(d);

            if (d.display_type != Block.Display.TABLE) {
                if (d.auto_width && !d.isImage && !d.no_layout && !(d.parent != null && d.parent.parent != null && (d.parent.parent.display_type == Block.Display.TABLE || d.parent.parent.display_type == Block.Display.INLINE_TABLE))) {
                    d.setWidth(-1, true);
                    d.performLayout();
                } else {
                    d.performLayout();
                    if (block.pref_size < d.width + d.margins[3] + d.margins[1]) {
                        block.pref_size = d.width + d.margins[3] + d.margins[1];
                    }
                    if (block.min_size < d.width + d.margins[3] + d.margins[1]) {
                        block.min_size = d.width + d.margins[3] + d.margins[1];
                    }
                }
            }

            if (d.positioning == Block.Position.RELATIVE) {
                d.setX(d._getX() + d.left);
                d.setY(d._getY() + d.top);
            }

            if (checkAbsolutePositioning(d)) return;

            d.no_draw = false;

            cur_x = new_line.cur_pos;

            last_block = d;

            int vh = d.viewport_height + (d.scrollbar_x != null ? d.scrollbar_x.getPreferredSize().height : 0);
            last_line.setHeight(vh);
            last_margin_top = 0;
            stack.remove(stack.lastIndexOf(d));
            return;
        } else if (d.display_type == Block.Display.INLINE_BLOCK || d.display_type == Block.Display.INLINE_TABLE) {
            if (last_line == null || last_line.elements.size() == 1 && last_line.elements.get(0) instanceof Block &&
                    ((Block)last_line.elements.get(0)).display_type == Block.Display.BLOCK) {
                if (last_line != null && last_line.elements.size() == 1) {
                    offset = ((Block)last_line.elements.get(0)).margins[2];
                }
                last_line = startNewLine(offset, d.clear_type, d);
            }
            cur_x = last_line.cur_pos;
            cur_y = last_line.getY();
            d.no_draw = true;
            d.line = null;

            int x = getFullLinePos(d);
            int w = getFullLineSize(d);

            if (d.display_type != Block.Display.INLINE_TABLE) {
                d.setWidth(d.width > 0 && !d.auto_width ? d.orig_width : -1, true);
                d.performLayout();
                if (d.auto_width && d.lines.size() == 1 && d.lines.get(0).cur_pos < d.lines.get(0).getWidth()) {
                    int line_width = d.lines.get(0).cur_pos;
                    d.lines.get(0).setWidth(line_width);
                    d.width = d.borderWidth[3] + d.paddings[3] + line_width + d.paddings[1] + d.borderWidth[1];
                    d.orig_width = (int)Math.round(d.width / d.ratio);
                    d.viewport_width = d.width;
                }
            }

            if (x + d.margins[1] + d.margins[3] + d.width > w &&
                    block.white_space != Block.WhiteSpace.NO_WRAP) {
                last_line = startNewLine(0, 0, d);
                cur_x = last_line.getX();
                cur_y = last_line.getY();
            }
            
            if (block.pref_size < last_line.cur_pos + d.width + d.margins[3] + d.margins[1]) {
                block.pref_size = last_line.cur_pos + d.width + d.margins[3] + d.margins[1];
            }
            if (block.min_size < d.width + d.margins[3] + d.margins[1]) {
                block.min_size = d.width + d.margins[3] + d.margins[1];
            }
            
            last_line.addElement(d);

            d.performLayout();
            
            if (d.positioning == Block.Position.RELATIVE) {
                d.setX(d._getX() + d.left);
                d.setY(d._getY() + d.top);
            }

            if (checkAbsolutePositioning(d)) return;

            if (last_block != null && last_block.line == last_line) {
                applyVerticalAlignment(d);
            }
            // set line height to the new maximum height
            int vh = d.viewport_height + (d.scrollbar_x != null ? d.scrollbar_x.getPreferredSize().height : 0);
            int h = vh + d.margins[0] + d.margins[2];
            if (h > last_line.getHeight()) {
                last_line.setHeight(h);
            }
            d.no_draw = false;
            last_block = d;
            stack.remove(stack.lastIndexOf(d));
            return;
        } else if (d.display_type == Block.Display.INLINE) {

            int index = 0;
            if (d.document != null) {
                if (d.parts.size() > 0 && d.parts.get(0).getParent() != null) {
                    Block p = d.parts.get(0);
                    index = p.getParent().getComponentZOrder(p) - d.parts.size() + 1;
                } else {
                    index = d.pos;
                }
                for (Block part: d.parts) {
                    if (part.bgImage != null && part.has_animation) {
                        part.stopWatcher();
                    }
                    d.document.root.remove(part);
                }
                if (d.parts.size() > 0) d.document.root.add(d, index);
            }
            d.parts.clear();

            Block b = d.clone();
            d.stopWatcher();
            d.width = -1;
            d.height = -1;
            d.viewport_width = 0;
            d.parts.add(b);
            b.pos = d.pos;

            if (b.node != null && b.node.tagName.equals("br")) {
                b.width = b.viewport_width = 0;
                b.height = b.viewport_height = 0;
                b.auto_width = false;
                b.auto_height = false;
                last_line.addElement(b);
                for (int i = 0; i < d.parts.size(); i++) {
                    d.document.root.add(d.parts.get(i), index++);
                }
                d.document.root.remove(d);
                last_line = startNewLine(0, 0, d.parent);
                return;
            }

            if (last_line == null || last_line.elements.size() == 1 && last_line.elements.get(0) instanceof Block &&
                    ((Block)last_line.elements.get(0)).display_type == Block.Display.BLOCK) {
                last_line = startNewLine(0, 0, b);
            }
            cur_x = last_line.cur_pos;
            cur_y = last_line.getY();

            if (!b.isImage) {
                b.width = b.viewport_width = last_line.getWidth() - last_line.cur_pos - b.margins[3];
                b.orig_width = (int)Math.floor(b.width / b.ratio);
            } else {
                b.performLayout();
            }

            if (b.width <= 0 && b.isImage && last_line.getWidth() - last_line.cur_pos - b.margins[3] < b.width) {
                Line new_line = startNewLine(0, 0, b);
                cur_x = new_line.getX();
                cur_y = new_line.getY();
                last_line = new_line;
                b.width = last_line.getWidth() - b.margins[3];
                b.viewport_width = b.width;
                b.orig_width = (int)Math.floor(b.width / b.ratio);
            }
            b.no_draw = true;

            b.viewport_width = b.width;
            b.viewport_height = b.height;

            last_line.addElement(b);

            last_line.cur_pos -= b.margins[1];

            if (!b.isImage) b.performLayout();

            if (b.lines.size() > 0 && b.lines.get(0).cur_pos < b.lines.get(0).getWidth()) {
                int line_width = b.lines.get(0).cur_pos;
                b.lines.get(0).setWidth(line_width);
                b.width = b.borderWidth[3] + b.paddings[3] + line_width + b.paddings[1] + b.borderWidth[1];
                b.viewport_width = b.width;
                b.orig_width = (int)Math.round(b.width / b.ratio);
                b.content_x_max = b.width;
                last_line.cur_pos = b._getX() + b.width - last_line.getX() + b.margins[1];
            }

            if (b.parent.content_x_max < last_line.cur_pos) {
                b.parent.content_x_max = last_line.cur_pos;
            }

            if (b.lines.size() == 0 && !b.isImage) {
                b.width = b.borderWidth[3] + b.paddings[3] + b.paddings[1] + b.borderWidth[1];
                b.viewport_width = b.width;
                b.orig_width = (int)Math.floor(b.width / b.ratio);
                int st = (b.text_bold || b.text_italic) ? ((b.text_bold ? Font.BOLD : 0) | (b.text_italic ? Font.ITALIC : 0)) : Font.PLAIN;
                Font f = new Font(b.fontFamily, st, b.fontSize);
                int fs = b.getFontMetrics(f).getHeight();
                b.height = b.borderWidth[0] + b.paddings[0] + fs + b.paddings[2] + b.borderWidth[2];
                b.viewport_height = b.height;
                b.orig_height = (int)Math.floor(b.height / b.ratio);
            }

            if (block.pref_size < last_line.cur_pos) {
                block.pref_size = last_line.cur_pos;
            }
            if (block.min_size < last_line.cur_pos) {
                block.min_size = last_line.cur_pos;
            }

            if (d.getParent() != null) {
                //index = d.getParent().getComponentCount() - d.pos - 1;
                for (int i = 0; i < d.parts.size(); i++) {
                    d.document.root.add(d.parts.get(i), index++);
                }
                d.document.root.remove(d);
            }

            if (b.bgImage != null && b.has_animation) {
                b.startWatcher();
            }

            if (b.positioning == Block.Position.RELATIVE) {
                b.setX(b._getX() + b.left);
                b.setY(b._getY() + b.top);
            }

            // a quick hack to enable vertical alignment with symbols
            if (last_block == null && last_line != null && last_line.elements.size() > 1) {
                last_block = new Block(block.document);
                last_block.width = ((Character)last_line.elements.get(last_line.elements.size()-2)).width;
                last_block.height = ((Character)last_line.elements.get(last_line.elements.size()-2)).height;
                last_block.fontFamily = block.fontFamily;
                last_block.fontSize = block.fontSize;
            }

            last_block = null;
            Line line = null;
            for (int i = 0; i < d.parts.size(); i++) {
                Block part = d.parts.get(i);
                if (part.line != line) last_block = null;
                line = part.line;
                applyVerticalAlignment(part);
                // set line height to the new maximum height
                int h = b.height + b.margins[0] + b.margins[2];
                if (h > line.getHeight()) {
                    line.setHeight(h);
                }
                last_block = part;
            }
            
            b.no_draw = false;
            last_block = b;

            stack.remove(stack.lastIndexOf(d));

            if (b.node != null) {
               b.node.fireEvent("layout", "render");
            }

            if (b.document.debug) {
                System.out.println();
                System.out.println("Layout ended for block " + b.toString());
            }

            return;
        }
    }

    private boolean checkAbsolutePositioning(Block d) {
        Block p = block;
        while (p != null && p.positioning != Block.Position.RELATIVE && p.positioning != Block.Position.ABSOLUTE) {
            p = p.parent;
        }
        if (p == null) p = block.document.root;
        if (d.positioning == Block.Position.ABSOLUTE) {
            if (d.auto_left) {
                d.setX(last_line.getX());
            } else {
                if (!d.auto_left && !d.auto_right && !d.auto_width && d.auto_x_margin &&
                      p.width - p.borderWidth[3] - p.borderWidth[1] - d.left - d.right - d.width > 0) {
                    int w = p.width - p.borderWidth[3] - p.borderWidth[1] - d.left - d.right - d.width;
                    d.margins[3] = Math.round(w / 2);
                    d.margins[1] = w - d.margins[3];
                }
                d.setX(p.borderWidth[3] + d.margins[3] + d.left);
            }
            if (d.auto_top) {
                d.setY(last_line.getY());
            } else {
                if (!d.auto_top && !d.auto_bottom && !d.auto_height && d.auto_y_margin &&
                      p.height - p.borderWidth[0] - p.borderWidth[2] - d.top - d.bottom - d.height > 0) {
                    int h = p.height - p.borderWidth[0] - p.borderWidth[2] - d.top - d.bottom - d.height;
                    d.margins[0] = Math.round(h / 2);
                    d.margins[2] = h - d.margins[0];
                }
                d.setY(p.borderWidth[0] + d.margins[0] + d.top);
            }
            if (last_line.elements.size() == 1 && last_line.elements.lastElement() instanceof Block &&
                    (Block)last_line.elements.lastElement() == d) {
                block.lines.remove(block.lines.size()-1);
            } else {
                last_line.elements.remove(d);
            }
            last_line = block.lines.size() > 0 ? block.lines.lastElement() : null;
            if (last_line != null) {
                cur_x = last_line.getX() + last_line.cur_pos;
                cur_y = last_line.getY();
            }
            return true;
        }
        return false;
    }

    public void applyVerticalAlignment(Block d) {
        int offset = 0;
        Line line = d.line;
        if (d.vertical_align == Block.VerticalAlign.ALIGN_MIDDLE) {
            offset = Math.round((line.height - d.height) / 2);
        }
        else if (d.vertical_align == Block.VerticalAlign.ALIGN_BOTTOM) {
            offset = line.height - d.height;
        }
        else if (d.vertical_align == Block.VerticalAlign.ALIGN_BASELINE && last_block != null) {
            int st1 = (last_block.text_bold || last_block.text_italic) ? ((last_block.text_bold ? Font.BOLD : 0) | (last_block.text_italic ? Font.ITALIC : 0)) : Font.PLAIN;
            int st2 = (d.text_bold || d.text_italic) ? ((d.text_bold ? Font.BOLD : 0) | (d.text_italic ? Font.ITALIC : 0)) : Font.PLAIN;
            Font f1 = new Font(last_block.fontFamily, st1, last_block.fontSize);
            Font f2 = new Font(d.fontFamily, st2, d.fontSize);
            FontMetrics m1 = last_block.getFontMetrics(f1);
            FontMetrics m2 = last_block.getFontMetrics(f2);
            offset = last_block.getOffsetTop() - line.getY() + last_block.height - last_block.borderWidth[2] - last_block.paddings[2] -
                     m1.getDescent() + m2.getDescent() - (d.height - d.borderWidth[2] - d.paddings[2]);
        }

        d.setY(line.getY() + offset);

        // update the line height
        if (offset + d.height > line.getHeight()) {
            line.setHeight(offset + d.height);
        }
        if (offset < d.margins[0] && d.display_type != Block.Display.INLINE) offset -= d.margins[0];
        if (offset < 0) {
            line.setHeight(line.getHeight() - offset);
            for (int i = 0; i < line.elements.size(); i++) {
                line.elements.get(i).setY(line.elements.get(i)._getY() - offset);
            }
        }
    }

    public static void applyHorizontalAlignment(Block block) {
        int style = (block.text_bold || block.text_italic) ? ((block.text_bold ? Font.BOLD : 0) | (block.text_italic ? Font.ITALIC : 0)) : Font.PLAIN;
        Font f = new Font(block.fontFamily, style, block.fontSize);
        int sp = block.getFontMetrics(f).stringWidth(" ");
        int new_sp = sp;
        int mod = 0;

        for (int i = 0; i < block.lines.size(); i++) {
            Line line = block.lines.get(i);

            int space_count = 0;

            if (block.text_align == Block.TextAlign.ALIGN_JUSTIFY) {
                for (int j = 0; j < line.elements.size(); j++) {
                    Drawable d = line.elements.get(j);
                    if (d instanceof Character && ((Character)d).getText().equals(" ") ||
                            j < line.elements.size()-1 && line.elements.get(j+1) instanceof Block &&
                            ((Block)line.elements.get(j+1)).display_type != Block.Display.INLINE) {
                        space_count++;
                    }
                }
                new_sp = (int)Math.floor((line.width - line.cur_pos) / space_count);
                mod = line.width - line.cur_pos - new_sp * space_count;
            }

            if (line.getWidth() == line.cur_pos) continue;
            int pos = 0;
            int space_index = 0;
            if (block.text_align == Block.TextAlign.ALIGN_CENTER) {
                pos += (int)Math.round(line.width - line.cur_pos) / 2;
            } else if (block.text_align == Block.TextAlign.ALIGN_RIGHT) {
                pos += (int)Math.round(line.width - line.cur_pos);
            }
            for (int j = 0; j < line.elements.size(); j++) {
                Drawable d = line.elements.get(j);
                int w = j < line.elements.size()-1 ? line.elements.get(j+1)._getX() - d._getX() : line.cur_pos - d._getX();
                if (d instanceof Character && ((Character)d).getText().equals(" ")) {
                    if (block.text_align != Block.TextAlign.ALIGN_JUSTIFY) {
                        d.setWidth(sp);
                    } else {
                        d.setWidth(space_index < mod ? new_sp + 1 : new_sp);
                        space_index++;
                    }
                }
                /* This is commented out since existing engines do not apply justification
                   to inline-block content, behaving as if text-align had been set to "left"

                if (block.text_align == Block.TextAlign.ALIGN_JUSTIFY &&
                       j > 0 && line.elements.get(j-1) instanceof Block &&
                       ((Block)line.elements.get(j-1)).display_type != Block.Display.INLINE) {
                    pos += space_index < mod ? new_sp + 1 : new_sp;
                }

                */
                d.setX(line.getX() + pos);
                pos += w;
            }
        }
    }

    public void positionFloat(Block d) {
        if (d.float_type == Block.FloatType.LEFT) {
            cur_x = float_left_offset + d.margins[3];         

            int last_block_margin = last_block != null ? last_block.margins[2] : 0;
            int last_block_y = last_block != null ? last_block.getOffsetTop() + last_block.height : 0;
            
            int offset = last_block_margin + d.margins[0];
            cur_y = float_left_last_el != null ? Math.max(float_left_last_el.getOffsetTop(), last_block_y + offset) :
                                                      last_block_y + offset;
            if ((d.clear_type & Block.ClearType.LEFT) > 0) {
                cur_x = d.margins[3];
                cur_y = float_left_last_el != null ? float_left_last_el.getOffsetTop() + float_left_last_el.height + float_left_last_el.margins[2] + d.margins[0] :
                                                      last_block_y + offset;
            }
            float_left_offset += d.margins[3] + d.width + d.margins[1];
            d.setX(cur_x);
            d.setY(cur_y);

            if (cur_x > block.width - block.borderWidth[1] - block.borderWidth[3] - block.paddings[1] - block.paddings[3] &&
                    !(d.width > block.width - block.borderWidth[1] - block.paddings[1] - block.borderWidth[3] - block.paddings[3])) {
                cur_x = d.margins[3];

                cur_y += float_left_last_el != null ? float_left_last_el.height + float_left_last_el.margins[2] :
                                                      0;
                float_left_offset = d.margins[3] + d.width + d.margins[1];
                d.setX(cur_x);
                d.setY(cur_y);
            }
            float_left_last_el = d;
            return;
        }
        if (d.float_type == Block.FloatType.RIGHT) {
            cur_x = block.width - block.borderWidth[1] - block.paddings[1] -
                    float_right_offset - d.margins[1] - d.width;
            float_right_offset += d.margins[3] + d.width + d.margins[1];
            
            if (cur_x < block.paddings[3] + d.margins[3] &&
                    !(d.width > block.width - block.borderWidth[1] - block.paddings[1] - block.borderWidth[3] - block.paddings[3])) {
                cur_x = block.width - block.borderWidth[1] - block.paddings[1] -
                        d.margins[1] - d.width;
                int offset = float_right_last_el != null ? Math.max(float_right_last_el.margins[2], d.margins[0]) :
                                                      Math.max(last_block.margins[2], d.margins[0]);
                cur_y += float_right_last_el != null ? float_right_last_el.height + offset :
                                                      last_block.height + offset;
                float_right_offset = d.margins[3] + d.width + d.margins[1];
                d.setX(cur_x);
                d.setY(cur_y);
            }
            float_right_last_el = d;
            return;
        }
    }

    public void layoutTable(Block b) {
        if (b.table == null) {
            b.table = new TableLayout(b);
        }
    }

    public int getFullLineSize(Block b) {
        b = b.parent;
        while (b.parent != null && b.display_type == Block.Display.INLINE) {
            b = b.parent;
        }
        if (b.display_type != Block.Display.INLINE && b.getLayouter() != null) {
            return b.getLayouter().last_line.getWidth();
        }
        return last_line.getWidth();
    }

    public int getFullLinePos(Block b) {
        b = b.parent;
        int pos = 0;
        while (b.parent != null && b.display_type == Block.Display.INLINE) {
            pos += b.getLayouter().last_line.cur_pos + b.getLayouter().last_line.getX();
            b = b.parent;
        }
        if (b.display_type != Block.Display.INLINE && b.getLayouter() != null) {
            return b.getLayouter().last_line.cur_pos + pos;
        }
        return last_line.cur_pos;
    }

    public Block getBlock() {
        return block;
    }

    public static Vector<Block> stack = new Vector<Block>();

    @Override
    public Layouter clone() {
        Layouter l = new Layouter(owner);
        l.cur_x = cur_x;
        l.cur_y = cur_y;
        l.float_left_offset = float_left_offset;
        l.float_right_offset = float_right_offset;
        l.float_left_last_el = float_left_last_el;
        l.float_right_last_el = float_right_last_el;
        l.last_block = last_block;
        l.block = block;
        return l;
    }

    private int cur_x = 0;
    private int cur_y = 0;

    private int last_margin_top = 0;

    private Block last_block;

    private Block block;

    private WebDocument owner;

    public Block cur_block;

    private int float_left_offset = 0;
    private int float_right_offset = 0;

    private int float_left_last_y = 0;
    private int float_right_last_y = 0;

    private Block float_left_last_el;
    private Block float_right_last_el;

    private int float_left_until = 0;
    private int float_right_until = 0;

    public Line last_line;

}
