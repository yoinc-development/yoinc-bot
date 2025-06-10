package ch.yoinc.listeners;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class YoincBotListener extends ListenerAdapter {


    public static VoiceChannel global_voiceChannel;
    public static List<Member> global_voiceChannelMembers;
    public static Member chameleon;
    public static String global_voteMessageID;

    public static Map<String, String> exclusiveMembers = null;
    Map<String, List<String>> categories = Map.of(
            "Nature", List.of("tree", "river", "mountain", "flower", "storm"),
            "Countries", List.of("Germany", "Brazil", "Japan", "Egypt", "Canada"),
            "Video Games", List.of("Minecraft", "Overwatch", "Valorant", "Skyrim", "Zelda"),
            "Car Brands", List.of("Toyota", "BMW", "Audi", "Honda", "Tesla"),
            "Counter Strike terminology", List.of("plant", "defuse", "smoke", "rush", "clutch")
    );


    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if(!event.getChannel().getType().equals(ChannelType.PRIVATE)) {
            Guild guild = event.getGuild();
            Member member = event.getMember();
            if (isThreadActive(event, member, guild)) {
                if (event.getMessage().getContentRaw().contains("vote")) {
                    event.getMessage().reply("Let's vote! Who is the chameleon? Pick the first letter of their username.").queue(message -> {
                        global_voteMessageID = message.getId();
                        for (Member global_voiceChannelMember : global_voiceChannelMembers) {
                            String emoteID = exclusiveMembers.get(global_voiceChannelMember.getId());
                            if (emoteID == null || emoteID.isEmpty()) {
                                char firstLetter = global_voiceChannelMember.getEffectiveName().charAt(0);
                                String regionalIndicator = new String(Character.toChars(0x1F1E6 + (firstLetter - 'a')));
                                message.addReaction(Emoji.fromUnicode(regionalIndicator)).queue();

                                char firstLetter2 = global_voiceChannelMember.getEffectiveName().charAt(1);
                                String regionalIndicator2 = new String(Character.toChars(0x1F1E6 + (firstLetter2 - 'a')));
                                message.addReaction(Emoji.fromUnicode(regionalIndicator2)).queue();
                            } else {
                                //TODO add exclusive member emojis
                            }
                        }
                    });
                }
            }
        }
    }

    //70% of this method was created by the robot. will validate and refactor after. sorry code purists.
    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if (!event.getUser().isBot() && event.getMessageId().equals(global_voteMessageID)) {
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
                                            event.getChannel().sendMessage("All voting is done").queue();

                                            //TODO figure out how to determine the voting results

                                            chameleon = null;
                                            global_voiceChannel = null;
                                            global_voiceChannelMembers = new ArrayList<>();
                                            global_voteMessageID = null;

                                            if (event.getChannel() instanceof ThreadChannel threadChannel) {
                                                threadChannel.getManager()
                                                        .setArchived(true)
                                                        .setLocked(true)
                                                        .queue();
                                            }
                                        }
                                    });
                        });
            });
        }
    }


    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        Channel eventChannel = event.getChannel();
        Member member = event.getMember();
        Guild guild = event.getGuild();

        if (eventChannel.getType().equals(ChannelType.PRIVATE)) {
            //no slash commands allowed in private channels
        } else if (eventChannel.getType().equals(ChannelType.TEXT)) {
            if ("chameleon".equals(event.getName())) {

                if (global_voiceChannel != null || !CollectionUtils.isEmpty(global_voiceChannelMembers)) {
                    event.reply("A game of Chameleon is already in progress in a channel").setEphemeral(true).queue();
                } else {
                    if (!isUserInVoiceChannel(member, guild.getVoiceChannels(), true)) {
                        event.reply("You need to be in a voice channel to use this command").setEphemeral(true).queue();
                    //TODO change this back - i only added this for testing (enjinia brain)
                    } else if (global_voiceChannelMembers.size() < 1) {
                        event.reply("You need more than 2 people to play Chameleon").setEphemeral(true).queue();
                    } else {
                        event.reply("Starting a new game of Chameleon").queue(response -> {
                            response.retrieveOriginal().queue(message -> {

                                Object[] crunchifyKeys = categories.keySet().toArray();
                                Object key = crunchifyKeys[new Random().nextInt(crunchifyKeys.length)];
                                List<Map.Entry<String, List<String>>> list = new ArrayList<Map.Entry<String, List<String>>>(categories.entrySet());
                                Collections.shuffle(list);
                                Map.Entry<String, List<String>> entry = list.getFirst();
                                String category = entry.getKey();

                                message.createThreadChannel("Category: " + category).queue(threadChannel -> {
                                    StringBuilder sb = new StringBuilder();
                                    sb.append("Playing with");
                                    for (Member m : global_voiceChannelMembers) {
                                        sb.append(" ").append(m.getEffectiveName()).append(",");
                                    }
                                    sb.setLength(sb.length() - 1);
                                    threadChannel.sendMessage(sb.toString()).queue();
                                    chameleon = global_voiceChannelMembers.get(new Random().nextInt(global_voiceChannelMembers.size()));
                                    threadChannel.sendMessage("We have a chameleon! Good luck " + chameleon.getEffectiveName() +"!").queue();
                                    threadChannel.sendMessage("Sending the word now...").queue();

                                    String word = entry.getValue().get(new Random().nextInt(entry.getValue().size()));
                                    for(Member m : global_voiceChannelMembers) {
                                        if(!m.equals(chameleon)) {
                                            m.getUser().openPrivateChannel().queue(privateChannel -> {
                                                privateChannel.sendMessage("The secret word is ... " + word + ". Good luck.").queue();
                                            });
                                        } else {
                                            m.getUser().openPrivateChannel().queue(privateChannel -> {
                                                privateChannel.sendMessage("You're the chameleon. Enjoy blending in. Remember: the category is " + category + ". Good luck.").queue();
                                            });
                                        }
                                    }
                                });
                            });
                        });
                    }
                }
            }
        }
    }

    protected boolean isThreadActive(MessageReceivedEvent event, Member member, Guild guild) {
        //channel type is a thread
        if (event.getChannel().getType().equals(ChannelType.GUILD_PUBLIC_THREAD)) {
            //thread is not locked or closed
            if (!event.getChannel().asThreadChannel().isLocked() && !event.getChannel().asThreadChannel().isArchived()) {
                //the thread owner is the bot
                if (event.getChannel().asThreadChannel().getOwner().getId().equals("BOT ID")) {
                    //the author of the message is not the bot and is currently in a voice channel
                    if (!event.getAuthor().isBot() && isUserInVoiceChannel(member, guild.getVoiceChannels(), false)) {
                        return true;
                    }
                }
            }

        }
        return false;
    }

    protected boolean isUserInVoiceChannel(Member member, List<VoiceChannel> voiceChannels, boolean initial) {
        VoiceChannel voiceChannel = voiceChannels.stream()
                .filter(vc -> vc.getMembers().contains(member))
                .findFirst()
                .orElse(null);

        if (voiceChannel == null) {
            return false;
        }

        if (initial) {
            global_voiceChannel = voiceChannel;
            global_voiceChannelMembers = voiceChannel.getMembers();
        }
        return true;
    }
}
