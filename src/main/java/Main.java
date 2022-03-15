import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.*;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.util.Locale;

public class Main{

	private static final String TOKEN = ""; // Your bot token from the Discord Developer Dashboard
	private static final String CARLBOT = ""; //Carl bot user ID
	private static final String SUGGESTIONS_CHANNEL = ""; //ID of channel suggestions are posted in
	private static final String SUGGESTIONS_QUEUE_CHANNEL = ""; //ID of the suggestions queue channel
	private static final String CONSIDERED_CHANNEL = ""; //ID of the considered suggestions channel


	public static void main(String[] args){
		try{
			JDABuilder.createDefault(TOKEN).addEventListeners(new MessageListener()).build().awaitReady();
		}catch(InterruptedException | LoginException e){
			e.printStackTrace();
		}
	}

	public static class MessageListener extends ListenerAdapter{

		@Override
		public void onMessageReceived(@NotNull MessageReceivedEvent event){
			Message msg = event.getMessage();
			TextChannel tc = event.getTextChannel();
			TextChannel suggestQueue = event.getJDA().getTextChannelById(SUGGESTIONS_QUEUE_CHANNEL);
			// Send the newly created suggestion to the queue
			if(tc.getId().equals(SUGGESTIONS_CHANNEL) && msg.getAuthor().getId().equals(CARLBOT)){
				if(msg.getEmbeds().size() > 0) suggestQueue.sendMessageEmbeds(msg.getEmbeds().get(0)).queue();
			}
		}

		@Override
		public void onMessageUpdate(MessageUpdateEvent event){
			Message msg = event.getMessage();
			TextChannel tc = event.getTextChannel();
			TextChannel consideredChannel = event.getJDA().getTextChannelById(CONSIDERED_CHANNEL);

			//check edit if it was in the suggestions, by carl bot and on an embed message
			if(tc.getId().equals(SUGGESTIONS_CHANNEL) && msg.getAuthor().getId().equals(CARLBOT) && msg.getEmbeds().size() > 0){
				MessageEmbed embed = msg.getEmbeds().get(0);
				String title = embed.getTitle();
				if(title == null) return;
				//if the suggestion is actioned in the suggestion channel, delete from the queue channel
				if(title.toLowerCase(Locale.ROOT).endsWith("approved") || title.toLowerCase(Locale.ROOT).endsWith("denied") || title.toLowerCase(Locale.ROOT).endsWith("implemented")){
					int suggNo = Integer.parseInt(title.substring(title.indexOf("#")+1, title.indexOf(" ", title.indexOf("#")+1)));
					//delete suggestion from queue
					findAndDeleteSuggestion(suggNo, event.getJDA(), SUGGESTIONS_QUEUE_CHANNEL);
					//try to find the suggestion in considered queue to delete it
					findAndDeleteSuggestion(suggNo, event.getJDA(), CONSIDERED_CHANNEL);
				}else if(title.toLowerCase(Locale.ROOT).endsWith("considered")){
					if(consideredChannel == null){
						event.getMessage().reply("Considered suggestions channel couldn't be found").queue();
						return;
					}
					//add to considered queue
					consideredChannel.sendMessageEmbeds(embed).queue();
					int suggNo = Integer.parseInt(title.substring(title.indexOf("#")+1, title.indexOf(" ", title.indexOf("#")+1)));
					//delete suggestion from queue
					findAndDeleteSuggestion(suggNo, event.getJDA(), SUGGESTIONS_QUEUE_CHANNEL);
				}
			}
		}

		private void findAndDeleteSuggestion(int suggestionNo, JDA jda, String channelID){
			//check all messages in channel for matching suggestion
			//can be resource intensive with lots of messages but the whole point of this bot is to keep the channels tidy, so should be fine
			for(Message msg : jda.getTextChannelById(channelID).getIterableHistory().cache(false)){
				if(msg.getEmbeds().size() > 0 && msg.getEmbeds().get(0).getTitle().startsWith("Suggestion #"+suggestionNo)){
					msg.delete().queue();
					break;
				}
			}
		}
	}

}
//Made by Suffocate#6660 & Slayace04#1000
