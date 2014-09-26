package de.uhrenbastler.watchcheck.ui;

/**
 * Created by clorenz on 13.02.14.
 */
public class SelectWatch {

    private String name;
    private String visibility;

    public SelectWatch(String name) {
        this.name = name;
        this.visibility = visibility;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }
}
