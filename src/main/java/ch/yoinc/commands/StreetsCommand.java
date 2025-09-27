package ch.yoinc.commands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class StreetsCommand implements Command {

    private final ConcurrentHashMap<String, String> activeGames;
    private final ConcurrentHashMap<String, Integer> scoreCollection;
    private final Random random;

    private static final String EMOJI_UPWARDS = "u+2b06";
    private static final String EMOJI_DOWNWARDS = "u+2b07";

    public StreetsCommand() {
        activeGames = new ConcurrentHashMap<>();
        scoreCollection = new ConcurrentHashMap<>();
        this.random = new Random();
    }

    @Override
    public void startCommand(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        event.reply("Starting a game of Streets...").setEphemeral(true).queue();
        event.getChannel().asTextChannel().createThreadChannel(member.getEffectiveName() + "'s game of Streets").queue(threadChannel ->
        {
            activeGames.put(threadChannel.getId(), member.getUser().getId());
            scoreCollection.put(member.getUser().getId(), 0);
            gamble(event, threadChannel.getId());
        });
    }

    /**
     * Start or continue a game of Streets.
     *
     * @param event    The event to handle
     * @param threadID The ID of the thread channel
     */
    public void gamble(Event event, String threadID) {
        if (event instanceof SlashCommandInteractionEvent) {
            event.getJDA().getThreadChannelById(threadID).sendMessage("Welcome to Streets.\nThe game is simple: Predict if the next number will be higher or lower. The number is between 1 and 10. For each correct prediction you get 1 point.").queue();
            event.getJDA().getThreadChannelById(threadID).sendMessage("Will the next number be higher or lower than: **" + (random.nextInt(10) + 1) + "**").queue(message -> {
                        message.addReaction(Emoji.fromUnicode(EMOJI_UPWARDS)).queue();
                        message.addReaction(Emoji.fromUnicode(EMOJI_DOWNWARDS)).queue();
                    }
            );
        } else if (event instanceof MessageReactionAddEvent) {
            CompletableFuture<Void> future = new CompletableFuture<>();
            AtomicReference<List<MessageReaction>> reactions = new AtomicReference<>();
            AtomicReference<String> numberFromMessage = new AtomicReference<>();

            ((MessageReactionAddEvent) event).retrieveMessage().queue(message -> {
                reactions.set(message.getReactions());
                numberFromMessage.set(message.getContentRaw().split("\\*\\*")[1]);
                future.complete(null);
            });
            future.join();

            String reaction = ((MessageReactionAddEvent) event).getReaction().getEmoji().asUnicode().getAsCodepoints().toLowerCase();
            int newNumber = random.nextInt(10) + 1;
            switch (reaction) {
                case EMOJI_UPWARDS:
                    if (newNumber >= Integer.parseInt(numberFromMessage.get())) {
                        event.getJDA().getThreadChannelById(threadID).sendMessage("You won! The next number is: **" + newNumber + "**").queue(message -> {
                            message.addReaction(Emoji.fromUnicode(EMOJI_UPWARDS)).queue();
                            message.addReaction(Emoji.fromUnicode(EMOJI_DOWNWARDS)).queue();
                        });
                        ((MessageReactionAddEvent) event).retrieveMessage().queue(message -> {
                            message.clearReactions().queue();
                            message.delete().queue();
                        });
                        scoreCollection.merge(((MessageReactionAddEvent) event).getMember().getId(), 1, Integer::sum);
                    } else {
                        CompletableFuture<Void> deletionFuture = new CompletableFuture<>();
                        event.getJDA().getThreadChannelById(threadID).sendMessage("You lost! The next number was: **" + newNumber + "**.\nYour total score: " + scoreCollection.get(((MessageReactionAddEvent) event).getMember().getId())).queue();
                        scoreCollection.remove(((MessageReactionAddEvent) event).getMember().getId());
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
                            message.delete().queue();
                        });
                        scoreCollection.merge(((MessageReactionAddEvent) event).getMember().getId(), 1, Integer::sum);
                    } else {
                        CompletableFuture<Void> deletionFuture = new CompletableFuture<>();
                        event.getJDA().getThreadChannelById(threadID).sendMessage("You lost! The next number was: **" + newNumber + "**.\nYour total score: " + scoreCollection.get(((MessageReactionAddEvent) event).getMember().getId())).queue();
                        scoreCollection.remove(((MessageReactionAddEvent) event).getMember().getId());
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

    /**
     * Checks if the user is in a game session.
     *
     * @param discordID The user's Discord ID
     * @param threadID The thread ID
     * @return True if the user is in a game session, false otherwise
     */
    public boolean isUserInSession(String discordID, String threadID) {
        if (threadID == null) {
            return activeGames.containsValue(discordID);
        } else {
            return activeGames.get(threadID).equals(discordID);
        }
    }

    public boolean isReactingCorrectly(String discordID, String threadID, String emojiCodepoints) {
        if (isUserInSession(discordID, threadID)) {
            return emojiCodepoints.equals(EMOJI_UPWARDS) || emojiCodepoints.equals(EMOJI_DOWNWARDS);
        }
        return false;
    }
}
