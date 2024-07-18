package com.malaclord.clientcommands.client.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import static com.malaclord.clientcommands.client.ClientCommandsClient.*;
import static com.malaclord.clientcommands.client.util.PlayerMessage.*;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;


public class ClientGiveCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {

        dispatcher
                .register(literal("client").then(literal("give")
                .then(argument("item", ItemStackArgumentType.itemStack(registryAccess))
                        .then(argument("amount", IntegerArgumentType.integer(0))
                                .executes(ClientGiveCommand::execute)
                        )).then(argument("item", ItemStackArgumentType.itemStack(registryAccess)).executes(ClientGiveCommand::execute)))
        );

        dispatcher.register(literal("cive")
                .then(argument("item", ItemStackArgumentType.itemStack(registryAccess))
                .then(argument("amount", IntegerArgumentType.integer(0))
                        .executes(ClientGiveCommand::execute)
                )).then(argument("item", ItemStackArgumentType.itemStack(registryAccess)).executes(ClientGiveCommand::execute)));
    }

    private static int execute(CommandContext<FabricClientCommandSource> context) {
        ClientPlayerEntity player = context.getSource().getPlayer();
        if (isGameModeNotCreative(player)) {
            sendNotInCreativeMessage(player);
            return 0;
        }

        ItemStack itemStack;

        try {
            int amount = 1;

            try {
                amount = IntegerArgumentType.getInteger(context,"amount");
            } catch (Exception ignored) {}

            itemStack = ItemStackArgumentType.getItemStackArgument(context,"item").createStack(amount,false);
        } catch (Exception ignored) {
            return 0;
        }

        if (player.getInventory().getEmptySlot() == -1 && player.getInventory().getOccupiedSlotWithRoomForStack(itemStack) == -1) {
            warn(player,"You don't have space in your inventory for this item!");
        } else {
            success(player, Text.literal("Gave you ").append(itemStack.getCount()+"").append(" ").append(itemStack.getName()).append("!"),context.getInput());
        }

        // Give item to player.
        player.giveItemStack(itemStack);

        syncInventory();

        return 1;
    }
}
