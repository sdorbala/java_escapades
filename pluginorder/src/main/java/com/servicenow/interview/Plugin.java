package com.servicenow.interview;

import java.util.ArrayList;
import java.util.List;

public class Plugin {
    public String id;
    public String name;
    List<String> dependencies;

    public Plugin(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public void addDependency(Plugin plugin) {
        if (dependencies == null) {
            dependencies = new ArrayList<>();
        }
        dependencies.add(plugin.id);
    }
}
