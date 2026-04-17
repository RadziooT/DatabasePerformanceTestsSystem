package example.utils;

public enum DatasetSize {
    SMALL(2),
    MEDIUM(5),
    LARGE(10);

    private final int warehouses;

    DatasetSize(int warehouses) {
        this.warehouses = warehouses;
    }

    public int warehouses() {
        return warehouses;
    }

    public static DatasetSize parse(String value) {
        String normalized = value == null ? "small" : value.trim().toLowerCase();
        return switch (normalized) {
            case "medium" -> MEDIUM;
            case "large" -> LARGE;
            default -> SMALL;
        };
    }
}
