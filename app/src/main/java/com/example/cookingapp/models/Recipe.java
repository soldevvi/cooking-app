package com.example.cookingapp.models;

public class Recipe {
    public static final String CATEGORY_BREAKFAST = "breakfast";
    public static final String CATEGORY_LUNCH     = "lunch";
    public static final String CATEGORY_DINNER    = "dinner";

    private int    id;
    private String nameKey;
    private String descKey;
    private String category;
    private String timeKey;
    private int    servingsKey;
    private String ingredientsKey;
    private String stepsKey;
    private int    imageResId;


    private String voiceResPrefix;

    private int    videoResId;

    public Recipe(int id, String nameKey, String descKey, String category,
                  String timeKey, int servingsKey, String ingredientsKey,
                  String stepsKey, int imageResId,
                  String voiceResPrefix, int videoResId) {
        this.id             = id;
        this.nameKey        = nameKey;
        this.descKey        = descKey;
        this.category       = category;
        this.timeKey        = timeKey;
        this.servingsKey    = servingsKey;
        this.ingredientsKey = ingredientsKey;
        this.stepsKey       = stepsKey;
        this.imageResId     = imageResId;
        this.voiceResPrefix = voiceResPrefix;
        this.videoResId     = videoResId;
    }

    public int    getId()             { return id; }
    public String getNameKey()        { return nameKey; }
    public String getDescKey()        { return descKey; }
    public String getCategory()       { return category; }
    public String getTimeKey()        { return timeKey; }
    public int    getServingsKey()    { return servingsKey; }
    public String getIngredientsKey() { return ingredientsKey; }
    public String getStepsKey()       { return stepsKey; }
    public int    getImageResId()     { return imageResId; }


    public String getVoiceResPrefix() { return voiceResPrefix; }

    public boolean hasVoice()         { return voiceResPrefix != null; }


    public int  getVideoResId()       { return videoResId; }

    public boolean hasVideo()         { return videoResId != 0; }
}
