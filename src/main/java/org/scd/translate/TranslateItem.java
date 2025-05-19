package org.scd.translate;

public class TranslateItem {
    private String en;

    private String cn;

    public TranslateItem() {
    }

    public TranslateItem(String en, String cn) {
        this.en = en;
        this.cn = cn;
    }

    public String getEn() {
        return en;
    }

    public void setEn(String en) {
        this.en = en;
    }

    public String getCn() {
        return cn;
    }

    public void setCn(String cn) {
        this.cn = cn;
    }

    @Override
    public String toString() {
        return "TranslateItem{" +
                "en='" + en + '\'' +
                ", cn='" + cn + '\'' +
                '}';
    }
}
