package com.aris.templateapp.ui.upload;

import java.util.ArrayList;
import java.util.List;

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
    /** Nomor elemen bertanda yang membungkus elemen ini. */
    public List<Integer> markedAncestors = new ArrayList<>();
    /** Nomor elemen bertanda di dalam elemen ini. */
    public List<Integer> markedDescendants = new ArrayList<>();
    /** Atribut modul dinamis yang mengisi elemen ini otomatis (mis. data-ws-org="nama"), atau null. */
    public String dynamicFilled;
    /** data-ws-link / data-ws-auth di elemen ini (href diatur otomatis), atau null. */
    public String dynamicLink;
    /** Penanda modul pertama di dalam elemen ini, atau null. */
    public String wrapsModule;
}
