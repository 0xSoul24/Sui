package app.rikka.sui.server;

import android.content.Context;
import android.ddm.DdmHandleAppName;
import android.os.ServiceManager;

import static app.rikka.sui.server.ServerConstants.LOGGER;

public class Starter {

    private static void waitSystemService(String name) {
        while (ServiceManager.getService(name) == null) {
            try {
                LOGGER.i("service " + name + " is not started, wait 1s.");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                LOGGER.w(e.getMessage(), e);
            }
        }
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            for (String arg : args) {
                if (arg.equals("--debug")) {
                    DdmHandleAppName.setAppName("sui", 0);
                }
            }
        }

        waitSystemService("package");
        waitSystemService("activity");
        waitSystemService(Context.USER_SERVICE);
        waitSystemService(Context.APP_OPS_SERVICE);

        Service.main();
    }
}
