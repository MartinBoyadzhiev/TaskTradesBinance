package dto;

import java.util.List;

public record StreamData(
        String e,
        long E,
        String s,
        long U,
        long u,
        List<List<String>> b,
        List<List<String>> a
) {}