import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;

public class Main{

	private static final String TOKEN = "";
	private static final String CONSIDERED_CHANNEL = "";
	private static final String CARLBOT = "";
	private static final String SUGGESTIONS_CHANNEL = "";

	public static void main(String[] args){
		try{
			JDABuilder.createDefault(TOKEN).addEventListeners(new EditListener()).build().awaitReady();
		}catch(InterruptedException | LoginException e){
			e.printStackTrace();
		}
	}

	public static class EditListener extends ListenerAdapter{
		@Override
		public void onMessageUpdate(MessageUpdateEvent event){
			Message msg = event.getMessage();
			TextChannel tc = event.getTextChannel();
			TextChannel consideredChannel = event.getJDA().getTextChannelById(CONSIDERED_CHANNEL);

			if(tc.getId().equals(SUGGESTIONS_CHANNEL) && msg.getAuthor().getId().equals(CARLBOT) && msg.getEmbeds().size() > 0){
				MessageEmbed embed = msg.getEmbeds().get(0);
				if(embed.getTitle() == null) return;
				if(embed.getTitle().endsWith("Approved") || embed.getTitle().endsWith("Denied")){
					msg.delete().queue();
				}else if(embed.getTitle().endsWith("Considered")){
					if(consideredChannel == null){
						event.getMessage().reply("Considered suggestions channel couldn't be found").queue();
						return;
					}
					consideredChannel.sendMessageEmbeds(embed).queue();
					msg.delete().queue();
				}
			}
		}
	}

}
