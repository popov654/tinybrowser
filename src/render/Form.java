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
                    if (input.formEntry == null) continue;
                    params.add(input.formEntry);
                }
                String response = Request.makeRequest(url, method, params, "cp1251", true, multipart);

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
                ((JTextComponent)input.getComponent(0)).setText(input.inputValue);
            }
            if (input.inputType >= Block.Input.RADIO && input.inputType <= Block.Input.CHECKBOX) {
                ((JToggleButton)input.getComponent(0)).getModel().setSelected(input.checked);
            }
        }
    }

    public Vector<Block> inputs = new Vector<Block>();
    public Block block;

    public String url;
    public String method = "GET";
    public boolean multipart = false;
}
