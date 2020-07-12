package com.minecolonies.coremod.colony.requestsystem.locations;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.location.ILocationFactory;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * Location described by an immutable blockpos and a dimension.
 */
public class StaticLocation implements ILocation
{

    private static final int NUMBER_OR_CONTEXTS = 1;

    @NotNull
    private final BlockPos pos;

    private final ResourceLocation dimension;

    public StaticLocation(@NotNull final BlockPos pos, final ResourceLocation dimension)
    {
        this.pos = pos;
        this.dimension = dimension;
    }

    /**
     * Method to get the location in the dimension
     *
     * @return The location.
     */
    @NotNull
    @Override
    public BlockPos getInDimensionLocation()
    {
        return pos;
    }

    /**
     * Method to get the dimension of the location.
     *
     * @return The dimension of the location.
     */
    @Override
    public ResourceLocation getDimension()
    {
        return dimension;
    }

    /**
     * Method to check if this location is reachable from the other.
     *
     * @param location The check if it is reachable from here.
     * @return True when reachable, false when not.
     */
    @Override
    public boolean isReachableFromLocation(@NotNull final ILocation location)
    {
        return location.getDimension() == getDimension();
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof StaticLocation))
        {
            return false;
        }

        final StaticLocation that = (StaticLocation) o;

        if (getDimension() != that.getDimension())
        {
            return false;
        }
        return pos.equals(that.pos);
    }

    @Override
    public int hashCode()
    {
        int result = pos.hashCode();
        result = 31 * result + getDimension().toString().hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return "Dim: " + dimension + " " + pos.getX() + "." + pos.getY() + "." + pos.getZ() + " ";
    }

    /**
     * Internal factory class.
     */
    @SuppressWarnings("squid:S2972")
    /**
     * We have this class the way it is for a reason.
     */
    public static class Factory implements ILocationFactory<BlockPos, StaticLocation>
    {

        ////// --------------------------- NBTConstants --------------------------- \\\\\\
        private static final String NBT_POS = "Pos";
        private static final String NBT_DIM = "Dim";
        ////// --------------------------- NBTConstants --------------------------- \\\\\\

        @NotNull
        @Override
        @SuppressWarnings("squid:LeftCurlyBraceStartLineCheck")
        /**
         * Moving the curly braces really makes the code hard to read.
         */
        public TypeToken<StaticLocation> getFactoryOutputType()
        {
            return TypeToken.of(StaticLocation.class);
        }

        @NotNull
        @Override
        @SuppressWarnings("squid:LeftCurlyBraceStartLineCheck")
        /**
         * Moving the curly braces really makes the code hard to read.
         */
        public TypeToken<BlockPos> getFactoryInputType()
        {
            return TypeToken.of(BlockPos.class);
        }

        /**
         * Method to serialize a given constructable.
         *
         * @param controller The controller that can be used to serialize complicated types.
         * @param request    The request to serialize.
         * @return The serialized data of the given requets.
         */
        @NotNull
        @Override
        public CompoundNBT serialize(@NotNull final IFactoryController controller, @NotNull final StaticLocation request)
        {
            final CompoundNBT compound = new CompoundNBT();
            compound.putLong(NBT_POS, request.getInDimensionLocation().toLong());
            compound.putString(NBT_DIM, request.getDimension().toString());
            return compound;
        }

        /**
         * Method to deserialize a given constructable.
         *
         * @param controller The controller that can be used to deserialize complicated types.
         * @param nbt        The data of the request that should be deserialized.
         * @return The request that corresponds with the given data in the nbt
         */
        @NotNull
        @Override
        public StaticLocation deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundNBT nbt)
        {
            final BlockPos pos = BlockPos.fromLong(nbt.getLong(NBT_POS));
            final String dim = nbt.getString(NBT_DIM);
            return new StaticLocation(pos, new ResourceLocation(dim));
        }

        @NotNull
        @Override
        public StaticLocation getNewInstance(@NotNull final IFactoryController factoryController, @NotNull final BlockPos blockPos, @NotNull final Object... context)
        {
            if (context.length != NUMBER_OR_CONTEXTS)
            {
                throw new IllegalArgumentException("Unsupported context - Not the correct amount available. Needed is 1!");
            }

            if (!(context[0] instanceof Integer))
            {
                throw new IllegalArgumentException("Unsupported context - First context object is not a Integer. Provide an Integer as Dimension.");
            }

            return new StaticLocation(blockPos, World.field_234918_g_.func_240901_a_());
        }

        /**
         * Method to get a new instance of a location given the input. Method not used in this factory.
         *
         * @param input The input to build a new location for.
         * @return The new output instance for a given input.
         */
        @NotNull
        @Override
        public StaticLocation getNewInstance(@NotNull final IFactoryController factoryController, @NotNull final BlockPos input)
        {
            return new StaticLocation(input, World.field_234918_g_.func_240901_a_());
        }

        @Override
        public void serialize(IFactoryController controller, StaticLocation input, PacketBuffer packetBuffer)
        {
            StaticLocation.serialize(packetBuffer, input);
        }

        @Override
        public StaticLocation deserialize(IFactoryController controller, PacketBuffer buffer) throws Throwable
        {
            return StaticLocation.deserialize(buffer);
        }
    }

    /**
     * Serialize this location to the given {@link PacketBuffer}.
     *
     * @param buffer the buffer to serialize this location to.
     */
    public static void serialize(PacketBuffer buffer, StaticLocation location)
    {
        buffer.writeBlockPos(location.pos);
        buffer.writeString(location.dimension.toString());
    }

    /**
     * Deserialize the location from the given {@link PacketBuffer}
     *
     * @param buffer the buffer to read.
     * @return the deserialized location.
     */
    public static StaticLocation deserialize(PacketBuffer buffer)
    {
        final BlockPos pos = buffer.readBlockPos();
        final ResourceLocation dimension = new ResourceLocation(buffer.readString(32767));

        return new StaticLocation(pos, dimension);
    }
}
