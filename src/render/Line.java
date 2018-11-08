package render;

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

        if (d instanceof Block) {
            Block b = (Block)d;
            cur_pos += b.margins[3];
        }
        if (d instanceof Character) {
            Character c = (Character)d;
            if (c.height > height) {
                height = c.height;
                //implement vertical align
            }
        }
        d.setX(left + cur_pos);
        if (d instanceof Block && ((Block)d).display_type != Block.Display.BLOCK) {
            Block b = (Block)d;
            b.setY(b.margins[0] + top);
        }
        else d.setY(top);
        System.out.println((d instanceof Character ? "Letter " : "Element ") + "positioned at (" + d._getX() + ", " + d._getY() + ")");
        
        if (d instanceof Block) {
            Block b = (Block)d;
            cur_pos += b.width + b.margins[1];
        } else if (d instanceof Character) {
            if (elements.size() > 1) {
                cur_pos += parent.letter_spacing;
            }
            cur_pos += ((Character)d).getWidth();
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
        left = value;
    }

    public void setY(int value) {
        top = value;
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

    public int cur_pos = 0;

    public int left;
    public int top;

    public int width;
    public int height;

    public Vector<Drawable> elements = new Vector<Drawable>();

    public Block parent;
}
