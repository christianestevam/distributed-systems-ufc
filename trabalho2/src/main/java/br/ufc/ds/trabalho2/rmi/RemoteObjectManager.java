package main.java.br.ufc.ds.trabalho2.rmi;

import java.util.HashMap;
import java.util.Map;

/**
 * Gerenciador de objetos remotos no servidor.
 */
public class RemoteObjectManager {
    private Map<String, Object> remoteObjects;

    public RemoteObjectManager() {
        this.remoteObjects = new HashMap<>();
    }

    public void registerObject(String objectName, Object object) {
        remoteObjects.put(objectName, object);
    }

    public Object getObject(String objectName) {
        return remoteObjects.get(objectName);
    }

    public boolean hasObject(String objectName) {
        return remoteObjects.containsKey(objectName);
    }

    public void unregisterObject(String objectName) {
        remoteObjects.remove(objectName);
    }

    public Map<String, Object> getAllObjects() {
        return new HashMap<>(remoteObjects);
    }
}
