package ch.yoinc.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public interface Command {

    /**
     * Start a command.
     *
     * @param event The slash command interaction event
     */
    void startCommand(SlashCommandInteractionEvent event);
}
