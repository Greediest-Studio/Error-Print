package com.smd.errorprint;

import net.minecraftforge.common.config.Config;

@Config(modid = Tags.MOD_ID, name = "GlErrorPrint")
public class ModConfig {

    @Config.Comment("启用OpenGL错误监控")
    public static boolean enableMonitoring = true;

    @Config.Comment({
            "日志级别 (只显示等于或高于此级别的错误):",
            "0 = ERROR (最高)",
            "1 = WARN",
            "2 = INFO (最低)"
    })
    @Config.RangeInt(min = 0, max = 2)
    public static int logLevel = 0;

    @Config.Comment("错误发生时记录堆栈跟踪")
    public static boolean logStackTrace = true;

    @Config.Comment({
            "堆栈跟踪记录深度",
            "-1 表示记录全部堆栈",
            "其他值表示最大记录行数"
    })
    @Config.RangeInt(min = -1)
    public static int stackTraceDepth = 15;
}