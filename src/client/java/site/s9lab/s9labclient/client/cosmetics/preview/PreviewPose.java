package site.s9lab.s9labclient.client.cosmetics.preview;

public enum PreviewPose {
    IDLE("Idle"),
    WALK("Walk"),
    CROUCH("Crouch");

    private final String label;

    PreviewPose(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public PreviewPose next() {
        PreviewPose[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
