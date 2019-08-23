package com.github.MeFisto94.jMonkeyDiscordBot.Commands;

import com.github.MeFisto94.jMonkeyDiscordBot.*;
import com.github.MeFisto94.jMonkeyDiscordBot.Git.*;
import com.google.common.base.Strings;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractLookupCommand extends Command {
    Main main;
    //((\w+)\s)?([^@\s]+)(@(.+))?
    //sdk Vector3f#clone@v3.1
    private static Pattern p = Pattern.compile("((\\w+)\\s)?([^@\\s]+)(@(.+))?");

    public AbstractLookupCommand(Main main) {
        this.main = main;
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        Matcher m = p.matcher(event.getArgs());
        if (!m.matches()) {
            event.replyError("Invalid Syntax!");
            return;
        } else {
            String repo = m.group(2);
            String object = m.group(3);
            String branch = m.group(5); // 4 is @branch, due to the outer matching group

            if (repo == null) {
                repo = "engine";
            }

            if (branch == null) {
                branch = "master";
            }

            String[] args = object.split("#");
            if (args.length == 2) {
                event.reply("Debug: Looking for Method " + args[1] + " in Class " + args[0] + " in Repo " +
                        Strings.nullToEmpty(repo) + " in Branch " + Strings.nullToEmpty(branch));

                GitRepo gitRepo = main.getRepositories().get(repo);
                GitBranch gitBranch;
                if (gitRepo == null) {
                    event.replyError("Could not find that repository!");
                    return;
                }
                if (!gitRepo.hasBranch(branch)) {
                    event.replyError("Don't have that branch!");
                    return;
                }
                if (gitRepo.needsToParseBranch(branch)) {
                    event.reply("I have to parse that branch first, please stand by!");
                    gitRepo.parseBranch(branch);
                }

                gitBranch = gitRepo.getBranch(branch);
                JavaClass jc = gitBranch.findClassBothCases(args[0]);
                if (jc == null) {
                    event.replyError("Could not find that class");
                    return;
                }

                Method method = jc.findMethodBothCases(args[1]);
                Field field = jc.findFieldBothCases(args[1]);

                if (method == null && field == null) {
                    event.replyError("Could not find that method or field");
                    return;
                }

                if (method != null) {
                    presentInformation(event, jc, method);
                }

                if (field != null) {
                    presentInformation(event, jc, field);
                }
            }
        }
    }

    protected abstract void presentInformation(CommandEvent event, JavaClass jc, Method m);
    protected abstract void presentInformation(CommandEvent event, JavaClass jc, Field f);
}
