package bot.maiden.modules

import bot.maiden.*
import bot.maiden.common.baseEmbed
import net.dv8tion.jda.api.entities.User
import java.awt.Color
import java.time.Duration
import kotlin.reflect.full.findAnnotation

fun User.isOwner(bot: Bot) = idLong == bot.ownerId

object Administration : Module {
    @Command(hidden = true)
    suspend fun say(context: CommandContext, text: String) {
        context.requester ?: return

        if (context.requester.isOwner(context.bot)) {
            context.message?.delete()?.await()
            context.channel.sendMessage(text).await()
        } else {
            context.reply("${context.requester.asMention} no")
        }
    }

    @Command(hidden = true)
    suspend fun sayin(context: CommandContext, query: String) {
        context.requester ?: return

        val splitIndex = query.indexOf(' ')
        val gc = query.substring(0, splitIndex).trim()
        val text = query.substring(splitIndex + 1).trim()

        val (guildId, channelId) = gc.split("/").map { it.toLong() }

        val channel = context.jda.getGuildById(guildId)?.getTextChannelById(channelId) ?: run {
            context.reply("I can't do that.")
            return
        }

        if (context.requester.isOwner(context.bot)) {
            channel.sendMessage(text).await()
        } else {
            context.reply("${context.requester.asMention} no")
        }
    }

    @Command
    suspend fun invite(context: CommandContext, ignore: String) {
        context.reply(
            baseEmbed(context)
                .setTitle("Invite ${context.jda.selfUser.name} to your server")
                .setThumbnail(context.jda.selfUser.avatarUrl)
                .setDescription(
                    "**[Click here](https://discord.com/api/oauth2/authorize?client_id=841947222492577812&permissions=8&scope=bot)**",
                )
                .build()
        )
    }

    @Command
    suspend fun help(context: CommandContext, ignore: String) {
        val ownerUser = context.jda.retrieveUserById(context.bot.ownerId).await()

        context.reply(
            baseEmbed(context)
                .setColor(Color.WHITE)
                .setTitle("About ${context.jda.selfUser.name}")
                .setImage("https://i.imgur.com/S4MOq1f.png")
                .setDescription(
                    """
                    Hi! I'm a bot made by `${ownerUser.asTag}` (${ownerUser.asMention}).
                    
                    This bot is currently self-hosted, so there may be downtime, but I'll try my best to keep it running. It's also very much a work in progress, so you can check back for new additions if you want.
                """.trimIndent()
                )
                .addField("Command prefix", "`m!`", true)
                .addField(
                    "Invite link",
                    "**[Click here](https://discord.com/api/oauth2/authorize?client_id=841947222492577812&permissions=8&scope=bot)**",
                    true
                )
                .addField(
                    "Getting started", """
                    Here are some commands you can try out to get started:
                    `m!commands`
                    `m!invite`
                """.trimIndent(), false
                )
                .addField(
                    "Environment information", """
                    Running on ${System.getProperty("os.name")} ${System.getProperty("os.version")}
                    Kotlin ${KotlinVersion.CURRENT} on JDK ${System.getProperty("java.version")}
                    ${context.database.version ?: "Unknown database"}

                    **Uptime**: ${Duration.ofMillis(System.currentTimeMillis() - START_TIMESTAMP).toPrettyString()}
                    **Server count**: ${context.jda.guilds.size}
                """.trimIndent(), true
                )
                .addField("Source repository", "[github:musubii/maiden](https://github.com/musubii/maiden)", true)
                .build()
        )
    }

    @Command
    suspend fun commands(context: CommandContext, ignore: String) {
        // TODO char limit
        context.reply(
            baseEmbed(context)
                .setTitle("List of commands")
                .setThumbnail(context.jda.selfUser.avatarUrl)
                .apply {
                    setDescription(
                        buildString {
                            for ((_, function) in context.commands) {
                                val annotation = function.findAnnotation<Command>() ?: continue
                                if (annotation.hidden) continue

                                appendLine("`${function.name}`")
                            }
                        }
                    )
                }
                .addField("Command prefix", "`m!`", true)
                .build()
        )
    }

    @Command(hidden = true)
    fun `set-motd`(context: CommandContext, motd: String) {
        context.requester ?: return

        if (context.requester.isOwner(context.bot)) {
            context.bot.motd = motd.takeUnless { it.isBlank() }
        }
    }

    @Command(hidden = true)
    suspend fun `throw`(context: CommandContext, ignore: String) {
        throw Exception("Success")
    }
}
