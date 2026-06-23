package com.nexusreds.addons.block.entity;

import com.nexusreds.addons.NexusRedsMod;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public class CopperPipeBlockEntity extends BlockEntity {
    
    // Array para guardar quais lados foram forçados pela Chave Nexus (0 a 5 baseado no Direction.getId())
    public boolean[] forcedConnections = new boolean[6];
    
    // Nível de energia atual do cano
    public int powerLevel = 0; 

    public CopperPipeBlockEntity(BlockPos pos, BlockState state) {
        super(NexusRedsMod.COPPER_PIPE_ENTITY, pos, state);
    }

    // Alterna a conexão e diz ao jogo que os dados mudaram para serem salvos
    public void toggleForcedConnection(Direction dir) {
        int index = dir.getId();
        forcedConnections[index] = !forcedConnections[index];
        markDirty();
    }

    public boolean isForced(Direction dir) {
        return forcedConnections[dir.getId()];
    }

    // --- LEITURA E ESCRITA DOS DADOS NO MUNDO ---
    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        for (int i = 0; i < 6; i++) {
            forcedConnections[i] = nbt.getBoolean("Forced_" + i);
        }
        powerLevel = nbt.getInt("Power");
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        for (int i = 0; i < 6; i++) {
            nbt.putBoolean("Forced_" + i, forcedConnections[i]);
        }
        nbt.putInt("Power", powerLevel);
    }

    // Sincronização visual imediata entre Servidor e Cliente
    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return createNbt(registryLookup);
    }
}