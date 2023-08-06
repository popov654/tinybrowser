package com.alstarsoft.tinybrowser.bridge;

import com.alstarsoft.tinybrowser.cache.Cache;
import com.alstarsoft.tinybrowser.htmlparser.Node;
import java.nio.charset.Charset;
import javax.swing.JFrame;
import com.alstarsoft.tinybrowser.render.Block;
import com.alstarsoft.tinybrowser.render.WebDocument;

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

    public void setAsyncImageLoad(boolean value) {
        asyncImageLoad = value;
    }

    public void setIframeImageLoad(boolean value) {
        asyncIframeLoad = value;
    }

    public void setEnableScripts(boolean value) {
        enableScripts = value;
    }

    private ResourceManager resourceManager;

    public Charset charset;

    public Builder builder;
    public Node rootNode;
    public Block rootBlock;
    public WebDocument document;
    public Cache cache;

    public Document parentDocument;
    public Node hostElement;

    public boolean asyncImageLoad = true;
    public boolean asyncIframeLoad = true;
    public boolean enableExternalStyles = true;
    public boolean enableScripts = true;

    public String title = "";
    public JFrame frame;
}
