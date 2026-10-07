package com.aris.templateapp.ui.upload;

/** Keterangan satu elemen dari {@code RakitMark.describe} (posisi dalam piksel CSS). */
public class ElementInfo {
    public int id;
    public String tag;
    /** Tebakan jenis isian: text · paragraph · image · link · button */
    public String kind;
    public String text;
    public String src;
    public String href;
    public String breadcrumb;
    public double top;
    public double height;
    public double fontSize;
    public int ratioW;
    public int ratioH;
    public boolean visible;
    public boolean hasParent;
    public boolean hasChild;
}
