package com.smd.errorprint;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.event.RenderGameOverlayEvent.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.client.event.GuiScreenEvent.DrawScreenEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION)
public class ErrorPrint {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);
    private static long lastErrorTime = 0;
    private static int lastErrorCode = GL11.GL_NO_ERROR;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("OpenGL Error Detector initialized");
        ConfigManager.sync(Tags.MOD_ID, Config.Type.INSTANCE);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        checkGLError("RenderWorldLastEvent");
    }

    @SubscribeEvent
    public void onRenderBlockOverlay(RenderBlockOverlayEvent event) {
        BlockPos pos = event.getBlockPos();
        LOGGER.info("Block overlay at: {}", pos);
        checkGLError("RenderBlockOverlayEvent");
    }

    @SubscribeEvent
    public void onRenderLivingPre(RenderLivingEvent.Pre event) {
        checkGLError("RenderLivingEvent.Pre");
    }

    @SubscribeEvent
    public void onRenderLivingPost(RenderLivingEvent.Post event) {
        checkGLError("RenderLivingEvent.Post");
    }

    @SubscribeEvent
    public void onRenderHand(RenderHandEvent event) {
        checkGLError("RenderHandEvent");
    }

    @SubscribeEvent
    public void onRenderSpecificHand(RenderSpecificHandEvent event) {
        checkGLError("RenderSpecificHandEvent");
    }

    @SubscribeEvent
    public void onDrawScreenPre(DrawScreenEvent.Pre event) {
        checkGLError("GuiScreenEvent.DrawScreenEvent.Pre");
    }

    @SubscribeEvent
    public void onDrawScreenPost(DrawScreenEvent.Post event) {
        checkGLError("GuiScreenEvent.DrawScreenEvent.Post");
    }

    @SubscribeEvent
    public void onRenderGameOverlayPre(Pre event) {
        checkGLError("RenderGameOverlayEvent.Pre");
    }

    @SubscribeEvent
    public void onRenderGameOverlayPost(Post event) {
        checkGLError("RenderGameOverlayEvent.Post");
    }

    @SubscribeEvent
    public void onRenderGameOverlayText(Text event) {
        checkGLError("RenderGameOverlayEvent.Text");
    }

    @SubscribeEvent
    public void onRenderGameOverlayChat(Chat event) {
        checkGLError("RenderGameOverlayEvent.Chat");
    }

    @SubscribeEvent
    public void onRenderGameOverlayBoss(BossInfo event) {
        checkGLError("RenderGameOverlayEvent.BossInfo");
    }

    @SubscribeEvent
    public void onRenderTooltipPre(RenderTooltipEvent.Pre event) {
        checkGLError("RenderTooltipEvent.Pre");
    }

    @SubscribeEvent
    public void onRenderTooltipColor(RenderTooltipEvent.Color event) {
        checkGLError("RenderTooltipEvent.Color");
    }

    @SubscribeEvent
    public void onRenderItemInFrame(RenderItemInFrameEvent event) {
        checkGLError("RenderItemInFrameEvent");
    }

    @SubscribeEvent
    public void onRenderFogDensity(EntityViewRenderEvent.FogDensity event) {
        checkGLError("EntityViewRenderEvent.FogDensity");
    }

    @SubscribeEvent
    public void onRenderFogColors(EntityViewRenderEvent.FogColors event) {
        checkGLError("EntityViewRenderEvent.FogColors");
    }

    @SubscribeEvent
    public void onCameraSetup(EntityViewRenderEvent.CameraSetup event) {
        checkGLError("EntityViewRenderEvent.CameraSetup");
    }

    @SubscribeEvent
    public void onRenderFog(EntityViewRenderEvent.RenderFogEvent event) {
        checkGLError("EntityViewRenderEvent.RenderFogEvent");
    }

    private void checkGLError(String context) {
        if (!ModConfig.enableMonitoring) return;

        int error;
        while ((error = GL11.glGetError()) != GL11.GL_NO_ERROR) {
            lastErrorTime = System.currentTimeMillis();
            lastErrorCode = error;
            logErrorDetails(error, context);
        }
    }

    private void logErrorDetails(int error, String context) {
        if (ModConfig.logLevel > getErrorSeverity(error)) return;

        String errorName = getGLErrorName(error);
        LOGGER.error("[ErrorPrint] OpenGL Error in {}: {} (0x{})",
                context, errorName, Integer.toHexString(error).toUpperCase());

        LOGGER.warn("Thread: {}", Thread.currentThread().getName());

        if (ModConfig.logStackTrace) {
            LOGGER.warn("Stack trace (depth: {}):", ModConfig.stackTraceDepth == -1 ? "ALL" : ModConfig.stackTraceDepth);
            Arrays.stream(Thread.currentThread().getStackTrace())
                    .skip(4)
                    .limit(ModConfig.stackTraceDepth == -1 ? Long.MAX_VALUE : ModConfig.stackTraceDepth)
                    .forEach(ste -> LOGGER.warn("\tat {}", ste));
        }

        LOGGER.info("⏱ Last error: {}ms ago", System.currentTimeMillis() - lastErrorTime);
    }

    private int getErrorSeverity(int error) {
        switch (error) {
            case GL11.GL_INVALID_ENUM:
            case GL11.GL_INVALID_VALUE:
            case GL11.GL_INVALID_OPERATION:
                return 1;
            case GL11.GL_STACK_OVERFLOW:
            case GL11.GL_STACK_UNDERFLOW:
            case GL11.GL_OUT_OF_MEMORY:
                return 0;
            default:
                return 0;
        }
    }

    private String getGLErrorName(int error) {
        switch (error) {
            case GL11.GL_NO_ERROR: return "NO_ERROR";
            case GL11.GL_INVALID_ENUM: return "INVALID_ENUM";
            case GL11.GL_INVALID_VALUE: return "INVALID_VALUE";
            case GL11.GL_INVALID_OPERATION: return "INVALID_OPERATION";
            case GL11.GL_STACK_OVERFLOW: return "STACK_OVERFLOW";
            case GL11.GL_STACK_UNDERFLOW: return "STACK_UNDERFLOW";
            case GL11.GL_OUT_OF_MEMORY: return "OUT_OF_MEMORY";
            default: return "UNKNOWN_ERROR (0x" + Integer.toHexString(error).toUpperCase() + ")";
        }
    }
}
