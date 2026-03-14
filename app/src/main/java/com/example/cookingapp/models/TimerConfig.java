package com.example.cookingapp.models;


public class TimerConfig {
    private final String labelKey;
    private final int    defaultSeconds;
    private final int    beepResId;
    private final boolean editable;

    public TimerConfig(String labelKey, int defaultSeconds, int beepResId, boolean editable) {
        this.labelKey       = labelKey;
        this.defaultSeconds = defaultSeconds;
        this.beepResId      = beepResId;
        this.editable       = editable;
    }

    public String  getLabelKey()       { return labelKey; }
    public int     getDefaultSeconds() { return defaultSeconds; }
    public int     getBeepResId()      { return beepResId; }
    public boolean isEditable()        { return editable; }
}
