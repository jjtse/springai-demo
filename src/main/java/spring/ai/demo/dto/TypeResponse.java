package spring.ai.demo.dto;

public record TypeResponse(Type type) {
    public enum Type {
        API, BATCH, UNKNOWN
    }
}

