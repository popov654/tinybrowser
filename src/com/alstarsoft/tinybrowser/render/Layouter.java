package com.alstarsoft.tinybrowser.render;

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
        float_left_last_el = null;
    }

    public void clearRight() {
        float_right_offset = 0;
        float_right_last_el = null;
    }

    public void clearBoth() {
        float_left_offset = 0;
        float_right_offset = 0;
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

        if (d != null && (d.display_type == Block.Display.BLOCK || d.display_type == Block.Display.FLEX || d.display_type == Block.Display.TABLE)) {
            if (block.lines.size() > 0 && block.lines.lastElement().elements.size() == 1 && block.lines.lastElement().elements.get(0) instanceof Block) {
                Block last = ((Block)block.lines.lastElement().elements.get(0));
                if (last.display_type == Block.Display.INLINE && !last.isImage && last.auto_width && last.node != null && last.node.nodeType == 3 && last.node.nodeValue.matches("\\s*")) {
                    cur_y -= block.lines.lastElement().getHeight();
                    block.lines.removeElement(block.lines.lastElement());
                    last_line = !block.lines.isEmpty() ? block.lines.lastElement() : null;
                }
            }
        }

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

        boolean is_flex = block.display_type == Block.Display.FLEX || block.display_type == Block.Display.INLINE_FLEX;
        boolean x_axis = block.flex_direction == Block.Direction.ROW || block.flex_direction == Block.Direction.ROW_REVERSED;

        Line new_line = new Line(block);
        if (x_axis) {
            cur_x = block.borderWidth[3] + block.paddings[3];
            cur_y = last_line != null && last_line.parent == new_line.parent ? last_line.getY() + last_line.getHeight() + offset : block.borderWidth[0] + block.paddings[0] + offset;
        } else {
            cur_x = last_line != null && last_line.parent == new_line.parent ? last_line.getY() + last_line.getHeight() + offset : block.borderWidth[3] + block.paddings[3] + offset;
            cur_y = block.borderWidth[0] + block.paddings[0];
        }

        int w = block.viewport_width - block.borderWidth[1] - block.paddings[1] - block.borderWidth[3] - block.paddings[3];

        if (float_left_last_el != null || float_right_last_el != null) {
            if (float_left_last_el != null && cur_y < float_left_last_el.getOffsetTop() +
                    float_left_last_el.height + float_left_last_el.margins[2]) {
                if ((clear & Block.ClearType.LEFT) > 0 || float_left_last_el.getOffsetLeft() +
                       float_left_last_el.width + float_left_last_el.margins[1] + d.margins[3] + d.width + d.margins[1] > block.width - block.paddings[3] - block.paddings[1]) {
                    int m = Math.max(float_left_last_el.margins[2], d.margins[0]);
                    cur_y += float_left_last_el.height + m;
                } else if (!block.rtl) {
                    cur_x += float_left_offset;
                    w -= float_left_offset;
                }
            }

            if (float_right_last_el != null && cur_y < float_right_last_el.getOffsetTop() +
                    float_right_last_el.height + float_right_last_el.margins[2]) {
                if ((clear & Block.ClearType.RIGHT) > 0) {
                    cur_y = float_right_last_el.height + float_right_last_el.margins[2];
                } else if (block.rtl) {
                    cur_x += float_right_offset;
                    w -= float_right_offset;
                }
            }
            new_line.setWidth(w);
        }

        new_line.setX(cur_x);
        new_line.setY(cur_y);

        if (w < 0) {
            w = b.viewport_width - b.borderWidth[1] - b.paddings[1] - b.borderWidth[3] - b.paddings[3];
        }
        if ((block.display_type == Block.Display.FLEX || block.display_type == Block.Display.INLINE_FLEX) &&
              (block.flex_direction == Block.Direction.COLUMN || block.flex_direction == Block.Direction.COLUMN_REVERSED)) {
            w = !block.auto_height ? b.viewport_height - b.borderWidth[2] - b.paddings[2] - b.borderWidth[0] - b.paddings[0] : Integer.MAX_VALUE;
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
    public boolean layout_break = false;


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
                    if (prev.display_type != Block.Display.INLINE) return false;
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
        int parent_width = Math.max(0, block.width - block.borderWidth[3] - block.paddings[3] - block.borderWidth[1] - block.paddings[1]);
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

        int offset = 0;
        if (block.text_overflow == Block.TextOverflow.ELLIPSIS) {
            offset = !block.rtl ? (int) Math.max(block.arc[2]  / 2.5, block.borderWidth[1] + block.paddings[1]) : (int) Math.max(block.arc[3]  / 2.5, block.borderWidth[3] + block.paddings[3]);
        }

        int last_pos = last_line.cur_pos;
        ch = str.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            Character c = new Character(last_line, ch[i]);
            int cw = label.getFontMetrics(font).stringWidth(".");
            if (!last_line.elements.isEmpty() && last_line.cur_pos > block.viewport_width - offset && last_line.top + label.getFontMetrics(font).getHeight() * 2 > block.viewport_height - block.borderWidth[2] - block.paddings[2] &&
                  block.text_overflow == Block.TextOverflow.ELLIPSIS) {
                while (!last_line.elements.isEmpty() && last_line.cur_pos > block.viewport_width - offset - cw * 3) {
                    Character last_char = (Character) last_line.elements.lastElement();
                    last_line.elements.remove(last_line.elements.size()-1);
                    last_line.cur_pos -= last_char.getWidth() + block.letter_spacing;
                }
                for (int j = 0; j < 3; j++) {
                    c = new Character(last_line, ".");
                    c.setColor(block.color);
                    c.setFont(block.getFont());
                    c.setWidth(cw);
                    last_line.addElement(c);
                }
                block.pref_size += last_line.cur_pos - last_pos;
                if (block.min_size < last_line.cur_pos - last_pos) {
                    block.min_size = last_line.cur_pos - last_pos;
                }
                layout_break = true;
                last_word = -1;
                return;
            }
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
        if (d.parent != null) {
            int index = d.parent.children.indexOf(d);
            Block prev_block = index > 0 ? d.parent.children.get(index-1) : null;
            Block next_block = index < d.parent.children.size()-1 ? d.parent.children.get(index+1) : null;
            if (d.display_type == Block.Display.INLINE && (next_block == null || next_block.isBlock() || prev_block == null || prev_block.isBlock()) &&
               !d.isImage && d.auto_width && d.node != null && d.node.nodeType == 3 && d.node.nodeValue.matches("\\s*")) {
                  return;
            }
        }
        if (d.display_type == Block.Display.NONE) return;
        int offset = (last_line == null ? block.borderWidth[0] + block.paddings[0] : 0) + d.margins[0];
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
        if (d.display_type == Block.Display.TABLE_ROW || d.display_type == Block.Display.TABLE_CELL) {
            return;
        }
        if (d.display_type == Block.Display.BLOCK || d.display_type == Block.Display.TABLE ||
              (d.display_type == Block.Display.FLEX && block.display_type != Block.Display.FLEX && block.display_type != Block.Display.INLINE_FLEX)) {
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
                if (d.childDocument != null) {
                    d.childDocument.setBounds(d._x_, d._y_, d.viewport_width, d.viewport_height);
                    d.childDocument.setSize(d.viewport_width, d.viewport_height);
                }
            }

            if (checkAbsolutePositioning(d)) {
                d.no_draw = false;
                last_block = d;
                stack.remove(stack.lastIndexOf(d));
                return;
            }

            d.no_draw = false;

            cur_x = new_line.cur_pos;

            last_block = d;

            int vh = d.viewport_height + (d.scrollbar_x != null ? d.scrollbar_x.getPreferredSize().height : 0);
            last_line.setHeight(vh);
            last_margin_top = 0;
            stack.remove(stack.lastIndexOf(d));
            return;
        } else if (block.display_type == Block.Display.FLEX || block.display_type == Block.Display.INLINE_FLEX ||
                d.display_type == Block.Display.INLINE_BLOCK || d.display_type == Block.Display.INLINE_TABLE || d.display_type == Block.Display.INLINE_FLEX) {

            boolean is_flex = block.display_type == Block.Display.FLEX || block.display_type == Block.Display.INLINE_FLEX;
            boolean x_axis = block.flex_direction == Block.Direction.ROW || block.flex_direction == Block.Direction.ROW_REVERSED;

            if (last_line != null && (block.display_type == Block.Display.FLEX || block.display_type == Block.Display.INLINE_FLEX)) {
                offset += block.flex_gap;
            }
            if (is_flex) {
                if (d.auto_width && block.flex_align_items == Block.FlexAlign.STRETCH) {
                    d.setWidth(-1, true);
                } else if (d.auto_width) {
                    d.width = d.viewport_width = Math.max(d.min_size, d.min_width);
                    d.orig_width = (int)Math.round((double) d.width / d.ratio);
                } else if (d.dimensions.containsKey("width")) {
                    if (d.dimensions.get("width").expression != null) {
                        d.setWidth((int) d.getValueInCssPixels(d.dimensions.get("width").expression), Block.Units.px);
                    } else {
                        d.setWidth((int) d.dimensions.get("width").length.value, d.dimensions.get("width").length.unit);
                    }
                }
                if (d.auto_height) {
                    d.height = d.viewport_height = 0;
                    d.orig_height = 0;
                } else if (d.dimensions.containsKey("height")) {
                    if (d.dimensions.get("height").expression != null) {
                        d.setHeight((int) d.getValueInCssPixels(d.dimensions.get("height").expression), Block.Units.px);
                    } else {
                        d.setHeight((int) d.dimensions.get("height").length.value, d.dimensions.get("height").length.unit);
                    }
                }
            }
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

            if (is_flex && d.flex_basis_mode != Block.FlexBasis.EXPLICIT) {
                if (d.flex_basis_mode != Block.FlexBasis.AUTO) {
                    d.performLayout();
                }
                if (d.flex_basis_mode == Block.FlexBasis.AUTO) {
                    d.flex_basis = d.width;
                } else if (d.flex_basis_mode == Block.FlexBasis.MIN_CONTENT) {
                    d.flex_basis = (d.flex_direction == Block.Direction.ROW || d.flex_direction == Block.Direction.ROW_REVERSED) ? d.min_size : d.content_y_max;
                } else if (d.flex_basis_mode == Block.FlexBasis.MAX_CONTENT) {
                    d.flex_basis = (d.flex_direction == Block.Direction.ROW || d.flex_direction == Block.Direction.ROW_REVERSED) ? d.content_x_max : d.content_y_max;
                }
            }

            if (d.display_type != Block.Display.INLINE_TABLE) {
                if (!d.no_layout) {
                    if (is_flex) {
                        if (x_axis) {
                            d.width = d.viewport_width = d.flex_basis;
                            d.orig_width = (int) Math.floor((double) d.width / d.ratio);
                            d.setWidth(d.width > 0 && !d.auto_width ? d.orig_width : -1, true);
                        } else {
                            d.height = d.viewport_height = d.flex_basis;
                            d.orig_height = (int) Math.floor((double) d.height / d.ratio);
                            d.setHeight(d.height > 0 && !d.auto_height ? d.orig_height : -1, true);
                        }
                    } else {
                        d.setWidth(d.width > 0 && !d.auto_width || d.inputType == Block.Input.NUMBER ? d.orig_width : -1, true);
                    }
                    d.performLayout();
                }
                if (x_axis && d.auto_width && d.inputType == Block.Input.NONE && d.lines.size() == 1 && d.lines.get(0).cur_pos < d.lines.get(0).getWidth()) {
                    int line_width = d.lines.get(0).cur_pos;
                    d.lines.get(0).setWidth(line_width);
                    d.width = d.borderWidth[3] + d.paddings[3] + line_width + d.paddings[1] + d.borderWidth[1];
                    d.orig_width = (int)Math.round(d.width / d.ratio);
                    d.viewport_width = d.width;
                } else if (!x_axis && d.auto_height && d.lines.size() == 1 && d.lines.get(0).cur_pos < d.lines.get(0).getWidth()) {
                    int line_width = d.lines.get(0).cur_pos;
                    d.lines.get(0).setWidth(line_width);
                    d.height = d.borderWidth[0] + d.paddings[0] + line_width + d.paddings[2] + d.borderWidth[2];
                    d.orig_height = (int)Math.round(d.height / d.ratio);
                    d.viewport_height = d.height;
                }
            }

            int gap = is_flex ? block.flex_gap : 0;
            if (last_line == null || last_line.elements.size() == 0) gap = 0;

            if ((x_axis && x + d.margins[1] + d.margins[3] + d.width + gap > w ||
                !x_axis && x + d.margins[2] + d.margins[0] + d.height + gap > w) &&
                    (last_line == null || last_line.elements.size() > 0) &&
                    (!(block.display_type == Block.Display.FLEX || block.display_type == Block.Display.INLINE_FLEX) && block.white_space != Block.WhiteSpace.NO_WRAP ||
                    block.flex_wrap != Block.WhiteSpace.NO_WRAP)) {
                last_line = startNewLine(is_flex ? block.flex_gap : 0, 0, d);
                cur_x = last_line.getX();
                cur_y = last_line.getY();
            }
            
            if (block.pref_size < last_line.cur_pos + d.width + d.margins[3] + d.margins[1]) {
                block.pref_size = last_line.cur_pos + d.width + d.margins[3] + d.margins[1];
            }
            if (block.min_size < d.width + d.margins[3] + d.margins[1]) {
                block.min_size = d.width + d.margins[3] + d.margins[1];
            }
            if (block.min_size < block.min_width) {
                block.min_size = block.min_width;
            }

            last_line.addElement(d);

            d.performLayout();
            
            if (d.positioning == Block.Position.RELATIVE) {
                d.setX(d._getX() + d.left);
                d.setY(d._getY() + d.top);
                if (d.childDocument != null) {
                    d.childDocument.setBounds(d._x_, d._y_, d.viewport_width, d.viewport_height);
                    d.childDocument.setSize(d.viewport_width, d.viewport_height);
                }
            }

            if (checkAbsolutePositioning(d)) {
                d.no_draw = false;
                last_block = d;
                stack.remove(stack.lastIndexOf(d));
                return;
            }

            if (last_block != null && last_block.line == last_line) {
                applyVerticalAlignment(d);
            }
            // set line height to the new maximum height
            int h = 0;
            if (x_axis) {
                int vh = d.viewport_height + (d.scrollbar_x != null ? d.scrollbar_x.getPreferredSize().height : 0);
                h = vh + d.margins[0] + d.margins[2];
            } else {
                int vh = d.viewport_width + (d.scrollbar_y != null ? d.scrollbar_y.getPreferredSize().width : 0);
                h = vh + d.margins[3] + d.margins[1];
            }
            if (h > last_line.getHeight()) {
                last_line.setHeight(h);
            }
            d.no_draw = false;
            last_block = d;
            stack.remove(stack.lastIndexOf(d));
            return;
        } else if (d.display_type == Block.Display.INLINE) {

            int[][] selection = new int[d.parts.size()][2];

            int index = 0;
            if (d.document != null) {
                if (d.parts.size() > 0 && d.parts.get(0).getParent() != null) {
                    Block p = d.parts.get(0);
                    index = p.getParent().getComponentZOrder(p) - d.parts.size() + 1;
                    if (index < 0) index = 0;
                } else {
                    index = d.pos >= 0 ? d.pos : (d.parent != null ? d.parent.children.indexOf(d) : 0);
                }
                for (int i = 0; i < d.parts.size(); i++) {
                    Block part = d.parts.get(i);
                    if (part.background != null && part.background.image != null && part.has_animation) {
                        part.stopWatcher();
                    }
                    int[] sel = part.getSelection();
                    selection[i][0] = sel != null ? sel[0] : -1;
                    selection[i][1] = sel != null ? sel[1] : -1;
                    d.document.root.remove(part);
                }
                if (d.parts.size() > 0) {
                    if (index >= 0 && index < d.document.root.getComponents().length) {
                        d.document.root.add(d, index);
                    } else {
                        d.document.root.add(d);
                    }
                }
            }
            d.saveSelectionRange();
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
                if (b.children.size() == 0 && !b.isImage && !b.auto_width || b.children.size() == 1 && b.children.get(0).type == Block.NodeTypes.TEXT && b.children.get(0).textContent.matches("\\s*")) {
                    return;
                }
                last_line = startNewLine(0, 0, b);
            }
            cur_x = last_line.cur_pos;
            cur_y = last_line.getY();

            if (!b.isImage) {
                b.width = b.viewport_width = Math.max(0, last_line.getWidth() - last_line.cur_pos - b.margins[!block.rtl ? 3 : 1]);
                b.orig_width = (int)Math.floor(b.width / b.ratio);
            } else {
                b.performLayout(true);
            }

            if (b.width <= 0 && b.isImage && last_line.getWidth() - last_line.cur_pos - b.margins[!block.rtl ? 3 : 1] < b.width) {
                Line new_line = startNewLine(0, 0, b);
                cur_x = new_line.getX();
                cur_y = new_line.getY();
                last_line = new_line;
                b.width = last_line.getWidth() - b.margins[!block.rtl ? 3 : 1];
                b.viewport_width = b.width;
                b.orig_width = (int)Math.floor(b.width / b.ratio);
            }
            b.no_draw = true;

            b.viewport_width = b.width;
            b.viewport_height = b.height;

            if (last_line.elements.size() == 0 && (b.children.size() == 0 && !b.isImage && b.auto_width || b.children.size() == 1 &&
                  b.children.get(0).type == Block.NodeTypes.TEXT && b.children.get(0).textContent.matches("\\s*"))) {
                return;
            }

            if (last_line.elements.size() > 0 && last_line.elements.lastElement() instanceof Block) {
                last_line.cur_pos += ((Block)last_line.elements.lastElement()).margins[!block.rtl ? 1 : 3];
            }

            last_line.addElement(b);

            last_line.cur_pos -= b.margins[!block.rtl ? 1 : 3];

            if (!b.isImage) b.performLayout(true);

            if (b.lines.size() > 0 && b.lines.get(0).cur_pos < b.lines.get(0).getWidth()) {
                int old_width = b.lines.get(0).width;
                int line_width = b.lines.get(0).cur_pos;
                b.lines.get(0).setWidth(line_width);
                if (block.rtl) {
                    b.lines.get(0).setX(b.lines.get(0).getX() + (old_width - line_width));
                }
                b.width = b.borderWidth[3] + b.paddings[3] + line_width + b.paddings[1] + b.borderWidth[1];
                b.viewport_width = b.width;
                b.orig_width = (int)Math.round(b.width / b.ratio);
                b.content_x_max = b.width;
                last_line.cur_pos -= (old_width - line_width);
                if (block.rtl) {
                    b.setX(last_line.width - last_line.cur_pos);
                }
            }

            if (b.parent.content_x_max < last_line.cur_pos) {
                b.parent.content_x_max = last_line.cur_pos;
            }

            if (b.lines.size() == 0 && !b.isImage) {
                int old_width = b.width;

                int st = (b.text_bold || b.text_italic) ? ((b.text_bold ? Font.BOLD : 0) | (b.text_italic ? Font.ITALIC : 0)) : Font.PLAIN;
                Font f = new Font(b.fontFamily, st, b.fontSize);

                b.width = b.borderWidth[3] + b.paddings[3] + b.paddings[1] + b.borderWidth[1];
                if (b.node.nodeValue.length() > 0) {
                    b.width += b.getFontMetrics(f).charWidth(' ');
                }
                b.viewport_width = b.width;
                b.orig_width = (int)Math.floor(b.width / b.ratio);
                b.content_x_max = b.width;
                last_line.cur_pos -= old_width - b.width;
                if (block.rtl) {
                    b.setX(b._getX());
                }
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

//            for (int i = 0; i < d.parts.size(); i++) {
//                if (i < selection.length && selection[i][0] > -1 && selection[i][1] > -1) {
//                    d.parts.get(i).setSelection(selection[i]);
//                }
//            }

            d.restoreSelectionRange();

            if (d.getParent() != null) {
                //index = d.getParent().getComponentCount() - d.pos - 1;
                for (int i = 0; i < d.parts.size(); i++) {
                    d.document.root.add(d.parts.get(i), index++);
                }
                d.document.root.remove(d);
            }

            if (b.background != null && b.background.image != null && b.has_animation) {
                b.startWatcher();
            }

            if (b.positioning == Block.Position.RELATIVE) {
                b.setX(b._getX() + b.left);
                b.setY(b._getY() + b.top);
                if (b.childDocument != null) {
                    b.childDocument.setBounds(b._x_, b._y_, b.viewport_width, b.viewport_height);
                    b.childDocument.setSize(b.viewport_width, b.viewport_height);
                }
            }

            // a quick hack to enable vertical alignment with symbols
            if (last_block == null && last_line != null && last_line.elements.size() > 1 &&
                    last_line.elements.get(last_line.elements.size()-2) instanceof Character) {
                last_block = new Block(block.document);
                last_block.width = ((Character)last_line.elements.get(last_line.elements.size()-2)).width;
                last_block.height = ((Character)last_line.elements.get(last_line.elements.size()-2)).height;
                last_block.fontFamily = block.fontFamily;
                last_block.fontSize = block.fontSize;
            }

            Line line = last_line;
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

            stack.remove(d);

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
        if (d.positioning == Block.Position.ABSOLUTE) {
            while (p != null && p.positioning != Block.Position.RELATIVE && p.positioning != Block.Position.ABSOLUTE) {
                p = p.parent;
            }
            if (p == null) p = block.document.root;
        } else if (d.positioning == Block.Position.FIXED) {
            p = block.document.root;
        }
        if (d.positioning == Block.Position.ABSOLUTE || d.positioning == Block.Position.FIXED) {
            if (d.auto_left) {
                d.setX(!block.rtl ? last_line.getX() : last_line.getX() + last_line.getWidth() - d.width);
            } else if (d.auto_right) {
                if (!d.auto_left && !d.auto_right && !d.auto_width && d.auto_x_margin &&
                      p.width - p.borderWidth[3] - p.borderWidth[1] - d.left - d.right - d.width > 0) {
                    int w = p.width - p.borderWidth[3] - p.borderWidth[1] - d.left - d.right - d.width;
                    d.margins[3] = Math.round(w / 2);
                    d.margins[1] = w - d.margins[3];
                }
                d.setX(p.borderWidth[3] + d.margins[3] + d.left);
            }
            if (!d.auto_right && !d.auto_width) {
                d.setX(block.viewport_width - d.width - d.right);
            }
            if (d.auto_top) {
                d.setY(last_line.getY());
            } else if (d.auto_bottom) {
                if (!d.auto_top && !d.auto_bottom && !d.auto_height && d.auto_y_margin &&
                      p.height - p.borderWidth[0] - p.borderWidth[2] - d.top - d.bottom - d.height > 0) {
                    int h = p.height - p.borderWidth[0] - p.borderWidth[2] - d.top - d.bottom - d.height;
                    d.margins[0] = Math.round(h / 2);
                    d.margins[2] = h - d.margins[0];
                }
                d.setY(p.borderWidth[0] + d.margins[0] + d.top);
            }
            if (!d.auto_bottom && !d.auto_height) {
                d.setY(block.viewport_height - d.height - d.bottom);
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
            if (d.childDocument != null) {
                d.childDocument.setBounds(d._x_, d._y_, d.viewport_width, d.viewport_height);
                d.childDocument.setSize(d.viewport_width, d.viewport_height);
            }
            return true;
        }
        return false;
    }

    public void applyVerticalAlignment(Block d) {
        applyVerticalAlignment(d, false);
    }

    public void applyVerticalAlignment(Block d, boolean no_height_update) {
        boolean is_flex = d.parent.display_type == Block.Display.FLEX || d.parent.display_type == Block.Display.INLINE_FLEX;
        boolean x_axis = d.parent.flex_direction == Block.Direction.ROW || d.parent.flex_direction == Block.Direction.ROW_REVERSED;

        Block b = d.original != null ? d.original : d;
        int index = d.line.elements.indexOf(b);
        if (index > 0 && d.node != null && d.node.nodeType == 3 && d.node.nodeValue.matches("\\s*")) {
            d.vertical_align = ((Block)d.line.elements.get(index-1)).vertical_align;
            d.height = d.parent.children.get(index-1).height;
        }

        int offset = 0;
        Line line = d.line;
        boolean isEmpty = d.display_type == Block.Display.INLINE && d.children.size() == 0 && d.textContent == null;
        int vertical_align = is_flex ? block.flex_align_items : d.vertical_align;
        if (vertical_align == Block.VerticalAlign.ALIGN_MIDDLE) {
            offset = Math.round((line.height - (x_axis ? d.height : d.width)) / 2);
        }
        else if (vertical_align == Block.VerticalAlign.ALIGN_BOTTOM) {
            offset = line.height - (x_axis ? d.height : d.width);
        }
        else if (vertical_align == Block.VerticalAlign.ALIGN_BASELINE && last_block != null && last_block.line == d.line && !isEmpty) {
            int st1 = (last_block.text_bold || last_block.text_italic) ? ((last_block.text_bold ? Font.BOLD : 0) | (last_block.text_italic ? Font.ITALIC : 0)) : Font.PLAIN;
            int st2 = (d.text_bold || d.text_italic) ? ((d.text_bold ? Font.BOLD : 0) | (d.text_italic ? Font.ITALIC : 0)) : Font.PLAIN;
            Font f1 = new Font(last_block.fontFamily, st1, last_block.fontSize);
            Font f2 = new Font(d.fontFamily, st2, d.fontSize);
            FontMetrics m1 = last_block.getFontMetrics(f1);
            FontMetrics m2 = last_block.getFontMetrics(f2);
            if (!is_flex || x_axis) {
                offset = last_block.getOffsetTop() - line.getY() + last_block.height - last_block.borderWidth[2] - last_block.paddings[2] -
                     m1.getDescent() + m2.getDescent() - (d.height - d.borderWidth[2] - d.paddings[2]);
            } else {
                offset = !block.rtl ? last_block.paddings[3] + last_block.borderWidth[3] - (d.paddings[3] + d.borderWidth[3]) : last_block.width - last_block.borderWidth[1] - last_block.paddings[1] - (d.width - d.borderWidth[1] - d.paddings[1]);
            }
        } else if (last_block != null && last_block.line == d.line) {
            offset = last_block.getOffsetTop() - line.getY();
        } else {
            offset = 0;
        }

        if (!is_flex || x_axis) {
            d.setY(line.getY() + offset);
        } else {
            d.setX(line.getX() + offset);
        }

        // update the line height
        if (no_height_update) return;
        if (!is_flex || x_axis) {
            if (offset + d.height > line.getHeight()) {
                line.setHeight(offset + d.height);
            }
            if (offset < d.margins[0] && d.display_type != Block.Display.INLINE) offset -= d.margins[0];
            if (offset < 0) {
                line.setHeight(line.getHeight() - offset);
                for (int i = 0; i < line.elements.size(); i++) {
                    line.elements.get(i).setY(line.elements.get(i)._getY() - offset);
                    if (line.elements.get(i) instanceof Block) {
                        applyVerticalAlignment((Block)line.elements.get(i), true);
                    }
                }
            }
        } else {
            if (offset + d.width > line.getHeight()) {
                line.setHeight(offset + d.width);
            }
            if (offset < d.margins[3] && d.display_type != Block.Display.INLINE) offset -= d.margins[3];
            if (offset < 0) {
                line.setHeight(line.getHeight() - offset);
                for (int i = 0; i < line.elements.size(); i++) {
                    line.elements.get(i).setX(line.elements.get(i)._getX() - offset);
                }
            }
        }
    }

    public static void applyHorizontalAlignment(Block block) {
        boolean is_flex = block.display_type == Block.Display.FLEX || block.display_type == Block.Display.INLINE_FLEX;
        boolean x_axis = block.flex_direction == Block.Direction.ROW || block.flex_direction == Block.Direction.ROW_REVERSED;

        int content_align = is_flex ? block.flex_justify : block.text_align;

        int style = (block.text_bold || block.text_italic) ? ((block.text_bold ? Font.BOLD : 0) | (block.text_italic ? Font.ITALIC : 0)) : Font.PLAIN;
        Font f = new Font(block.fontFamily, style, block.fontSize);
        int sp = block.getFontMetrics(f).stringWidth(" ");
        int new_sp = sp;
        int mod = 0;

        for (int i = 0; i < block.lines.size(); i++) {
            Line line = block.lines.get(i);

            int space_count = 0;

            if (content_align == Block.TextAlign.ALIGN_JUSTIFY || content_align == Block.FlexJustify.SPACE_AROUND || content_align == Block.FlexJustify.SPACE_EVENLY) {
                for (int j = 0; j < line.elements.size(); j++) {
                    Drawable d = line.elements.get(j);
                    if (d instanceof Character && ((Character)d).getText().equals(" ") ||
                            j < line.elements.size()-1 && line.elements.get(j+1) instanceof Block &&
                            (((Block)line.elements.get(j+1)).display_type != Block.Display.INLINE || is_flex)) {
                        space_count++;
                    }
                }
                if (content_align == Block.FlexJustify.SPACE_AROUND || content_align == Block.FlexJustify.SPACE_EVENLY) {
                    space_count += 2;
                }
                if (!is_flex) {
                    new_sp = space_count > 0 ? (int)Math.floor((line.width - line.cur_pos) / space_count) : 0;
                    mod = line.width - line.cur_pos - new_sp * space_count;
                } else if (content_align != Block.FlexJustify.SPACE_AROUND) {
                    new_sp = space_count > 0 ? (int)Math.floor((line.width + block.flex_gap * (line.elements.size()-1) - line.cur_pos) / space_count) : 0;
                    mod = line.width + block.flex_gap * (line.elements.size()-1) - line.cur_pos - new_sp * space_count;
                } else {
                    new_sp = space_count > 0 ? (int)Math.floor((line.width + block.flex_gap * (line.elements.size()-1) - line.cur_pos) / (space_count-1)) : 0;
                    mod = line.width + block.flex_gap * (line.elements.size()-1) - line.cur_pos - new_sp * (space_count-1);
                }
            }

            if (line.getWidth() == line.cur_pos) continue;
            int pos = 0;
            int space_index = 0;
            if (content_align == Block.TextAlign.ALIGN_CENTER) {
                pos += (int)Math.round(line.width - line.cur_pos) / 2;
            } else if (content_align == Block.TextAlign.ALIGN_RIGHT) {
                pos += (int)Math.round(line.width - line.cur_pos);
            }
            for (int j = 0; j < line.elements.size(); j++) {
                Drawable d = line.elements.get(j);
                int w = 0;
                if (!is_flex) {
                    w = j < line.elements.size()-1 ? line.elements.get(j+1)._getX() - d._getX() : line.getX() + line.cur_pos - d._getX();
                } else {
                    if (x_axis) {
                        w = j < line.elements.size()-1 ? line.elements.get(j+1)._getX() - d._getX() - block.flex_gap : line.getX() + line.cur_pos - d._getX();
                    } else {
                        w = j < line.elements.size()-1 ? line.elements.get(j+1)._getY() - d._getY() - block.flex_gap : line.getY() + line.cur_pos - d._getY();
                    }
                }
                if (d instanceof Character && ((Character)d).getText().equals(" ")) {
                    if (content_align != Block.TextAlign.ALIGN_JUSTIFY) {
                        if (!is_flex || x_axis) {
                            d.setWidth(sp);
                        } else {
                            d.setHeight(sp);
                        }
                    } else {
                        if (!is_flex || x_axis) {
                            d.setWidth(space_index < mod ? new_sp + 1 : new_sp);
                        } else {
                            d.setHeight(space_index < mod ? new_sp + 1 : new_sp);
                        }
                        space_index++;
                    }
                }

                /* Block justification should only be available for flex containers */

                if (is_flex && (content_align == Block.TextAlign.ALIGN_JUSTIFY || content_align == Block.FlexJustify.SPACE_AROUND || content_align == Block.FlexJustify.SPACE_EVENLY) &&
                      (j > 0 || content_align == Block.FlexJustify.SPACE_AROUND || content_align == Block.FlexJustify.SPACE_EVENLY)) {
                    if (j == 0 && content_align == Block.FlexJustify.SPACE_AROUND) {
                        pos += (int) Math.round(new_sp / 2);
                        space_index--;
                    } else {
                        pos += space_index < mod ? new_sp + 1 : new_sp;
                    }
                    space_index++;
                }

                if (!is_flex || x_axis) {
                    d.setX(line.getX() + pos);
                } else {
                    d.setY(line.getY() + pos);
                }
                pos += w;
            }
        }
    }

    public void positionFloat(Block d) {
        int availableWidth = block.width - float_left_offset - float_right_offset;
        if (d.float_type == Block.FloatType.LEFT) {
            cur_x = float_left_offset + d.margins[3];         

            int last_block_margin = last_block != null ? last_block.margins[2] : 0;
            int last_block_y = last_block != null ? last_block.getOffsetTop() + last_block.height : block.paddings[0];
            
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

            if (cur_x > block.width - block.borderWidth[1] - block.borderWidth[3] - block.paddings[1] - block.paddings[3] && d.width > availableWidth) {
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
            cur_x = block.width - block.borderWidth[1] -
                    float_right_offset - d.margins[1] - d.width;
            float_right_offset += d.margins[3] + d.width + d.margins[1];
            d.setX(cur_x);
            d.setY(cur_y);

            if (cur_x > block.width - block.borderWidth[1] - block.borderWidth[3] - block.paddings[1] - block.paddings[3] && d.width > availableWidth) {
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

    public void resetFloatOffsets() {
        float_left_offset = block.borderWidth[3] + block.paddings[3];
        float_right_offset = block.borderWidth[1] + block.paddings[1];
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

    public int getFloatOffsetLeft() {
        return float_left_offset;
    }

    public int getFloatOffsetRight() {
        return float_right_offset;
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

    private Block float_left_last_el;
    private Block float_right_last_el;

    public Line last_line;

}
