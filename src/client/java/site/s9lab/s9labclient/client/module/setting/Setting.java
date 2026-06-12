package site.s9lab.s9labclient.client.module.setting;

public abstract class Setting<T> {
    private final String name;
    private T value;

    protected Setting(String name, T value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
