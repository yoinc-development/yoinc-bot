package ch.yoinc.tasks;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.util.Properties;

public class CleanupTask implements ScheduledTask {
    @Override
    public void execute(JDA jda, Properties properties) {
        for (Guild guild : jda.getGuilds()) {

            Role npcRole = guild.getRoleById(properties.getProperty("discord.role.npc"));
            Role idyetRole = guild.getRoleById(properties.getProperty("discord.role.idyet"));
            Role bagRole = guild.getRoleById(properties.getProperty("discord.role.bag"));
            Role tieronebagRole = guild.getRoleById(properties.getProperty("discord.role.tieronebag"));

            if (npcRole == null || idyetRole == null || bagRole == null || tieronebagRole == null) {
                return;
            } else {
                for (Member member : guild.getMembers()) {
                    if (!member.getUser().isBot()) {
                        if (member.getRoles().isEmpty()) {
                            guild.addRoleToMember(member, npcRole).queue();
                        } else if ((member.getRoles().contains(bagRole) || member.getRoles().contains(tieronebagRole)) && !member.getRoles().contains(idyetRole)) {
                            guild.addRoleToMember(member, idyetRole).queue();
                        }
                    }
                }
            }
        }
    }

    @Override
    public String getTaskName() {
        return "CleanupTask";
    }
}
