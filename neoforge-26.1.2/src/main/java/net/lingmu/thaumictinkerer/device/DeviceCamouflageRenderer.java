package net.lingmu.thaumictinkerer.device;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid="thaumictinkerer",value=Dist.CLIENT)
public final class DeviceCamouflageRenderer implements BlockEntityRenderer<VersionMachine,DeviceCamouflageRenderer.State> {
    public static final class State extends BlockEntityRenderState { MovingBlockRenderState disguise; boolean platform; }
    public DeviceCamouflageRenderer(BlockEntityRendererProvider.Context context){}
    @SubscribeEvent public static void register(EntityRenderersEvent.RegisterRenderers event){event.registerBlockEntityRenderer(DeviceContent.MACHINE.get(),DeviceCamouflageRenderer::new);}
    @Override public State createRenderState(){return new State();}
    @Override public void extractRenderState(VersionMachine machine,State state,float partialTick,Vec3 cameraPosition,ModelFeatureRenderer.CrumblingOverlay overlay){
        BlockEntityRenderer.super.extractRenderState(machine,state,partialTick,cameraPosition,overlay);
        state.disguise=null;
        state.platform=machine.kind()==DeviceContent.Kind.PLATFORM;
        if(machine.kind()!=DeviceContent.Kind.CAMO && !state.platform && !machine.getBlockState().getValue(VersionDeviceBlock.DISGUISED))return;
        if(machine.getLevel() instanceof ClientLevel level){
            state.disguise=new MovingBlockRenderState();
            state.disguise.randomSeedPos=machine.getBlockPos();state.disguise.blockPos=machine.getBlockPos();state.disguise.blockState=machine.camouflage;
            state.disguise.biome=level.getBiome(machine.getBlockPos());state.disguise.cardinalLighting=level.cardinalLighting();state.disguise.lightEngine=level.getLightEngine();
        }
    }
    @Override public void submit(State state,PoseStack poses,SubmitNodeCollector collector,CameraRenderState camera){
        if(state.disguise==null)return;
        poses.pushPose();if(state.platform){poses.translate(0,.875,0);poses.scale(1,.125F,1);}
        collector.submitMovingBlock(poses,state.disguise);poses.popPose();
    }
}
