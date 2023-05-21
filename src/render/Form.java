package render;

import java.util.Vector;
import javax.swing.JToggleButton;
import javax.swing.text.JTextComponent;
import network.FormEntry;
import network.Request;

/**
 *
 * @author Alex
 */
public class Form {

    public Form(Block block) {
        block.form = this;
        this.block = block;
    }

    public void submit() {
        Thread t = new Thread() {
            @Override
            public void run() {
                if (block.node != null) {
                    block.node.fireEvent("beforesubmit", "render");
                    if (block.node.defaultPrevented) {
                        block.node.defaultPrevented = false;
                        return;
                    }
                }
                Vector<FormEntry> params = new Vector<FormEntry>();
                for (Block input: inputs) {
                    if (input.formEntries == null) continue;
                    for (FormEntry entry: input.formEntries) {
                        params.add(entry);
                    }
                }
                String response = Request.makeRequest(url, method, params, "cp1251", true, multipart);

                block.document.debug = true;
                if (block.document.debug) {
                    System.out.println("Response:\n\n" + response);
                }

                if (block.node != null) {
                    block.node.fireEvent("submit", "render");
                    if (block.node.defaultPrevented) {
                        block.node.defaultPrevented = false;
                        return;
                    }
                }
            }
        };
        t.start();
    }

    public void reset() {
        if (block.node != null) {
            block.node.fireEvent("reset", "render");
            if (block.node.defaultPrevented) {
                block.node.defaultPrevented = false;
                return;
            }
        }
        for (Block input: inputs) {
            input.inputValue = input.defaultInputValue;
            input.checked = input.defaultChecked;
            if (input.inputType >= Block.Input.TEXT && input.inputType <= Block.Input.TEXTAREA) {
                java.awt.Component[] c = input.getComponents();
                for (int i = 0; i < c.length; i++) {
                    if (c[i] instanceof JTextComponent) {
                        ((JTextComponent)c[i]).setText(input.inputValue);
                        break;
                    }
                }
            }
            if (input.inputType >= Block.Input.RADIO && input.inputType <= Block.Input.CHECKBOX) {
                java.awt.Component[] c = input.getComponents();
                for (int i = 0; i < c.length; i++) {
                    if (c[i] instanceof JToggleButton) {
                        ((JToggleButton)input.getComponent(0)).getModel().setSelected(input.checked);
                        break;
                    }
                }
            }
            if (input.inputType == Block.Input.SELECT && input.children.size() > 1) {
                Block list = input.children.get(1);
                int index = -1;
                for (int i = 0; i < list.children.size(); i++) {
                    if (list.children.get(i).inputValue.equals(input.inputValue)) {
                        index = i;
                        break;
                    }
                }
                input.setInputSelectedIndex(index);
            }
            if (input.inputType == Block.Input.FILE) {
                input.inputValue = "";
                Block label = input.children.get(0);
                label.children.get(0).textContent = "No file selected";
                label.performLayout();
                label.forceRepaint();
            }
        }
    }

    public Vector<Block> inputs = new Vector<Block>();
    public Block block;

    public String url;
    public String method = "GET";
    public boolean multipart = false;
}
