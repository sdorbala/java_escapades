package com.servicenow.interview;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");

        Plugin plugin1 = new Plugin("Id: 1", "one");
        Plugin plugin2 = new Plugin("Id: 2", "two");
        Plugin plugin3 = new Plugin("Id: 3", "three");
        Plugin plugin4 = new Plugin("Id: 4", "four");
        Plugin plugin5 = new Plugin("Id: 5", "five");
        Plugin plugin6 = new Plugin("Id: 6", "six");
        Plugin plugin7 = new Plugin("Id: 7", "seven");
        Plugin plugin8 = new Plugin("Id: 8", "eight");

        plugin1.addDependency(plugin2);
        plugin1.addDependency(plugin3);
        plugin1.addDependency(plugin6);

        plugin2.addDependency(plugin3);

        plugin3.addDependency(plugin4);
        plugin3.addDependency(plugin5);

        plugin6.addDependency(plugin7);
        plugin6.addDependency(plugin8);

        List<Plugin> plugins = new ArrayList<>(Arrays.asList(plugin1, plugin2, plugin3, plugin4, plugin5, plugin6, plugin7, plugin8));

        List<String> dependencies = getDependencies(plugins);
        System.out.println("Dependency order: " + dependencies.stream().collect(Collectors.joining(",")));
    }

    private static List<String> getDependencies(List<Plugin> plugins) {
        List<String> result = new ArrayList<>();
        Map<String, Plugin> pluginMap = new HashMap<>();
        for (Plugin plugin : plugins) {
            pluginMap.put(plugin.id, plugin);
        }

        Set<String> dedup = new HashSet<>();
        for (Plugin plugin : plugins) {
            if (dedup.contains(plugin.id)) {
                continue;
            }
            List<String> deps = processPlugin(plugin, pluginMap);
            if (!deps.isEmpty()) {
                for (int i = 0; i < deps.size(); i++) {
                    String depId = deps.get(i);
                    if (!dedup.contains(depId)) {
                        dedup.add(depId);
                        result.add(depId);
                    }
                }
                if (!dedup.contains(plugin.id)) {
                    dedup.add(plugin.id);
                    result.add(plugin.id);
                }
            }
        }
        
        return result;
    }

    private static List<String> processPlugin(Plugin plugin, Map<String, Plugin> pluginMap) {
        List<String> thisDeps = new ArrayList<>();
        if (plugin.dependencies != null && plugin.dependencies.size() > 0) {
            for (String dep : plugin.dependencies) {
                thisDeps.addAll(processPlugin(pluginMap.get(dep), pluginMap));
            }
        }
        thisDeps.add(plugin.id);
        return thisDeps;
    }
}
