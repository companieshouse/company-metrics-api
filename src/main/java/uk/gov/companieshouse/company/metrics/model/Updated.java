package uk.gov.companieshouse.company.metrics.model;

public class Updated {

    private String at;

    private String by;

    private String type;

    /**
     * Instantiate company insolvency updated data.
     * @param at timestamp for the delta change
     * @param by updated by
     * @param type the delta type
     */
    public Updated(String at, String by, String type) {
        this.at = at;
        this.by = by;
        this.type = type;
    }

    /**
     * Default Constructor.
     */
    public Updated() {
        this.at = null;
        this.by = null;
        this.type = null;
    }

    public String getAt() {
        return at;
    }

    public void setAt(String at) {
        this.at = at;
    }

    public String getBy() {
        return by;
    }

    public void setBy(String by) {
        this.by = by;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
