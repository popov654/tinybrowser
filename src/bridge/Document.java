package bridge;

import cache.Cache;
import htmlparser.Node;
import javax.swing.JFrame;
import render.Block;
import render.WebDocument;

/**
 *
 * @author Alex
 */
public class Document {

    public Document(Builder builder, Node rootNode, Block rootBlock) {
        this.builder = builder;
        this.rootNode = rootNode;
        this.rootBlock = rootBlock;
        resourceManager = new ResourceManager(this);
    }

    public Document(Builder builder, Node rootNode, Block rootBlock, Cache cache) {
        this(builder, rootNode, rootBlock);
        this.cache = cache;
    }

    public Block getRootBlock() {
        return rootBlock;
    }

    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    private ResourceManager resourceManager;

    public Builder builder;
    public Node rootNode;
    public Block rootBlock;
    public WebDocument document;
    public Cache cache;

    public String title = "";
    public JFrame frame;
}
