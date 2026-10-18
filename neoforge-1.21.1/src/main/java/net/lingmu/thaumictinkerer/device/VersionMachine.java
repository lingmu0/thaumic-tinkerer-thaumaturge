package net.lingmu.thaumictinkerer.device;

import java.util.UUID;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.level.block.state.BlockState;

public final class VersionMachine extends MachineBlockEntity {
    public VersionMachine(BlockPos pos,BlockState state){super(pos,state);}
    @Override public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket(){return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);}
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries){return saveWithoutMetadata(registries);}
    @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider registries){
        super.saveAdditional(tag,registries);ContainerHelper.saveAllItems(tag,inventory,registries);
        if(target!=null){tag.putBoolean("Linked",true);tag.putLong("Target",target.asLong());}
        if(owner!=null)tag.putString("Owner",owner.toString());
        tag.putBoolean("Alternate",alternate);tag.putBoolean("Adult",adult);tag.putBoolean("Powered",lastPowered);
        tag.putInt("Distance",distance);tag.putInt("Progress",progress);tag.putInt("EnchantLevel",enchantmentLevel);tag.putString("Enchantment",enchantment);
        tag.put("Camouflage",NbtUtils.writeBlockState(camouflage));
    }
    @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider registries){
        super.loadAdditional(tag,registries);ContainerHelper.loadAllItems(tag,inventory,registries);
        target=tag.getBoolean("Linked")?BlockPos.of(tag.getLong("Target")):null;
        try{owner=UUID.fromString(tag.getString("Owner"));}catch(IllegalArgumentException ignored){owner=null;}
        alternate=tag.getBoolean("Alternate");adult=!tag.contains("Adult")||tag.getBoolean("Adult");lastPowered=tag.getBoolean("Powered");
        distance=Math.clamp(tag.getInt("Distance"),1,16);progress=Math.clamp(tag.getInt("Progress"),0,100);enchantmentLevel=Math.max(1,tag.getInt("EnchantLevel"));enchantment=tag.getString("Enchantment");
        if(tag.contains("Camouflage"))camouflage=NbtUtils.readBlockState(registries.lookupOrThrow(net.minecraft.core.registries.Registries.BLOCK),tag.getCompound("Camouflage"));
    }
}
