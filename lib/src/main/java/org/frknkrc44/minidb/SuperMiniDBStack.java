package org.frknkrc44.minidb;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public class SuperMiniDBStack {
        private final Map<String, SuperMiniDB> dbInstances = new LinkedHashMap<>();
        private final File path;
        private final boolean useAes;

        public SuperMiniDBStack(File path, boolean useAes) {
                this.path = path;
                this.useAes = useAes;
        }

        public SuperMiniDB getInstance(String name) {
                if (!dbInstances.containsKey(name)) {
                        dbInstances.put(name, new SuperMiniDB(name, path, false, useAes));
                }

                return dbInstances.get(name);
        }
}
