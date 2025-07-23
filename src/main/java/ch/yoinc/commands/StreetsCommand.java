package ch.yoinc.commands;

import ch.yoinc.services.DiscordService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class StreetsCommand implements Command {

    private final DiscordService discordService;
    private final Map<String, String> activeGames;
    private final Random random;

    public final String EMOJI_UPWARDS = "u+2b06";
    public final String EMOJI_DOWNWARDS = "u+2b07";

    public StreetsCommand(DiscordService discordService) {
        this.discordService = discordService;
        activeGames = new HashMap<>();
        this.random = new Random();
    }

    @Override
    public void startCommand(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        event.reply("Starting a game of Streets...").setEphemeral(true).queue();
        event.getChannel().asTextChannel().createThreadChannel(member.getEffectiveName() + "'s game of Streets").queue(threadChannel ->
        {
            activeGames.put(threadChannel.getId(), member.getUser().getId());
            gamble(event, threadChannel.getId());
        });
    }

    public void gamble(Event event, String threadID) {

        if (event instanceof SlashCommandInteractionEvent) {
            int number = random.nextInt(10) + 1;
            event.getJDA().getThreadChannelById(threadID).sendMessage("Welcome to Street. Will the next number be higher or lower than: **" + number + "**").queue(message -> {
                        message.addReaction(Emoji.fromUnicode(EMOJI_UPWARDS)).queue();
                        message.addReaction(Emoji.fromUnicode(EMOJI_DOWNWARDS)).queue();
                    }
            );
        } else if (event instanceof MessageReactionAddEvent) {
            CompletableFuture<Void> future = new CompletableFuture<>();
            AtomicReference<List<MessageReaction>> reactions = new AtomicReference<>();
            AtomicReference<String> numberFromMessage = new AtomicReference<>();

            int newNumber = random.nextInt(10) + 1;

            ((MessageReactionAddEvent) event).retrieveMessage().queue(message -> {
                reactions.set(message.getReactions());
                numberFromMessage.set(message.getContentRaw().split("\\*\\*")[1]);
                future.complete(null);
            });
            future.join();

            String reaction = ((MessageReactionAddEvent) event).getReaction().getEmoji().asUnicode().getAsCodepoints().toLowerCase();
            switch (reaction) {
                case EMOJI_UPWARDS:
                    if (newNumber >= Integer.parseInt(numberFromMessage.get())) {
                        event.getJDA().getThreadChannelById(threadID).sendMessage("You won! The next number is: **" + newNumber + "**").queue(message -> {
                            message.addReaction(Emoji.fromUnicode(EMOJI_UPWARDS)).queue();
                            message.addReaction(Emoji.fromUnicode(EMOJI_DOWNWARDS)).queue();
                        });
                        ((MessageReactionAddEvent) event).retrieveMessage().queue(message -> {
                            message.clearReactions().queue();
                        });
                    } else {
                        CompletableFuture<Void> deletionFuture = new CompletableFuture<>();
                        event.getJDA().getThreadChannelById(threadID).sendMessage("You lost! The next number was: **" + newNumber + "**").queue();
                        ((MessageReactionAddEvent) event).retrieveMessage().queue(message -> {
                            message.clearReactions().queue();
                            deletionFuture.complete(null);
                        });
                        deletionFuture.join();
                        ((MessageReactionAddEvent) event).getChannel().asThreadChannel().getManager().setArchived(true).setLocked(true).queue();
                        activeGames.remove(threadID);
                    }
                    break;
                case EMOJI_DOWNWARDS:
                    if (newNumber <= Integer.parseInt(numberFromMessage.get())) {
                        event.getJDA().getThreadChannelById(threadID).sendMessage("You won! The next number is: **" + newNumber + "**").queue(message -> {
                            message.addReaction(Emoji.fromUnicode(EMOJI_UPWARDS)).queue();
                            message.addReaction(Emoji.fromUnicode(EMOJI_DOWNWARDS)).queue();
                        });
                        ((MessageReactionAddEvent) event).retrieveMessage().queue(message -> {
                            message.clearReactions().queue();
                        });
                    } else {
                        CompletableFuture<Void> deletionFuture = new CompletableFuture<>();
                        event.getJDA().getThreadChannelById(threadID).sendMessage("You lost! The next number was: **" + newNumber + "**").queue();
                        ((MessageReactionAddEvent) event).retrieveMessage().queue(message -> {
                            message.clearReactions().queue();
                            deletionFuture.complete(null);
                        });
                        deletionFuture.join();
                        ((MessageReactionAddEvent) event).getChannel().asThreadChannel().getManager().setArchived(true).setLocked(true).queue();
                        activeGames.remove(threadID);
                    }
                    break;
            }

        }
    }

    public boolean isUserInSession(String discordID, String threadID) {
        if (threadID == null) {
            return activeGames.containsValue(discordID);
        } else {
            return activeGames.get(threadID).equals(discordID);
        }
    }

    public boolean isReactingCorrectly(String discordID, String threadID, String emojiCodepoints) {
        if (isUserInSession(discordID, threadID)) {
            if (emojiCodepoints.equals(EMOJI_UPWARDS) || emojiCodepoints.equals(EMOJI_DOWNWARDS)) {
                return true;
            }
        }
        return false;
    }
}
