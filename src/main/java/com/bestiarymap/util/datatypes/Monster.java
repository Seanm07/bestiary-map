package com.bestiarymap.util.datatypes;

import lombok.Getter;

import java.util.List;

public class Monster {
    @Getter
    private String name;

    @Getter
    private List<Spawn> spawns;
}
