package net.lingmu.thaumictinkerer.device;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid="thaumictinkerer",value=Dist.CLIENT)
public final class DeviceCamouflageRenderer implements BlockEntityRenderer<VersionMachine> {
    public DeviceCamouflageRenderer(BlockEntityRendererProvider.Context context){}
    @SubscribeEvent public static void register(EntityRenderersEvent.RegisterRenderers event){event.registerBlockEntityRenderer(DeviceContent.MACHINE.get(),DeviceCamouflageRenderer::new);}
    @Override public void render(VersionMachine machine,float partialTick,PoseStack poses,MultiBufferSource buffers,int light,int overlay){
        if(machine.kind()!=DeviceContent.Kind.CAMO && machine.kind()!=DeviceContent.Kind.PLATFORM && !machine.getBlockState().getValue(VersionDeviceBlock.DISGUISED))return;
        poses.pushPose();
        if(machine.kind()==DeviceContent.Kind.PLATFORM){poses.translate(0,.875,0);poses.scale(1,.125F,1);}
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(machine.camouflage,poses,buffers,light,overlay);
        poses.popPose();
    }
}
