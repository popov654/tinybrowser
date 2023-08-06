package com.alstarsoft.tinybrowser.cssparser;

/**
 *
 * @author Alex
 */
public class SelectorGroup {

    public SelectorGroup() {}

    public SelectorGroup(String query) {
        if (query == null || query.isEmpty()) return;
        String[] parts = query.split(" and ");
        media = parts[0].trim().replace("media ", "");
        for (int i = 1; i < parts.length; i++) {
            String[] p = parts[i].replaceAll("^\\(|\\)$", "").split("\\s*:\\s*");
            if (p.length < 2) continue;
            if (p[0].equals("min-width")) {
                minWidth = Integer.parseInt(p[1].trim().replaceAll("[a-z%-]+$", ""));
            } else if (p[0].equals("max-width")) {
                maxWidth = Integer.parseInt(p[1].trim().replaceAll("[a-z%-]+$", ""));
            } else if (p[0].equals("min-height")) {
                minHeight = Integer.parseInt(p[1].trim().replaceAll("[a-z%-]+$", ""));
            } else if (p[0].equals("max-height")) {
                maxHeight = Integer.parseInt(p[1].trim().replaceAll("[a-z%-]+$", ""));
            } else if (p[0].equals("min-device-pixel-ratio")) {
                minDpi = Integer.parseInt(p[1].trim().replaceAll("[a-z%-]+$", ""));
            } else if (p[0].equals("max-device-pixel-ratio")) {
                maxDpi = Integer.parseInt(p[1].trim().replaceAll("[a-z%-]+$", ""));
            }
        }
    }



    public String media = "all";
    public int minWidth = 0;
    public int maxWidth = Integer.MAX_VALUE;
    public int minHeight = 0;
    public int maxHeight = Integer.MAX_VALUE;
    public int minDpi = 0;
    public int maxDpi = Integer.MAX_VALUE;

}
