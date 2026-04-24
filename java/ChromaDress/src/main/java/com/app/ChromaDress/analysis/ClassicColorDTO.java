package com.app.ChromaDress.analysis;

import java.io.Serializable;
import java.util.List;

public record ClassicColorDTO(List<List<String>> suggestions) implements Serializable {
}
