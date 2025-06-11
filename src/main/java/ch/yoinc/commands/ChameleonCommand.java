package ch.yoinc.commands;

import ch.yoinc.DiscordService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import org.codehaus.plexus.util.StringUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ChameleonCommand implements Command {

    public boolean running = false;

    DiscordService discordService;

    public VoiceChannel global_voiceChannel;
    public List<Member> global_voiceChannelMembers;
    public Member chameleon;
    public String global_voteMessageID;

    public static Map<String, String> exclusiveMembers = Map.of("-", "-");
    Map<String, List<String>> categories = Map.of(
            "Nature", List.of("tree", "river", "mountain", "flower", "storm"),
            "Countries", List.of("Germany", "Brazil", "Japan", "Egypt", "Canada"),
            "Video Games", List.of("Minecraft", "Overwatch", "Valorant", "Skyrim", "Zelda"),
            "Car Brands", List.of("Toyota", "BMW", "Audi", "Honda", "Tesla"),
            "Counter Strike terminology", List.of("plant", "defuse", "smoke", "rush", "clutch")
    );

    public ChameleonCommand(DiscordService discordService) {
        this.discordService = discordService;
    }

    @Override
    public void startCommand(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        Guild guild = event.getGuild();

        global_voiceChannel = discordService.getCurrentVoiceChannel(member, guild.getVoiceChannels());
        global_voiceChannelMembers = discordService.getAllActiveVoiceChannelMembers(global_voiceChannel);

        if (global_voiceChannelMembers.size() >= 1) {
            event.reply("Starting game...").setEphemeral(true).queue();

            running = true;
            List<String> keys = new ArrayList<>(categories.keySet());
            Collections.shuffle(keys);
            String chosenCategory = keys.getFirst();

            event.getChannel().asTextChannel().createThreadChannel("Chameleon: " + chosenCategory).queue(threadChannel -> {
                global_voteMessageID = threadChannel.getId();

                StringBuilder sb = new StringBuilder();
                sb.append("Playing with");
                for (Member m : global_voiceChannelMembers) {
                    sb.append(" ").append(m.getEffectiveName()).append(",");
                }
                sb.setLength(sb.length() - 1);
                threadChannel.sendMessage(sb.toString()).queue();
                chameleon = global_voiceChannelMembers.get(new Random().nextInt(global_voiceChannelMembers.size()));
                threadChannel.sendMessage("We have a chameleon! Good luck!").queue();
                threadChannel.sendMessage("Sending the word now...").queue();

                String word = categories.get(chosenCategory).get(categories.get(chosenCategory).size() - 1);
                for (Member m : global_voiceChannelMembers) {
                    if (!m.equals(chameleon)) {
                        m.getUser().openPrivateChannel().queue(privateChannel -> {
                            privateChannel.sendMessage("The secret word is ... " + word + ". Good luck.").queue();
                        });
                    } else {
                        m.getUser().openPrivateChannel().queue(privateChannel -> {
                            privateChannel.sendMessage("You're the chameleon. Enjoy blending in. Remember: the category is " + chosenCategory + ". Good luck.").queue();
                        });
                    }
                }
            });
        } else {
            event.reply("You need at least 3 people in a voice channel to start a game of Chameleon").setEphemeral(true).queue();
            running = false;
        }
    }

    public void messageReceived(MessageReceivedEvent event) {
        Guild guild = event.getGuild();
        Member member = event.getMember();
        if (discordService.isThreadActive(event, member, guild)) {
            if (event.getMessage().getContentRaw().toLowerCase().contains("vote")) {
                event.getMessage().reply("Let's vote! Who is the chameleon? Pick the first letter of their username.").queue(message -> {
                    global_voteMessageID = message.getId();
                    for (Member global_voiceChannelMember : global_voiceChannelMembers) {
                        String emoteID = exclusiveMembers.get(global_voiceChannelMember.getId());
                        if (emoteID == null || emoteID.isEmpty()) {
                            char firstLetter = global_voiceChannelMember.getEffectiveName().charAt(0);
                            String regionalIndicator = new String(Character.toChars(0x1F1E6 + (firstLetter - 'a')));
                            message.addReaction(Emoji.fromUnicode(regionalIndicator)).queue();
                        } else {
                            message.addReaction(guild.getEmojisByName(emoteID, true).getFirst()).queue();
                        }
                    }
                });
            }
        }
    }

    public void vote(MessageReactionAddEvent event) {
        User reactingUser = event.getUser();
        event.retrieveMessage().queue(message -> {
            List<MessageReaction> reactions = message.getReactions();
            List<String> allowedEmojiNames = new ArrayList<>();
            List<CompletableFuture<Void>> allowedEmojiFutures = new ArrayList<>();

            for (MessageReaction reaction : reactions) {
                CompletableFuture<Void> future = new CompletableFuture<>();
                allowedEmojiFutures.add(future);

                reaction.retrieveUsers().queue(users -> {
                    if (users.stream().anyMatch(User::isBot)) {
                        allowedEmojiNames.add(reaction.getEmoji().getName());
                    }
                    future.complete(null);
                });
            }

            CompletableFuture.allOf(allowedEmojiFutures.toArray(new CompletableFuture[0]))
                    .thenRun(() -> {
                        String addedEmoji = event.getReaction().getEmoji().getName();

                        if (!allowedEmojiNames.contains(addedEmoji)) {
                            message.removeReaction(event.getReaction().getEmoji(), reactingUser).queue();
                            return;
                        }

                        for (MessageReaction reaction : reactions) {
                            String emojiName = reaction.getEmoji().getName();
                            if (!emojiName.equals(addedEmoji)) {
                                reaction.retrieveUsers().queue(users -> {
                                    if (users.stream().anyMatch(u -> u.getId().equals(reactingUser.getId()))) {
                                        message.removeReaction(reaction.getEmoji(), reactingUser).queue();
                                    }
                                });
                            }
                        }

                        AtomicInteger totalHumanReactions = new AtomicInteger();
                        List<CompletableFuture<Void>> countFutures = new ArrayList<>();

                        for (MessageReaction reaction : reactions) {
                            CompletableFuture<Void> countFuture = new CompletableFuture<>();
                            countFutures.add(countFuture);

                            reaction.retrieveUsers().queue(users -> {
                                long humanCount = users.stream().filter(u -> !u.isBot()).count();
                                totalHumanReactions.addAndGet((int) humanCount);
                                countFuture.complete(null);
                            });
                        }

                        CompletableFuture.allOf(countFutures.toArray(new CompletableFuture[0]))
                                .thenRun(() -> {
                                    if (totalHumanReactions.get() == global_voiceChannelMembers.size()) {
                                        endGame(event);
                                    }
                                });
                    });
        });
    }

    private void endGame(MessageReactionAddEvent event) {
        AtomicReference<MessageReaction> topReaction = new AtomicReference<>();
        AtomicInteger count = new AtomicInteger();

        event.retrieveMessage().queue(message -> {
            message.getReactions().forEach(reaction -> {
                int tempCount = reaction.getCount();
                if (tempCount > count.get()) {
                    topReaction.set(reaction);
                    count.set(tempCount);
                }
            });

            String votedResult = "";
            for (Map.Entry<String, String> entry : exclusiveMembers.entrySet()) {
                if (entry.getValue().equals(topReaction.get().getEmoji().getName())) {
                    votedResult = event.getJDA().getUserById(entry.getKey()).getName();
                }
            }

            if (StringUtils.isEmpty(votedResult)) {
                event.getChannel().sendMessage("That's the end of the game! The chameleon is " + chameleon.getEffectiveName() + ".\nAnd you voted for the emoji " + topReaction.get().getEmoji().getName() + ".").queue(success -> {
                    chameleon = null;
                    global_voiceChannel = null;
                    global_voiceChannelMembers = new ArrayList<>();
                    global_voteMessageID = null;
                    running = false;

                    if (event.getChannel() instanceof ThreadChannel threadChannel) {
                        threadChannel.getManager().setArchived(true).setLocked(true).queue();
                    }
                });
            } else {
                event.getChannel().sendMessage("That's the end of the game! The chameleon is " + chameleon.getEffectiveName() + ".\nAnd you voted for " + votedResult + ".").queue(success -> {
                    chameleon = null;
                    global_voiceChannel = null;
                    global_voiceChannelMembers = new ArrayList<>();
                    global_voteMessageID = null;
                    running = false;

                    if (event.getChannel() instanceof ThreadChannel threadChannel) {
                        threadChannel.getManager().setArchived(true).setLocked(true).queue();
                    }
                });
            }
        });
    }
}
