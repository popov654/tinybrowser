package com.alstarsoft.tinybrowser.render;

import java.awt.Font;
import java.util.Vector;

/**
 *
 * @author Alex
 */
public class Line {

    public Line(Block block) {
        parent = block;
        parent.lines.add(this);
    }

    private void checkIsListItem() {
        if (parent.list_item_type == 0) return;

        int style = (parent.text_bold || parent.text_italic) ? ((parent.text_bold ? Font.BOLD : 0) | (parent.text_italic ? Font.ITALIC : 0)) : Font.PLAIN;
        Font font = new Font("Tahoma", style, parent.fontSize);

        if (parent.list_item_type < 10) {
            Character c = new Character(null, Block.lm[parent.list_item_type]);
            c.setFont(font);

            int offset = parent.getFontMetrics(font).stringWidth(c.getText());
            c.width = offset;
            c.height = parent.getFontMetrics(font).getHeight();
            offset += Math.round(parent.getFontMetrics(font).getHeight() / 1.8);
            if (parent.list_item_type == 5 || parent.list_item_type == 6) {
                offset = (int)Math.round(offset * 0.76);
            }
            cur_pos += offset;
        }
        if (parent.list_item_type >= 10 && parent.list_item_type < 16) {

            cur_pos += parent.parent.list_max_offset;
                
        }
    }

    public void addElement(Drawable d) {
        elements.add(d);
        d.setLine(this);

        if (cur_pos == 0) checkIsListItem();

        if (cur_pos > 0 && (parent.display_type == Block.Display.FLEX || parent.display_type == Block.Display.INLINE_FLEX)) {
            cur_pos += parent.flex_gap;
        }

        boolean is_flex = parent.display_type == Block.Display.FLEX || parent.display_type == Block.Display.INLINE_FLEX;

        boolean x_axis = parent.display_type != Block.Display.FLEX && parent.display_type != Block.Display.INLINE_FLEX ||
                         parent.flex_direction != Block.Direction.COLUMN && parent.flex_direction != Block.Direction.COLUMN_REVERSED;

        if (d instanceof Block) {
            Block b = (Block)d;
            int margin = x_axis ? b.margins[!parent.rtl ? 3 : 1] : b.margins[parent.flex_direction != Block.Direction.COLUMN_REVERSED ? 2 : 0];
            cur_pos += margin;
        }
        if (d instanceof Character) {
            Character c = (Character)d;
            if (x_axis) {
                if (c.height > height) {
                    height = c.height;
                    //implement vertical align
                }
            } else {
                if (c.width > height) {
                    height = c.width;
                    //implement vertical align
                }
            }
        }
        
        int x = !parent.rtl ? left + cur_pos : Math.max(0, width - cur_pos - d._getWidth());
        int y = top;

        if (is_flex) {
            if (parent.flex_direction == Block.Direction.ROW_REVERSED) {
                x = Math.max(0, width - cur_pos - d._getWidth());
            } else if (parent.flex_direction == Block.Direction.COLUMN) {
                x = left;
                y = top + cur_pos;
            } else if (parent.flex_direction == Block.Direction.COLUMN_REVERSED) {
                x = left;
                y = Math.max(0, width - cur_pos - d._getWidth());
            }
        }

        if (!(d instanceof Block && (((Block)d).positioning == Block.Position.ABSOLUTE || ((Block)d).positioning == Block.Position.FIXED))) {
            d.setX(x);
            d.setY(y);
        }

        if (d instanceof Block && (is_flex || ((Block)d).display_type != Block.Display.BLOCK)) {
            Block b = (Block)d;
            //b.setY(b.margins[0] + top);
            int vertical_align = is_flex ? parent.flex_align_items : b.vertical_align;
            int offset = x_axis ? b.margins[0] : b.margins[3];
            if (x_axis && b.margins[0] + b.height + b.margins[2] > height || !x_axis && b.margins[3] + b.width + b.margins[1] > height) {
                height = x_axis ? b.margins[0] + b.height + b.margins[2] : b.margins[3] + b.width + b.margins[1];
                if (vertical_align == Block.VerticalAlign.ALIGN_MIDDLE) {
                    offset = Math.round((height - (x_axis ? b.height : b.width)) / 2);
                }
                else if (vertical_align == Block.VerticalAlign.ALIGN_BOTTOM) {
                    offset = height - (x_axis ? b.height : b.width);
                }
                //implement vertical align
            }
            if (x_axis) {
                d.setY(offset + top);
            } else {
                d.setX(offset + left);
            }
        }
        else if (d instanceof Block) {
            Block b = (Block)d;
            if (b.auto_width) {
                b.width = Math.max(0, width - b.margins[3] - b.margins[1]);
                b.orig_width = (int)Math.floor(b.width / b.ratio);
            }
        }

        if (d instanceof Block && ((Block)d).node != null && ((Block)d).node.tagName.equals("br")) {
            if (x_axis) {
                ((Block)d).margins[0] = height - 1;
                d.setY(height - ((Block)d).height + top - 1);
            } else {
                ((Block)d).margins[3] = height - 1;
                d.setX(height - ((Block)d).height + top - 1);
            }
        }
        
        if (parent.document.debug) {
            System.out.println((d instanceof Character ? "Letter " : "Element ") + "positioned at (" + d._getX() + ", " + d._getY() + ")");
        }
        
        if (d instanceof Block) {
            Block b = (Block)d;
            int delta = x_axis ? b.margins[!is_flex && !parent.rtl ? 1 : 3] : b.margins[parent.flex_direction != Block.Direction.COLUMN_REVERSED ? 2 : 0];
            cur_pos += (x_axis ? b.width : b.height) + delta;
        } else if (d instanceof Character) {
            if (elements.size() > 1) {
                cur_pos += parent.letter_spacing;
            }
            cur_pos += x_axis ? ((Character)d).getWidth() : ((Character)d).getHeight();
        }
    }

    public int getOffsetLeft() {
        return (parent == null) ? left : parent.getOffsetLeft() + left;
    }

    public int getOffsetTop() {
        return (parent == null) ? top : parent.getOffsetTop() + top;
    }

    public int getX() {
        return left;
    }

    public int getY() {
        return top;
    }

    public void setX(int value) {
        int old_value = left;
        left = value;
        boolean x_axis = parent.display_type != Block.Display.FLEX && parent.display_type != Block.Display.INLINE_FLEX ||
                         parent.flex_direction != Block.Direction.COLUMN && parent.flex_direction != Block.Direction.COLUMN_REVERSED;
        for (Drawable d: elements) {
            d.setX(d._getX() + left - old_value);
        }
    }

    public void setY(int value) {
        int old_value = top;
        top = value;
        boolean y_axis = parent.display_type != Block.Display.FLEX && parent.display_type != Block.Display.INLINE_FLEX ||
                         parent.flex_direction != Block.Direction.COLUMN && parent.flex_direction != Block.Direction.COLUMN_REVERSED;
        for (Drawable d: elements) {
            d.setY(d._getY() + top - old_value);
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setWidth(int value) {
        width = value;
    }

    public void setHeight(int value) {
        height = value;
    }

    @Override
    public String toString() {
        return "(" + left + "," + top + ") [" + width + "x" + height + "]" + elements.toString();
    }

    public int cur_pos = 0;

    public int left;
    public int top;

    public int width;
    public int height;

    public Vector<Drawable> elements = new Vector<Drawable>();

    public Block parent;
}
