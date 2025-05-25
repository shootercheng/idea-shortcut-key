package org.scd.export;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdeaKeyMapData {
    private String enId;

    private String cnId;

    private String shortcutKey;
}
