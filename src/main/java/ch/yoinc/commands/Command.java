package ch.yoinc.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public interface Command {
    void startCommand(SlashCommandInteractionEvent event);
}
