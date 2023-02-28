package com.github.MeFisto94.jMonkeyDiscordBot.Commands;

import com.github.MeFisto94.jMonkeyDiscordBot.*;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;

public class JavadocCommand extends AbstractLookupCommand {

    public JavadocCommand(Main main) {
        super(main);
        this.name = "javadoc";
        this.aliases = new String[] { "d" };
        this.help = "Search the jMonkeyEngine javadoc for a given Method";
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
            event.reply("#####################################");
            event.reply(mi.extractSignature() + "\n" + mi.extractJavaDoc()
                    + "\n" + mi.getJavadocLink());
            event.reply("#####################################");
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
            String javadoc = mi.extractJavaDoc();

            if (javadoc == null) {
                builder.addField("Javadoc", "No Javadoc Resources Found!", false);
            } else {
                builder.addField("Javadoc", "```java\n" + javadoc + "\n```", false);
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
            event.reply("#####################################");
            event.reply(fi.extractSignature() + "\n" + fi.extractJavaDoc()
                    + "\n" + fi.getJavadocLink());
            event.reply("#####################################");
        }
    }

    protected void presentEmbed(CommandEvent event, JavaClass jc, Field f) {
        if (f.getFieldCount() > 1) {
            event.reply("Found " + f.getFieldCount() + " overloads of this field:");
        }

        for (int i = 0; i < f.getFieldCount(); i++) {
            FieldInformation fi = new FieldInformation(f, i);
            EmbedBuilder builder = new EmbedBuilder();

            builder.setFooter(jc.getPackage() + "." + jc.getTypeName() + " in module " + jc.getParent().getName(), null);
            builder.setAuthor(fi.extractSignature(), fi.getJavadocLink());
            builder.setTitle("Branch: master", fi.getGithubLink());
            String javadoc = fi.extractJavaDoc();

            if (javadoc == null) {
                builder.addField("Javadoc", "No Javadoc Resources Found!", false);
            } else {
                builder.addField("Javadoc", "```java\n" + javadoc + "\n```", false);
            }

            event.reply(builder.build());
        }
    }
}
