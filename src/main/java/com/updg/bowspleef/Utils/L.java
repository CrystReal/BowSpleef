package com.updg.bowspleef.Utils;

import com.updg.bowspleef.BowSpleefPlugin;

import java.util.logging.Level;

/**
 * Created by Alex
 * Date: 15.12.13  13:17
 */
public class L {
    public static void $(String str) {
        BowSpleefPlugin.getInstance().getLogger().log(Level.INFO, str);
    }

    public static void $(Level l, String str) {
        BowSpleefPlugin.getInstance().getLogger().log(l, str);
    }
}
