package com.bestiarymap.util;

import lombok.Getter;

import java.util.List;

public class Monster {
    @Getter
    private String name;

    @Getter
    private List<Spawn> spawns;
}
