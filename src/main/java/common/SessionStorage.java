package common;

import lombok.Getter;

@Getter
public class SessionStorage {

    public static final ThreadLocal<SessionStorage> INSTANCE = ThreadLocal.withInitial(SessionStorage::new);

    private SessionStorage() {
    }
}
