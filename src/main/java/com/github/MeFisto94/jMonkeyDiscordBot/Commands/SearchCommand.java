package com.github.MeFisto94.jMonkeyDiscordBot.Commands;

import com.github.MeFisto94.jMonkeyDiscordBot.*;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.core.EmbedBuilder;

public class SearchCommand extends AbstractLookupCommand {
    Main main;

    public SearchCommand(Main main) {
        super(main);
        this.name = "search";
        this.aliases = new String[] { "s" };
        this.help = "Search the jMonkeyEngine Codebase for a given Method or Field";
    }

    @Override
    protected void presentInformation(CommandEvent event, JavaClass jc, Method m) {
        if (Main.useEmbeds) {
            presentEmbed(event, jc, m);
        } else {
            presentText(event, jc, m);
        }
    }

    @Override
    protected void presentInformation(CommandEvent event, JavaClass jc, Field f) {
        if (Main.useEmbeds) {
            presentEmbed(event, jc, f);
        } else {
            presentText(event, jc, f);
        }
    }

    protected void presentText(CommandEvent event, JavaClass jc, Method m) {
        if (m.getMethodCount() > 1) {
            event.reply("Found " + m.getMethodCount() + " overloads of this method: ");
        }
        for (int i = 0; i < m.getMethodCount(); i++) {
            MethodInformation mi = new MethodInformation(m, i);
            event.reply(mi.toString() + "\n" + mi.getGithubLink()
                    + "\n" + mi.getJavadocLink());
        }
    }

    protected void presentEmbed(CommandEvent event, JavaClass jc, Method m) {
        if (m.getMethodCount() > 1) {
            event.reply("Found " + m.getMethodCount() + " overloads of this method:");
        }

        for (int i = 0; i < m.getMethodCount(); i++) {
            MethodInformation mi = new MethodInformation(m, i);
            EmbedBuilder builder = new EmbedBuilder();

            builder.setFooter(jc.getPackage() + "." + jc.getTypeName() + " in module " + jc.getParent().getName(), null);
            builder.setAuthor(mi.extractSignature(), mi.getJavadocLink());
            builder.setTitle("Branch: master", mi.getGithubLink());
            String code = mi.extractSignature() + " " + mi.getContent(20);

            if (code == null) {
                builder.addField("Code Exempt", "No Code Found!", false);
            } else {
                builder.addField("Code Exempt", "```java\n" + code + "\n``` [maybe cutted]", false);
            }

            event.reply(builder.build());
        }
    }

    protected void presentText(CommandEvent event, JavaClass jc, Field f) {
        if (f.getFieldCount() > 1) {
            event.reply("Found " + f.getFieldCount() + " overloads of this field: ");
        }
        for (int i = 0; i < f.getFieldCount(); i++) {
            FieldInformation fi = new FieldInformation(f, i);
            event.reply(fi.toString() + "\n" + fi.getGithubLink()
                    + "\n" + fi.getJavadocLink());
        }
    }

    protected void presentEmbed(CommandEvent event, JavaClass jc, Field f) {
        if (f.getFieldCount() > 1) {
            event.reply("Found " + f.getFieldCount() + " overloads of this field: ");
        }
        for (int i = 0; i < f.getFieldCount(); i++) {
            FieldInformation fi = new FieldInformation(f, i);
            EmbedBuilder builder = new EmbedBuilder();

            builder.setFooter(jc.getPackage() + "." + jc.getTypeName() + " in module " + jc.getParent().getName(), null);
            builder.setAuthor(fi.extractSignature(), fi.getJavadocLink());
            builder.setTitle("Branch: master", fi.getGithubLink());
            String code = fi.extractSignature();

            if (code == null) {
                builder.addField("Definition", "No Definition found!", false);
            } else {
                builder.addField("Code Exempt", "```java\n" + code + "\n```", false);
            }

            event.reply(builder.build());
        }
    }
}
