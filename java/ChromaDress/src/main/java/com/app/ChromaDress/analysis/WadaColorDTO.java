package com.app.ChromaDress.analysis;

import java.io.Serializable;
import java.util.List;

public record WadaColorDTO(String originalColor, String wadaName, String wadaHex,
                           List<List<String>> combinations) implements Serializable {

}
