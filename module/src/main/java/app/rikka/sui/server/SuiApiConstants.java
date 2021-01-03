package app.rikka.sui.server;

import androidx.annotation.RestrictTo;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP_PREFIX;

public class SuiApiConstants {

    public static final int SERVER_VERSION = 11;

    public static final String PERMISSION = "moe.shizuku.manager.permission.API_V23";

    // binder
    public static final String BINDER_DESCRIPTOR = "moe.shizuku.server.IShizukuService";
    public static final int BINDER_TRANSACTION_transact = 1;

    // user service
    @RestrictTo(LIBRARY_GROUP_PREFIX)
    public static final int USER_SERVICE_TRANSACTION_destroy = 16777115;

    public static final String USER_SERVICE_ARG_TAG = "shizuku:user-service-arg-tag";
    public static final String USER_SERVICE_ARG_COMPONENT = "shizuku:user-service-arg-component";
    public static final String USER_SERVICE_ARG_DEBUGGABLE = "shizuku:user-service-arg-debuggable";
    public static final String USER_SERVICE_ARG_VERSION_CODE = "shizuku:user-service-arg-version-code";
    public static final String USER_SERVICE_ARG_PROCESS_NAME = "shizuku:user-service-arg-process-name";

    @RestrictTo(LIBRARY_GROUP_PREFIX)
    public static final String USER_SERVICE_ARG_TOKEN = "shizuku:user-service-arg-token";
}
