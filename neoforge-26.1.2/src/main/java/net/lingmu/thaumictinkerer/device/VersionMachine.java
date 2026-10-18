package net.lingmu.thaumictinkerer.device;

import java.util.UUID;
import net.minecraft.core.*;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;

public final class VersionMachine extends MachineBlockEntity {
    public VersionMachine(BlockPos pos,BlockState state){super(pos,state);}
    @Override public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket(){return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);}
    @Override public net.minecraft.nbt.CompoundTag getUpdateTag(HolderLookup.Provider registries){return saveWithoutMetadata(registries);}
    @Override protected void saveAdditional(ValueOutput output){
        super.saveAdditional(output);ContainerHelper.saveAllItems(output,inventory);
        if(target!=null)output.store("Target",BlockPos.CODEC,target);
        if(owner!=null)output.putString("Owner",owner.toString());
        output.putBoolean("Alternate",alternate);output.putBoolean("Adult",adult);output.putBoolean("Powered",lastPowered);
        output.putInt("Distance",distance);output.putInt("Progress",progress);output.putInt("EnchantLevel",enchantmentLevel);output.putString("Enchantment",enchantment);
        output.store("Camouflage",BlockState.CODEC,camouflage);
    }
    @Override protected void loadAdditional(ValueInput input){
        super.loadAdditional(input);ContainerHelper.loadAllItems(input,inventory);
        target=input.read("Target",BlockPos.CODEC).orElse(null);
        try{owner=UUID.fromString(input.getStringOr("Owner",""));}catch(IllegalArgumentException ignored){owner=null;}
        alternate=input.getBooleanOr("Alternate",false);adult=input.getBooleanOr("Adult",true);lastPowered=input.getBooleanOr("Powered",false);
        distance=Math.clamp(input.getIntOr("Distance",1),1,16);progress=Math.clamp(input.getIntOr("Progress",0),0,100);enchantmentLevel=Math.max(1,input.getIntOr("EnchantLevel",1));enchantment=input.getStringOr("Enchantment","");
        camouflage=input.read("Camouflage",BlockState.CODEC).orElse(camouflage);
    }
    @Override public void preRemoveSideEffects(BlockPos pos,BlockState state){if(kind()!=DeviceContent.Kind.INTERFACE)super.preRemoveSideEffects(pos,state);}
}
